package de.a12.studio.models;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
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
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.LinkConstraints;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
import de.a12.studio.models.treemodel.ExpansionStrategy;
import de.a12.studio.models.treemodel.SlotBox;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeModelContent;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewModelFactory {

  // Type Definition Models don't have their own header modelType; they are DocumentModels flagged with
  // this annotation (see ModelFactory), so the header written here must stay "document".
  private static final String TD_ONLY_ANNOTATION = "tdonly";

  public static ProjectItem createModel(@NonNull ProjectItem parent, @NonNull ModelType modelType, @NonNull String name) throws IOException {
    A12Model<?> model = buildModel(modelType, name);

    ProjectItem item = parent.createChildModel(name);
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

  private static A12Model<?> buildModel(ModelType modelType, String name) throws IOException {
    return switch (modelType) {
      case DOCUMENT -> buildDocumentModel(new DocumentModel(), name);
      case TYPEDEFINITION -> buildTypeDefinitionModel(name);
      case FORM -> buildFormModel();
      case OVERVIEW -> buildOverviewModel();
      case APPLICATION -> buildApplicationModel();
      case MASTERDETAIL -> buildMasterDetailModel();
      case RELATIONSHIP -> buildRelationshipModel();
      case CONTENT -> buildContentModel();
      case PRINT -> buildPrintModel(name);
      case TREE -> buildTreeModel();
    };
  }

  private static <T extends DocumentModel> T buildDocumentModel(@NonNull T model, @NonNull String name) {
    DocumentModelContent content = new DocumentModelContent();
    ModelInfo modelInfo = new ModelInfo();
    modelInfo.setName(name);
    content.setModelInfo(modelInfo);
    content.setModelConfig(defaultModelConfig());
    content.setModelRoot(new ModelRoot());
    model.setContent(content);
    model.setLocales(defaultLocales());
    return model;
  }

  // Kernel deserialization requires these fields to be present; values match the convention used
  // across existing document models in this project (see e.g. Person_DM.json).
  private static ModelConfig defaultModelConfig() {
    ModelConfig modelConfig = new ModelConfig();
    modelConfig.setTimeZone("UTC");
    modelConfig.setDecimalSeparator(".");
    ConditionLanguage conditionLanguage = new ConditionLanguage();
    conditionLanguage.setCode("en_US");
    modelConfig.setConditionLanguage(conditionLanguage);
    return modelConfig;
  }

  private static List<Locale> defaultLocales() {
    Locale en = new Locale();
    en.setCode("en");
    Locale de = new Locale();
    de.setCode("de");
    return new ArrayList<>(List.of(en, de));
  }

  private static TypeDefinitionModel buildTypeDefinitionModel(@NonNull String name) {
    TypeDefinitionModel model = buildDocumentModel(new TypeDefinitionModel(), name);

    Annotation tdOnly = new Annotation();
    tdOnly.setName(TD_ONLY_ANNOTATION);
    tdOnly.setValue("true");
    model.setAnnotations(new ArrayList<>(List.of(tdOnly)));
    return model;
  }

  private static FormModel buildFormModel() {
    FormModel model = new FormModel();
    model.setContent(new FormModelContent());
    return model;
  }

  private static OverviewModel buildOverviewModel() {
    OverviewModel model = new OverviewModel();
    model.setContent(new OverviewModelContent());
    return model;
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

  private static RelationshipModel buildRelationshipModel() {
    RelationshipModel model = new RelationshipModel();
    RelationshipModelContent content = new RelationshipModelContent();
    content.setDuplicatesAllowed(false);
    // A relationship always connects exactly two entities; start with two empty ones to fill in.
    content.getEntityCharacteristics().add(emptyEntityCharacteristic());
    content.getEntityCharacteristics().add(emptyEntityCharacteristic());
    model.setContent(content);
    model.setLocales(defaultLocales());
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

  private static ContentModel buildContentModel() {
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
    model.setLocales(defaultLocales());
    return model;
  }

  // Mirrors the empty print model skeleton the SME print editor creates (see PrintModel.json in the
  // basic testing workspace): metadata computations prefilled, no segments or element definitions.
  private static PrintModel buildPrintModel(String name) {
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
    model.setLocales(defaultLocales());
    return model;
  }

  private static ComputationStep computationStep(String operation) {
    ComputationStep step = new ComputationStep();
    step.setId(nanoId());
    step.setOperation(operation);
    return step;
  }

  private static TreeModel buildTreeModel() {
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
    model.setLocales(defaultLocales());
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
