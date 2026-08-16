package de.a12.studio.ui.editors.formmodel.modelsettings;

import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
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
 * Edits {@link FormModelContent#getInlineRepeatReadonlyPresentation()} (SME's {@code
 * FormModelFrame-form.json} {@code controlgrid-3b119}, "General Inline Repeat Settings"): the counterpart to
 * {@link GeneralSettingsPanelController}'s Readonly Presentation field, but only for Fields inside an Inline
 * Repeat.
 */
public class GeneralInlineRepeatSettingsPanelController extends AbstractPropertyEditor implements Initializable {

  private static final Map<String, String> LABELS = new LinkedHashMap<>();

  /** Default {@link FormModelContent#getInlineRepeatReadonlyPresentation()} behavior when the field is unset. */
  private static final String READONLY_PRESENTATION_DEFAULT = "INPUT";

  static {
    LABELS.put(READONLY_PRESENTATION_DEFAULT, "input");
    LABELS.put("TEXT", "text output");
  }

  @FXML
  private ComboBox<String> readonlyPresentationCombo;

  private FormModel model;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    readonlyPresentationCombo.getItems().addAll(LABELS.keySet());
    readonlyPresentationCombo.setConverter(new StringConverter<>() {
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
            .orElse(null);
      }
    });

    bindComboBox(readonlyPresentationCombo, (element, value) -> getContent().setInlineRepeatReadonlyPresentation(value));
  }

  /** Hides this panel entirely for model types other than {@link FormModel}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull FormModel model) {
    this.model = model;
    String readonlyPresentation = getContent().getInlineRepeatReadonlyPresentation();
    setFieldValue(readonlyPresentationCombo,
        readonlyPresentation == null ? READONLY_PRESENTATION_DEFAULT : readonlyPresentation);
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
