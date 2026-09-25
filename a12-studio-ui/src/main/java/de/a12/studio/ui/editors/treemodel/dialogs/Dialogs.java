package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.ExpansionDepth;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class Dialogs {

  private Dialogs() {
  }

  public static Optional<TreeColumn> showColumnForAdd(Stage owner) {
    return showColumn(owner, StudioBundle.get("add_column_title"), null);
  }

  public static Optional<TreeColumn> showColumnForEdit(Stage owner, TreeColumn existing) {
    return showColumn(owner, StudioBundle.get("edit_column_title"), existing);
  }

  public static Optional<ExpansionDepth> showExpansionDepthForAdd(Stage owner, List<String> relationships) {
    return showExpansionDepth(owner, StudioBundle.get("tree_expansion_depths_panel.add_title"), relationships, null);
  }

  public static Optional<ExpansionDepth> showExpansionDepthForEdit(Stage owner, List<String> relationships, ExpansionDepth existing) {
    return showExpansionDepth(owner, StudioBundle.get("tree_expansion_depths_panel.edit_title"), relationships, existing);
  }

  /** Returns a draft node (no id yet) carrying the chosen Document Model, drag &amp; drop flag and column mapping. */
  public static Optional<TreeNode> showNodeForAdd(Stage owner, TreeModel model, ProjectItem projectItem) {
    return showNode(owner, StudioBundle.get("add_node_type_title"), model, projectItem, null);
  }

  /** Returns a draft node carrying the edited values only; the caller copies them onto {@code existing}. */
  public static Optional<TreeNode> showNodeForEdit(Stage owner, TreeModel model, ProjectItem projectItem, TreeNode existing) {
    return showNode(owner, StudioBundle.get("edit_node_type_title"), model, projectItem, existing);
  }

  private static Optional<TreeNode> showNode(Stage owner, String title, TreeModel model, ProjectItem projectItem, TreeNode existing) {
    FXMLLoader fxmlLoader = new FXMLLoader(TreeNodeDialogController.class.getResource("tree-node-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("tree-node-dialog", fxmlLoader, owner, title);
    TreeNodeDialogController controller = (TreeNodeDialogController) stage.getUserData();
    controller.init(stage, model, projectItem, existing);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }

  private static Optional<ExpansionDepth> showExpansionDepth(Stage owner, String title, List<String> relationships, ExpansionDepth existing) {
    FXMLLoader fxmlLoader = new FXMLLoader(TreeExpansionDepthDialogController.class.getResource("tree-expansion-depth-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("tree-expansion-depth-dialog", fxmlLoader, owner, title);
    TreeExpansionDepthDialogController controller = (TreeExpansionDepthDialogController) stage.getUserData();
    controller.init(stage, relationships, existing);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }

  private static Optional<TreeColumn> showColumn(Stage owner, String title, TreeColumn existing) {
    FXMLLoader fxmlLoader = new FXMLLoader(TreeColumnDialogController.class.getResource("tree-column-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("tree-column-dialog", fxmlLoader, owner, title);
    TreeColumnDialogController controller = (TreeColumnDialogController) stage.getUserData();
    controller.init(stage, existing);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }
}
