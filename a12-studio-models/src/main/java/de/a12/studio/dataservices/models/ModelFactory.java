package de.a12.studio.dataservices.models;

import de.a12.studio.dataservices.util.JsonSettings;
import de.a12.studio.dataservices.models.applicationmodel.ApplicationModel;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.formmodel.FormModel;
import de.a12.studio.dataservices.models.overviewmodel.OverviewModel;
import de.a12.studio.dataservices.projects.ProjectItem;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.nio.file.Path;

@Slf4j
public class ModelFactory {

  @Nullable
  public static A12Model load(@NonNull ProjectItem projectItem) {
    if (projectItem.isFolder() || !projectItem.getName().toLowerCase().endsWith(".json")) {
      return null;
    }

    try {
      JsonNode root = JsonSettings.objectMapper.readTree(Path.of(projectItem.getPath()));
      String modelTypeValue = root.path("header").path("modelType").asString(null);
      if (modelTypeValue == null) {
        return null;
      }

      ModelType modelType = ModelType.fromValue(modelTypeValue);
      return switch (modelType) {
        case DOCUMENT -> JsonSettings.objectMapper.treeToValue(root, DocumentModel.class);
        case OVERVIEW -> JsonSettings.objectMapper.treeToValue(root, OverviewModel.class);
        case APPLICATION -> JsonSettings.objectMapper.treeToValue(root, ApplicationModel.class);
        case FORM -> JsonSettings.objectMapper.treeToValue(root, FormModel.class);
        default -> {
          log.warn("Model type '{}' of '{}' is not supported yet", modelType, projectItem.getPath());
          yield null;
        }
      };
    }
    catch (Exception e) {
      log.warn("Failed to load model from '{}': {}", projectItem.getPath(), e.getMessage(), e);
      return null;
    }
  }
}
