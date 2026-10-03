package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.treemodel.TreeEvents;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.ui.util.ProjectDocumentModels;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** The project's models a Tree Model editor needs: relationships, and the Document and Combination Models. */
public final class TreeProjectModels {

  private TreeProjectModels() {
  }

  /** Resolves a relationship model id of {@code projectItem}'s project to the model (or {@code null}). */
  public static Function<String, RelationshipModel> relationships(@NonNull ProjectItem projectItem) {
    Map<String, RelationshipModel> byId = new HashMap<>();
    for (RelationshipModel relationship : ProjectDocumentModels.getRelationshipModelsConnectedTo(projectItem, null)) {
      byId.put(relationship.getId(), relationship);
    }
    return byId::get;
  }

  /**
   * The models the super type / sub type graph of a tree is built from: the project's Document Models and Combination
   * Models (SME's sub type resolution works on every standalone Document Model type).
   */
  public static List<A12Model<?>> heterogeneityModels(@NonNull ProjectItem projectItem) {
    List<A12Model<?>> models = new ArrayList<>(ProjectDocumentModels.getOtherDocumentModelsWithGenerated(projectItem));
    models.addAll(ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.COMBINATION));
    return models;
  }

  /** The ids of the Document Models and Combination Models a node type may be based on, sorted. */
  public static List<String> nodeDocumentModelIds(@NonNull ProjectItem projectItem) {
    return heterogeneityModels(projectItem).stream().map(A12Model::getId).sorted().toList();
  }

  /** Whether one of the relationships {@code model} uses has a link Document Model (no copy/paste events then). */
  public static boolean hasLinkDocumentModel(TreeModel model, @NonNull ProjectItem projectItem) {
    return model != null && TreeEvents.hasLinkDocumentModel(model, relationships(projectItem));
  }
}
