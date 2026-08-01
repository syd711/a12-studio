package de.a12.studio.models;

import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.models.projects.ProjectItem;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.nio.file.Path;

@Slf4j
public class ModelFactory {

  // Type Definition Models don't have their own modelType; they are DocumentModels flagged with this
  // header annotation, so a plain modelType lookup can't tell them apart from a regular DocumentModel.
  private static final String TD_ONLY_ANNOTATION = "tdonly";

  @Nullable
  public static A12Model<?> load(@NonNull ProjectItem projectItem) {
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
        case DOCUMENT -> {
          Class<? extends DocumentModel> targetClass = isTypeDefinitionOnly(root) ? TypeDefinitionModel.class : DocumentModel.class;
          yield JsonSettings.objectMapper.treeToValue(root, targetClass);
        }
        case OVERVIEW -> JsonSettings.objectMapper.treeToValue(root, OverviewModel.class);
        case APPLICATION -> JsonSettings.objectMapper.treeToValue(root, ApplicationModel.class);
        case FORM -> JsonSettings.objectMapper.treeToValue(root, FormModel.class);
        case MASTERDETAIL -> JsonSettings.objectMapper.treeToValue(root, MasterDetailModel.class);
        case RELATIONSHIP -> JsonSettings.objectMapper.treeToValue(root, RelationshipModel.class);
        case CONTENT -> JsonSettings.objectMapper.treeToValue(root, ContentModel.class);
        case PRINT -> JsonSettings.objectMapper.treeToValue(root, PrintModel.class);
        case TREE -> JsonSettings.objectMapper.treeToValue(root, TreeModel.class);
        case COMBINATION -> JsonSettings.objectMapper.treeToValue(root, CombinedDocumentModel.class);
        case MAPPING -> JsonSettings.objectMapper.treeToValue(root, MappingModel.class);
        case QUERY -> JsonSettings.objectMapper.treeToValue(root, QueryModel.class);
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

  private static boolean isTypeDefinitionOnly(@NonNull JsonNode root) {
    for (JsonNode annotation : root.path("header").path("annotations")) {
      if (TD_ONLY_ANNOTATION.equals(annotation.path("name").asString(null))
          && "true".equals(annotation.path("value").asString(null))) {
        return true;
      }
    }
    return false;
  }
}
