package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.DateRangeTypeOptions;
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
 * {@code rangeSeparator}/{@code youngerThan1900Check}/{@code interpretationOfYear}/{@code notInDCustomFormat}/
 * {@code notInDCustomRangeSeparator} are exposed here as plain controls with no cross-field validity checks:
 * SME conditionally restricts some of these based on {@code format} (e.g. {@code youngerThan1900Check} only
 * applies when the format includes a year), but the exact conditions weren't independently confirmable from
 * the documentation available in this repo, so no enable/disable or validation logic was guessed at.
 */
public class DataTypeDateRangeConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String FORMAT_YEAR = "yyyy";
  private static final String FORMAT_MONTH = "MM";
  private static final String FORMAT_DAY_MONTH_YEAR = "yyyy-MM-dd";
  private static final String FORMAT_MONTH_YEAR = "yyyy-MM";
  private static final String FORMAT_DAY_MONTH = "MM-dd";

  private static final Map<String, String> FORMAT_LABELS = new LinkedHashMap<>();
  static {
    FORMAT_LABELS.put(FORMAT_YEAR, "Only Year (" + FORMAT_YEAR + "/" + FORMAT_YEAR + ")");
    FORMAT_LABELS.put(FORMAT_MONTH, "Only Month (" + FORMAT_MONTH + "/" + FORMAT_MONTH + ")");
    FORMAT_LABELS.put(FORMAT_DAY_MONTH_YEAR, "Day, Month and Year (" + FORMAT_DAY_MONTH_YEAR + "/" + FORMAT_DAY_MONTH_YEAR + ")");
    FORMAT_LABELS.put(FORMAT_MONTH_YEAR, "Only Month and Year (" + FORMAT_MONTH_YEAR + "/" + FORMAT_MONTH_YEAR + ")");
    FORMAT_LABELS.put(FORMAT_DAY_MONTH, "Only Day and Month (" + FORMAT_DAY_MONTH + "/" + FORMAT_DAY_MONTH + ")");
  }

  @FXML
  private ComboBox<String> formatComboBox;

  @FXML
  private TextField rangeSeparatorField;

  @FXML
  private TextField interpretationOfYearField;

  @FXML
  private TextField notInDCustomFormatField;

  @FXML
  private TextField notInDCustomRangeSeparatorField;

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

    bindComboBox(formatComboBox, (element, value) -> withDateRangeTypeOptions(element, options -> options.setFormat(value == null || value.isEmpty() ? null : value)));
    bindTextField(rangeSeparatorField, (element, value) -> withDateRangeTypeOptions(element, options -> options.setRangeSeparator(blankToNull(value))));
    bindTextField(interpretationOfYearField, (element, value) -> withDateRangeTypeOptions(element, options -> options.setInterpretationOfYear(blankToNull(value))));
    bindTextField(notInDCustomFormatField, (element, value) -> withDateRangeTypeOptions(element, options -> options.setNotInDCustomFormat(blankToNull(value))));
    bindTextField(notInDCustomRangeSeparatorField, (element, value) -> withDateRangeTypeOptions(element, options -> options.setNotInDCustomRangeSeparator(blankToNull(value))));
    bindCheckBox(youngerThan1900CheckBox, (element, value) -> withDateRangeTypeOptions(element, options -> options.setYoungerThan1900Check(value ? true : null)));
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    Optional<DateRangeFieldType> dateRangeFieldType = getDateRangeFieldType(element);
    DateRangeTypeOptions options = dateRangeFieldType.map(DateRangeFieldType::getDateRangeType).orElse(null);
    String format = options != null ? options.getFormat() : null;

    // A date range without a format is invalid, so default to the first format option as soon as the
    // field is selected instead of leaving the combo box blank until the user manually picks one.
    if (dateRangeFieldType.isPresent() && (format == null || format.isEmpty())) {
      format = FORMAT_LABELS.keySet().iterator().next();
      String defaultFormat = format;
      withDateRangeTypeOptions(element, o -> o.setFormat(defaultFormat));
      commitChange();
    }

    setFieldValue(formatComboBox, format);
    setFieldValue(rangeSeparatorField, options != null && options.getRangeSeparator() != null ? options.getRangeSeparator() : "");
    setFieldValue(interpretationOfYearField, options != null && options.getInterpretationOfYear() != null ? options.getInterpretationOfYear() : "");
    setFieldValue(notInDCustomFormatField, options != null && options.getNotInDCustomFormat() != null ? options.getNotInDCustomFormat() : "");
    setFieldValue(notInDCustomRangeSeparatorField, options != null && options.getNotInDCustomRangeSeparator() != null ? options.getNotInDCustomRangeSeparator() : "");
    setFieldValue(youngerThan1900CheckBox, options != null && Boolean.TRUE.equals(options.getYoungerThan1900Check()));
  }

  @Override
  protected String validationProperty() {
    return ElementProperty.DATA_TYPE;
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private static void withDateRangeTypeOptions(Element element, Consumer<DateRangeTypeOptions> mutator) {
    getDateRangeFieldType(element).ifPresent(dateRangeFieldType -> {
      DateRangeTypeOptions options = dateRangeFieldType.getDateRangeType();
      if (options == null) {
        options = new DateRangeTypeOptions();
        dateRangeFieldType.setDateRangeType(options);
      }
      mutator.accept(options);
    });
  }

  private static Optional<DateRangeFieldType> getDateRangeFieldType(Element element) {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof DateRangeFieldType dateRangeFieldType) {
      return Optional.of(dateRangeFieldType);
    }
    return Optional.empty();
  }
}
