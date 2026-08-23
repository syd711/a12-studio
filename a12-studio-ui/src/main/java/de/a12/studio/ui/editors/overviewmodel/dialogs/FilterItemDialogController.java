package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.models.overviewmodel.FilterItemOptions;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.overviewmodel.OverviewElementOptions;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Add/edit dialog for a single {@link FilterItem}, opened from {@link FilterGroupDialogController} by clicking
 * a row or its Edit button, or by the Add button. Nested inside that dialog, so - unlike a top-level dialog such
 * as {@link OverviewColumnDialogController} - {@link #onDialogSubmit} doesn't itself persist anything; the
 * {@link FilterItem} is mutated live and the outer {@link FilterGroupDialogController}'s own OK does the actual
 * save, in one go, once the whole group is confirmed.
 * <p>
 * Only Field Reference-based items are supported - see {@link FilterItem}'s class doc for which field-type-
 * specific {@link FilterItemOptions} are modeled and which (Boolean/Enumeration/Number/Date, and
 * Filter-Definition-based/query items) aren't.
 */
public class FilterItemDialogController implements DialogController {

  @FXML
  private ComboBox<String> fieldRefField;
  @FXML
  private TextField typeField;
  @FXML
  private CheckBox showInFilterBarField;
  @FXML
  private CheckBox collapsedField;
  @FXML
  private LocalizedTextPanelController labelController;
  @FXML
  private IconPanelController iconController;
  @FXML
  private Button okButton;

  // Shared by the embedded label/icon panels so their commits aren't persisted while this dialog (or its owning
  // FilterGroupDialogController) is open: the outer dialog persists everything itself, in one go, once its own
  // OK is pressed.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private FilterItem item;

  private FilterItemSnapshot snapshot;

  private ElementIndex documentModelIndex;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits.
  private boolean updatingFromModel;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    labelController.configureCustom("label", "LABEL");
    labelController.setSaveMode(saveMode);
    iconController.setSaveMode(saveMode);

    fieldRefField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      setFieldId(newValue);
      item.setType(OverviewElementOptions.filterItemFieldType(documentModelIndex, newValue));
      updateTypeField();
      validate();
    });
    showInFilterBarField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        item.setShowInFilterBar(newValue ? Boolean.TRUE : null);
      }
    });
    collapsedField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        item.setCollapsed(newValue ? Boolean.TRUE : null);
      }
    });
  }

  void init(Stage stage, ElementIndex documentModelIndex, @NonNull FilterItem item) {
    this.stage = stage;
    this.item = item;
    this.documentModelIndex = documentModelIndex;
    this.snapshot = new FilterItemSnapshot(item);

    fieldRefField.getItems().setAll(OverviewElementOptions.elementIds(documentModelIndex));
    OverviewElementOptions.applyElementRefConverter(fieldRefField, documentModelIndex);

    updatingFromModel = true;
    try {
      fieldRefField.setValue(item.getOptions() != null ? item.getOptions().getFieldId() : null);
      showInFilterBarField.setSelected(Boolean.TRUE.equals(item.getShowInFilterBar()));
      collapsedField.setSelected(Boolean.TRUE.equals(item.getCollapsed()));
    }
    finally {
      updatingFromModel = false;
    }
    updateTypeField();

    labelController.setCustom(item::getLabel);
    iconController.setCustom(item::getIcon, item::setIcon);

    validate();
  }

  /** Unregisters the embedded panels once this dialog is closed - see {@link Dialogs#showFilterItem}, which
   * calls this from the stage's {@code onHidden} handler. */
  void destroy() {
    labelController.destroy();
    iconController.destroy();
  }

  @Override
  public void onDialogCancel() {
    snapshot.restore();
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  private void updateTypeField() {
    typeField.setText(item.getType() != null ? item.getType() : "");
  }

  private void validate() {
    okButton.setDisable(fieldRefField.getValue() == null);
  }

  private void setFieldId(String fieldId) {
    if (fieldId == null) {
      if (item.getOptions() != null) {
        item.getOptions().setFieldId(null);
      }
      return;
    }
    FilterItemOptions options = item.getOptions();
    if (options == null) {
      options = new FilterItemOptions();
      item.setOptions(options);
    }
    options.setFieldId(fieldId);
  }
}
