package de.a12.studio.models.features;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import tools.jackson.databind.JsonNode;
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
  // MasterDetail's overviewModel & FormMapping.documentModel/formModel / TreeNode.documentModelRef /
  // RelationshipModelContent.linkDocumentModel / IncludeConfig.reference & ModelReference.reference /
  // print FieldRef.model. "name" is only a reference when its sibling "modelType" marks the object as a
  // ModelDescriptor - every other "name" in these models (modules, scenes, buttons, ...) must stay untouched.
  private static final Set<String> REFERENCE_FIELD_NAMES = Set.of(
      "documentModel", "overviewModel", "formModel", "documentModelRef", "linkDocumentModel", "reference", "model");

  public static boolean isValidGroupName(String name) {
    return name != null && GROUP_NAME_PATTERN.matcher(name).matches();
  }

  @Override
  public ApplicationGroupResult apply(Project project) throws A12StudioFeatureException {
    String groupName = project.getSettings().getAdvancedSettings().getApplicationGroupName();
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

  private static String stripExistingGroupPrefix(A12Model<?> model) {
    String id = model.getId();
    String existingGroup = findAnnotationValue(model, ANNOTATION_NAME);
    if (existingGroup != null && id.startsWith(existingGroup + "_")) {
      return id.substring(existingGroup.length() + 1);
    }
    return id;
  }

  private static String findAnnotationValue(A12Model<?> model, String name) {
    for (Annotation annotation : model.getAnnotations()) {
      if (name.equals(annotation.getName())) {
        return annotation.getValue();
      }
    }
    return null;
  }

  private static void setApplicationGroupAnnotation(A12Model<?> model, String groupName) {
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
      if (rewriteNode(node, idMap)) {
        @SuppressWarnings("unchecked")
        C updated = (C) JsonSettings.objectMapper.treeToValue(node, content.getClass());
        model.setContent(updated);
        changed = true;
      }
    }
    return changed;
  }

  private static boolean rewriteNode(JsonNode node, Map<String, String> idMap) {
    boolean changed = false;
    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      boolean modelDescriptorShape = objectNode.has("modelType");
      for (String fieldName : List.copyOf(objectNode.propertyNames())) {
        JsonNode value = objectNode.get(fieldName);
        boolean isReferenceField = REFERENCE_FIELD_NAMES.contains(fieldName)
            || ("name".equals(fieldName) && modelDescriptorShape);
        if (isReferenceField && value.isString()) {
          String mapped = idMap.get(value.asString());
          if (mapped != null) {
            objectNode.put(fieldName, mapped);
            changed = true;
            continue;
          }
        }
        if (value.isObject() || value.isArray()) {
          changed |= rewriteNode(value, idMap);
        }
      }
    }
    else if (node.isArray()) {
      for (JsonNode child : node) {
        changed |= rewriteNode(child, idMap);
      }
    }
    return changed;
  }
}
