package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.overviewmodel.OverviewColumnOptions;
import de.a12.studio.ui.editors.overviewmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
 * Edits an {@link OverviewModel}'s {@code content.columns}: one draggable, reorderable row per {@link
 * Column}, summarizing its Field (the referenced Document Model element, resolved via {@link
 * OverviewColumnOptions}), Sortable, Width and Pin Direction. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by e.g.
 * {@link OverviewFeaturesPanelController}. Clicking a row opens {@link Dialogs#showColumnDialog}, the full column
 * editor. Also edits
 * {@link OverviewConfiguration} flags displayed alongside the column list: Enable Columns Resize, Show
 * Number Of Entries and Skip Initial Load (moved here from {@link OverviewFeaturesPanelController} since
 * all are about how the resulting table of columns is presented).
 */
public class OverviewColumnsPanelController extends AbstractPropertyEditor implements Initializable {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getColumns().
  private static final DataFormat COLUMN_INDEX = new DataFormat("application/x-a12-overview-column-index");

  @FXML
  private HBox columnHeaders;

  @FXML
  private VBox columnRows;

  @FXML
  private Label columnsEmptyLabel;

  @FXML
  private CheckBox enableColumnsResizeField;

  @FXML
  private CheckBox showRowCountField;

  @FXML
  private CheckBox skipInitialLoadField;

  private OverviewModel model;

  private ElementIndex documentModelIndex;

  private String documentModelId;

  // Set while enableColumnsResizeField/showRowCountField are being repopulated from the model, so those
  // programmatic updates aren't mistaken for user edits and don't trigger a save.
  private boolean updatingFromModel;

  // Notified after every structural change (add/reorder/delete), so the owning editor can keep sibling
  // panels whose choices derive from this list (e.g. the Sorting panel's column picker) in sync.
  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    enableColumnsResizeField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setEnableColumnsResize(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
    });
    showRowCountField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setShowRowCount(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
    });
    skipInitialLoadField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setSkipInitialLoad(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    rebuildRows();

    updatingFromModel = true;
    try {
      OverviewConfiguration configuration = model.getContent().getConfiguration();
      enableColumnsResizeField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getEnableColumnsResize()));
      showRowCountField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getShowRowCount()));
      skipInitialLoadField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getSkipInitialLoad()));
    }
    finally {
      updatingFromModel = false;
    }
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  /** Re-points the "Field" summary of every row at the currently referenced Document Model. */
  public void setDocumentModelIndex(ElementIndex documentModelIndex, String documentModelId) {
    this.documentModelIndex = documentModelIndex;
    this.documentModelId = documentModelId;
    rebuildRows();
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  @FXML
  private void onAdd() {
    Column column = new Column();
    column.setId("column-" + shortId());
    column.setWidth(1.0);
    getColumns().add(column);
    rebuildRows();
    notifyChanged();
  }

  private List<Column> getColumns() {
    return model.getContent().getColumns();
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    columnRows.getChildren().clear();

    List<Column> columns = getColumns();
    boolean empty = columns.isEmpty();
    columnHeaders.setVisible(!empty);
    columnHeaders.setManaged(!empty);
    columnsEmptyLabel.setVisible(empty);
    columnsEmptyLabel.setManaged(empty);

    for (int index = 0; index < columns.size(); index++) {
      columnRows.getChildren().add(createRow(columns.get(index), index, columns.size()));
    }
  }

  private HBox createRow(Column column, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Node fieldCell = createFieldCell(column, index);
    Label sortableLabel = createRowLabel(Boolean.TRUE.equals(column.getSortable()) ? "Yes" : "No", "overviewColumnSortable-" + index, 70.0, column);
    Label widthLabel = createRowLabel(column.getWidth() != null ? String.valueOf(column.getWidth()) : "", "overviewColumnWidth-" + index, 70.0, column);
    Label pinDirectionLabel = createRowLabel(column.getPinDirection() != null ? column.getPinDirection() : "", "overviewColumnPinDirection-" + index, 100.0, column);

    HBox row = new HBox(10.0, dragHandle, fieldCell, sortableLabel, widthLabel, pinDirectionLabel, createActionsBox(column, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, COLUMN_INDEX, index, this::moveColumn);
    return row;
  }

  /** The "Field" cell: the resolved field path, or - for an expression column, per {@link
   * OverviewColumnOptions#isExpressionColumn} - "Expression Column" next to an epsilon icon, mirroring SME's
   * inline expression-column icon in its own Columns repeat table. */
  private Node createFieldCell(Column column, int index) {
    String summary = fieldSummary(column);
    Label label = new Label(summary);
    label.setId("overviewColumnField-" + index);
    label.setTooltip(WidgetFactory.createTooltip(summary));

    HBox cell = new HBox(6.0, label);
    if (OverviewColumnOptions.isExpressionColumn(column)) {
      FontIcon icon = new FontIcon(Icons.ELEMENT_EXPRESSION);
      icon.setIconSize(16);
      cell.getChildren().add(0, icon);
    }
    cell.setAlignment(Pos.CENTER_LEFT);
    cell.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(cell, Priority.ALWAYS);
    cell.setCursor(Cursor.HAND);
    cell.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(column);
      }
    });
    return cell;
  }

  private Label createRowLabel(String text, String id, double width, Column column) {
    Label label = new Label(text);
    label.setId(id);
    label.setPrefWidth(width);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(column);
      }
    });
    return label;
  }

  private String fieldSummary(Column column) {
    return OverviewColumnOptions.describe(column, documentModelIndex);
  }

  private void openEditDialog(Column column) {
    Dialogs.showColumnDialog(Studio.stage, documentModelIndex, documentModelId, column);
    rebuildRows();
  }

  private void moveColumn(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getColumns(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      notifyChanged();
    }
  }

  private HBox createActionsBox(Column column, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(column));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this column?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getColumns().remove(column);
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

  private void notifyChanged() {
    commitHeaderChange();
    onChange.run();
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }
}
