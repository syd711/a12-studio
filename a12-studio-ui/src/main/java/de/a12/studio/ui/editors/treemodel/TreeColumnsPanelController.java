package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeConfiguration;
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
import javafx.scene.control.ComboBox;
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
import java.util.UUID;

/**
 * Edits a {@link TreeModel}'s {@code content.columns}: one draggable, reorderable row per {@link TreeColumn},
 * summarizing its Name, Width, Fixed Width and Pin Direction. Clicking a row opens {@link
 * Dialogs#showColumnForEdit}; the Add button opens {@link Dialogs#showColumnForAdd}. Also edits {@link
 * TreeConfiguration#getHierarchicalColumnRef()} via a combo box populated from the current column names, kept
 * inline here (rather than its own panel) since it is directly derived from this panel's own column list -
 * mirrors how {@link de.a12.studio.ui.editors.overviewmodel.OverviewColumnsPanelController} bundles its
 * directly-related config flags next to the Add button. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by e.g. {@link
 * de.a12.studio.ui.editors.overviewmodel.OverviewColumnsPanelController}.
 */
public class TreeColumnsPanelController extends AbstractPropertyEditor implements Initializable {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getColumns().
  private static final DataFormat COLUMN_INDEX = new DataFormat("application/x-a12-tree-column-index");

  @FXML
  private HBox columnHeaders;

  @FXML
  private VBox columnRows;

  @FXML
  private Label columnsEmptyLabel;

  @FXML
  private ComboBox<String> hierarchicalColumnField;

  private TreeModel model;

  // Set while hierarchicalColumnField is being repopulated from the model, so that repopulation isn't mistaken
  // for a user edit and doesn't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    hierarchicalColumnField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setHierarchicalColumnRef(columnIdForName(newValue));
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull TreeModel model) {
    this.model = model;
    rebuildRows();
    refreshHierarchicalColumnField();
  }

  private List<TreeColumn> getColumns() {
    return model.getContent().getColumns();
  }

  private TreeConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new TreeConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  @FXML
  private void onAdd() {
    Dialogs.showColumnForAdd(Studio.stage).ifPresent(column -> {
      column.setId("column-" + shortId());
      getColumns().add(column);
      rebuildRows();
      notifyChanged();
    });
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    columnRows.getChildren().clear();

    List<TreeColumn> columns = getColumns();
    boolean empty = columns.isEmpty();
    columnHeaders.setVisible(!empty);
    columnHeaders.setManaged(!empty);
    columnsEmptyLabel.setVisible(empty);
    columnsEmptyLabel.setManaged(empty);

    for (int index = 0; index < columns.size(); index++) {
      columnRows.getChildren().add(createRow(columns.get(index), index, columns.size()));
    }
  }

  private HBox createRow(TreeColumn column, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label nameLabel = createRowLabel(column.getName(), "treeColumnName-" + index, column);
    nameLabel.getStyleClass().add("path-text");
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);

    Label widthLabel = createRowLabel(column.getWidth() != null ? String.valueOf(column.getWidth()) : "", "treeColumnWidth-" + index, column);
    lockWidth(widthLabel, 70.0);
    Label fixedWidthLabel = createRowLabel(StudioBundle.get(Boolean.TRUE.equals(column.getFixedWidth()) ? "yes" : "no"), "treeColumnFixedWidth-" + index, column);
    lockWidth(fixedWidthLabel, 100.0);
    Label pinDirectionLabel = createRowLabel(column.getPinDirection() != null ? column.getPinDirection() : "", "treeColumnPinDirection-" + index, column);
    lockWidth(pinDirectionLabel, 200.0);

    // Fixed widths here and in tree-columns-panel.fxml's header must stay in sync so labels sit above their values.
    HBox actionsBox = createActionsBox(column, index, rowCount);
    lockWidth(actionsBox, 120.0);
    HBox row = new HBox(10.0, dragHandle, nameLabel, widthLabel, fixedWidthLabel, pinDirectionLabel, actionsBox);
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

  private void openEditDialog(TreeColumn column) {
    Dialogs.showColumnForEdit(Studio.stage, column).ifPresent(edited -> {
      column.setName(edited.getName());
      column.setWidth(edited.getWidth());
      column.setFixedWidth(edited.getFixedWidth());
      column.setPinDirection(edited.getPinDirection());
      rebuildRows();
      notifyChanged();
    });
  }

  private void moveColumn(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getColumns(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      notifyChanged();
    }
  }

  private HBox createActionsBox(TreeColumn column, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(column));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_column"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getColumns().remove(column);
        for (TreeNode node : model.getContent().getNodes()) {
          node.getColumns().removeIf(mapping -> column.getId() != null && column.getId().equals(mapping.getColumnRef()));
        }
        rebuildRows();
        notifyChanged();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getColumns(), fromIndex, toIndex);
    rebuildRows();
    notifyChanged();
  }

  private void refreshHierarchicalColumnField() {
    updatingFromModel = true;
    try {
      List<String> columnNames = getColumns().stream().map(TreeColumn::getName).toList();
      hierarchicalColumnField.getItems().setAll(columnNames);
      String hierarchicalRef = model.getContent().getConfiguration() != null
          ? model.getContent().getConfiguration().getHierarchicalColumnRef()
          : null;
      hierarchicalColumnField.setValue(columnNameForId(hierarchicalRef));
    }
    finally {
      updatingFromModel = false;
    }
  }

  private String columnNameForId(String columnId) {
    if (columnId == null) {
      return null;
    }
    return getColumns().stream()
        .filter(column -> columnId.equals(column.getId()))
        .map(TreeColumn::getName)
        .findFirst()
        .orElse(null);
  }

  private String columnIdForName(String name) {
    if (name == null) {
      return null;
    }
    return getColumns().stream()
        .filter(column -> name.equals(column.getName()))
        .map(TreeColumn::getId)
        .findFirst()
        .orElse(null);
  }

  private void notifyChanged() {
    refreshHierarchicalColumnField();
    commitHeaderChange();
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }
}
