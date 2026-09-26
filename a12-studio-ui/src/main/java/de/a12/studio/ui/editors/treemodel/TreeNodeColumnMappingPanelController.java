package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.editors.treemodel.dialogs.ColumnMappingEditor;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Shows the selected node type's column mapping as one reorderable {@code module-row} per tree column: the column's
 * Name, the Document Model field of the node type it shows, and the column's Width and Pin Direction, all as labels. A
 * click on a label or the pencil button opens {@link Dialogs#showColumnForEdit(javafx.stage.Stage, TreeColumn, List,
 * String)}, which edits the column's attributes and the node's field for it; the arrows and the drag handle reorder the
 * tree's columns, the trash button removes the column. The columns themselves belong to the tree (so edits show in
 * {@link TreeColumnsPanelController} too, see {@link #setOnColumnsChange}); only the field is per node type and is
 * written to the node's own mapping list, in place. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern. Call {@link #setNode} again after
 * the node's Document Model or the tree's columns changed.
 */
public class TreeNodeColumnMappingPanelController extends AbstractPropertyEditor implements Initializable {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into the tree's columns.
  private static final DataFormat COLUMN_INDEX = new DataFormat("application/x-a12-tree-node-column-index");

  @FXML
  private HBox mappingHeaders;

  @FXML
  private VBox columnMappingRows;

  @FXML
  private Label noColumnsLabel;

  private TreeModel model;
  private ProjectItem projectItem;
  private TreeNode node;

  // Resolves the node's mapped field ids to their paths; rebuilt with the rows, null if the node's Document Model is unknown.
  private ElementIndex elementIndex;

  // Notified after the tree's columns were edited, moved or removed here, so the Columns panel can follow.
  private Runnable onColumnsChange = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
  }

  public void setModel(@NonNull TreeModel model, @NonNull ProjectItem projectItem) {
    this.model = model;
    this.projectItem = projectItem;
  }

  public void setOnColumnsChange(@NonNull Runnable onColumnsChange) {
    this.onColumnsChange = onColumnsChange;
  }

  /** Binds the panel to {@code node}, or to nothing ({@code null}). */
  public void setNode(TreeNode node) {
    this.node = model != null ? node : null;
    rebuildRows();
  }

  private List<TreeColumn> getColumns() {
    return model.getContent().getColumns();
  }

  private void rebuildRows() {
    columnMappingRows.getChildren().clear();
    elementIndex = node != null && projectItem != null ? ColumnMappingEditor.elementIndexFor(projectItem, node.getDocumentModelRef()) : null;

    List<TreeColumn> columns = node != null ? getColumns() : List.of();
    boolean empty = columns.isEmpty();
    mappingHeaders.setVisible(!empty);
    mappingHeaders.setManaged(!empty);
    noColumnsLabel.setVisible(empty);
    noColumnsLabel.setManaged(empty);

    for (int index = 0; index < columns.size(); index++) {
      columnMappingRows.getChildren().add(createRow(columns.get(index), index, columns.size()));
    }
  }

  private HBox createRow(TreeColumn column, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label nameLabel = createRowLabel(column.getName(), "columnMappingName-" + index, column);
    nameLabel.getStyleClass().add("path-text");
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);

    String field = ColumnMappingEditor.mappedElementRef(node.getColumns(), column.getId());
    Label fieldLabel = createRowLabel(field != null ? ColumnMappingEditor.displayPath(elementIndex, field) : "", "columnMappingField-" + index, column);
    fieldLabel.getStyleClass().add("path-text");
    lockWidth(fieldLabel, 220.0);
    Label widthLabel = createRowLabel(column.getWidth() != null ? String.valueOf(column.getWidth()) : "", "columnMappingWidth-" + index, column);
    lockWidth(widthLabel, 70.0);
    Label pinDirectionLabel = createRowLabel(column.getPinDirection() != null ? column.getPinDirection() : "", "columnMappingPinDirection-" + index, column);
    lockWidth(pinDirectionLabel, 120.0);

    // Fixed widths here and in tree-node-column-mapping-panel.fxml's header must stay in sync so labels sit above their values.
    HBox actionsBox = createActionsBox(column, index, rowCount);
    lockWidth(actionsBox, 120.0);
    HBox row = new HBox(10.0, dragHandle, nameLabel, fieldLabel, widthLabel, pinDirectionLabel, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, COLUMN_INDEX, index, this::moveColumn);
    return row;
  }

  private static void lockWidth(Region region, double width) {
    region.setMinWidth(width);
    region.setPrefWidth(width);
    region.setMaxWidth(width);
  }

  private Label createRowLabel(String text, String id, TreeColumn column) {
    Label label = new Label(text);
    label.setId(id);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(column);
      }
    });
    return label;
  }

  private HBox createActionsBox(TreeColumn column, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, StudioBundle.get("edit_column_title"), () -> openEditDialog(column));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_column"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        removeColumn(column);
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void openEditDialog(TreeColumn column) {
    // Without a project item (an editor not opened from the project tree) there are no fields to offer.
    List<String> fieldOptions = projectItem != null ? ColumnMappingEditor.fieldOptionsFor(projectItem, node.getDocumentModelRef()) : List.of();
    String field = ColumnMappingEditor.mappedElementRef(node.getColumns(), column.getId());
    Dialogs.showColumnForEdit(Studio.stage, column, fieldOptions, elementIndex, field).ifPresent(edited -> applyEdit(column, edited.column(), edited.field()));
  }

  /** Copies the edited attributes onto the tree's {@code column} and stores the chosen field in the node's mapping. */
  void applyEdit(TreeColumn column, TreeColumn edited, String field) {
    column.setName(edited.getName());
    column.setWidth(edited.getWidth());
    column.setFixedWidth(edited.getFixedWidth());
    column.setPinDirection(edited.getPinDirection());
    setMapping(column.getId(), field);
    columnsChanged();
  }

  /** Removes the tree's {@code column}, and with it every node type's mapping of it, and a hierarchical-column choice of it. */
  void removeColumn(TreeColumn column) {
    getColumns().remove(column);
    for (TreeNode treeNode : model.getContent().getNodes()) {
      treeNode.getColumns().removeIf(mapping -> column.getId() != null && column.getId().equals(mapping.getColumnRef()));
    }
    if (model.getContent().getConfiguration() != null && column.getId() != null
        && column.getId().equals(model.getContent().getConfiguration().getHierarchicalColumnRef())) {
      model.getContent().getConfiguration().setHierarchicalColumnRef(null);
    }
    columnsChanged();
  }

  private void moveColumn(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getColumns(), fromIndex, insertBeforeIndex)) {
      columnsChanged();
    }
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getColumns(), fromIndex, toIndex);
    columnsChanged();
  }

  private void columnsChanged() {
    rebuildRows();
    commitHeaderChange();
    onColumnsChange.run();
  }

  /** Sets the node's field for {@code columnId}; clearing it drops the mapping unless it carries a display-mode override. */
  private void setMapping(String columnId, String elementRef) {
    if (columnId == null) {
      return;
    }
    if (elementRef == null) {
      node.getColumns().removeIf(mapping -> columnId.equals(mapping.getColumnRef()) && mapping.getConfiguration() == null);
      if (node.getColumns().stream().noneMatch(mapping -> columnId.equals(mapping.getColumnRef()))) {
        return;
      }
    }
    ColumnMappingEditor.setMappedElementRef(node.getColumns(), columnId, elementRef);
  }
}
