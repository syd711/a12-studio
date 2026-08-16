package de.a12.studio.ui.editors.formmodel.modelsettings;

import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Edits {@link FormModelContent#getDetachedRepeatCommitButtonEnablement()} (SME's {@code
 * FormModelFrame-form.json} {@code controlgrid-7499b}, "General Detached Repeat Settings"): whether a
 * Detached Repeat's commit button stays enabled, is hidden, or is disabled when no data has changed in its
 * detail screen. The default option ("Show enabled Button") maps to a {@code null} model value (the property
 * is omitted from the saved JSON, matching SME), represented here by {@link #DEFAULT} in the combo box.
 */
public class GeneralDetachedRepeatSettingsPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String DEFAULT = "";

  private static final Map<String, String> LABELS = new LinkedHashMap<>();
  static {
    LABELS.put(DEFAULT, "Show enabled Button");
    LABELS.put("HIDDEN", "Hide Button");
    LABELS.put("DISABLED", "Disable Button");
  }

  @FXML
  private ComboBox<String> commitButtonEnablementCombo;

  private FormModel model;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    commitButtonEnablementCombo.getItems().addAll(LABELS.keySet());
    commitButtonEnablementCombo.setConverter(new StringConverter<>() {
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

    bindComboBox(commitButtonEnablementCombo, (Element element, String value) ->
        getContent().setDetachedRepeatCommitButtonEnablement(DEFAULT.equals(value) ? null : value));
  }

  /** Hides this panel entirely for model types other than {@link FormModel}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull FormModel model) {
    this.model = model;
    String value = getContent().getDetachedRepeatCommitButtonEnablement();
    setFieldValue(commitButtonEnablementCombo, value == null ? DEFAULT : value);
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
