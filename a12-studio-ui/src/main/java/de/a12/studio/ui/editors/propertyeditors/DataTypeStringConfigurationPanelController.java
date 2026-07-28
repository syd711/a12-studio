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
import de.a12.studio.ui.util.Icons;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class DataTypeStringConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField minLengthField;

  @FXML
  private TextField maxLengthField;

  @FXML
  private TextField patternField;

  @FXML
  private CheckBox lineBreaksCheckBox;

  @FXML
  private CheckBox alphabeticalSortingCheckBox;

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

    bindTextField(minLengthField, (element, value) -> withStringTypeOptions(element, options -> options.setMinLength(parseInteger(value))));
    bindTextField(maxLengthField, (element, value) -> withStringTypeOptions(element, options -> options.setMaxLength(parseInteger(value))));
    bindTextField(patternField, (element, value) -> withStringTypeOptions(element, options -> options.setPattern(value.isEmpty() ? null : value)));
    bindCheckBox(lineBreaksCheckBox, (element, value) -> withStringTypeOptions(element, options -> options.setLineBreaksPermitted(value ? true : null)));
    bindCheckBox(alphabeticalSortingCheckBox, (element, value) -> withStringTypeOptions(element, options -> options.setAlphabeticalSorting(value ? true : null)));
  }

  public ReadOnlyStringProperty patternProperty() {
    return patternField.textProperty();
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);
    this.projectItem = Studio.getSelectedProjectItem();

    StringTypeOptions options = getStringFieldType(element).map(StringFieldType::getStringType).orElse(null);
    setFieldValue(minLengthField, options != null && options.getMinLength() != null ? String.valueOf(options.getMinLength()) : "");
    setFieldValue(maxLengthField, options != null && options.getMaxLength() != null ? String.valueOf(options.getMaxLength()) : "");
    setFieldValue(patternField, options != null && options.getPattern() != null ? options.getPattern() : "");
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

    Button editButton = createActionButton(Icons.PENCIL, "Edit", () -> openSuggestionsDialog(locale));

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
    Dialogs.showSuggestions(Studio.stage, "Suggestions for Locale " + locale.getCode(), getSuggestionValues(locale.getCode()))
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

  private static Button createActionButton(String iconLiteral, String tooltip, Runnable action) {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(16);
    icon.getStyleClass().add("toolbar-icon");

    Button button = new Button();
    button.getStyleClass().add("default-button");
    button.setGraphic(icon);
    button.setTooltip(new Tooltip(tooltip));
    button.setOnAction(event -> action.run());
    return button;
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
