package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.ExpansionDepth;
import de.a12.studio.models.treemodel.TreeChildRelationshipConfiguration;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.models.treemodel.TreeNodeActionGroup;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

  /** Returns a draft node (no id yet) carrying the chosen Document Model. */
  public static Optional<TreeNode> showNodeForAdd(Stage owner, ProjectItem projectItem) {
    return showNode(owner, StudioBundle.get("add_node_type_title"), projectItem, null);
  }

  /** Returns a draft node carrying the edited Document Model only; the caller copies it onto {@code existing}. */
  public static Optional<TreeNode> showNodeForEdit(Stage owner, ProjectItem projectItem, TreeNode existing) {
    return showNode(owner, StudioBundle.get("edit_node_type_title"), projectItem, existing);
  }

  /** Returns a new child relationship configuration (with an id) once the dialog is confirmed. */
  public static Optional<TreeChildRelationshipConfiguration> showChildRelationshipForAdd(Stage owner, TreeModel model, ProjectItem projectItem, TreeNode node) {
    TreeChildRelationshipConfiguration configuration = new TreeChildRelationshipConfiguration();
    configuration.setId("crc-" + shortId());
    return showChildRelationship(owner, StudioBundle.get("add_child_relationship_title"), model, projectItem, node, configuration);
  }

  /** Edits a working copy of {@code existing}; the caller replaces the original with the returned one. */
  public static Optional<TreeChildRelationshipConfiguration> showChildRelationshipForEdit(Stage owner, TreeModel model, ProjectItem projectItem,
      TreeNode node, TreeChildRelationshipConfiguration existing) {
    return showChildRelationship(owner, StudioBundle.get("edit_child_relationship_title"), model, projectItem, node, copyOf(existing));
  }

  /** Edits {@code draft}, a new action, and returns it once the dialog is confirmed; the caller adds it to its list. */
  public static Optional<TreeNodeAction> showActionForAdd(Stage owner, ProjectItem projectItem, TreeNodeAction draft, boolean insertOnly) {
    return showAction(owner, StudioBundle.get("add_tree_node_action_title"), projectItem, draft, insertOnly);
  }

  /** Edits a working copy of {@code existing}, so a Cancel leaves the real action untouched; the caller replaces the original. */
  public static Optional<TreeNodeAction> showActionForEdit(Stage owner, ProjectItem projectItem, TreeNodeAction existing, boolean insertOnly) {
    return showAction(owner, StudioBundle.get("edit_tree_node_action_title"), projectItem, copyOf(existing), insertOnly);
  }

  /** Returns a new context-menu group once the dialog is confirmed; the caller adds it to the node's menu. */
  public static Optional<TreeNodeActionGroup> showContextMenuGroupForAdd(Stage owner, ProjectItem projectItem) {
    return showContextMenuGroup(owner, StudioBundle.get("add_context_menu_group_title"), projectItem, new TreeNodeActionGroup());
  }

  /** Edits a working copy of {@code existing}; the caller replaces the original with the returned one. */
  public static Optional<TreeNodeActionGroup> showContextMenuGroupForEdit(Stage owner, ProjectItem projectItem, TreeNodeActionGroup existing) {
    return showContextMenuGroup(owner, StudioBundle.get("edit_context_menu_group_title"), projectItem, copyOf(existing));
  }

  private static Optional<TreeChildRelationshipConfiguration> showChildRelationship(Stage owner, String title, TreeModel model, ProjectItem projectItem,
      TreeNode node, TreeChildRelationshipConfiguration configuration) {
    FXMLLoader fxmlLoader = new FXMLLoader(TreeChildRelationshipDialogController.class.getResource("tree-child-relationship-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("tree-child-relationship-dialog", fxmlLoader, owner, title);
    TreeChildRelationshipDialogController controller = (TreeChildRelationshipDialogController) stage.getUserData();
    controller.init(stage, model, projectItem, node, configuration);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }

  private static Optional<TreeNodeAction> showAction(Stage owner, String title, ProjectItem projectItem, TreeNodeAction action, boolean insertOnly) {
    FXMLLoader fxmlLoader = new FXMLLoader(TreeNodeActionDialogController.class.getResource("tree-node-action-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("tree-node-action-dialog", fxmlLoader, owner, title);
    TreeNodeActionDialogController controller = (TreeNodeActionDialogController) stage.getUserData();
    controller.init(stage, projectItem, action, insertOnly);
    stage.setOnHidden(event -> controller.destroy());
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed() ? Optional.of(controller.getAction()) : Optional.empty();
  }

  private static Optional<TreeNodeActionGroup> showContextMenuGroup(Stage owner, String title, ProjectItem projectItem, TreeNodeActionGroup group) {
    FXMLLoader fxmlLoader = new FXMLLoader(TreeNodeContextMenuGroupDialogController.class.getResource("tree-node-context-menu-group-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("tree-node-context-menu-group-dialog", fxmlLoader, owner, title);
    TreeNodeContextMenuGroupDialogController controller = (TreeNodeContextMenuGroupDialogController) stage.getUserData();
    controller.init(stage, projectItem, group);
    stage.setOnHidden(event -> controller.destroy());
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed() ? Optional.of(controller.getGroup()) : Optional.empty();
  }

  private static TreeChildRelationshipConfiguration copyOf(TreeChildRelationshipConfiguration source) {
    return JsonSettings.objectMapper.readValue(JsonSettings.objectMapper.writeValueAsString(source), TreeChildRelationshipConfiguration.class);
  }

  private static TreeNodeAction copyOf(TreeNodeAction source) {
    return JsonSettings.objectMapper.readValue(JsonSettings.objectMapper.writeValueAsString(source), TreeNodeAction.class);
  }

  private static TreeNodeActionGroup copyOf(TreeNodeActionGroup source) {
    return JsonSettings.objectMapper.readValue(JsonSettings.objectMapper.writeValueAsString(source), TreeNodeActionGroup.class);
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }

  private static Optional<TreeNode> showNode(Stage owner, String title, ProjectItem projectItem, TreeNode existing) {
    FXMLLoader fxmlLoader = new FXMLLoader(TreeNodeDialogController.class.getResource("tree-node-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("tree-node-dialog", fxmlLoader, owner, title);
    TreeNodeDialogController controller = (TreeNodeDialogController) stage.getUserData();
    controller.init(stage, projectItem, existing);
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
