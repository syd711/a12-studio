package de.a12.studio.models;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelContent;
import de.a12.studio.models.contentmodel.ContentConfiguration;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentModelContent;
import de.a12.studio.models.documentmodel.ConditionLanguage;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.models.documentmodel.ModelInfo;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.FormScreenGenerator;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.mappingmodel.MappingModelContent;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.masterdetailmodel.MasterDetailModelContent;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.OverviewModelContent;
import de.a12.studio.models.printmodel.ComputationStep;
import de.a12.studio.models.printmodel.PrintGeneral;
import de.a12.studio.models.printmodel.PrintMetadata;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.printmodel.PrintModelContent;
import de.a12.studio.models.printmodel.PrintSegments;
import de.a12.studio.models.printmodel.PrintStructureEntry;
import de.a12.studio.models.printmodel.SegmentDefaults;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.LinkConstraints;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModelContent;
import de.a12.studio.models.treemodel.ExpansionStrategy;
import de.a12.studio.models.treemodel.SlotBox;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeModelContent;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class NewModelFactory {

  // Type Definition Models don't have their own header modelType; they are DocumentModels flagged with
  // this annotation (see ModelFactory), so the header written here must stay "document".
  private static final String TD_ONLY_ANNOTATION = "tdonly";

  /**
   * Extension hook: something that can adjust the filename of a model about to be created.
   * Registered by plugin-aware code (e.g. {@code de.a12.studio.ui.Studio}) so that plugin-contributed
   * {@code INewModelNameInterceptor}s run on every model creation, without this module depending on
   * the plugin manager. Applied to the filename only, never to the display name seeded into the
   * model's own content (see {@link #buildModel}).
   */
  public interface NewModelNameHook {
    @NonNull String adjustName(@NonNull ProjectItem parent, ModelType modelType, @NonNull String proposedName);
  }

  private static final List<NewModelNameHook> nameHooks = new CopyOnWriteArrayList<>();

  public static void registerNameHook(NewModelNameHook hook) {
    nameHooks.add(hook);
  }

  private static String applyNameHooks(ProjectItem parent, ModelType modelType, String name) {
    String result = name;
    for (NewModelNameHook hook : nameHooks) {
      result = hook.adjustName(parent, modelType, result);
    }
    return result;
  }

  public static ProjectItem createModel(@NonNull ProjectItem parent, @NonNull ModelType modelType, @NonNull String name) throws IOException {
    return createModel(parent, modelType, name, null);
  }

  /**
   * Persists an already-constructed {@link A12Model} (e.g. one assembled by an import wizard) as a
   * new project item under {@code parent}.  The caller is responsible for populating the model's
   * {@code content} and {@code locales}; this method sets the id, modelType and modelVersion from
   * the model's own {@link A12Model#getModelType()} field, defaulting to
   * {@link ModelType#DOCUMENT} when the type is not yet set.
   *
   * @param parent    the folder that will own the new file
   * @param model     the pre-built model to persist
   * @param name      the desired filename (without extension)
   * @return the new {@link ProjectItem} backed by the written file
   * @throws IOException if the file cannot be created or written
   */
  public static ProjectItem createModelFromExisting(@NonNull ProjectItem parent,
                                                    @NonNull A12Model<?> model,
                                                    @NonNull String name) throws IOException {
    ModelType modelType = model.getModelType() != null ? model.getModelType() : ModelType.DOCUMENT;
    String fileName = applyNameHooks(parent, modelType, name);
    ProjectItem item = parent.createChildModel(fileName);
    model.setId(ProjectItem.idFromFileName(item.getName()));
    model.setModelType(modelType);
    model.setModelVersion(modelType.getCurrentVersion());
    item.setModel(model);
    item.save();
    return item;
  }

  /**
   * Exposes the shared {@link ModelConfig} defaults so callers outside this class (e.g. import
   * wizards) can reuse them without duplicating the logic.
   */
  public static ModelConfig defaultModelConfig() {
    ModelConfig modelConfig = new ModelConfig();
    modelConfig.setTimeZone("UTC");
    modelConfig.setDecimalSeparator(".");
    ConditionLanguage conditionLanguage = new ConditionLanguage();
    conditionLanguage.setCode("en_US");
    modelConfig.setConditionLanguage(conditionLanguage);
    return modelConfig;
  }

  public static ProjectItem createModel(@NonNull ProjectItem parent, @NonNull ModelType modelType, @NonNull String name,
      String documentModelId) throws IOException {
    return createModel(parent, modelType, name, documentModelId, false);
  }

  public static ProjectItem createModel(@NonNull ProjectItem parent, @NonNull ModelType modelType, @NonNull String name,
      String documentModelId, boolean buildScreensFromFields) throws IOException {
    A12Model<?> model = buildModel(parent, modelType, name, resolveDefaultLocales(parent), documentModelId, buildScreensFromFields);

    String fileName = applyNameHooks(parent, modelType, name);
    ProjectItem item = parent.createChildModel(fileName);
    // Type Definition Models are persisted with header modelType "document" (see TD_ONLY_ANNOTATION
    // above), so the version must come from ModelType.DOCUMENT too, not from the typedefinition entry.
    ModelType headerModelType = model instanceof TypeDefinitionModel ? ModelType.DOCUMENT : modelType;
    model.setId(ProjectItem.idFromFileName(item.getName()));
    model.setModelType(headerModelType);
    model.setModelVersion(headerModelType.getCurrentVersion());

    item.setModel(model);
    item.save();
    return item;
  }

  private static A12Model<?> buildModel(ProjectItem parent, ModelType modelType, String name, List<Locale> locales,
      String documentModelId, boolean buildScreensFromFields) throws IOException {
    return switch (modelType) {
      case DOCUMENT -> buildDocumentModel(new DocumentModel(), name, locales);
      case TYPEDEFINITION -> buildTypeDefinitionModel(name, locales);
      case FORM -> buildFormModel(parent, documentModelId, locales, buildScreensFromFields);
      case OVERVIEW -> buildOverviewModel(documentModelId);
      case APPLICATION -> buildApplicationModel();
      case MASTERDETAIL -> buildMasterDetailModel();
      case RELATIONSHIP -> buildRelationshipModel(locales);
      case CONTENT -> buildContentModel(locales);
      case PRINT -> buildPrintModel(name, locales);
      case TREE -> buildTreeModel(locales);
      case COMBINATION -> buildCombinationModel(locales);
      case MAPPING -> buildMappingModel(locales);
      case QUERY -> buildQueryModel(locales);
      case STRUCTURALMAPPING -> buildStructuralMappingModel(locales);
    };
  }

  private static <T extends DocumentModel> T buildDocumentModel(@NonNull T model, @NonNull String name, List<Locale> locales) {
    DocumentModelContent content = new DocumentModelContent();
    ModelInfo modelInfo = new ModelInfo();
    modelInfo.setName(name);
    content.setModelInfo(modelInfo);
    content.setModelConfig(defaultModelConfig());
    content.setModelRoot(new ModelRoot());
    model.setContent(content);
    model.setLocales(locales);
    return model;
  }

  // New models seed their locales from the project's settings.json (general.locales) so they stay
  // consistent with the languages the rest of the project is maintained in.
  private static List<Locale> resolveDefaultLocales(ProjectItem parent) {
    ProjectItem root = parent;
    while (root.getParent() != null) {
      root = root.getParent();
    }

    List<Locale> projectLocales = ProjectRootSettings.load(root.getFile()).getGeneral().getLocales();
    if (projectLocales.isEmpty()) {
      return fallbackLocales();
    }

    List<Locale> locales = new ArrayList<>();
    for (Locale projectLocale : projectLocales) {
      Locale locale = new Locale();
      locale.setCode(projectLocale.getCode());
      locales.add(locale);
    }
    return locales;
  }

  private static List<Locale> fallbackLocales() {
    Locale en = new Locale();
    en.setCode("en");
    Locale de = new Locale();
    de.setCode("de");
    return new ArrayList<>(List.of(en, de));
  }

  private static TypeDefinitionModel buildTypeDefinitionModel(@NonNull String name, List<Locale> locales) {
    TypeDefinitionModel model = buildDocumentModel(new TypeDefinitionModel(), name, locales);

    Annotation tdOnly = new Annotation();
    tdOnly.setName(TD_ONLY_ANNOTATION);
    tdOnly.setValue("true");
    model.setAnnotations(new ArrayList<>(List.of(tdOnly)));
    return model;
  }

  // alias equal to reference, matching the shape of an existing FM's data-binding reference (see e.g.
  // Invoice_FM.json / GeneralSettingsPanelController#applyDocumentModelReference).
  private static FormModel buildFormModel(ProjectItem parent, String documentModelId, List<Locale> locales,
      boolean buildScreensFromFields) {
    FormModel model = new FormModel();
    FormModelContent content = new FormModelContent();
    model.setContent(content);
    if (documentModelId != null && !documentModelId.isBlank()) {
      model.getModelReferences().add(documentModelReference(ModelReference.PURPOSE_DATA_BINDING, documentModelId, documentModelId));
      if (buildScreensFromFields) {
        DocumentModel documentModel = findDocumentModel(parent, documentModelId);
        if (documentModel != null) {
          FormScreenGenerator.generate(content, documentModel, locales);
        }
      }
    }
    return model;
  }

  // Searches the whole project tree (from its root, like #resolveDefaultLocales) for the Document Model
  // backing a newly created Form Model, so "Build Screens from Fields" can read its field structure.
  private static DocumentModel findDocumentModel(ProjectItem parent, String documentModelId) {
    ProjectItem root = parent;
    while (root.getParent() != null) {
      root = root.getParent();
    }
    return findDocumentModelInTree(root, documentModelId);
  }

  private static DocumentModel findDocumentModelInTree(ProjectItem item, String documentModelId) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        DocumentModel found = findDocumentModelInTree(child, documentModelId);
        if (found != null) {
          return found;
        }
      }
      return null;
    }
    return item.getModel() instanceof DocumentModel documentModel && documentModelId.equals(documentModel.getId())
        ? documentModel : null;
  }

  // alias fixed to "DM", matching the shape of an existing OM's document-model-for-overview reference
  // (see e.g. Company_OM.json / OverviewReferencePanelController#syncModelReferences).
  private static OverviewModel buildOverviewModel(String documentModelId) {
    OverviewModel model = new OverviewModel();
    model.setContent(new OverviewModelContent());
    if (documentModelId != null && !documentModelId.isBlank()) {
      model.getModelReferences().add(documentModelReference(ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW, "DM", documentModelId));
    }
    return model;
  }

  private static ModelReference documentModelReference(String purpose, String alias, String documentModelId) {
    ModelReference reference = new ModelReference();
    reference.setModelType(ModelType.DOCUMENT);
    reference.setPurpose(purpose);
    reference.setAlias(alias);
    reference.setReference(documentModelId);
    return reference;
  }

  private static ApplicationModel buildApplicationModel() {
    ApplicationModel model = new ApplicationModel();
    model.setContent(new ApplicationModelContent());
    return model;
  }

  private static MasterDetailModel buildMasterDetailModel() {
    MasterDetailModel model = new MasterDetailModel();
    MasterDetailModelContent content = new MasterDetailModelContent();
    content.setType("overview");
    model.setContent(content);
    return model;
  }

  private static RelationshipModel buildRelationshipModel(List<Locale> locales) {
    RelationshipModel model = new RelationshipModel();
    RelationshipModelContent content = new RelationshipModelContent();
    content.setDuplicatesAllowed(false);
    // A relationship always connects exactly two entities; start with two empty ones to fill in.
    content.getEntityCharacteristics().add(emptyEntityCharacteristic());
    content.getEntityCharacteristics().add(emptyEntityCharacteristic());
    model.setContent(content);
    model.setLocales(locales);
    return model;
  }

  private static EntityCharacteristic emptyEntityCharacteristic() {
    EntityCharacteristic entity = new EntityCharacteristic();
    entity.setOrdered(false);
    Multiplicity multiplicity = new Multiplicity();
    multiplicity.setUnbounded(true);
    LinkConstraints constraints = new LinkConstraints();
    constraints.setMultiplicity(multiplicity);
    entity.setLinkConstraints(constraints);
    return entity;
  }

  private static ContentModel buildContentModel(List<Locale> locales) {
    ContentModel model = new ContentModel();
    ContentModelContent content = new ContentModelContent();
    content.setConfiguration(new ContentConfiguration());

    ContentElement root = new ContentElement();
    root.setId(shortId());
    root.setType("Box");
    root.setNamespace("com.mgmtp.a12.contentengine");
    root.setChildren(new ArrayList<>());
    content.setRoot(root);

    model.setContent(content);
    model.setLocales(locales);
    return model;
  }

  // Mirrors the empty print model skeleton the SME print editor creates (see PrintModel.json in the
  // basic testing workspace): metadata computations prefilled, no segments or element definitions.
  private static PrintModel buildPrintModel(String name, List<Locale> locales) {
    PrintModel model = new PrintModel();
    PrintModelContent content = new PrintModelContent();
    content.setId("PRINT_MODEL_CONTENT");

    PrintGeneral general = new PrintGeneral();
    general.setId("PRINT_MODEL_CONTENT_GENERAL");
    PrintMetadata metadata = new PrintMetadata();
    metadata.setId(nanoId());
    metadata.getTitleComputation().add(computationStep("\"" + name + "\""));
    metadata.getDescriptionComputation().add(computationStep("\"\""));
    metadata.getLanguageComputation().add(computationStep("\"EN\""));
    metadata.getAuthorComputation().add(computationStep("\"A12 Studio\""));
    general.setMetadata(metadata);
    general.setStructure(new ArrayList<PrintStructureEntry>());
    SegmentDefaults segmentDefaults = new SegmentDefaults();
    segmentDefaults.setId(nanoId());
    segmentDefaults.setFontSize(12);
    general.setSegmentDefaults(segmentDefaults);
    content.setGeneral(general);

    PrintSegments segments = new PrintSegments();
    segments.setId(nanoId());
    content.setSegments(segments);

    model.setContent(content);
    model.setLocales(locales);
    return model;
  }

  private static ComputationStep computationStep(String operation) {
    ComputationStep step = new ComputationStep();
    step.setId(nanoId());
    step.setOperation(operation);
    return step;
  }

  private static TreeModel buildTreeModel(List<Locale> locales) {
    TreeModel model = new TreeModel();
    TreeModelContent content = new TreeModelContent();
    TreeConfiguration configuration = new TreeConfiguration();
    ExpansionStrategy expansionStrategy = new ExpansionStrategy();
    expansionStrategy.setType("level_by_level");
    configuration.setExpansionStrategy(expansionStrategy);
    content.setConfiguration(configuration);
    content.setSubHeaderBox(new SlotBox());
    content.setFooterBox(new SlotBox());
    model.setContent(content);
    model.setLocales(locales);
    return model;
  }

  private static CombinedDocumentModel buildCombinationModel(List<Locale> locales) {
    CombinedDocumentModel model = new CombinedDocumentModel();
    model.setContent(new CombinedDocumentModelContent());
    model.setLocales(locales);
    return model;
  }

  private static MappingModel buildMappingModel(List<Locale> locales) {
    MappingModel model = new MappingModel();
    model.setContent(new MappingModelContent());
    model.setLocales(locales);
    return model;
  }

  private static QueryModel buildQueryModel(List<Locale> locales) {
    QueryModel model = new QueryModel();
    model.setContent(new QueryModelContent());
    model.setLocales(locales);
    return model;
  }

  private static StructuralMappingModel buildStructuralMappingModel(List<Locale> locales) {
    StructuralMappingModel model = new StructuralMappingModel();
    model.setContent(new StructuralMappingModelContent());
    model.setLocales(locales);
    return model;
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
  }

  // Print model node ids follow the nanoid style of the SME print editor (21 url-safe characters).
  private static String nanoId() {
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
    StringBuilder id = new StringBuilder(21);
    java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();
    for (int i = 0; i < 21; i++) {
      id.append(alphabet.charAt(random.nextInt(alphabet.length())));
    }
    return id.toString();
  }
}
