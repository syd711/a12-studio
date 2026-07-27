package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.documentmodel.CustomFieldFieldType;
import de.a12.studio.models.documentmodel.CustomFieldTypeOptions;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class DataTypeCustomConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField nameField;

  @FXML
  private TextField displayNameField;

  @FXML
  private TextField minLengthField;

  @FXML
  private TextField maxLengthField;

  @FXML
  private GridPane lengthGrid;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);

    lengthGrid.managedProperty().bindBidirectional(lengthGrid.visibleProperty());

    WidgetFactory.restrictToNumericInput(minLengthField);
    WidgetFactory.restrictToNumericInput(maxLengthField);

    bindTextField(nameField, (element, value) -> withCustomFieldTypeOptions(element, options -> options.setName(value.isEmpty() ? null : value)));
    bindTextField(displayNameField, (element, value) -> withCustomFieldTypeOptions(element, options -> options.setDisplayName(value.isEmpty() ? null : value)));
    bindTextField(minLengthField, (element, value) -> withCustomFieldTypeOptions(element, options -> options.setMinLength(parseInteger(value))));
    bindTextField(maxLengthField, (element, value) -> withCustomFieldTypeOptions(element, options -> options.setMaxLength(parseInteger(value))));
  }

  /**
   * Min./Max. Length constrain the runtime value entered for the custom field, which has no equivalent when
   * this panel configures a {@link de.a12.studio.models.documentmodel.TypeDefinition} itself, so the type
   * definition editor hides this section entirely.
   */
  public void hideLengthGrid() {
    lengthGrid.setVisible(false);
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    CustomFieldTypeOptions options = getCustomFieldFieldType(element).map(CustomFieldFieldType::getCustomFieldType).orElse(null);
    setFieldValue(nameField, options != null && options.getName() != null ? options.getName() : "");
    setFieldValue(displayNameField, options != null && options.getDisplayName() != null ? options.getDisplayName() : "");
    setFieldValue(minLengthField, options != null && options.getMinLength() != null ? String.valueOf(options.getMinLength()) : "");
    setFieldValue(maxLengthField, options != null && options.getMaxLength() != null ? String.valueOf(options.getMaxLength()) : "");
  }

  @Override
  protected String validationProperty() {
    return ElementProperty.DATA_TYPE;
  }

  private static void withCustomFieldTypeOptions(Element element, Consumer<CustomFieldTypeOptions> mutator) {
    getCustomFieldFieldType(element).ifPresent(customFieldFieldType -> {
      CustomFieldTypeOptions options = customFieldFieldType.getCustomFieldType();
      if (options == null) {
        options = new CustomFieldTypeOptions();
        customFieldFieldType.setCustomFieldType(options);
      }
      mutator.accept(options);
    });
  }

  private static Optional<CustomFieldFieldType> getCustomFieldFieldType(Element element) {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof CustomFieldFieldType customFieldFieldType) {
      return Optional.of(customFieldFieldType);
    }
    return Optional.empty();
  }

  private static Integer parseInteger(String value) {
    return value.isEmpty() ? null : Integer.valueOf(value);
  }
}
