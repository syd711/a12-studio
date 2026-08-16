package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import org.jspecify.annotations.NonNull;

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
}
