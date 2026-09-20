package de.a12.studio.models.util;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.querymodel.ql.QueryLanguageException;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences.HasCall;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences.Reference;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences.Replacement;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rewrites cross-model references inside {@link A12Model} instances when one or more model ids
 * change (e.g. because of a rename or an application-group prefix change).
 *
 * <p>Two locations are updated:
 * <ol>
 *   <li>{@code header.modelReferences} – the structured reference list every model carries.</li>
 *   <li>Content fields – every JSON field whose name is in {@link #REFERENCE_FIELD_NAMES}, plus
 *       the special {@code name} field inside a {@code ModelDescriptor} shape (one that also has a
 *       {@code modelType} sibling), plus the {@code id} field inside a
 *       {@code StructuralMappingModel} wrapper.</li>
 *   <li>Query Language text – the relationship model named in a {@code Has("<relationship>", "<role>", ...)} call
 *       of a {@code filterDefinition} (a Query Model's, root or per relationship hop). Text that isn't valid Query
 *       Language is left alone.</li>
 * </ol>
 *
 * <p>The field-name set mirrors the one maintained by the Application Groups plugin – both sites
 * must stay in sync when new reference-holding fields are added to the model schemas.
 */
public final class ModelReferenceRewriter {

  // Field names the model validators already treat as holding a model id: ModelDescriptor.documentModel /
  // MasterDetail's overviewModel, treeModel & FormMapping.documentModel/formModel / TreeNode.documentModelRef /
  // RelationshipModelContent.linkDocumentModel / IncludeConfig.reference & ModelReference.reference /
  // print FieldRef.model & Calculation.model / QueryModelContent.targetDocumentModel & QuerySort.relationshipModel /
  // CombinedDocumentModelContent.baseModelId / Mapping Model's MappingTarget.dmId, MappingSource.dmId,
  // PreComputationFragmentRef.dmId, OverallModelRef.dmId / combineddocumentmodel's DocumentModelIdRef.dmId &
  // SelectionModelIdRef.smId. "name" is only a reference when its sibling "modelType" marks the object as a
  // ModelDescriptor - every other "name" in these models (modules, scenes, buttons, ...) must stay untouched.
  public static final Set<String> REFERENCE_FIELD_NAMES = Set.of(
      "documentModel", "overviewModel", "formModel", "documentModelRef", "linkDocumentModel", "reference", "model",
      "treeModel", "targetDocumentModel", "relationshipModel", "baseModelId", "dmId", "smId");

  // See REFERENCE_FIELD_NAMES javadoc above: the one "id"-named reference field, scoped to its wrapper.
  private static final String STRUCTURAL_MAPPING_MODEL_REF_FIELD_NAME = "StructuralMappingModel";

  private static final String FILTER_DEFINITION_FIELD_NAME = "filterDefinition";

  private ModelReferenceRewriter() {
  }

  /** {@code text} with the relationship model of every {@code Has(...)} call (nested ones included) mapped via
   * {@code idMap}; the field paths of a filter are element paths, not model ids, and are not touched here. */
  private static String rewriteFilterDefinition(String text, Map<String, String> idMap) {
    List<Reference> references;
    try {
      references = QueryLanguageReferences.extract(text);
    }
    catch (QueryLanguageException e) {
      return text;
    }
    List<Replacement> replacements = new ArrayList<>();
    collectHasRewrites(references, idMap, replacements);
    return replacements.isEmpty() ? text : QueryLanguageReferences.replace(text, replacements);
  }

  private static void collectHasRewrites(List<Reference> references, Map<String, String> idMap,
      List<Replacement> replacements) {
    if (references == null) {
      return;
    }
    for (Reference reference : references) {
      if (reference instanceof HasCall has) {
        String mapped = idMap.get(has.relationshipModel());
        if (mapped != null) {
          replacements.add(new Replacement(has.relationshipStart(), has.relationshipStop(), '"' + mapped + '"'));
        }
        collectHasRewrites(has.constraint(), idMap, replacements);
        collectHasRewrites(has.linkConstraint(), idMap, replacements);
      }
    }
  }

  /**
   * Rewrites all references in {@code model} that match any key in {@code idMap}, replacing them
   * with the corresponding value.
   *
   * @return {@code true} if at least one reference was rewritten (caller should save the model)
   */
  public static <C> boolean rewriteReferences(A12Model<C> model, Map<String, String> idMap) {
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
    return changed;
  }

  /**
   * Recursively walks {@code node} and rewrites any string value found under a reference field
   * name, using {@code idMap} as the substitution table.
   *
   * @param enclosingFieldName the JSON field name the current node was reached through (null at
   *                           content root); array descent keeps the parent field name so that
   *                           "id" can be trusted as a reference only inside a StructuralMappingModel.
   */
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
        if (FILTER_DEFINITION_FIELD_NAME.equals(fieldName) && value.isString()) {
          String rewritten = rewriteFilterDefinition(value.asString(), idMap);
          if (!rewritten.equals(value.asString())) {
            objectNode.put(fieldName, rewritten);
            changed = true;
          }
          continue;
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
