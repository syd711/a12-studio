package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.InitialExpansion;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.editors.treemodel.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Edits a {@link TreeModel}'s {@code content.nodes}: one draggable, reorderable row per node type,
 * summarizing its Document Model and whether drag &amp; drop is allowed. A leading radio button (or a click
 * anywhere on the row) selects the row, which {@link #setOnSelectionChange} reports so the owning editor can show
 * that node type's configuration ({@link TreeNodeConfigurationPanelController}) below the list. The pencil button
 * (or a double click) opens {@link Dialogs#showNodeForEdit}, the Add button below the rows {@link
 * Dialogs#showNodeForAdd}; both only ask for the node's Document Model (drag &amp; drop and the per-column field
 * mapping are edited in {@link TreeNodeConfigurationPanelController}, which calls {@link #refresh()} after a
 * change that shows in the rows). Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by e.g. {@link
 * TreeColumnsPanelController}. The header's {@code modelReferences} track the node Document Models (purpose
 * "document-model-for-tree"), synced by {@link #syncModelReferences()}. Editing a row only touches what the
 * dialog edits (the Document Model); everything else on the node - drag &amp; drop, columns, actions,
 * child relationship configurations, icon, ... - is left as it was.
 */
public class TreeNodeTypesPanelController extends AbstractPropertyEditor implements Initializable {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getNodes().
  private static final DataFormat NODE_INDEX = new DataFormat("application/x-a12-tree-node-index");

  private static final String SELECTED_ROW_STYLE = "module-row-selected";

  // Must match the leading spacer in tree-node-types-panel.fxml's header.
  private static final double RADIO_COLUMN_WIDTH = 20.0;

  @FXML
  private HBox nodeHeaders;

  @FXML
  private VBox nodeRows;

  @FXML
  private Label nodesEmptyLabel;

  private TreeModel model;
  private ProjectItem projectItem;

  // One radio per row; exactly the selected node's is on. Clicking anywhere on a row selects it.
  private final ToggleGroup selectionGroup = new ToggleGroup();
  private TreeNode selectedNode;
  // Set while rows are being rebuilt, so the radios' programmatic state changes aren't mistaken for clicks.
  private boolean rebuilding;

  // Notified after every structural change (add/edit/reorder/delete), so the owning editor can keep the Root
  // panel's choices - derived from the nodes' child relationship configurations - in sync.
  private Runnable onChange = () -> {
  };

  // Notified whenever the selected node changes and after every rebuild (an edit changes the selected node in
  // place, so the editor below has to re-read it); null when there are no node types.
  private Consumer<TreeNode> onSelectionChange = node -> {
  };

  public void setModel(@NonNull TreeModel model, @NonNull ProjectItem projectItem) {
    this.model = model;
    this.projectItem = projectItem;
    this.selectedNode = null;
    rebuildRows();
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void setOnSelectionChange(@NonNull Consumer<TreeNode> onSelectionChange) {
    this.onSelectionChange = onSelectionChange;
  }

  /** The node type whose configuration is shown below the list, or {@code null} while there are none. */
  public TreeNode getSelectedNode() {
    return selectedNode;
  }

  private List<TreeNode> getNodes() {
    return model.getContent().getNodes();
  }

  @FXML
  private void onAdd() {
    Dialogs.showNodeForAdd(Studio.stage, projectItem).ifPresent(node -> {
      node.setId("node-" + shortId());
      getNodes().add(node);
      selectedNode = node;
      rebuildRows();
      notifyChanged();
    });
  }

  /** Re-renders the rows (e.g. after drag &amp; drop was toggled elsewhere) without reporting a selection change. */
  public void refresh() {
    rebuildRows(false);
  }

  private void rebuildRows() {
    rebuildRows(true);
  }

  private void rebuildRows(boolean notifySelection) {
    if (model == null) {
      return;
    }
    rebuilding = true;
    try {
      nodeRows.getChildren().clear();
      selectionGroup.getToggles().clear();

      List<TreeNode> nodes = getNodes();
      boolean empty = nodes.isEmpty();
      nodeHeaders.setVisible(!empty);
      nodeHeaders.setManaged(!empty);
      nodesEmptyLabel.setVisible(empty);
      nodesEmptyLabel.setManaged(empty);

      // Keep the selection across a rebuild; fall back to the first node when it is gone (or nothing was selected yet).
      if (selectedNode == null || !nodes.contains(selectedNode)) {
        selectedNode = empty ? null : nodes.get(0);
      }

      for (int index = 0; index < nodes.size(); index++) {
        nodeRows.getChildren().add(createRow(nodes.get(index), index, nodes.size()));
      }
    }
    finally {
      rebuilding = false;
    }
    if (notifySelection) {
      onSelectionChange.accept(selectedNode);
    }
  }

  private void select(TreeNode node) {
    if (rebuilding || node == selectedNode) {
      return;
    }
    selectedNode = node;
    for (Node row : nodeRows.getChildren()) {
      boolean selected = row.getUserData() == node;
      row.getStyleClass().remove(SELECTED_ROW_STYLE);
      if (selected) {
        row.getStyleClass().add(SELECTED_ROW_STYLE);
        ((RadioButton) ((HBox) row).getChildren().get(0)).setSelected(true);
      }
    }
    onSelectionChange.accept(node);
  }

  private HBox createRow(TreeNode node, int index, int rowCount) {
    RadioButton selectRadio = new RadioButton();
    selectRadio.setId("treeNodeSelect-" + index);
    selectRadio.setToggleGroup(selectionGroup);
    selectRadio.setSelected(node == selectedNode);
    lockWidth(selectRadio, RADIO_COLUMN_WIDTH);
    selectRadio.selectedProperty().addListener((observable, oldValue, selected) -> {
      if (selected) {
        select(node);
      }
    });

    FontIcon dragHandle = RowFactory.createDragHandle();

    String documentModel = node.getDocumentModelRef() != null ? node.getDocumentModelRef() : StudioBundle.get("no_document_model_selected");
    Label documentModelLabel = createRowLabel(documentModel, "treeNodeDocumentModel-" + index, node);
    documentModelLabel.getStyleClass().add("path-text");
    documentModelLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(documentModelLabel, Priority.ALWAYS);

    Label dragDropLabel = createRowLabel(StudioBundle.get(isDndEnabled(node) ? "yes" : "no"), "treeNodeDragDrop-" + index, node);
    lockWidth(dragDropLabel, 130.0);

    // Fixed widths here and in tree-node-types-panel.fxml's header must stay in sync so labels sit above their values.
    HBox actionsBox = createActionsBox(node, index, rowCount);
    lockWidth(actionsBox, 120.0);
    HBox row = new HBox(10.0, selectRadio, dragHandle, documentModelLabel, dragDropLabel, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    row.setUserData(node);
    if (node == selectedNode) {
      row.getStyleClass().add(SELECTED_ROW_STYLE);
    }
    // A click anywhere on the row (including its buttons) selects it; a drag on the handle never produces a click.
    row.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> select(node));
    RowFactory.setupRowDragAndDrop(row, dragHandle, NODE_INDEX, index, this::moveNode);
    return row;
  }

  private static boolean isDndEnabled(TreeNode node) {
    return node.getConfiguration() != null && Boolean.TRUE.equals(node.getConfiguration().get("dnd"));
  }

  private static void lockWidth(Region region, double width) {
    region.setMinWidth(width);
    region.setPrefWidth(width);
    region.setMaxWidth(width);
  }

  // A single click only selects the row (see createRow); a double click opens the edit dialog, like the pencil button.
  private Label createRowLabel(String text, String id, TreeNode node) {
    Label label = new Label(text);
    label.setId(id);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        openEditDialog(node);
      }
    });
    return label;
  }

  private void openEditDialog(TreeNode node) {
    Dialogs.showNodeForEdit(Studio.stage, projectItem, node).ifPresent(edited -> {
      node.setDocumentModelRef(edited.getDocumentModelRef());
      rebuildRows();
      notifyChanged();
    });
  }

  private void moveNode(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getNodes(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      notifyChanged();
    }
  }

  private HBox createActionsBox(TreeNode node, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, StudioBundle.get("edit_node_type_title"), () -> openEditDialog(node));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_node_type"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        removeNode(node);
        rebuildRows();
        notifyChanged();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  /** Removes {@code node}; a Root that pointed at one of its child relationship configurations is cleared too, as in SME. */
  private void removeNode(TreeNode node) {
    getNodes().remove(node);
    removeFromInitialExpansion(node);
    if (model.getContent().getConfiguration() != null
        && TreeRootPanelController.childRelationshipConfigurationIds(node).contains(model.getContent().getConfiguration().getRootRef())) {
      model.getContent().getConfiguration().setRootRef(null);
    }
  }

  /** A deleted node type no longer takes part in the initial expansion; no node types left means all, i.e. an absent key. */
  private void removeFromInitialExpansion(TreeNode node) {
    if (model.getContent().getConfiguration() == null || model.getContent().getConfiguration().getExpansionStrategy() == null) {
      return;
    }
    InitialExpansion initialExpansion = model.getContent().getConfiguration().getExpansionStrategy().getInitialExpansion();
    if (initialExpansion != null && initialExpansion.getAffectedNodeRefs() != null && node.getId() != null) {
      initialExpansion.getAffectedNodeRefs().remove(node.getId());
      if (initialExpansion.getAffectedNodeRefs().isEmpty()) {
        initialExpansion.setAffectedNodeRefs(null);
      }
    }
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getNodes(), fromIndex, toIndex);
    rebuildRows();
    notifyChanged();
  }

  /** One header reference per distinct node Document Model, purpose "document-model-for-tree". */
  private void syncModelReferences() {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(reference -> ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_TREE.equals(reference.getPurpose()));
    List<String> seen = new ArrayList<>();
    int index = 1;
    for (TreeNode node : getNodes()) {
      String documentModel = node.getDocumentModelRef();
      if (documentModel == null || documentModel.isBlank() || seen.contains(documentModel)) {
        continue;
      }
      seen.add(documentModel);
      ModelReference reference = new ModelReference();
      reference.setPurpose(ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_TREE);
      reference.setModelType(ModelType.DOCUMENT);
      reference.setAlias("DM" + index++);
      reference.setReference(documentModel);
      references.add(reference);
    }
  }

  private void notifyChanged() {
    syncModelReferences();
    commitHeaderChange();
    onChange.run();
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }
}
