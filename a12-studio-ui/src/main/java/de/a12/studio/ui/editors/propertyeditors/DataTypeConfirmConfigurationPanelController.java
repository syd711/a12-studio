package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.ConfirmFieldType;
import de.a12.studio.models.documentmodel.ConfirmTypeOptions;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Edits a {@link ConfirmFieldType}'s only option, {@link ConfirmTypeOptions#getNotInDCustomTrueValue()}. Note:
 * unlike SME's Confirm field type (which SME's own meta-model validates as a true/false pair, per
 * {@code TRUE_FALSE_EQUALS}/{@code TRUE_FALSE_INVALID}), a12-studio's {@link ConfirmTypeOptions} only models this
 * one field - there is no {@code trueValue}/{@code falseValue} pair in this codebase's data model to expose here;
 * confirmed by reading {@link ConfirmFieldType}/{@link ConfirmTypeOptions} directly, not assumed from SME parity.
 */
public class DataTypeConfirmConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField customTrueValueField;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);
    bindTextField(customTrueValueField, (element, value) ->
        withConfirmTypeOptions(element, options -> options.setNotInDCustomTrueValue(value == null || value.isBlank() ? null : value)));
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);
    ConfirmTypeOptions options = getConfirmFieldType(element).map(ConfirmFieldType::getConfirmType).orElse(null);
    setFieldValue(customTrueValueField, options != null && options.getNotInDCustomTrueValue() != null ? options.getNotInDCustomTrueValue() : "");
  }

  @Override
  protected String validationProperty() {
    return ElementProperty.DATA_TYPE;
  }

  private static void withConfirmTypeOptions(Element element, Consumer<ConfirmTypeOptions> mutator) {
    getConfirmFieldType(element).ifPresent(confirmFieldType -> {
      ConfirmTypeOptions options = confirmFieldType.getConfirmType();
      if (options == null) {
        options = new ConfirmTypeOptions();
        confirmFieldType.setConfirmType(options);
      }
      mutator.accept(options);
    });
  }

  private static Optional<ConfirmFieldType> getConfirmFieldType(Element element) {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof ConfirmFieldType confirmFieldType) {
      return Optional.of(confirmFieldType);
    }
    return Optional.empty();
  }
}
