package de.a12.studio.plugin.applicationgroups;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.features.A12StudioFeatureException;
import de.a12.studio.models.features.A12StudioProjectFeature;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Renames every model file of a project with the project's application group name as a
 * "{@code <group>_}" prefix, and stamps a matching {@code applicationGroup} header annotation on
 * every model. Re-running with a different group name strips the previously applied prefix (found
 * via the annotation, not by guessing from the filename) before applying the new one, and every
 * in-project reference to a renamed model - both the structured {@code header.modelReferences} list
 * and the typed content fields the model validators already treat as real references - is rewritten
 * to keep the project internally consistent.
 */
public class ApplicationGroupFeature implements A12StudioProjectFeature<ApplicationGroupResult> {

  public static final String ANNOTATION_NAME = "applicationGroup";

  private static final Pattern GROUP_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

  // Field names the model validators already treat as holding a model id: ModelDescriptor.documentModel /
  // MasterDetail's overviewModel, treeModel & FormMapping.documentModel/formModel / TreeNode.documentModelRef /
  // RelationshipModelContent.linkDocumentModel / IncludeConfig.reference & ModelReference.reference /
  // print FieldRef.model & Calculation.model / QueryModelContent.targetDocumentModel & QuerySort.relationshipModel /
  // CombinedDocumentModelContent.baseModelId / Mapping Model's MappingTarget.dmId, MappingSource.dmId,
  // PreComputationFragmentRef.dmId, OverallModelRef.dmId / combineddocumentmodel's DocumentModelIdRef.dmId &
  // SelectionModelIdRef.smId. "name" is only a reference when its sibling "modelType" marks the object as a
  // ModelDescriptor - every other "name" in these models (modules, scenes, buttons, ...) must stay untouched.
  // Deliberately NOT in this set: bare "id" - it's the generic local-element id field on dozens of unrelated
  // classes (Button.id, TreeNode.id, Cell.id, overviewmodel.Column.id, ...) throughout these content trees, so
  // matching on the name alone would risk rewriting a coincidental id collision. StructuralMappingModelRef.id
  // (Mapping Model's content.StructuralMappingModel.id, pointing at a Structural Mapping Model) is the one
  // legitimate "id"-named reference field; it's handled separately in rewriteNode by requiring the enclosing
  // field to be literally "StructuralMappingModel", not by adding "id" here.
  private static final Set<String> REFERENCE_FIELD_NAMES = Set.of(
      "documentModel", "overviewModel", "formModel", "documentModelRef", "linkDocumentModel", "reference", "model",
      "treeModel", "targetDocumentModel", "relationshipModel", "baseModelId", "dmId", "smId");

  // See REFERENCE_FIELD_NAMES javadoc above: the one "id"-named reference field, scoped to its wrapper.
  private static final String STRUCTURAL_MAPPING_MODEL_REF_FIELD_NAME = "StructuralMappingModel";

  // Form Model's relationship-widget config, matching SME's BINDING_CONFIG_ANNOTATION_NAME
  // (client/src/modules/formModel/transformer/binding/graphTransformations.ts). It's stored as an opaque
  // JSON-encoded string annotation, not typed content, so the rewriteNode() walk over model.getContent()
  // never sees it - each BindingModel entry's "name" (e.g. {"name": "PersonCompany_Person_SelectedItems_OM",
  // "use": "link"} inside a components[].models[] array) is matched byte-for-byte against a real model's
  // header.id at runtime (see ComponentModelInfo.createFromUseAndModelType /
  // showOverviewModelForBinding.ts's overviewModel.header.id comparison - there is no alias indirection),
  // so it needs the same idMap rewrite as every other reference field or SME throws "Model info for X could
  // not be determined" after the referenced model is renamed.
  private static final String BINDING_CONFIGURATION_ANNOTATION_NAME = "bindingConfiguration";

  public static boolean isValidGroupName(String name) {
    return name != null && GROUP_NAME_PATTERN.matcher(name).matches();
  }

  @Override
  public ApplicationGroupResult apply(Project project) throws A12StudioFeatureException {
    String groupName = ApplicationGroupsSettings.load(project.getFolder()).getApplicationGroupName();
    if (!isValidGroupName(groupName)) {
      throw new A12StudioFeatureException("\"" + groupName + "\" is not a valid application group name.");
    }

    List<ProjectItem> items = new ArrayList<>();
    collectModelItems(project.getRoot(), items);

    Map<ProjectItem, String> newIds = new LinkedHashMap<>();
    Map<String, String> idMap = new HashMap<>();
    for (ProjectItem item : items) {
      A12Model<?> model = item.getModel();
      String newId = groupName + "_" + stripExistingGroupPrefix(model);
      newIds.put(item, newId);
      if (!newId.equals(model.getId())) {
        idMap.put(model.getId(), newId);
      }
    }

    int renamedCount = 0;
    for (ProjectItem item : items) {
      A12Model<?> model = item.getModel();
      String newId = newIds.get(item);
      boolean idChanged = !newId.equals(model.getId());
      setApplicationGroupAnnotation(model, groupName);
      if (idChanged) {
        try {
          item.renameTo(newId + ".json");
        }
        catch (IOException e) {
          throw new A12StudioFeatureException(
              "Failed to rename \"" + item.getName() + "\" to \"" + newId + ".json\": " + e.getMessage(), e);
        }
        renamedCount++;
      }
      else {
        item.save();
      }
    }

    int referencesUpdatedCount = 0;
    if (!idMap.isEmpty()) {
      for (ProjectItem item : items) {
        if (rewriteReferences(item.getModel(), idMap)) {
          item.save();
          referencesUpdatedCount++;
        }
      }
    }

    return new ApplicationGroupResult(groupName, renamedCount, referencesUpdatedCount);
  }

