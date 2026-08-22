package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * "Additional Settings" property editor for a selected {@link Control} node, mirroring SME's {@code
 * Control-form.json} "Additional Settings" section:
 * <ul>
 *   <li><b>Initial value</b> — the model-wide {@link FieldConfigEntry#getInitialValue()} for the bound field
 *       (a plain free-text override; SME's type-aware enum/date pickers aren't reproduced here).</li>
 *   <li><b>Exposition</b> — the model-wide {@link FieldConfigEntry#getExposition()}, e.g. "COMPACT"/"FULL".</li>
 *   <li><b>Position of Hint and Validation Messages</b> — {@link Control#getMessageExposition()} ("TOOLTIP" or
 *       unset, which renders as a message box).</li>
 *   <li><b>Readonly</b> — {@link Control#getReadonly()}.</li>
 *   <li><b>Readonly presentation</b> — {@link Control#getReadonlyPresentation()}, a per-Control override of the
 *       model-wide {@link FormModelContent#getReadonlyPresentation()} default.</li>
 *   <li><b>Show asterisk</b> — {@link Control#getMarkingOfRequiredFields()}, a per-Control override of the
 *       model-wide {@link FormModelContent#getMarkingOfRequiredFields()} default.</li>
 * </ul>
 * {@code Initial value}/{@code Exposition} live on the model-wide {@link FieldConfigEntry} (found/created via
 * {@link FieldConfigEntryHelper}, matching {@link ControlHintPanelController}'s "Field Configuration" side);
 * the rest are per-Control fields. Not tied to a single {@code Element} (this edits a {@link Cell}, not a
 * document-model {@link de.a12.studio.models.documentmodel.Element}), so it follows the model-header pattern
 * ({@code bindComboBox}/{@code bindCheckBox} ignoring their unused {@code Element} argument), mirroring {@link
 * ControlAccessibilityPanelController}.
 */
public class AdditionalSettingsPanelController extends AbstractPropertyEditor implements Initializable {

  private static final Map<String, String> EXPOSITION_LABELS = new LinkedHashMap<>();
  private static final Map<String, String> MESSAGE_EXPOSITION_LABELS = new LinkedHashMap<>();
  private static final Map<String, String> READONLY_PRESENTATION_LABELS = new LinkedHashMap<>();

  /** Display labels for {@link FormModelContent#getMarkingOfRequiredFields()} values, no "(default)" suffix. */
  private static final Map<String, String> MARKING_OF_REQUIRED_FIELDS_BASE_LABELS = new LinkedHashMap<>();

  private static final String NO_INITIAL_VALUE = "No initial value";

  /** {@link FormModelContent#getMarkingOfRequiredFields()} behavior when unset, mirrors GeneralSettingsPanelController. */
  private static final String MARKING_OF_REQUIRED_FIELDS_DEFAULT = "REQUIRED";

  /** {@link FormModelContent#getReadonlyPresentation()} behavior when unset, mirrors GeneralSettingsPanelController. */
  private static final String READONLY_PRESENTATION_DEFAULT = "INPUT";

  static {
    EXPOSITION_LABELS.put(null, "compact (default)");
    EXPOSITION_LABELS.put("AUTOCOMPLETE", "autocomplete");
    EXPOSITION_LABELS.put("FULL", "full");
    EXPOSITION_LABELS.put("INLINE", "inline");
    EXPOSITION_LABELS.put("BOOLEAN_SELECT", "boolean select");
    EXPOSITION_LABELS.put("CHECKBOX", "checkbox");
    EXPOSITION_LABELS.put("SWITCH", "switch");
    EXPOSITION_LABELS.put("SWITCH_WITH_VALUES", "switch with values");
    EXPOSITION_LABELS.put("THUMBNAIL_OR_ICON", "thumbnail or icon");

    MESSAGE_EXPOSITION_LABELS.put(null, "message box (default)");
    MESSAGE_EXPOSITION_LABELS.put("TOOLTIP", "tooltip");

    READONLY_PRESENTATION_LABELS.put(null, "default (default)");
    READONLY_PRESENTATION_LABELS.put(READONLY_PRESENTATION_DEFAULT, "input");
    READONLY_PRESENTATION_LABELS.put("TEXT", "text output");

    MARKING_OF_REQUIRED_FIELDS_BASE_LABELS.put(MARKING_OF_REQUIRED_FIELDS_DEFAULT, "If required");
    MARKING_OF_REQUIRED_FIELDS_BASE_LABELS.put("NONE", "Never");
    MARKING_OF_REQUIRED_FIELDS_BASE_LABELS.put("ALWAYS", "Always");
  }

  @FXML
  private ComboBox<String> initialValueCombo;
  @FXML
  private ComboBox<String> expositionCombo;
  @FXML
  private ComboBox<String> messagePositionCombo;
  @FXML
  private CheckBox readonlyCheckBox;
  @FXML
  private ComboBox<String> readonlyPresentationCombo;
  @FXML
  private Label readonlyPresentationHintLabel;
  @FXML
  private ComboBox<String> markingOfRequiredFieldsCombo;

  private Control control;
  private FormModelContent content;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    initialValueCombo.setEditable(true);
    initialValueCombo.getItems().setAll(NO_INITIAL_VALUE);
    initialValueCombo.setConverter(freeTextConverter());

    expositionCombo.getItems().setAll(EXPOSITION_LABELS.keySet());
    expositionCombo.setConverter(displayConverter(EXPOSITION_LABELS));

    messagePositionCombo.getItems().setAll(MESSAGE_EXPOSITION_LABELS.keySet());
    messagePositionCombo.setConverter(displayConverter(MESSAGE_EXPOSITION_LABELS));

    readonlyPresentationCombo.getItems().setAll(READONLY_PRESENTATION_LABELS.keySet());
    readonlyPresentationCombo.setConverter(displayConverter(READONLY_PRESENTATION_LABELS));

    bindComboBox(initialValueCombo, (el, value) ->
        entry().setInitialValue(NO_INITIAL_VALUE.equals(value) || value == null || value.isBlank() ? null : value));
    bindComboBox(expositionCombo, (el, value) -> entry().setExposition(value));
    bindComboBox(messagePositionCombo, (el, value) -> control.setMessageExposition(value));
    bindCheckBox(readonlyCheckBox, (el, value) -> control.setReadonly(value ? Boolean.TRUE : null));
    bindComboBox(readonlyPresentationCombo, (el, value) -> control.setReadonlyPresentation(value));
    bindComboBox(markingOfRequiredFieldsCombo, (el, value) -> control.setMarkingOfRequiredFields(value));
  }

  public void setControl(@NonNull Control control, @Nullable FormModelContent content) {
    this.control = control;
    this.content = content;

    FieldConfigEntry entry = entry();
    setFieldValue(initialValueCombo, entry.getInitialValue() == null ? NO_INITIAL_VALUE : entry.getInitialValue());
    setFieldValue(expositionCombo, entry.getExposition());

    setFieldValue(messagePositionCombo, control.getMessageExposition());
    setFieldValue(readonlyCheckBox, Boolean.TRUE.equals(control.getReadonly()));
    setFieldValue(readonlyPresentationCombo, control.getReadonlyPresentation());

    String resolvedReadonlyPresentation = content != null && content.getReadonlyPresentation() != null
        ? content.getReadonlyPresentation() : READONLY_PRESENTATION_DEFAULT;
    readonlyPresentationHintLabel.setText(StudioBundle.get("additional_settings_readonly_presentation_default_hint",
        READONLY_PRESENTATION_LABELS.getOrDefault(resolvedReadonlyPresentation, resolvedReadonlyPresentation)));

    markingOfRequiredFieldsCombo.setConverter(displayConverter(markingOfRequiredFieldsLabels()));
    List<String> markingOfRequiredFieldsItems = new ArrayList<>();
    markingOfRequiredFieldsItems.add(null);
    markingOfRequiredFieldsItems.addAll(MARKING_OF_REQUIRED_FIELDS_BASE_LABELS.keySet());
    setComboBoxItems(markingOfRequiredFieldsCombo, markingOfRequiredFieldsItems);
    setFieldValue(markingOfRequiredFieldsCombo, control.getMarkingOfRequiredFields());
  }

  private Map<String, String> markingOfRequiredFieldsLabels() {
    String resolved = content != null && content.getMarkingOfRequiredFields() != null
        ? content.getMarkingOfRequiredFields() : MARKING_OF_REQUIRED_FIELDS_DEFAULT;
    Map<String, String> labels = new LinkedHashMap<>();
    labels.put(null, MARKING_OF_REQUIRED_FIELDS_BASE_LABELS.getOrDefault(resolved, resolved) + " (default)");
    labels.putAll(MARKING_OF_REQUIRED_FIELDS_BASE_LABELS);
    return labels;
  }

  private FieldConfigEntry entry() {
    return FieldConfigEntryHelper.findOrCreate(control, content);
  }

  private static StringConverter<String> displayConverter(Map<String, String> labels) {
    return new StringConverter<>() {
      @Override
      public String toString(String value) {
        return labels.getOrDefault(value, value);
      }

      @Override
      public String fromString(String displayName) {
        return labels.entrySet().stream()
            .filter(entry -> entry.getValue().equals(displayName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
      }
    };
  }

  private static StringConverter<String> freeTextConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null || value.isBlank() ? NO_INITIAL_VALUE : value;
      }

      @Override
      public String fromString(String displayName) {
        return NO_INITIAL_VALUE.equals(displayName) ? null : displayName;
      }
    };
  }
}
