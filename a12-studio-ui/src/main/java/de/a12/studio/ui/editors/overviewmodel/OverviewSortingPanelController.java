package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnRef;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.overview.OverviewInitialSortingReferenceValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.overviewmodel.OverviewColumnOptions;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Edits an {@link OverviewModel}'s {@code content.configuration.initialSorting}: one draggable, reorderable
 * row per {@link ColumnRef}, each picking one of the columns defined in the Columns panel via an inline combo
 * box (order determines sort priority - only the first entry's sort direction icon is shown at runtime, per
 * SME). Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the
 * model-header pattern used by e.g. {@link OverviewColumnsPanelController}. {@link
 * OverviewInitialSortingReferenceValidator} flags entries left pointing at a column that was since deleted;
 * that error is queried directly (like {@link de.a12.studio.ui.editors.applicationmodel.ModulesPanelController#refreshNameUniquenessError}) since there
 * is no single bound Element for the base class's own validation plumbing to key off of.
 */
public class OverviewSortingPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getSorting().
  private static final DataFormat SORTING_INDEX = new DataFormat("application/x-a12-overview-sorting-index");

  @FXML
  private VBox sortingRows;

  @FXML
  private Label sortingEmptyLabel;

  private OverviewModel model;

  private ElementIndex documentModelIndex;

  // Set while a row's combo box is being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    rebuildRows();
  }

  /** Re-points the column picker's "Field" summary at the currently referenced Document Model. */
  public void setDocumentModelIndex(ElementIndex documentModelIndex) {
    this.documentModelIndex = documentModelIndex;
    rebuildRows();
  }

  /** Called by the owning editor whenever the Columns panel changes, since this panel's picker choices and
   * its own dangling-reference validation both derive from the current column list. */
  public void refresh() {
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    ensureConfiguration().getInitialSorting().add(new ColumnRef());
    rebuildRows();
    commitHeaderChange();
  }

  private List<ColumnRef> getSorting() {
    OverviewConfiguration configuration = model.getContent().getConfiguration();
    return configuration != null ? configuration.getInitialSorting() : List.of();
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private List<Column> getColumns() {
    return model.getContent().getColumns();
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    refreshValidationError();
    sortingRows.getChildren().clear();

    List<ColumnRef> sorting = getSorting();
    boolean empty = sorting.isEmpty();
    sortingEmptyLabel.setVisible(empty);
    sortingEmptyLabel.setManaged(empty);

    for (int index = 0; index < sorting.size(); index++) {
      sortingRows.getChildren().add(createRow(sorting.get(index), index, sorting.size()));
    }
  }

  private void refreshValidationError() {
    if (getSorting().isEmpty()) {
      hideError();
      return;
    }
    List<ModelValidationError> errors =
        Studio.getValidationService().validateElement(model, OverviewInitialSortingReferenceValidator.ELEMENT_ID);
    if (errors.isEmpty()) {
      hideError();
    } else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private HBox createRow(ColumnRef columnRef, int index, int rowCount) {
    List<Column> columns = getColumns();

    FontIcon dragHandle = RowFactory.createDragHandle();

    ComboBox<String> columnField = new ComboBox<>();
    columnField.setId("overviewSortingColumn-" + index);
    columnField.setPromptText(StudioBundle.get("select_a_column"));
    columnField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(columnField, Priority.ALWAYS);
    columnField.getItems().setAll(OverviewColumnOptions.columnIds(columns));
    OverviewColumnOptions.applyColumnConverter(columnField, columns, documentModelIndex);

    updatingFromModel = true;
    try {
      columnField.setValue(columnRef.getIdref());
    }
    finally {
      updatingFromModel = false;
    }
    updateColumnValidationState(columnField, columnRef);
    columnField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      columnRef.setIdref(newValue);
      commitHeaderChange();
      refreshValidationError();
      updateColumnValidationState(columnField, columnRef);
    });

    HBox row = new HBox(10.0, dragHandle, columnField, createActionsBox(columnRef, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, SORTING_INDEX, index, this::moveSorting);
    return row;
  }

  /** Flags {@code columnField} with a red border and an explanatory tooltip when the {@link Column} it
   * currently points at is itself pointing at a field path that can't be resolved against {@link
   * #documentModelIndex} - same "unresolved" semantics as {@link
   * OverviewColumnOptions#isUnresolvedElementRef}, whose {@link OverviewColumnsPanelController} counterpart
   * flags it on a plain summary {@code Label} instead, since here the column is picked via a combo box rather
   * than rendered as text. A dangling {@code idref} (no such column at all) is left to {@link
   * #refreshValidationError}'s panel-level error, since there's no resolved {@link Column} to check a field
   * path on. */
  private void updateColumnValidationState(ComboBox<String> columnField, ColumnRef columnRef) {
    Column column = getColumns().stream()
        .filter(candidate -> candidate.getId() != null && candidate.getId().equals(columnRef.getIdref()))
        .findFirst()
        .orElse(null);
    if (OverviewColumnOptions.isUnresolvedElementRef(column, documentModelIndex)) {
      if (!columnField.getStyleClass().contains("validation-error")) {
        columnField.getStyleClass().add("validation-error");
      }
      columnField.setTooltip(WidgetFactory.createTooltip(
          StudioBundle.get("path_could_not_be_resolved", column.getElementRef())));
    }
    else {
      columnField.getStyleClass().remove("validation-error");
      columnField.setTooltip(null);
    }
  }

  private void moveSorting(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getSorting(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitHeaderChange();
    }
  }

  private HBox createActionsBox(ColumnRef columnRef, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_sorting_entry"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getSorting().remove(columnRef);
        rebuildRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getSorting(), fromIndex, toIndex);
    rebuildRows();
    commitHeaderChange();
  }
}
