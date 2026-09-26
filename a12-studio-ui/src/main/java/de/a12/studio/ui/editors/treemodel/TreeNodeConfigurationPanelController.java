package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.models.treemodel.TreeNodeInheritance;
import de.a12.studio.models.treemodel.TreeNodeInheritance.Part;
import de.a12.studio.ui.editors.overviewmodel.StylesPanelController;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Shows, below the node types list ({@link TreeNodeTypesPanelController}), the configuration of the node type
 * selected there - what SME shows in a node type's detail screen next to its Document Model, drag &amp; drop flag and
 * column mapping (those stay in the node type dialog): {@link TreeNodeInheritPanelController} (Inherit From
 * Supertype), the Icon ({@link IconPanelController}), {@link TreeChildRelationshipsPanelController}, {@link
 * TreeNodeActionsPanelController}, {@link TreeNodeContextMenuPanelController}, {@link
 * TreeNodeRowActivationPanelController}, the Row Title ({@link LocalizedTextPanelController}) and the Styles
 * ({@link StylesPanelController}). A part the node inherits from its super type node is hidden, as in SME. Not a
 * property editor itself, just the container that binds them all to the selected node via {@link #setNode}; each
 * panel persists its own changes. The owner learns of edits to the child relationship configurations, which feed the
 * Root panel's choices, through {@link #setOnRelationshipsChange}.
 */
public class TreeNodeConfigurationPanelController implements Initializable {

  @FXML
  private Label titleLabel;

  @FXML
  private Label noSelectionLabel;

  @FXML
  private VBox content;

  @FXML
  private TreeNodeInheritPanelController inheritPanelController;

  @FXML
  private IconPanelController iconPanelController;

  @FXML
  private Node iconPanel;

  @FXML
  private TreeChildRelationshipsPanelController childRelationshipsPanelController;

  @FXML
  private Node childRelationshipsPanel;

  @FXML
  private TreeNodeActionsPanelController actionsPanelController;

  @FXML
  private Node actionsPanel;

  @FXML
  private TreeNodeContextMenuPanelController contextMenuPanelController;

  @FXML
  private Node contextMenuPanel;

  @FXML
  private TreeNodeRowActivationPanelController rowActivationPanelController;

  @FXML
  private Node rowActivationPanel;

  @FXML
  private LocalizedTextPanelController rowTitlePanelController;

  @FXML
  private Node rowTitlePanel;

  @FXML
  private StylesPanelController stylesPanelController;

  @FXML
  private Node stylesPanel;

  private TreeModel model;
  private ProjectItem projectItem;
  private TreeNode node;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    rowTitlePanelController.configureCustom("rowTitle", StudioBundle.get("tree_node_configuration_panel.row_title"));
    inheritPanelController.setOnInheritanceChange(part -> refreshNode());
    setNode(null);
  }

  public void setModel(@NonNull TreeModel model, @NonNull ProjectItem projectItem) {
    this.model = model;
    this.projectItem = projectItem;
    childRelationshipsPanelController.setModel(model, projectItem);
    contextMenuPanelController.setProjectItem(projectItem);
    // Read on every access, since the selected node changes; a node without selection has no (editable) actions.
    actionsPanelController.configure(StudioBundle.get("actions"), ".nodeActions", projectItem,
        () -> node != null ? node.getActions() : List.of(), () -> false, TreeNodeConfigurationPanelController::newRowAction);
    setNode(null);
  }

  /** Called whenever a child relationship configuration was added, changed, moved or removed. */
  public void setOnRelationshipsChange(@NonNull Runnable onRelationshipsChange) {
    childRelationshipsPanelController.setOnChange(onRelationshipsChange);
  }

  /** Shows the configuration of {@code node}, or asks for a node type to be selected ({@code null}). */
  public void setNode(TreeNode node) {
    this.node = node;
    boolean selected = node != null && model != null;
    content.setVisible(selected);
    content.setManaged(selected);
    noSelectionLabel.setVisible(!selected);
    noSelectionLabel.setManaged(!selected);
    titleLabel.setText(selected ? StudioBundle.get("tree_node_configuration_panel.title", describe(node)) : "");
    if (selected) {
      refreshNode();
    }
    else {
      childRelationshipsPanelController.setNode(null);
      contextMenuPanelController.setNode(null);
    }
  }

  /** Re-reads everything from the selected node and hides the parts it inherits. */
  private void refreshNode() {
    inheritPanelController.setNode(node, hasSuperTypeNode());
    iconPanelController.setCustom(node::getIcon, node::setIcon);
    childRelationshipsPanelController.setNode(node);
    actionsPanelController.refresh();
    contextMenuPanelController.setNode(node);
    rowActivationPanelController.setNode(node);
    rowTitlePanelController.setCustom(node::getRowTitle);
    stylesPanelController.setCustom(node::getStyles);

    setShown(iconPanel, Part.ICON);
    setShown(childRelationshipsPanel, Part.CHILD_RELATIONSHIP_CONFIGURATIONS);
    setShown(actionsPanel, Part.ACTIONS);
    setShown(contextMenuPanel, Part.CONTEXT_MENU);
    setShown(rowActivationPanel, Part.DEFAULT_ROW_ACTION);
    setShown(rowTitlePanel, Part.ROW_TITLE);
    setShown(stylesPanel, Part.STYLES);
  }

  private void setShown(Node panel, Part part) {
    boolean shown = !TreeNodeInheritance.isInherited(node, part);
    panel.setVisible(shown);
    panel.setManaged(shown);
  }

  /** SME requires a row action to have a priority (and writes Destructive with it); a context-menu action has neither. */
  private static TreeNodeAction newRowAction() {
    TreeNodeAction action = new TreeNodeAction();
    action.setPrimary(false);
    action.setDestructive(false);
    return action;
  }

  private boolean hasSuperTypeNode() {
    if (projectItem == null) {
      return false;
    }
    Collection<DocumentModel> documentModels = ProjectDocumentModels.getOtherDocumentModels(projectItem);
    List<TreeNode> nodes = model.getContent().getNodes();
    return TreeNodeInheritance.isSubTypeNode(node, nodes, documentModels);
  }

  private static String describe(TreeNode node) {
    return node.getDocumentModelRef() != null ? node.getDocumentModelRef() : StudioBundle.get("no_document_model_selected");
  }
}
