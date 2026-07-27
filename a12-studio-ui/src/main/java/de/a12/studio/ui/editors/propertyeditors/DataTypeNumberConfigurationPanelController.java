package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.NumberTypeOptions;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class DataTypeNumberConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String TRAIT_AMOUNT = "amount";
  private static final String TRAIT_PERCENT = "percent";
  private static final String TRAIT_PERMILLE = "permille";

  private static final List<String> TRAITS = List.of(TRAIT_AMOUNT, TRAIT_PERCENT, TRAIT_PERMILLE);

  @FXML
  private CheckBox decimalPlacesCheckBox;

  @FXML
  private CheckBox zeroAllowedCheckBox;

  @FXML
  private CheckBox positivesOnlyCheckBox;

  @FXML
  private CheckBox leadingZerosAllowedCheckBox;

  @FXML
  private VBox minDecimalPlacesBox;

  @FXML
  private VBox maxDecimalPlacesBox;

  @FXML
  private TextField minDecimalPlacesField;

  @FXML
  private TextField maxDecimalPlacesField;

  @FXML
  private TextField minValueField;

  @FXML
  private TextField maxValueField;

  @FXML
  private TextField maxIntegerDigitsField;

  @FXML
  private ComboBox<String> unitComboBox;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);

    WidgetFactory.restrictToNumericInput(minDecimalPlacesField);
    WidgetFactory.restrictToNumericInput(maxDecimalPlacesField);
    WidgetFactory.restrictToDecimalInput(minValueField);
    WidgetFactory.restrictToDecimalInput(maxValueField);
    WidgetFactory.restrictToNumericInput(maxIntegerDigitsField);

    unitComboBox.getItems().addAll(TRAITS);
    unitComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(String trait) {
        return trait == null ? "" : Character.toUpperCase(trait.charAt(0)) + trait.substring(1);
      }

      @Override
      public String fromString(String displayName) {
        return displayName == null || displayName.isEmpty() ? null : displayName.toLowerCase();
      }
    });

    minDecimalPlacesBox.managedProperty().bind(minDecimalPlacesBox.visibleProperty());
    minDecimalPlacesBox.visibleProperty().bind(decimalPlacesCheckBox.selectedProperty());
    maxDecimalPlacesBox.managedProperty().bind(maxDecimalPlacesBox.visibleProperty());
    maxDecimalPlacesBox.visibleProperty().bind(decimalPlacesCheckBox.selectedProperty());

    bindCheckBox(decimalPlacesCheckBox, (element, value) -> withNumberTypeOptions(element, options -> {
      if (!value) {
        options.setMinFractionalDigits(null);
        options.setMaxFractionalDigits(null);
        setFieldValue(minDecimalPlacesField, "");
        setFieldValue(maxDecimalPlacesField, "");
      }
    }));
    bindTextField(minDecimalPlacesField, (element, value) -> withNumberTypeOptions(element, options -> options.setMinFractionalDigits(parseInteger(value))));
    bindTextField(maxDecimalPlacesField, (element, value) -> withNumberTypeOptions(element, options -> options.setMaxFractionalDigits(parseInteger(value))));

    bindCheckBox(zeroAllowedCheckBox, (element, value) -> withNumberTypeOptions(element, options -> options.setZeroNotAllowed(value ? null : true)));
    bindCheckBox(positivesOnlyCheckBox, (element, value) -> withNumberTypeOptions(element, options -> options.setPositivesOnly(value ? true : null)));
    bindCheckBox(leadingZerosAllowedCheckBox, (element, value) -> withNumberTypeOptions(element, options -> options.setLeadingZerosAllowed(value ? true : null)));

    bindTextField(minValueField, (element, value) -> withNumberTypeOptions(element, options -> options.setMinValue(parseDouble(value))));
    bindTextField(maxValueField, (element, value) -> withNumberTypeOptions(element, options -> options.setMaxValue(parseDouble(value))));
    bindTextField(maxIntegerDigitsField, (element, value) -> withNumberTypeOptions(element, options -> options.setMaxIntegerDigits(parseInteger(value))));

    bindComboBox(unitComboBox, (element, value) -> withNumberTypeOptions(element, options -> options.setTrait(value == null || value.isEmpty() ? null : value)));
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    NumberTypeOptions options = getNumberFieldType(element).map(NumberFieldType::getNumberType).orElse(null);
    boolean hasDecimalPlaces = options != null && (options.getMinFractionalDigits() != null || options.getMaxFractionalDigits() != null);
    setFieldValue(decimalPlacesCheckBox, hasDecimalPlaces);
    setFieldValue(minDecimalPlacesField, options != null && options.getMinFractionalDigits() != null ? String.valueOf(options.getMinFractionalDigits()) : "");
    setFieldValue(maxDecimalPlacesField, options != null && options.getMaxFractionalDigits() != null ? String.valueOf(options.getMaxFractionalDigits()) : "");
    setFieldValue(zeroAllowedCheckBox, options == null || !Boolean.TRUE.equals(options.getZeroNotAllowed()));
    setFieldValue(positivesOnlyCheckBox, options != null && Boolean.TRUE.equals(options.getPositivesOnly()));
    setFieldValue(leadingZerosAllowedCheckBox, options != null && Boolean.TRUE.equals(options.getLeadingZerosAllowed()));
    setFieldValue(minValueField, options != null && options.getMinValue() != null ? String.valueOf(options.getMinValue()) : "");
    setFieldValue(maxValueField, options != null && options.getMaxValue() != null ? String.valueOf(options.getMaxValue()) : "");
    setFieldValue(maxIntegerDigitsField, options != null && options.getMaxIntegerDigits() != null ? String.valueOf(options.getMaxIntegerDigits()) : "");
    setFieldValue(unitComboBox, options != null ? options.getTrait() : null);
  }

  @Override
  protected String validationProperty() {
    return ElementProperty.DATA_TYPE;
  }

  private static void withNumberTypeOptions(Element element, Consumer<NumberTypeOptions> mutator) {
    getNumberFieldType(element).ifPresent(numberFieldType -> {
      NumberTypeOptions options = numberFieldType.getNumberType();
      if (options == null) {
        options = new NumberTypeOptions();
        numberFieldType.setNumberType(options);
      }
      mutator.accept(options);
    });
  }

  private static Optional<NumberFieldType> getNumberFieldType(Element element) {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof NumberFieldType numberFieldType) {
      return Optional.of(numberFieldType);
    }
    return Optional.empty();
  }

  private static Integer parseInteger(String value) {
    return value.isEmpty() ? null : Integer.valueOf(value);
  }

  private static Double parseDouble(String value) {
    try {
      return value.isEmpty() ? null : Double.valueOf(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
