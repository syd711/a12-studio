package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.DateFragmentFieldType;
import de.a12.studio.models.documentmodel.DateFragmentTypeOptions;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * {@code youngerThan1900Check}/{@code notInDCustomFormat} are exposed here as plain controls with no
 * cross-field validity checks - see the same caveat on {@link DataTypeDateRangeConfigurationPanelController}.
 */
public class DataTypeDateFragmentConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String FORMAT_YEAR = "yyyy";
  private static final String FORMAT_MONTH = "MM";
  private static final String FORMAT_MONTH_YEAR = "yyyy-MM";
  private static final String FORMAT_DAY_MONTH = "MM-dd";

  private static final Map<String, String> FORMAT_LABELS = new LinkedHashMap<>();
  static {
    FORMAT_LABELS.put(FORMAT_YEAR, "Only Year (" + FORMAT_YEAR + ")");
    FORMAT_LABELS.put(FORMAT_MONTH, "Only Month (" + FORMAT_MONTH + ")");
    FORMAT_LABELS.put(FORMAT_MONTH_YEAR, "Only Month and Year (" + FORMAT_MONTH_YEAR + ")");
    FORMAT_LABELS.put(FORMAT_DAY_MONTH, "Only Day and Month (" + FORMAT_DAY_MONTH + ")");
  }

  @FXML
  private ComboBox<String> formatComboBox;

  @FXML
  private TextField notInDCustomFormatField;

  @FXML
  private CheckBox youngerThan1900CheckBox;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);

    formatComboBox.getItems().addAll(FORMAT_LABELS.keySet());
    formatComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(String format) {
        return format == null ? "" : FORMAT_LABELS.getOrDefault(format, format);
      }

      @Override
      public String fromString(String displayName) {
        return FORMAT_LABELS.entrySet().stream()
            .filter(entry -> entry.getValue().equals(displayName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
      }
    });

    bindComboBox(formatComboBox, (element, value) -> withDateFragmentTypeOptions(element, options -> options.setFormatOfFragment(value == null || value.isEmpty() ? null : value)));
    bindTextField(notInDCustomFormatField, (element, value) -> withDateFragmentTypeOptions(element, options -> options.setNotInDCustomFormat(value == null || value.isBlank() ? null : value)));
    bindCheckBox(youngerThan1900CheckBox, (element, value) -> withDateFragmentTypeOptions(element, options -> options.setYoungerThan1900Check(value ? true : null)));
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    Optional<DateFragmentFieldType> dateFragmentFieldType = getDateFragmentFieldType(element);
    DateFragmentTypeOptions options = dateFragmentFieldType.map(DateFragmentFieldType::getDateFragmentType).orElse(null);
    String format = options != null ? options.getFormatOfFragment() : null;

    // A date fragment without a format is invalid, so default to the first format option as soon as the
    // field is selected instead of leaving the combo box blank until the user manually picks one.
    if (dateFragmentFieldType.isPresent() && (format == null || format.isEmpty())) {
      format = FORMAT_LABELS.keySet().iterator().next();
      String defaultFormat = format;
      withDateFragmentTypeOptions(element, o -> o.setFormatOfFragment(defaultFormat));
      commitChange();
    }

    setFieldValue(formatComboBox, format);
    setFieldValue(notInDCustomFormatField, options != null && options.getNotInDCustomFormat() != null ? options.getNotInDCustomFormat() : "");
    setFieldValue(youngerThan1900CheckBox, options != null && Boolean.TRUE.equals(options.getYoungerThan1900Check()));
  }

  @Override
  protected String validationProperty() {
    return ElementProperty.DATA_TYPE;
  }

  private static void withDateFragmentTypeOptions(Element element, Consumer<DateFragmentTypeOptions> mutator) {
    getDateFragmentFieldType(element).ifPresent(dateFragmentFieldType -> {
      DateFragmentTypeOptions options = dateFragmentFieldType.getDateFragmentType();
      if (options == null) {
        options = new DateFragmentTypeOptions();
        dateFragmentFieldType.setDateFragmentType(options);
      }
      mutator.accept(options);
    });
  }

  private static Optional<DateFragmentFieldType> getDateFragmentFieldType(Element element) {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof DateFragmentFieldType dateFragmentFieldType) {
      return Optional.of(dateFragmentFieldType);
    }
    return Optional.empty();
  }
}
