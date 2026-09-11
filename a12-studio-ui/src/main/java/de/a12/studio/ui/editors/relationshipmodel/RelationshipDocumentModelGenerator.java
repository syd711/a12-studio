package de.a12.studio.ui.editors.relationshipmodel;

import de.a12.studio.models.Locale;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.models.documentmodel.ModelInfo;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.editors.documentmodel.DocumentModelElementFactory;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements the Relationship Model editor's "Generate Document Models" action (see {@link
 * de.a12.studio.ui.editors.relationshipmodel.RelationshipModelEditorController#onGenerateDocumentModels}),
 * mirroring SME's {@code dmGenerator.ts}/{@code rmSagas.ts}: for every {@link EntityCharacteristic}, generates a
 * Document Model named {@code "<relationshipId>_<role>____generated"} with a "target" group including the
 * entity's own Document Model, plus (if the relationship declares one) a "relationship" group including the Link
 * Document Model - so the generated model works as a combined view of the entity together with the relationship's
 * link data. Unlike SME, a12-studio resolves an Include purely via {@code IncludeConfig.reference} (a plain
 * Document Model id, see {@code IncludePropertiesPanelController}) rather than a header {@code ModelReference}
 * alias, so no header reference propagation (SME's {@code getRelevantModelReferences}) is needed here.
 */
public final class RelationshipDocumentModelGenerator {

  private static final String GENERATED_SUFFIX = "____generated";

  private RelationshipDocumentModelGenerator() {
  }

  public static String generatedModelId(@NonNull String relationshipId, @NonNull String role) {
    return relationshipId + "_" + role + GENERATED_SUFFIX;
  }

  /**
   * Every Document Model elsewhere in the project whose id matches {@code "<relationshipId>_<role>____generated"}
   * - found project-wide (not limited to one folder) since a relationship previously generated into a different
   * folder, or whose role names have since changed, should still be offered for cleanup, mirroring SME's {@code
   * getExistingGeneratedEntries}.
   */
  public static List<ProjectItem> findExistingGeneratedModels(@NonNull String relationshipId, @NonNull ProjectItem contextItem) {
    String prefix = relationshipId + "_";
    return ProjectDocumentModels.getOtherDocumentModels(contextItem).stream()
        .map(DocumentModel::getId)
        .filter(id -> id.startsWith(prefix) && id.endsWith(GENERATED_SUFFIX))
        .map(ProjectDocumentModels::findProjectItemByModelId)
        .flatMap(Optional::stream)
        .toList();
  }

  public static void deleteAll(@NonNull List<ProjectItem> items) throws IOException {
    for (ProjectItem item : items) {
      item.delete();
      StudioEventManager.getInstance().fireModelDeletedEvent(item);
    }
  }

  /**
   * Resolves every entity's Document Model and the Link Document Model (if set), without generating anything,
   * throwing an {@link IllegalStateException} if one is missing or unset. Callers should validate this way
   * <em>before</em> deleting any stale generated models found via {@link #findExistingGeneratedModels}, so an
   * invalid relationship never costs the user their previously generated models.
   */
  public static void validate(@NonNull RelationshipModel relationship, @NonNull ProjectItem contextItem) {
    List<DocumentModel> allModels = ProjectDocumentModels.getOtherDocumentModels(contextItem);
    resolveOptionalDocumentModel(relationship.getContent().getLinkDocumentModelValue(), allModels);
    for (EntityCharacteristic entity : relationship.getContent().getEntityCharacteristics()) {
      resolveTargetDocumentModel(entity, allModels);
    }
  }

  /**
   * Generates one Document Model per {@code relationship}'s entity characteristic into {@code targetFolder}. Call
   * {@link #validate} first and delete any stale generated models it's meant to replace - otherwise regenerating
   * into the same folder fails with an "already exists" {@link IOException} for every stale file left in place.
   */
  public static List<ProjectItem> generate(@NonNull RelationshipModel relationship, @NonNull ProjectItem contextItem,
      @NonNull ProjectItem targetFolder) throws IOException {
    List<DocumentModel> allModels = ProjectDocumentModels.getOtherDocumentModels(contextItem);
    DocumentModel linkModel = resolveOptionalDocumentModel(relationship.getContent().getLinkDocumentModelValue(), allModels);

    List<EntityCharacteristic> entities = relationship.getContent().getEntityCharacteristics();
    List<DocumentModel> targetModels = new ArrayList<>();
    for (EntityCharacteristic entity : entities) {
      targetModels.add(resolveTargetDocumentModel(entity, allModels));
    }

    List<ProjectItem> created = new ArrayList<>();
    for (int i = 0; i < entities.size(); i++) {
      DocumentModel generatedModel = buildGeneratedModel(targetModels.get(i), linkModel, contextItem);
      String id = generatedModelId(relationship.getId(), entities.get(i).getRole());
      ProjectItem item = NewModelFactory.createModelFromExisting(targetFolder, generatedModel, id);
      StudioEventManager.getInstance().fireModelSavedEvent(item);
      created.add(item);
    }
    return created;
  }

  private static DocumentModel resolveTargetDocumentModel(@NonNull EntityCharacteristic entity, @NonNull List<DocumentModel> allModels) {
    String documentModelId = entity.getDocumentModel();
    if (documentModelId == null || documentModelId.isBlank()) {
      String role = entity.getRole() != null && !entity.getRole().isBlank() ? entity.getRole() : "?";
      throw new IllegalStateException(StudioBundle.get("generate_document_models_missing_target_model", role));
    }
    return findDocumentModel(documentModelId, allModels);
  }

  private static DocumentModel resolveOptionalDocumentModel(String documentModelId, @NonNull List<DocumentModel> allModels) {
    return documentModelId == null || documentModelId.isBlank() ? null : findDocumentModel(documentModelId, allModels);
  }

  private static DocumentModel findDocumentModel(@NonNull String documentModelId, @NonNull List<DocumentModel> allModels) {
    return allModels.stream()
        .filter(model -> documentModelId.equals(model.getId()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(StudioBundle.get("generate_document_models_model_not_found", documentModelId)));
  }

  private static DocumentModel buildGeneratedModel(@NonNull DocumentModel targetModel, DocumentModel linkModel,
      @NonNull ProjectItem contextItem) {
    DocumentModel generated = new DocumentModel();
    DocumentModelContent content = new DocumentModelContent();
    content.setModelInfo(new ModelInfo());
    content.setModelConfig(buildModelConfig(targetModel));
    ModelRoot modelRoot = new ModelRoot();
    content.setModelRoot(modelRoot);
    generated.setContent(content);

    addIncludeGroup(modelRoot, "target", targetModel);
    if (linkModel != null) {
      addIncludeGroup(modelRoot, "relationship", linkModel);
    }

    generated.setLocales(intersectLocales(targetModel, linkModel));
    applyIntersectedRoles(generated, targetModel, linkModel, contextItem);

    return generated;
  }

  private static void addIncludeGroup(@NonNull ModelRoot modelRoot, @NonNull String groupName, @NonNull DocumentModel includedModel) {
    GroupElement group = (GroupElement) DocumentModelElementFactory.newGroupElement(new ArrayList<>(modelRoot.getRootGroups()), modelRoot);
    group.setName(groupName);
    modelRoot.getRootGroups().add(group);

    GroupElement include = (GroupElement) DocumentModelElementFactory.newIncludeElement(group.getGroup().getElements(), modelRoot);
    include.setName(firstRootGroupName(includedModel));
    include.getGroup().getIncludeConfig().setReference(includedModel.getId());
    group.getGroup().getElements().add(include);
  }

  // Mirrors SME's getNameOfFirstGroup: names the Include after the included model's own first top-level group,
  // falling back to the included model's id if it has none yet (an empty Document Model).
  private static String firstRootGroupName(@NonNull DocumentModel documentModel) {
    List<GroupElement> rootGroups = documentModel.getContent() != null && documentModel.getContent().getModelRoot() != null
        ? documentModel.getContent().getModelRoot().getRootGroups()
        : List.of();
    return rootGroups.isEmpty() ? documentModel.getId() : rootGroups.get(0).getName();
  }

  private static ModelConfig buildModelConfig(@NonNull DocumentModel targetModel) {
    ModelConfig config = NewModelFactory.defaultModelConfig();
    ModelConfig targetConfig = targetModel.getContent() != null ? targetModel.getContent().getModelConfig() : null;
    if (targetConfig != null) {
      if (targetConfig.getTimeZone() != null) {
        config.setTimeZone(targetConfig.getTimeZone());
      }
      if (targetConfig.getDecimalSeparator() != null) {
        config.setDecimalSeparator(targetConfig.getDecimalSeparator());
      }
    }
    return config;
  }

  // Mirrors SME's intersectLocales: the Link Document Model's locales (when set) restrict the target's own.
  private static List<Locale> intersectLocales(@NonNull DocumentModel targetModel, DocumentModel linkModel) {
    if (linkModel == null) {
      return copyLocales(targetModel.getLocales());
    }
    Set<String> linkCodes = linkModel.getLocales().stream().map(Locale::getCode).collect(Collectors.toSet());
    return targetModel.getLocales().stream()
        .filter(locale -> linkCodes.contains(locale.getCode()))
        .map(RelationshipDocumentModelGenerator::copyLocale)
        .collect(Collectors.toList());
  }

  private static List<Locale> copyLocales(@NonNull List<Locale> locales) {
    return locales.stream().map(RelationshipDocumentModelGenerator::copyLocale).collect(Collectors.toList());
  }

  private static Locale copyLocale(@NonNull Locale locale) {
    Locale copy = new Locale();
    copy.setCode(locale.getCode());
    return copy;
  }

  // Mirrors SME's intersectRoles: the Link Document Model's roles (when set) restrict the target's own.
  private static void applyIntersectedRoles(@NonNull DocumentModel generated, @NonNull DocumentModel targetModel,
      DocumentModel linkModel, @NonNull ProjectItem contextItem) {
    List<String> roles = new ArrayList<>(RolesEditorPanelController.findDocumentModelRoles(contextItem, targetModel.getId()));
    if (linkModel != null) {
      roles.retainAll(RolesEditorPanelController.findDocumentModelRoles(contextItem, linkModel.getId()));
    }
    RolesEditorPanelController.applyRoles(generated, roles);
  }
}
