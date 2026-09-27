package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.EnumeratedStringFilter;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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

/**
 * Edits {@link OverviewModel}'s {@code content.configuration.filterConfiguration.enumeratedStringFilter}: whether
 * string filter fields are shown as a paginated multi-select list instead of a plain text input, which String
 * fields feed that list ({@link EnumeratedStringFilter#getFields()}), and (when enabled) the page size of that
 * list. Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header
 * pattern used by e.g. {@link PagingBehaviourPanelController}. The presence of the {@code enumeratedStringFilter}
 * object itself is the enabled flag, matching the reference implementation's export behavior (an absent object
 * means disabled; a present one is exported without its own {@code enabled} key). The field list is a row-per-
 * {@link FieldRef} editor restricted to String fields (see {@link OverviewElementOptions#stringElementIds}),
 * mirroring {@link CustomSelectionOfFieldsPanelController} minus its Subtype column.
 */
public class FilterStringFieldsMultiSelectPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int DEFAULT_PAGING_SIZE = 10;

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getFields().
  private static final DataFormat FIELD_INDEX = new DataFormat("application/x-a12-overview-enumerated-string-filter-field-index");

  @FXML
  private CheckBox enabledField;
  @FXML
  private VBox pagingSizeBox;
  @FXML
  private Spinner<Integer> pagingSizeField;
  @FXML
  private VBox fieldsBox;
  @FXML
  private VBox fieldRows;
  @FXML
  private Label fieldsEmptyLabel;
  @FXML
  private Button addButton;

  private OverviewModel model;

  private ElementIndex documentModelIndex;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    pagingSizeField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, DEFAULT_PAGING_SIZE));
    WidgetFactory.restrictToNumericInput(pagingSizeField.getEditor());

    pagingSizeBox.visibleProperty().bind(enabledField.selectedProperty());
    pagingSizeBox.managedProperty().bind(pagingSizeBox.visibleProperty());
    fieldsBox.visibleProperty().bind(enabledField.selectedProperty());
    fieldsBox.managedProperty().bind(fieldsBox.visibleProperty());

    enabledField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (newValue) {
        EnumeratedStringFilter filter = new EnumeratedStringFilter();
        filter.setPagingSize(DEFAULT_PAGING_SIZE);
        ensureFilterConfiguration().setEnumeratedStringFilter(filter);
        updatingFromModel = true;
        try {
          pagingSizeField.getValueFactory().setValue(DEFAULT_PAGING_SIZE);
        }
        finally {
          updatingFromModel = false;
        }
      }
      else {
        ensureFilterConfiguration().setEnumeratedStringFilter(null);
      }
      commitHeaderChange();
      rebuildRows();
    });
    pagingSizeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      EnumeratedStringFilter filter = ensureFilterConfiguration().getEnumeratedStringFilter();
      if (filter != null) {
        filter.setPagingSize(newValue);
        commitHeaderChange();
      }
    });
  }

  /** Irrelevant for {@link FilterConfiguration#FILTER_MODE_CUSTOM_FILTER} - hidden for that filter mode, see
   * {@link OverviewModelEditorController}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /** Re-points the field picker rows at the currently referenced Document Model. */
  public void setDocumentModelIndex(ElementIndex documentModelIndex) {
    this.documentModelIndex = documentModelIndex;
    rebuildRows();
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      EnumeratedStringFilter filter = currentFilter();
      boolean enabled = filter != null;
      enabledField.setSelected(enabled);
      pagingSizeField.getValueFactory().setValue(
          enabled && filter.getPagingSize() != null ? filter.getPagingSize() : DEFAULT_PAGING_SIZE);
    }
    finally {
      updatingFromModel = false;
    }
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    EnumeratedStringFilter filter = ensureFilterConfiguration().getEnumeratedStringFilter();
    if (filter == null) {
      return;
    }
    filter.getFields().add(new FieldRef());
    rebuildRows();
    commitHeaderChange();
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

    for (int index = 0; index < fields.size(); index++) {
      fieldRows.getChildren().add(createRow(fields.get(index), index, fields.size()));
    }
  }

  private List<FieldRef> currentFields() {
    EnumeratedStringFilter filter = currentFilter();
    return filter != null ? filter.getFields() : List.of();
  }

  private HBox createRow(FieldRef fieldRef, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    ComboBox<String> fieldField = new ComboBox<>();
    fieldField.setId("enumeratedStringFilterField-" + index);
    fieldField.setPromptText(StudioBundle.get("select_a_field"));
    fieldField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(fieldField, Priority.ALWAYS);
    fieldField.getItems().setAll(OverviewElementOptions.stringElementIds(documentModelIndex));
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

    HBox actionsBox = createActionsBox(fieldRef, index, rowCount);

    HBox row = new HBox(10.0, dragHandle, fieldField, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, FIELD_INDEX, index, this::moveField);
    return row;
  }

  /** Flags {@code fieldField} with a red border and an explanatory tooltip when {@code fieldRef}'s field id is
   * set but doesn't resolve against {@link #documentModelIndex} - a dangling reference, matching {@link
   * CustomSelectionOfFieldsPanelController#updateFieldValidationState}. */
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
    if (RowFactory.reorder(currentFields(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitHeaderChange();
    }
  }

  private HBox createActionsBox(FieldRef fieldRef, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_field"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        currentFields().remove(fieldRef);
        rebuildRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(currentFields(), fromIndex, toIndex);
    rebuildRows();
    commitHeaderChange();
  }

  private EnumeratedStringFilter currentFilter() {
    OverviewConfiguration configuration = model.getContent().getConfiguration();
    FilterConfiguration filterConfiguration = configuration != null ? configuration.getFilterConfiguration() : null;
    return filterConfiguration != null ? filterConfiguration.getEnumeratedStringFilter() : null;
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
}
