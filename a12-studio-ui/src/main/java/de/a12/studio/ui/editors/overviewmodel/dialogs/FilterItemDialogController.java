package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.BooleanUserAccessOption;
import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.models.overviewmodel.FilterItemOptions;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.overviewmodel.OverviewElementOptions;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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
  private VBox matchingOptionsBox;
  @FXML
  private CheckBox invertField;
  @FXML
  private CheckBox invertUserAccessField;
  @FXML
  private CheckBox emptyField;
  @FXML
  private CheckBox emptyUserAccessField;
  @FXML
  private CheckBox caseSensitiveField;
  @FXML
  private CheckBox caseSensitiveUserAccessField;
  @FXML
  private CheckBox exactMatchField;
  @FXML
  private CheckBox exactMatchUserAccessField;
  @FXML
  private javafx.scene.control.Label noTypeSpecificOptionsLabel;
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
    labelController.configureCustom("label", StudioBundle.get("label"));
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

    invertField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureInvert().setValue(newValue);
      }
    });
    invertUserAccessField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureInvert().setEnabled(newValue);
      }
    });
    emptyField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureEmpty().setValue(newValue);
      }
    });
    emptyUserAccessField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureEmpty().setEnabled(newValue);
      }
    });
    caseSensitiveField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureCaseSensitive().setValue(newValue);
      }
    });
    caseSensitiveUserAccessField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureCaseSensitive().setEnabled(newValue);
      }
    });
    exactMatchField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureExactMatch().setValue(newValue);
      }
    });
    exactMatchUserAccessField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureExactMatch().setEnabled(newValue);
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

      BooleanUserAccessOption invert = currentInvert();
      invertField.setSelected(invert != null && Boolean.TRUE.equals(invert.getValue()));
      invertUserAccessField.setSelected(invert != null && Boolean.TRUE.equals(invert.getEnabled()));
      BooleanUserAccessOption empty = currentEmpty();
      emptyField.setSelected(empty != null && Boolean.TRUE.equals(empty.getValue()));
      emptyUserAccessField.setSelected(empty != null && Boolean.TRUE.equals(empty.getEnabled()));
      BooleanUserAccessOption caseSensitive = currentCaseSensitive();
      caseSensitiveField.setSelected(caseSensitive != null && Boolean.TRUE.equals(caseSensitive.getValue()));
      caseSensitiveUserAccessField.setSelected(caseSensitive != null && Boolean.TRUE.equals(caseSensitive.getEnabled()));
      BooleanUserAccessOption exactMatch = currentExactMatch();
      exactMatchField.setSelected(exactMatch != null && Boolean.TRUE.equals(exactMatch.getValue()));
      exactMatchUserAccessField.setSelected(exactMatch != null && Boolean.TRUE.equals(exactMatch.getEnabled()));
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
    boolean isStringField = "string".equals(item.getType());
    matchingOptionsBox.setVisible(isStringField);
    matchingOptionsBox.setManaged(isStringField);
    noTypeSpecificOptionsLabel.setVisible(!isStringField);
    noTypeSpecificOptionsLabel.setManaged(!isStringField);
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
    ensureOptions().setFieldId(fieldId);
  }

  private FilterItemOptions ensureOptions() {
    if (item.getOptions() == null) {
      item.setOptions(new FilterItemOptions());
    }
    return item.getOptions();
  }

  private BooleanUserAccessOption currentInvert() {
    return item.getOptions() != null ? item.getOptions().getInvert() : null;
  }

  private BooleanUserAccessOption ensureInvert() {
    FilterItemOptions options = ensureOptions();
    if (options.getInvert() == null) {
      options.setInvert(new BooleanUserAccessOption());
    }
    return options.getInvert();
  }

  private BooleanUserAccessOption currentEmpty() {
    return item.getOptions() != null ? item.getOptions().getEmpty() : null;
  }

  private BooleanUserAccessOption ensureEmpty() {
    FilterItemOptions options = ensureOptions();
    if (options.getEmpty() == null) {
      options.setEmpty(new BooleanUserAccessOption());
    }
    return options.getEmpty();
  }

  private BooleanUserAccessOption currentCaseSensitive() {
    return item.getOptions() != null ? item.getOptions().getCaseSensitive() : null;
  }

  private BooleanUserAccessOption ensureCaseSensitive() {
    FilterItemOptions options = ensureOptions();
    if (options.getCaseSensitive() == null) {
      options.setCaseSensitive(new BooleanUserAccessOption());
    }
    return options.getCaseSensitive();
  }

  private BooleanUserAccessOption currentExactMatch() {
    return item.getOptions() != null ? item.getOptions().getExactMatch() : null;
  }

  private BooleanUserAccessOption ensureExactMatch() {
    FilterItemOptions options = ensureOptions();
    if (options.getExactMatch() == null) {
      options.setExactMatch(new BooleanUserAccessOption());
    }
    return options.getExactMatch();
  }
}