  private static void collectModelItems(ProjectItem item, List<ProjectItem> result) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectModelItems(child, result);
      }
    }
    else if (item.getModel() != null) {
      result.add(item);
    }
  }

  static String stripExistingGroupPrefix(A12Model<?> model) {
    String id = model.getId();
    String existingGroup = findAnnotationValue(model, ANNOTATION_NAME);
    if (existingGroup != null && id.startsWith(existingGroup + "_")) {
      return id.substring(existingGroup.length() + 1);
    }
    return id;
  }

  static String findAnnotationValue(A12Model<?> model, String name) {
    for (Annotation annotation : model.getAnnotations()) {
      if (name.equals(annotation.getName())) {
        return annotation.getValue();
      }
    }
    return null;
  }

  static void setApplicationGroupAnnotation(A12Model<?> model, String groupName) {
    for (Annotation annotation : model.getAnnotations()) {
      if (ANNOTATION_NAME.equals(annotation.getName())) {
        annotation.setValue(groupName);
        return;
      }
    }
    Annotation annotation = new Annotation();
    annotation.setName(ANNOTATION_NAME);
    annotation.setValue(groupName);
    model.getAnnotations().add(annotation);
  }

  private static <C> boolean rewriteReferences(A12Model<C> model, Map<String, String> idMap) {
    boolean changed = false;
    for (ModelReference reference : model.getModelReferences()) {
      String mapped = idMap.get(reference.getReference());
      if (mapped != null) {
        reference.setReference(mapped);
        changed = true;
      }
    }

    C content = model.getContent();
    if (content != null) {
      JsonNode node = JsonSettings.objectMapper.valueToTree(content);
      if (rewriteNode(node, idMap, null)) {
        @SuppressWarnings("unchecked")
        C updated = (C) JsonSettings.objectMapper.treeToValue(node, content.getClass());
        model.setContent(updated);
        changed = true;
      }
    }

    changed |= rewriteBindingConfigurationAnnotations(model, idMap);
    return changed;
  }

  private static boolean rewriteBindingConfigurationAnnotations(A12Model<?> model, Map<String, String> idMap) {
    boolean changed = false;
    for (Annotation annotation : model.getAnnotations()) {
      if (!BINDING_CONFIGURATION_ANNOTATION_NAME.equals(annotation.getName())) {
        continue;
      }
      String value = annotation.getValue();
      if (value == null || value.isBlank()) {
        continue;
      }
      JsonNode node;
      try {
        node = JsonSettings.objectMapper.readTree(value);
      }
      catch (Exception e) {
        continue;
      }
      if (rewriteBindingModelNames(node, idMap)) {
        annotation.setValue(JsonSettings.objectMapper.writer().without(SerializationFeature.INDENT_OUTPUT)
            .writeValueAsString(node));
        changed = true;
      }
    }
    return changed;
  }

  // Rewrites the literal model-id "name" of each BindingModel entry, identified by its sibling "use" field
  // (see BINDING_CONFIGURATION_ANNOTATION_NAME javadoc) - "name" only means a model reference in this shape,
  // so other unrelated "name" fields in the same JSON (component names, the relationship widget's own
  // "details.name", "relationshipName", ...) are deliberately left untouched.
  private static boolean rewriteBindingModelNames(JsonNode node, Map<String, String> idMap) {
    boolean changed = false;
    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      JsonNode nameNode = objectNode.get("name");
      if (objectNode.has("use") && nameNode != null && nameNode.isString()) {
        String mapped = idMap.get(nameNode.asString());
        if (mapped != null) {
          objectNode.put("name", mapped);
          changed = true;
        }
      }
      for (String fieldName : List.copyOf(objectNode.propertyNames())) {
        JsonNode value = objectNode.get(fieldName);
        if (value.isObject() || value.isArray()) {
          changed |= rewriteBindingModelNames(value, idMap);
        }
      }
    }
    else if (node.isArray()) {
      for (JsonNode child : node) {
        changed |= rewriteBindingModelNames(child, idMap);
      }
    }
    return changed;
  }

  // enclosingFieldName is the JSON field name the current node was reached through (null at the content
  // root, and unchanged as array elements are descended into) - it's how "id" can be trusted as a reference
  // only inside a StructuralMappingModelRef, without treating every other class's local "id" field as one.
  private static boolean rewriteNode(JsonNode node, Map<String, String> idMap, String enclosingFieldName) {
    boolean changed = false;
    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      boolean modelDescriptorShape = objectNode.has("modelType");
      boolean structuralMappingModelRefShape = STRUCTURAL_MAPPING_MODEL_REF_FIELD_NAME.equals(enclosingFieldName);
      for (String fieldName : List.copyOf(objectNode.propertyNames())) {
        JsonNode value = objectNode.get(fieldName);
        boolean isReferenceField = REFERENCE_FIELD_NAMES.contains(fieldName)
            || ("name".equals(fieldName) && modelDescriptorShape)
            || ("id".equals(fieldName) && structuralMappingModelRefShape);
        if (isReferenceField && value.isString()) {
          String mapped = idMap.get(value.asString());
          if (mapped != null) {
            objectNode.put(fieldName, mapped);
            changed = true;
            continue;
          }
        }
        if (value.isObject() || value.isArray()) {
          changed |= rewriteNode(value, idMap, fieldName);
        }
      }
    }
    else if (node.isArray()) {
      for (JsonNode child : node) {
        changed |= rewriteNode(child, idMap, enclosingFieldName);
      }
    }
    return changed;
  }
}
