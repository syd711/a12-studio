package de.a12.studio.dataservices.models;

import de.a12.studio.dataservices.models.applicationmodel.ApplicationModel;
import de.a12.studio.dataservices.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.DocumentModelContent;
import de.a12.studio.dataservices.models.documentmodel.ModelConfig;
import de.a12.studio.dataservices.models.documentmodel.ModelInfo;
import de.a12.studio.dataservices.models.documentmodel.ModelRoot;
import de.a12.studio.dataservices.models.formmodel.FormModel;
import de.a12.studio.dataservices.models.formmodel.FormModelContent;
import de.a12.studio.dataservices.models.overviewmodel.OverviewModel;
import de.a12.studio.dataservices.models.overviewmodel.OverviewModelContent;
import de.a12.studio.dataservices.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.dataservices.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class NewModelFactory {

  // Type Definition Models don't have their own header modelType; they are DocumentModels flagged with
  // this annotation (see ModelFactory), so the header written here must stay "document".
  private static final String TD_ONLY_ANNOTATION = "tdonly";

  public static ProjectItem createModel(@NonNull ProjectItem parent, @NonNull ModelType modelType, @NonNull String name) throws IOException {
    A12Model model = buildModel(modelType, name);

    ProjectItem item = parent.createChildModel(name);
    model.setId(UUID.randomUUID().toString());
    model.setModelType(model instanceof TypeDefinitionModel ? ModelType.DOCUMENT : modelType);
    model.setModelVersion(modelType.getCurrentVersion());

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
    content.setModelConfig(new ModelConfig());
    content.setModelRoot(new ModelRoot());
    model.setContent(content);
    return model;
  }

  private static TypeDefinitionModel buildTypeDefinitionModel(@NonNull String name) {
    TypeDefinitionModel model = buildDocumentModel(new TypeDefinitionModel(), name);

    Annotation tdOnly = new Annotation();
    tdOnly.setName(TD_ONLY_ANNOTATION);
    tdOnly.setValue("true");
    model.setAnnotations(List.of(tdOnly));
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
