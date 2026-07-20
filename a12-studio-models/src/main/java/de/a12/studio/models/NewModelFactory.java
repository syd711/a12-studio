package de.a12.studio.models;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.documentmodel.ConditionLanguage;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.models.documentmodel.ModelInfo;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.OverviewModelContent;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewModelFactory {

  // Type Definition Models don't have their own header modelType; they are DocumentModels flagged with
  // this annotation (see ModelFactory), so the header written here must stay "document".
  private static final String TD_ONLY_ANNOTATION = "tdonly";

  public static ProjectItem createModel(@NonNull ProjectItem parent, @NonNull ModelType modelType, @NonNull String name) throws IOException {
    A12Model model = buildModel(modelType, name);

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

  private static A12Model buildModel(ModelType modelType, String name) throws IOException {
    return switch (modelType) {
      case DOCUMENT -> buildDocumentModel(new DocumentModel(), name);
      case TYPEDEFINITION -> buildTypeDefinitionModel(name);
      case FORM -> buildFormModel();
      case OVERVIEW -> buildOverviewModel();
      case APPLICATION -> buildApplicationModel();
      default -> throw new IOException("Model type '" + modelType.getDisplayName() + "' is not supported yet");
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
}
