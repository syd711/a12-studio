package de.a12.studio.models.combineddocumentmodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import tools.jackson.core.type.TypeReference;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves a header {@code modelReferences} entry that may name either a plain {@link DocumentModel} or a
 * {@link CombinedDocumentModel} to the {@link DocumentModel} a field-reference picker (an Overview Model
 * column, a Form Model control) should resolve {@code elementRef}s against - shared by {@link
 * de.a12.studio.ui.editors.overviewmodel.OverviewModelEditorController}'s live element-index and the
 * wireframe preview services ({@code ApplicationModelPreviewService}, {@code FormModelPreviewService}),
 * which both need the same resolution.
 * <p>
 * a12-studio has no real Document Model join/expansion engine (unlike SME's kernel-backed
 * {@code DocumentModelJoiningService}/{@code CombModelReferenceHelper} - see {@code
 * docs/sme-reference-comparison.md}'s Combination Model section), so this only approximates it: it merges
 * the base Document Model's root groups and type definitions with those of every {@code Addition} step's
 * additive model (recursively, so a base model that is itself a Combination Model is expanded too), in
 * {@code CombinationSteps} order. {@code Selection} steps (which narrow the field set) and {@code
 * DecorationForFields}/{@code DecorationForGroups} steps (which only override existing fields' labels/config,
 * not add new ids) are intentionally not applied - skipping them can only make extra fields resolvable that
 * a real join would have hidden or reworded, never hide a field that should resolve, which is the
 * conservative direction for a field-reference picker to err in.
 * <p>
 * Every element an Addition step contributes gets its id rewritten to {@code md5Hex(additiveModelId) + "_" +
 * originalId} - e.g. {@code PersonEmployee_Ad}'s field {@code F7} becomes {@code
 * 3ebb47b738ad9c6e3c36113ff04df00d_F7} (verified against {@code PersonEmployee_Ov.json}/{@code
 * PersonEmployee_Fm.json} in {@code testing/workspaces}, whose {@code elementRef}s already use exactly this
 * form). This mirrors the real a12 kernel's combination join, which must rewrite additive-model ids the same
 * way to keep them collision-free against the base model and every other additive model that might reuse the
 * same short local ids (e.g. {@code F6}, {@code G10}). Base-model elements are left with their original ids,
 * matching the kernel's behavior of only rewriting the additive side.
 */
public final class CombinedDocumentModelElements {

  private CombinedDocumentModelElements() {
  }

  /**
   * {@code null} if {@code modelId} is {@code null}, doesn't resolve to any model reachable from {@code
   * contextItem}'s project, or resolves to something that's neither a {@link DocumentModel} nor a {@link
   * CombinedDocumentModel} with a resolvable base model.
   */
  public static DocumentModel resolveForFieldReferences(ProjectItem contextItem, String modelId) {
    if (modelId == null) {
      return null;
    }
    ProjectItem item = contextItem.findByModelId(modelId);
    if (item == null || item.getModel() == null) {
      return null;
    }
    if (item.getModel() instanceof DocumentModel documentModel) {
      return documentModel;
    }
    if (item.getModel() instanceof CombinedDocumentModel combinedModel) {
      return expand(contextItem, combinedModel);
    }
    return null;
  }

  private static DocumentModel expand(ProjectItem contextItem, CombinedDocumentModel combinedModel) {
    if (combinedModel.getContent() == null) {
      return null;
    }
    DocumentModel base = resolveForFieldReferences(contextItem, combinedModel.getContent().getBaseModelId());
    if (base == null || base.getContent() == null || base.getContent().getModelRoot() == null) {
      return null;
    }

    List<GroupElement> rootGroups = new ArrayList<>(orEmpty(base.getContent().getModelRoot().getRootGroups()));
    List<TypeDefinition> typeDefinitions = new ArrayList<>(orEmpty(base.getContent().getTypeDefinitions()));

    for (CombinationStep step : orEmpty(combinedModel.getContent().getCombinationSteps())) {
      if (step.getType() != CombinationStepType.ADDITION || step.getAdditiveModel() == null) {
        continue;
      }
      DocumentModel additiveModel = resolveForFieldReferences(contextItem, step.getAdditiveModel().getDmId());
      if (additiveModel == null || additiveModel.getContent() == null || additiveModel.getContent().getModelRoot() == null) {
        continue;
      }
      List<GroupElement> additiveGroups = cloneRootGroups(additiveModel.getContent().getModelRoot().getRootGroups());
      rewriteIds(additiveGroups, md5Hex(additiveModel.getId()) + "_");
      rootGroups.addAll(additiveGroups);
      typeDefinitions.addAll(orEmpty(additiveModel.getContent().getTypeDefinitions()));
    }

    DocumentModel merged = new DocumentModel();
    merged.setId(combinedModel.getId());
    merged.setModelType(ModelType.DOCUMENT);

    ModelRoot modelRoot = new ModelRoot();
    modelRoot.setRootGroups(rootGroups);

    DocumentModelContent content = new DocumentModelContent();
    content.setModelInfo(base.getContent().getModelInfo());
    content.setModelConfig(base.getContent().getModelConfig());
    content.setModelRoot(modelRoot);
    content.setTypeDefinitions(typeDefinitions);
    merged.setContent(content);
    return merged;
  }

  /**
   * Deep-copies {@code rootGroups} (via a Jackson round-trip, since every {@link Element} subtype is already
   * fully annotated for this tree shape) so {@link #rewriteIds} can mutate ids on the copy without touching
   * the additive model's own loaded object graph, which may still be open in another editor tab or referenced
   * by another Combination Model. Serialized through {@code writerFor(TypeReference)} rather than plain
   * {@code writeValueAsString(Object)}: the latter loses the list's declared element type to generics erasure
   * (it only sees a raw {@code ArrayList} at runtime), so {@code Element}'s {@code @JsonTypeInfo} discriminator
   * never gets written for the *root* elements of the list (nested elements are fine, since {@code
   * GroupConfig.elements}'s own declared field type carries the polymorphic type through reflection) - and
   * without it, reading the clone back fails with "missing type id property 'type'".
   */
  private static List<GroupElement> cloneRootGroups(List<GroupElement> rootGroups) {
    TypeReference<List<GroupElement>> type = new TypeReference<>() {
    };
    String json = JsonSettings.objectMapper.writerFor(type).writeValueAsString(rootGroups);
    return JsonSettings.objectMapper.readValue(json, type);
  }

  private static void rewriteIds(List<? extends Element> elements, String prefix) {
    for (Element element : elements) {
      if (element.getId() != null) {
        element.setId(prefix + element.getId());
      }
      if (element instanceof GroupElement group && group.getGroup() != null && group.getGroup().getElements() != null) {
        rewriteIds(group.getGroup().getElements(), prefix);
      }
    }
  }

  private static String md5Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
      }
      return hex.toString();
    }
    catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("MD5 not available", e);
    }
  }

  private static <T> List<T> orEmpty(List<T> list) {
    return list == null ? List.of() : list;
  }
}
