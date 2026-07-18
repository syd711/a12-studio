package de.a12.studio.dataservices.models;

import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.DocumentModelContent;
import de.a12.studio.dataservices.models.documentmodel.ModelConfig;
import de.a12.studio.dataservices.models.documentmodel.ModelInfo;
import de.a12.studio.dataservices.models.documentmodel.ModelRoot;
import de.a12.studio.dataservices.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.UUID;

public class NewModelFactory {

  public static ProjectItem createModel(@NonNull ProjectItem parent, @NonNull ModelType modelType, @NonNull String name) throws IOException {
    A12Model model = buildModel(modelType, name);

    ProjectItem item = parent.createChildModel(name);
    model.setId(UUID.randomUUID().toString());
    model.setModelType(modelType);
    model.setModelVersion(modelType.getCurrentVersion());

    item.setModel(model);
    item.save();
    return item;
  }

  private static A12Model buildModel(ModelType modelType, String name) throws IOException {
    if (modelType != ModelType.DOCUMENT) {
      throw new IOException("Model type '" + modelType.getDisplayName() + "' is not supported yet");
    }

    DocumentModel model = new DocumentModel();
    DocumentModelContent content = new DocumentModelContent();
    ModelInfo modelInfo = new ModelInfo();
    modelInfo.setName(name);
    content.setModelInfo(modelInfo);
    content.setModelConfig(new ModelConfig());
    content.setModelRoot(new ModelRoot());
    model.setContent(content);
    return model;
  }
}
