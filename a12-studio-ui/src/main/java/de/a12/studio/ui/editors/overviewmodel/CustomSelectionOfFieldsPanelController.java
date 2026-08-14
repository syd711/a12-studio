package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.FieldRef;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
 * Edits an {@link OverviewModel}'s {@code content.configuration.filterConfiguration.fields}: one draggable,
 * reorderable row per {@link FieldRef}, matching the reference metamodel's "Custom Selection Of Fields" group
 * (whose validation message is literally "Please define list Custom selection of fields.") - the {@code fields}
 * group sits right before {@code sectionData} there, so this panel is placed the same way relative to {@link
 * OverviewSectionDataPanelController} in {@code overview-model-editor.fxml}. Each row picks a Document Model
 * field via an inline combo box (see {@link OverviewElementOptions}), mirroring {@link
 * OverviewSortingPanelController}'s column picker. The reference metamodel's row also has a "Subtype" field
 * (for referencing a field of a heterogeneous relationship's sub-model into the Filter Selector) - that column
 * is intentionally left empty for now, matching {@link OverviewSectionDataPanelController}'s dialog being
 * empty for now.
 */
public class CustomSelectionOfFieldsPanelController extends AbstractPropertyEditor implements Initializable {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getFields().
  private static final DataFormat FIELD_INDEX = new DataFormat("application/x-a12-overview-custom-field-index");

  // Matches the fixed-width spacer reserved after the "Field" header in custom-selection-of-fields-panel.fxml,
  // so rows' move/delete buttons line up under it instead of stealing space from the Subtype/Field columns.
  private static final double ACTIONS_BOX_WIDTH = 70.0;

  @FXML
  private Label subtypeInfoIcon;

  @FXML
  private HBox fieldColumnHeaders;

  @FXML
  private VBox fieldRows;

  @FXML
  private Label fieldsEmptyLabel;

  private OverviewModel model;

  private ElementIndex documentModelIndex;

  // Set while a row's combo box is being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(subtypeInfoIcon, StudioBundle.get("for_heterogeneous_data_please_specify_a_subtype_to_add_field_"));
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    rebuildRows();
  }

  /** Only relevant for {@link FilterConfiguration#FILTER_MODE_CUSTOM_LIST} - hidden for every other filter
   * mode, see {@link OverviewModelEditorController}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /** Re-points every row's field picker at the currently referenced Document Model. */
  public void setDocumentModelIndex(ElementIndex documentModelIndex) {
    this.documentModelIndex = documentModelIndex;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    getFields().add(new FieldRef());
    rebuildRows();
    commitHeaderChange();
  }

  private List<FieldRef> getFields() {
    return ensureFilterConfiguration().getFields();
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    fieldRows.getChildren().clear();

    List<FieldRef> fields = currentFields();
    boolean empty = fields.isEmpty();
    fieldsEmptyLabel.setVisible(empty);
    fieldsEmptyLabel.setManaged(empty);
    fieldColumnHeaders.setVisible(!empty);
    fieldColumnHeaders.setManaged(!empty);

    for (int index = 0; index < fields.size(); index++) {
      fieldRows.getChildren().add(createRow(fields.get(index), index, fields.size()));
    }
  }

  private List<FieldRef> currentFields() {
    FilterConfiguration filterConfiguration = currentFilterConfiguration();
    return filterConfiguration != null ? filterConfiguration.getFields() : List.of();
  }

  private HBox createRow(FieldRef fieldRef, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Region subtypeCell = new Region();
    subtypeCell.setId("customFieldSubtype-" + index);
    HBox.setHgrow(subtypeCell, Priority.ALWAYS);
    subtypeCell.setMaxWidth(Double.MAX_VALUE);

    ComboBox<String> fieldField = new ComboBox<>();
    fieldField.setId("customFieldField-" + index);
    fieldField.setPromptText(StudioBundle.get("select_a_field"));
    fieldField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(fieldField, Priority.ALWAYS);
    fieldField.getItems().setAll(OverviewElementOptions.elementIds(documentModelIndex));
    OverviewElementOptions.applyElementRefConverter(fieldField, documentModelIndex);

    updatingFromModel = true;
    try {
      fieldField.setValue(fieldRef.getFieldId());
    }
    finally {
      updatingFromModel = false;
    }
    updateFieldValidationState(fieldField, fieldRef);
    fieldField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      fieldRef.setFieldId(newValue);
      commitHeaderChange();
      updateFieldValidationState(fieldField, fieldRef);
    });

    GridPane contentGrid = new GridPane();
    contentGrid.setHgap(10.0);
    contentGrid.setMaxWidth(Double.MAX_VALUE);
    ColumnConstraints subtypeColumn = new ColumnConstraints();
    subtypeColumn.setPercentWidth(33.33);
    ColumnConstraints fieldColumn = new ColumnConstraints();
    fieldColumn.setPercentWidth(66.67);
    contentGrid.getColumnConstraints().addAll(subtypeColumn, fieldColumn);
    contentGrid.add(subtypeCell, 0, 0);
    contentGrid.add(fieldField, 1, 0);
    HBox.setHgrow(contentGrid, Priority.ALWAYS);

    HBox actionsBox = createActionsBox(fieldRef, index, rowCount);
    actionsBox.setPrefWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMinWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMaxWidth(ACTIONS_BOX_WIDTH);
    HBox.setHgrow(actionsBox, Priority.NEVER);

    HBox row = new HBox(10.0, dragHandle, contentGrid, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, FIELD_INDEX, index, this::moveField);
    return row;
  }

  /** Flags {@code fieldField} with a red border and an explanatory tooltip when {@code fieldRef}'s field id is
   * set but doesn't resolve against {@link #documentModelIndex} - a dangling reference. Same "unresolved"
   * semantics as {@link OverviewColumnOptions#isUnresolvedElementRef}, whose {@link
   * OverviewColumnsPanelController} counterpart flags it on a plain summary {@code Label} instead, since here
   * the field is picked via a combo box rather than rendered as text. */
  private void updateFieldValidationState(ComboBox<String> fieldField, FieldRef fieldRef) {
    String fieldId = fieldRef.getFieldId();
    boolean unresolved = fieldId != null && !fieldId.isBlank() && !OverviewElementOptions.isResolved(documentModelIndex, fieldId);
    if (unresolved) {
      if (!fieldField.getStyleClass().contains("validation-error")) {
        fieldField.getStyleClass().add("validation-error");
      }
      fieldField.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("path_could_not_be_resolved", OverviewElementOptions.displayPath(documentModelIndex, fieldId))));
    }
    else {
      fieldField.getStyleClass().remove("validation-error");
      fieldField.setTooltip(null);
    }
  }

  private void moveField(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getFields(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitHeaderChange();
    }
  }

  private HBox createActionsBox(FieldRef fieldRef, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_field"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getFields().remove(fieldRef);
        rebuildRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getFields(), fromIndex, toIndex);
    rebuildRows();
    commitHeaderChange();
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private FilterConfiguration ensureFilterConfiguration() {
    OverviewConfiguration configuration = ensureConfiguration();
    if (configuration.getFilterConfiguration() == null) {
      configuration.setFilterConfiguration(new FilterConfiguration());
    }
    return configuration.getFilterConfiguration();
  }

  private FilterConfiguration currentFilterConfiguration() {
    OverviewConfiguration configuration = model.getContent().getConfiguration();
    return configuration != null ? configuration.getFilterConfiguration() : null;
  }
}
