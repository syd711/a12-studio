package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A relationship a {@link de.a12.studio.models.querymodel.QuerySort} can traverse before reaching the field it
 * sorts by: {@link #NONE} (sort a field on the target Document Model directly), or one {@code <RelationshipModel,
 * role>} pair found across every {@link RelationshipModel} in the project. Shared between {@link
 * QuerySortingPanelController} (row summary/validation) and {@link
 * de.a12.studio.ui.editors.querymodel.dialogs.QuerySortDialogController} (the Relationship Traversal combo).
 */
public record QueryTraversalOption(String relationshipModel, String targetRole) {

  public static final QueryTraversalOption NONE = new QueryTraversalOption(null, null);

  public static List<QueryTraversalOption> options(@NonNull ProjectItem projectItem) {
    List<QueryTraversalOption> options = new ArrayList<>();
    options.add(NONE);
    for (A12Model<?> candidate : ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.RELATIONSHIP)) {
      if (candidate instanceof RelationshipModel relationshipModel && relationshipModel.getContent() != null) {
        for (EntityCharacteristic characteristic : relationshipModel.getContent().getEntityCharacteristics()) {
          if (characteristic.getRole() != null) {
            options.add(new QueryTraversalOption(relationshipModel.getId(), characteristic.getRole()));
          }
        }
      }
    }
    return options;
  }

  public String display() {
    if (equals(NONE)) {
      return StudioBundle.get("sort_directly_on_target_document_model");
    }
    return relationshipModel + " → " + targetRole;
  }

  /**
   * Every {@code <RelationshipModel, role>} pair reachable from {@code sourceDocumentModelId} - i.e. every role
   * declared on a {@link RelationshipModel} that also declares {@code sourceDocumentModelId} for some role
   * (including {@code sourceDocumentModelId} itself, for a self-referencing relationship such as a hierarchy's
   * Parent/Child). Used by the Model Tree's "Add Relationship" picker (see {@code
   * QueryAddRelationshipDialogController}), unlike {@link #options}'s unscoped "every role in the project" list
   * (kept as-is for the Sort dialog, which only needs *a* valid traversal, not one reachable from a specific node).
   * No {@link #NONE} entry - unlike a sort traversal, "add a relationship" always adds one.
   */
  public static List<QueryTraversalOption> optionsConnectedTo(@NonNull ProjectItem projectItem, @NonNull String sourceDocumentModelId) {
    List<QueryTraversalOption> options = new ArrayList<>();
    for (A12Model<?> candidate : ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.RELATIONSHIP)) {
      if (!(candidate instanceof RelationshipModel relationshipModel) || relationshipModel.getContent() == null) {
        continue;
      }
      List<EntityCharacteristic> characteristics = relationshipModel.getContent().getEntityCharacteristics();
      boolean connected = characteristics.stream().anyMatch(c -> sourceDocumentModelId.equals(c.getDocumentModel()));
      if (!connected) {
        continue;
      }
      for (EntityCharacteristic characteristic : characteristics) {
        if (characteristic.getRole() != null) {
          options.add(new QueryTraversalOption(relationshipModel.getId(), characteristic.getRole()));
        }
      }
    }
    return options;
  }

  /** The Document Model {@code targetRole} resolves to within {@code relationshipModelId}, or null if the
   * relationship or role doesn't exist. */
  @Nullable
  public static DocumentModel resolveTargetDocumentModel(@NonNull ProjectItem projectItem, String relationshipModelId, String targetRole) {
    if (relationshipModelId == null || targetRole == null) {
      return null;
    }
    for (A12Model<?> candidate : ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.RELATIONSHIP)) {
      if (!(candidate instanceof RelationshipModel relationshipModel) || !relationshipModelId.equals(relationshipModel.getId())
          || relationshipModel.getContent() == null) {
        continue;
      }
      String documentModelId = relationshipModel.getContent().getEntityCharacteristics().stream()
          .filter(c -> targetRole.equals(c.getRole()))
          .map(EntityCharacteristic::getDocumentModel)
          .findFirst()
          .orElse(null);
      if (documentModelId == null) {
        return null;
      }
      return ProjectDocumentModels.getOtherDocumentModels(projectItem).stream()
          .filter(dm -> dm.getId().equals(documentModelId))
          .findFirst()
          .orElse(null);
    }
    return null;
  }
}
