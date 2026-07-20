package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.StringTypeOptions;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class DataTypeConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

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

    StringTypeOptions options = getStringFieldType(element).map(StringFieldType::getStringType).orElse(null);
    setFieldValue(minLengthField, options != null && options.getMinLength() != null ? String.valueOf(options.getMinLength()) : "");
    setFieldValue(maxLengthField, options != null && options.getMaxLength() != null ? String.valueOf(options.getMaxLength()) : "");
    setFieldValue(patternField, options != null && options.getPattern() != null ? options.getPattern() : "");
    setFieldValue(lineBreaksCheckBox, options != null && Boolean.TRUE.equals(options.getLineBreaksPermitted()));
    setFieldValue(alphabeticalSortingCheckBox, options != null && Boolean.TRUE.equals(options.getAlphabeticalSorting()));
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
