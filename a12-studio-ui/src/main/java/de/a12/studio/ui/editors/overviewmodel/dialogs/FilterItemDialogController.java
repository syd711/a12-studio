package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.BooleanUserAccessOption;
import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.models.overviewmodel.FilterItemOptions;
import de.a12.studio.models.overviewmodel.FilterOptionToggle;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.overviewmodel.OverviewElementOptions;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Add/edit dialog for a single {@link FilterItem}, opened from {@link FilterGroupDialogController} by clicking
 * a row or its Edit button, or by the Add button. Nested inside that dialog, so - unlike a top-level dialog such
 * as {@link OverviewColumnDialogController} - {@link #onDialogSubmit} doesn't itself persist anything; the
 * {@link FilterItem} is mutated live and the outer {@link FilterGroupDialogController}'s own OK does the actual
 * save, in one go, once the whole group is confirmed.
 * <p>
 * Only Field Reference-based items are supported - see {@link FilterItem}'s class doc for which field-type-
 * specific {@link FilterItemOptions} are modeled and which (Boolean/Confirm criteria, Enumeration/Multi-select
 * Initial Criteria/Pinned Values, and Filter-Definition-based/query items) aren't - see {@code TODO.md}'s
 * Overview Model section.
 */
public class FilterItemDialogController implements DialogController {

  @FXML
  private ComboBox<String> fieldRefField;
  @FXML
  private TextField typeField;
  @FXML
  private CheckBox preferFilterBarField;
  @FXML
  private CheckBox collapsedField;
  @FXML
  private LocalizedTextPanelController labelController;
  @FXML
  private IconPanelController iconController;
  @FXML
  private VBox matchingOptionsBox;
  @FXML
  private ComboBox<String> stringViewModeField;
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
  private VBox enumerationOptionsBox;
  @FXML
  private CheckBox enumerationCompactViewField;
  @FXML
  private VBox rangesBox;
  @FXML
  private GridPane rangesGrid;
  @FXML
  private VBox periodsBox;
  @FXML
  private GridPane periodsGrid;
  @FXML
  private Label noTypeSpecificOptionsLabel;
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
    preferFilterBarField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        item.setPreferFilterBar(newValue ? Boolean.TRUE : null);
      }
    });
    collapsedField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        item.setCollapsed(newValue ? Boolean.TRUE : null);
      }
    });

    stringViewModeField.setItems(FXCollections.observableArrayList(
        (String) null, OverviewElementOptions.STRING_VIEW_MODE_TEXT_FIELD, OverviewElementOptions.STRING_VIEW_MODE_LIST));
    stringViewModeField.setConverter(displayConverter(value -> switch (value == null ? "" : value) {
      case OverviewElementOptions.STRING_VIEW_MODE_TEXT_FIELD -> StudioBundle.get("view_mode_text_field");
      case OverviewElementOptions.STRING_VIEW_MODE_LIST -> StudioBundle.get("view_mode_list");
      default -> "";
    }));
    stringViewModeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureOptions().setViewMode(newValue);
      }
    });

    enumerationCompactViewField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureOptions().setViewMode(newValue ? OverviewElementOptions.ENUMERATION_VIEW_MODE_COMPACT : null);
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
      preferFilterBarField.setSelected(Boolean.TRUE.equals(item.getPreferFilterBar()));
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

      stringViewModeField.setValue(item.getOptions() != null ? item.getOptions().getViewMode() : null);
      enumerationCompactViewField.setSelected(item.getOptions() != null
          && OverviewElementOptions.ENUMERATION_VIEW_MODE_COMPACT.equals(item.getOptions().getViewMode()));
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
    String type = item.getType();
    typeField.setText(type != null ? type : "");

    boolean isStringField = "string".equals(type);
    boolean isEnumerationField = "enumeration".equals(type);
    boolean showRanges = OverviewElementOptions.supportsRanges(type);
    boolean showPeriods = OverviewElementOptions.supportsPeriods(type);

    matchingOptionsBox.setVisible(isStringField);
    matchingOptionsBox.setManaged(isStringField);
    enumerationOptionsBox.setVisible(isEnumerationField);
    enumerationOptionsBox.setManaged(isEnumerationField);
    rangesBox.setVisible(showRanges);
    rangesBox.setManaged(showRanges);
    periodsBox.setVisible(showPeriods);
    periodsBox.setManaged(showPeriods);

    boolean hasTypeSpecificOptions = isStringField || isEnumerationField || showRanges || showPeriods;
    noTypeSpecificOptionsLabel.setVisible(!hasTypeSpecificOptions);
    noTypeSpecificOptionsLabel.setManaged(!hasTypeSpecificOptions);

    if (showRanges) {
      rebuildToggleGrid(rangesGrid, ensureRanges(), FilterItemDialogController::rangeOptionLabelKey);
    }
    if (showPeriods) {
      rebuildToggleGrid(periodsGrid, ensurePeriods(type), FilterItemDialogController::periodOptionLabelKey);
    }
  }

  /** Rebuilds {@code grid} with one row per {@code toggles} entry: the option's localized label, an "Enabled"
   * checkbox and a "Default" checkbox that enforces the fixture-observed invariant of at most one default entry
   * per list (and that a default entry is always enabled). */
  private void rebuildToggleGrid(GridPane grid, List<FilterOptionToggle> toggles, Function<String, String> labelKey) {
    grid.getChildren().clear();
    grid.getColumnConstraints().clear();
    for (int row = 0; row < toggles.size(); row++) {
      FilterOptionToggle toggle = toggles.get(row);

      Label optionLabel = new Label(StudioBundle.get(labelKey.apply(toggle.getOption())));
      CheckBox enabledCheckBox = new CheckBox(StudioBundle.get("enabled"));
      CheckBox defaultCheckBox = new CheckBox(StudioBundle.get("default"));

      updatingFromModel = true;
      try {
        enabledCheckBox.setSelected(Boolean.TRUE.equals(toggle.getEnabled()));
        defaultCheckBox.setSelected(Boolean.TRUE.equals(toggle.getDefaultOption()));
        defaultCheckBox.setDisable(!enabledCheckBox.isSelected());
      }
      finally {
        updatingFromModel = false;
      }

      enabledCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
        if (updatingFromModel) {
          return;
        }
        toggle.setEnabled(newValue);
        defaultCheckBox.setDisable(!newValue);
        if (!newValue && Boolean.TRUE.equals(toggle.getDefaultOption())) {
          defaultCheckBox.setSelected(false);
        }
      });
      defaultCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
        if (updatingFromModel) {
          return;
        }
        toggle.setDefaultOption(newValue ? Boolean.TRUE : null);
        if (newValue) {
          for (FilterOptionToggle other : toggles) {
            if (other != toggle) {
              other.setDefaultOption(null);
            }
          }
          rebuildToggleGrid(grid, toggles, labelKey);
        }
      });

      grid.add(optionLabel, 0, row);
      grid.add(enabledCheckBox, 1, row);
      grid.add(defaultCheckBox, 2, row);
    }
  }

  private static String rangeOptionLabelKey(String option) {
    return switch (option) {
      case "fromTo" -> "range_from_to";
      case "fromOnly" -> "range_from_only";
      case "toOnly" -> "range_to_only";
      case "exact" -> "range_exact";
      default -> option;
    };
  }

  private static String periodOptionLabelKey(String option) {
    return switch (option) {
      case "date" -> "period_date";
      case "time" -> "period_time";
      case "dateTime" -> "period_date_time";
      case "year" -> "period_year";
      case "yearMonth" -> "period_year_month";
      case "month" -> "period_month";
      default -> option;
    };
  }

  private List<FilterOptionToggle> ensureRanges() {
    FilterItemOptions options = ensureOptions();
    if (options.getRanges().isEmpty()) {
      options.getRanges().addAll(OverviewElementOptions.defaultRanges());
    }
    return options.getRanges();
  }

  private List<FilterOptionToggle> ensurePeriods(String type) {
    FilterItemOptions options = ensureOptions();
    if (options.getPeriods().isEmpty()) {
      options.getPeriods().addAll(OverviewElementOptions.defaultPeriods(type));
    }
    return options.getPeriods();
  }

  private static StringConverter<String> displayConverter(Function<String, String> display) {
    return new StringConverter<>() {
      @Override
      public String toString(String value) {
        return display.apply(value);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
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
