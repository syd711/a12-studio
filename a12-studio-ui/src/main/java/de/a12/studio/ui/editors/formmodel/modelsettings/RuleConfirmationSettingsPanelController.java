package de.a12.studio.ui.editors.formmodel.modelsettings;

import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Edits {@link FormModelContent#getDisableRuleConfirmation()} and {@link
 * FormModelContent#getHideConfirmationSummary()} (SME's {@code FormModelFrame-form.json} {@code
 * controlgrid_b50f7}, "Rule Confirmation Settings"). The combo box's default option ("always show
 * confirmation") maps to a {@code null} model value (omitted from the saved JSON), represented here by
 * {@link #DEFAULT}, mirroring {@link GeneralDetachedRepeatSettingsPanelController}. The checkbox is the
 * inverse of the underlying property, matching SME's {@code technicalField_showConfirmationSummary} (checked
 * == {@code hideConfirmationSummary} unset, unchecked == {@code true}).
 */
public class RuleConfirmationSettingsPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String DEFAULT = "";

  private static final Map<String, String> LABELS = new LinkedHashMap<>();
  static {
    LABELS.put(DEFAULT, "always show confirmation");
    LABELS.put("INFO", "info only");
    LABELS.put("WARNING", "info and warning");
  }

  @FXML
  private ComboBox<String> disableRuleConfirmationCombo;

  @FXML
  private CheckBox showConfirmationSummaryCheckBox;

  private FormModel model;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    disableRuleConfirmationCombo.getItems().addAll(LABELS.keySet());
    disableRuleConfirmationCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null ? "" : LABELS.getOrDefault(value, value);
      }

      @Override
      public String fromString(String displayName) {
        return LABELS.entrySet().stream()
            .filter(entry -> entry.getValue().equals(displayName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(DEFAULT);
      }
    });

    bindComboBox(disableRuleConfirmationCombo, (element, value) ->
        getContent().setDisableRuleConfirmation(DEFAULT.equals(value) ? null : value));
    bindCheckBox(showConfirmationSummaryCheckBox, (element, selected) ->
        getContent().setHideConfirmationSummary(selected ? null : Boolean.TRUE));
  }

  /** Hides this panel entirely for model types other than {@link FormModel}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull FormModel model) {
    this.model = model;
    String value = getContent().getDisableRuleConfirmation();
    setFieldValue(disableRuleConfirmationCombo, value == null ? DEFAULT : value);
    setFieldValue(showConfirmationSummaryCheckBox, !Boolean.TRUE.equals(getContent().getHideConfirmationSummary()));
  }

  private FormModelContent getContent() {
    FormModelContent content = model.getContent();
    if (content == null) {
      content = new FormModelContent();
      model.setContent(content);
    }
    return content;
  }
}
