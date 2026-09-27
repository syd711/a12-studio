package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeEvents;
import de.a12.studio.models.treemodel.TreeHeterogeneity;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Where an action is edited, which decides what its pickers offer: the Events of {@code eventContext} (without the
 * copy/paste ones while a relationship has a link Document Model) and, for an insert action, the Document Models that can
 * be created at a Position - by the node type the action belongs to, or - {@code virtualRoot} - the Root's node type.
 *
 * @param node the node type the action belongs to; {@code null} for the Virtual Root
 */
public record TreeActionContext(@NonNull TreeModel model, @Nullable TreeNode node, TreeEvents.Context eventContext, boolean virtualRoot) {

  /** A row action or context menu action of {@code node}. */
  public static TreeActionContext forNode(@NonNull TreeModel model, @NonNull TreeNode node) {
    return new TreeActionContext(model, node, TreeEvents.Context.ROW, false);
  }

  /** An action of the Virtual Root's row or context menu. */
  public static TreeActionContext forVirtualRoot(@NonNull TreeModel model) {
    return new TreeActionContext(model, null, TreeEvents.Context.HEADER, true);
  }

  public List<String> eventCandidates(@NonNull ProjectItem projectItem) {
    return TreeEvents.candidates(eventContext, TreeProjectModels.hasLinkDocumentModel(model, projectItem));
  }

  /** The Document Models an insert action may create at {@code position} (may be {@code null}). */
  public List<String> documentModelCandidates(@NonNull ProjectItem projectItem, @Nullable String position) {
    if (virtualRoot) {
      return TreeHeterogeneity.rootInsertCandidates(model, TreeProjectModels.heterogeneityModels(projectItem));
    }
    if (node == null) {
      return List.of();
    }
    return TreeHeterogeneity.insertCandidates(model, node, position, TreeProjectModels.relationships(projectItem),
        TreeProjectModels.heterogeneityModels(projectItem));
  }
}
