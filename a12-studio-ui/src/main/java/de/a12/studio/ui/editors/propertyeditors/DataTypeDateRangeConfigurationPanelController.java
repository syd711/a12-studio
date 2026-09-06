package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.DateRangeTypeOptions;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
 * {@code youngerThan1900Check}/{@code notInDCustomFormat}/{@code notInDCustomRangeSeparator} are SME
 * "expert" properties that its own editor never lets the user add, change or remove (see
 * {@code ExpertProps} in SME's {@code dateRange.ts}) - they round-trip if already present in a model's
 * JSON but have no UI here either, matching {@code rangeSeparator}/{@code format}/{@code interpretationOfYear},
 * which SME does expose.
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
    FORMAT_LABELS.put(FORMAT_DAY_MONTH, "Only Day and Month (" + FORMAT_DAY_MONTH + ")");
  }

  private static final String INTERPRETATION_FROM = "FROM";
  private static final String INTERPRETATION_TO = "TO";

  private static final Map<String, String> INTERPRETATION_OF_YEAR_KEYS = new LinkedHashMap<>();
  static {
    INTERPRETATION_OF_YEAR_KEYS.put(null, "interpretation_of_year_standard");
    INTERPRETATION_OF_YEAR_KEYS.put(INTERPRETATION_FROM, "interpretation_of_year_from");
    INTERPRETATION_OF_YEAR_KEYS.put(INTERPRETATION_TO, "interpretation_of_year_to");
  }

  @FXML
  private ComboBox<String> formatComboBox;

  @FXML
  private TextField rangeSeparatorField;

  @FXML
  private ComboBox<String> interpretationOfYearComboBox;

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

    interpretationOfYearComboBox.getItems().addAll(INTERPRETATION_OF_YEAR_KEYS.keySet());
    interpretationOfYearComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(String interpretationOfYear) {
        return StudioBundle.get(INTERPRETATION_OF_YEAR_KEYS.get(interpretationOfYear));
      }

      @Override
      public String fromString(String displayName) {
        return INTERPRETATION_OF_YEAR_KEYS.entrySet().stream()
            .filter(entry -> StudioBundle.get(entry.getValue()).equals(displayName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
      }
    });

    bindComboBox(formatComboBox, (element, value) -> withDateRangeTypeOptions(element, options -> options.setFormat(value == null || value.isEmpty() ? null : value)));
    bindTextField(rangeSeparatorField, (element, value) -> withDateRangeTypeOptions(element, options -> options.setRangeSeparator(blankToNull(value))));
    bindComboBox(interpretationOfYearComboBox, (element, value) -> withDateRangeTypeOptions(element, options -> options.setInterpretationOfYear(value)));
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
    setFieldValue(interpretationOfYearComboBox, options != null ? options.getInterpretationOfYear() : null);
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
