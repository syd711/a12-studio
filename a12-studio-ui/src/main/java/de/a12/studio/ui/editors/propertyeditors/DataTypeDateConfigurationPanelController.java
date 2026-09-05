package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.DateTimeFieldType;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.TimeFieldType;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Shared {@code format} editor for {@link DateFieldType}, {@link DateTimeFieldType} and {@link TimeFieldType} -
 * unlike {@link DataTypeDateRangeConfigurationPanelController}/{@link DataTypeDateFragmentConfigurationPanelController},
 * this format string isn't a small closed set of kernel-recognized tokens (SME's {@code FORMAT_MISSING} rule
 * doesn't document one), so the combo box here is editable free text with a few common presets rather than a
 * fixed, non-editable enumeration.
 */
public class DataTypeDateConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> DATE_PRESETS = List.of("yyyy-MM-dd", "dd.MM.yyyy", "MM/dd/yyyy");
  private static final List<String> DATE_TIME_PRESETS = List.of("yyyy-MM-dd'T'HH:mm:ss", "dd.MM.yyyy HH:mm", "MM/dd/yyyy hh:mm a");
  private static final List<String> TIME_PRESETS = List.of("HH:mm:ss", "HH:mm", "hh:mm a");

  @FXML
  private ComboBox<String> formatComboBox;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);
    formatComboBox.setEditable(true);
    bindComboBox(formatComboBox, (element, value) -> setFormat(element, value == null || value.isEmpty() ? null : value));
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    FieldTypeKind kind = FieldTypeKind.of(element);
    setEditorVisible(kind != null);
    if (kind == null) {
      return;
    }

    setTitle(StudioBundle.get(kind.titleKey));
    formatComboBox.getItems().setAll(kind.presets);

    String format = getFormat(element, kind);
    // A Date/DateTime/Time field without a format is invalid, so default to the first preset as soon as the
    // field is selected instead of leaving the combo box blank until the user manually picks one - same
    // reasoning as DataTypeDateFragmentConfigurationPanelController/DataTypeDateRangeConfigurationPanelController.
    if (format == null || format.isEmpty()) {
      format = kind.presets.get(0);
      setFormat(element, format);
      commitChange();
    }
    setFieldValue(formatComboBox, format);
  }

  @Override
  protected String validationProperty() {
    return ElementProperty.DATA_TYPE;
  }

  private static String getFormat(Element element, FieldTypeKind kind) {
    return kind.formatGetter.apply(fieldType(element));
  }

  private static void setFormat(Element element, String value) {
    FieldTypeKind kind = FieldTypeKind.of(element);
    if (kind != null) {
      kind.formatSetter.accept(fieldType(element), value);
    }
  }

  private static Object fieldType(Element element) {
    return ((FieldElement) element).getField().getFieldType();
  }

  private enum FieldTypeKind {
    DATE("date_type", DATE_PRESETS,
        fieldType -> ((DateFieldType) fieldType).getDateType().getFormat(),
        (fieldType, value) -> ((DateFieldType) fieldType).getDateType().setFormat(value)),
    DATE_TIME("date_time_type", DATE_TIME_PRESETS,
        fieldType -> ((DateTimeFieldType) fieldType).getDateTimeType().getFormat(),
        (fieldType, value) -> ((DateTimeFieldType) fieldType).getDateTimeType().setFormat(value)),
    TIME("time_type", TIME_PRESETS,
        fieldType -> ((TimeFieldType) fieldType).getTimeType().getFormat(),
        (fieldType, value) -> ((TimeFieldType) fieldType).getTimeType().setFormat(value));

    private final String titleKey;
    private final List<String> presets;
    private final Function<Object, String> formatGetter;
    private final BiConsumer<Object, String> formatSetter;

    FieldTypeKind(String titleKey, List<String> presets, Function<Object, String> formatGetter,
        BiConsumer<Object, String> formatSetter) {
      this.titleKey = titleKey;
      this.presets = presets;
      this.formatGetter = formatGetter;
      this.formatSetter = formatSetter;
    }

    static FieldTypeKind of(Element element) {
      if (!(element instanceof FieldElement fieldElement) || fieldElement.getField() == null) {
        return null;
      }
      Object fieldType = fieldElement.getField().getFieldType();
      if (fieldType instanceof DateFieldType) {
        return DATE;
      } else if (fieldType instanceof DateTimeFieldType) {
        return DATE_TIME;
      } else if (fieldType instanceof TimeFieldType) {
        return TIME;
      }
      return null;
    }
  }
}
