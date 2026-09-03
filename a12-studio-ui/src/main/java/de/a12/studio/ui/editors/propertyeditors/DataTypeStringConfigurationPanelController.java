package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.Locale;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.HintList;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.StringTypeOptions;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.dialogs.Dialogs;
import de.a12.studio.ui.events.LocalesChangedEvent;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.SystemUtil;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class DataTypeStringConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private final Debouncer debouncer = new Debouncer();

  // Set while patternComboBox is being repopulated from the model, so those programmatic updates aren't
  // mistaken for user edits. Local to this panel since the pattern combo isn't bound via the base class's
  // bindComboBox (it's a ComboBox<Object> with two listeners - value and editor text - not a plain String combo).
  private boolean updatingFromModel;

  @FXML
  private TextField minLengthField;

  @FXML
  private TextField maxLengthField;

  @FXML
  private ComboBox<Object> patternComboBox;

  @FXML
  private CheckBox lineBreaksCheckBox;

  @FXML
  private CheckBox alphabeticalSortingCheckBox;

  @FXML
  private Hyperlink regexTestLink;

  @FXML
  private HBox suggestionsColumnHeaders;

  @FXML
  private VBox suggestionsRows;

  @FXML
  private Label suggestionsEmptyLabel;

  private ProjectItem projectItem;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);

    WidgetFactory.restrictToNumericInput(minLengthField);
    WidgetFactory.restrictToNumericInput(maxLengthField);

    // Populate combo with preset entries; the editable text field sits on top for free typing.
    patternComboBox.getItems().setAll(RegexPreset.ALL);

    // Show "Description  —  pattern" for presets, raw string for anything else (typed text).
    patternComboBox.setButtonCell(new PatternCell());
    patternComboBox.setCellFactory(lv -> new PatternCell());

    // When the combo value changes (selection or direct typing) persist the raw pattern string.
    patternComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel) {
        return;
      }
      String rawPattern = toRawPattern(newVal);
      withStringTypeOptions(element, options -> options.setPattern(rawPattern == null || rawPattern.isEmpty() ? null : rawPattern));
      debouncer.debounce("patternComboBox", this::commitChange, COMMIT_DEBOUNCE_MS, true);
    });

    // Also react to the user typing inside the editable field directly (valueProperty only fires on commit).
    patternComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel) {
        return;
      }
      String rawPattern = newVal == null || newVal.isEmpty() ? null : newVal;
      withStringTypeOptions(element, options -> options.setPattern(rawPattern));
      debouncer.debounce("patternComboBox", this::commitChange, COMMIT_DEBOUNCE_MS, true);
    });

    bindTextField(minLengthField, (element, value) -> withStringTypeOptions(element, options -> options.setMinLength(parseInteger(value))));
    bindTextField(maxLengthField, (element, value) -> withStringTypeOptions(element, options -> options.setMaxLength(parseInteger(value))));
    bindCheckBox(lineBreaksCheckBox, (element, value) -> withStringTypeOptions(element, options -> options.setLineBreaksPermitted(value ? true : null)));
    bindCheckBox(alphabeticalSortingCheckBox, (element, value) -> withStringTypeOptions(element, options -> options.setAlphabeticalSorting(value ? true : null)));
  }

  @FXML
  private void openRegexTestEnvironment() {
    SystemUtil.openUrl("https://regex101.com/");
  }

  /** Returns the raw pattern string from whatever the combo holds (preset record or plain string). */
  private static String toRawPattern(Object value) {
    if (value instanceof RegexPreset preset) {
      return preset.pattern();
    }
    return value != null ? value.toString() : null;
  }

  public ReadOnlyStringProperty patternProperty() {
    return patternComboBox.getEditor().textProperty();
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);
    this.projectItem = Studio.getSelectedProjectItem();

    StringTypeOptions options = getStringFieldType(element).map(StringFieldType::getStringType).orElse(null);
    setFieldValue(minLengthField, options != null && options.getMinLength() != null ? String.valueOf(options.getMinLength()) : "");
    setFieldValue(maxLengthField, options != null && options.getMaxLength() != null ? String.valueOf(options.getMaxLength()) : "");

    // Populate pattern: show raw text in editor (presets aren't re-matched on load — user sees the stored expression).
    String pattern = options != null && options.getPattern() != null ? options.getPattern() : "";
    updatingFromModel = true;
    try {
      patternComboBox.getEditor().setText(pattern);
      patternComboBox.setValue(pattern);
    } finally {
      updatingFromModel = false;
    }

    setFieldValue(lineBreaksCheckBox, options != null && Boolean.TRUE.equals(options.getLineBreaksPermitted()));
    setFieldValue(alphabeticalSortingCheckBox, options != null && Boolean.TRUE.equals(options.getAlphabeticalSorting()));
    rebuildSuggestionsRows();
  }

  @Override
  protected String validationProperty() {
    return ElementProperty.DATA_TYPE;
  }

  @Override
  public void localesChanged(@NonNull LocalesChangedEvent event) {
    if (event.getItem().equals(projectItem)) {
      rebuildSuggestionsRows();
    }
  }

  // ----- Cell rendering -----

  /** Shows "Description  —  pattern" for RegexPreset items, plain text for everything else. */
  private static final class PatternCell extends ListCell<Object> {
    @Override
    protected void updateItem(Object item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setTooltip(null);
      } else if (item instanceof RegexPreset preset) {
        setText(preset.description() + "  \u2014  " + preset.pattern());
      } else {
        setText(item.toString());
      }
    }
  }

  // ----- Suggestions -----

  private void rebuildSuggestionsRows() {
    suggestionsRows.getChildren().clear();

    List<Locale> locales = getModelLocales();
    boolean empty = locales.isEmpty();
    suggestionsColumnHeaders.setVisible(!empty);
    suggestionsColumnHeaders.setManaged(!empty);
    suggestionsRows.setVisible(!empty);
    suggestionsRows.setManaged(!empty);
    suggestionsEmptyLabel.setVisible(empty);
    suggestionsEmptyLabel.setManaged(empty);

    for (Locale locale : locales) {
      suggestionsRows.getChildren().add(createSuggestionsRow(locale));
    }
  }

  private HBox createSuggestionsRow(Locale locale) {
    Label localeLabel = new Label(locale.getCode());
    localeLabel.setPrefWidth(80.0);

    List<String> values = getSuggestionValues(locale.getCode());
    Label valuesLabel = new Label(values.isEmpty() ? "" : String.join(", ", values));
    valuesLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(valuesLabel, Priority.ALWAYS);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openSuggestionsDialog(locale));

    HBox row = new HBox(10.0, localeLabel, valuesLabel, editButton);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    row.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        openSuggestionsDialog(locale);
      }
    });
    return row;
  }

  private void openSuggestionsDialog(Locale locale) {
    Dialogs.showSuggestions(Studio.stage, StudioBundle.get("suggestions_for_locale", locale.getCode()), getSuggestionValues(locale.getCode()))
        .ifPresent(newValues -> {
          withStringTypeOptions(element, options -> setSuggestionValues(options, locale.getCode(), newValues));
          rebuildSuggestionsRows();
          commitChange();
        });
  }

  private List<String> getSuggestionValues(String localeCode) {
    StringTypeOptions options = getStringFieldType(element).map(StringFieldType::getStringType).orElse(null);
    if (options == null) {
      return List.of();
    }
    return options.getHintList().stream()
        .filter(hintList -> localeCode.equals(hintList.getLocale()))
        .findFirst()
        .map(HintList::getValues)
        .orElse(List.of());
  }

  private static void setSuggestionValues(StringTypeOptions options, String localeCode, List<String> values) {
    Optional<HintList> existing = options.getHintList().stream()
        .filter(hintList -> localeCode.equals(hintList.getLocale()))
        .findFirst();
    if (values.isEmpty()) {
      existing.ifPresent(options.getHintList()::remove);
    } else if (existing.isPresent()) {
      existing.get().setValues(values);
    } else {
      HintList hintList = new HintList();
      hintList.setLocale(localeCode);
      hintList.setValues(values);
      options.getHintList().add(hintList);
    }
  }

  private List<Locale> getModelLocales() {
    if (projectItem == null || projectItem.getModel() == null) {
      return List.of();
    }
    return projectItem.getModel().getLocales();
  }

  private static void withStringTypeOptions(Element element, Consumer<StringTypeOptions> mutator) {
    getStringFieldType(element).ifPresent(stringFieldType -> {
      StringTypeOptions options = stringFieldType.getStringType();
      if (options == null) {
        options = new StringTypeOptions();
        stringFieldType.setStringType(options);
      }
      mutator.accept(options);
    });
  }

  private static Optional<StringFieldType> getStringFieldType(Element element) {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof StringFieldType stringFieldType) {
      return Optional.of(stringFieldType);
    }
    return Optional.empty();
  }

  private static Integer parseInteger(String value) {
    return value.isEmpty() ? null : Integer.valueOf(value);
  }
}
