package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
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
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
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

/**
 * Edits a {@link TreeModel}'s {@code content.nodes}: one draggable, reorderable row per node type,
 * summarizing its Document Model and whether drag &amp; drop is allowed. Clicking a row opens {@link
 * Dialogs#showNodeForEdit} (Document Model, drag &amp; drop, per-column field mapping); the Add button below
 * the rows opens {@link Dialogs#showNodeForAdd}. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by e.g. {@link
 * TreeColumnsPanelController}. The header's {@code modelReferences} track the node Document Models (purpose
 * "document-model-for-tree"), synced by {@link #syncModelReferences()}. Editing a row only touches what the
 * dialog edits (Document Model, drag &amp; drop, column mapping); everything else on the node - actions,
 * child relationship configurations, icon, ... - is left as it was.
 */
public class TreeNodeTypesPanelController extends AbstractPropertyEditor implements Initializable {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getNodes().
  private static final DataFormat NODE_INDEX = new DataFormat("application/x-a12-tree-node-index");

  @FXML
  private HBox nodeHeaders;

  @FXML
  private VBox nodeRows;

  @FXML
  private Label nodesEmptyLabel;

  private TreeModel model;
  private ProjectItem projectItem;

  // Notified after every structural change (add/edit/reorder/delete), so the owning editor can keep the Root
  // panel's choices - derived from the nodes' child relationship configurations - in sync.
  private Runnable onChange = () -> {
  };

  public void setModel(@NonNull TreeModel model, @NonNull ProjectItem projectItem) {
    this.model = model;
    this.projectItem = projectItem;
    rebuildRows();
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  private List<TreeNode> getNodes() {
    return model.getContent().getNodes();
  }

  @FXML
  private void onAdd() {
    Dialogs.showNodeForAdd(Studio.stage, model, projectItem).ifPresent(node -> {
      node.setId("node-" + shortId());
      getNodes().add(node);
      rebuildRows();
      notifyChanged();
    });
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    nodeRows.getChildren().clear();

    List<TreeNode> nodes = getNodes();
    boolean empty = nodes.isEmpty();
    nodeHeaders.setVisible(!empty);
    nodeHeaders.setManaged(!empty);
    nodesEmptyLabel.setVisible(empty);
    nodesEmptyLabel.setManaged(empty);

    for (int index = 0; index < nodes.size(); index++) {
      nodeRows.getChildren().add(createRow(nodes.get(index), index, nodes.size()));
    }
  }

  private HBox createRow(TreeNode node, int index, int rowCount) {
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
    HBox row = new HBox(10.0, dragHandle, documentModelLabel, dragDropLabel, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
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

  private Label createRowLabel(String text, String id, TreeNode node) {
    Label label = new Label(text);
    label.setId(id);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(node);
      }
    });
    return label;
  }

  private void openEditDialog(TreeNode node) {
    Dialogs.showNodeForEdit(Studio.stage, model, projectItem, node).ifPresent(edited -> {
      node.setDocumentModelRef(edited.getDocumentModelRef());
      node.setConfiguration(edited.getConfiguration());
      node.setColumns(edited.getColumns());
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
    if (model.getContent().getConfiguration() != null
        && TreeRootPanelController.childRelationshipConfigurationIds(node).contains(model.getContent().getConfiguration().getRootRef())) {
      model.getContent().getConfiguration().setRootRef(null);
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
