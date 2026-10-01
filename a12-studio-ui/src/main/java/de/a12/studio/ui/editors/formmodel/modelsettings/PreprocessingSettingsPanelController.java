package de.a12.studio.ui.editors.formmodel.modelsettings;

import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Edits {@link FormModelContent#getOpenNewDocumentPreProcessing()} and {@link
 * FormModelContent#getOpenExistingDocumentPreProcessing()} (SME's {@code FormModelFrame.json} type definition
 * {@code OpenDocumentPreProcessing}: {@code NONE}, {@code COMPUTATIONS}, {@code COMPUTATIONS_AND_DEPENDENCIES}).
 * "No preprocessing" is the default and maps to a {@code null} model value (omitted from the saved JSON),
 * represented here by {@link #DEFAULT}, mirroring {@link RuleConfirmationSettingsPanelController}.
 */
public class PreprocessingSettingsPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String DEFAULT = "";

  private static final Map<String, String> LABEL_KEYS = new LinkedHashMap<>();
  static {
    LABEL_KEYS.put(DEFAULT, "preprocessing.none");
    LABEL_KEYS.put("COMPUTATIONS", "preprocessing.computations");
    LABEL_KEYS.put("COMPUTATIONS_AND_DEPENDENCIES", "preprocessing.computations_and_dependencies");
  }

  @FXML
  private ComboBox<String> newDocumentCombo;

  @FXML
  private ComboBox<String> existingDocumentCombo;

  private FormModel model;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    for (ComboBox<String> combo : List.of(newDocumentCombo, existingDocumentCombo)) {
      combo.getItems().addAll(LABEL_KEYS.keySet());
      combo.setConverter(new StringConverter<>() {
        @Override
        public String toString(String value) {
          String key = LABEL_KEYS.get(value == null ? DEFAULT : value);
          return key != null ? StudioBundle.get(key) : value;
        }

        @Override
        public String fromString(String displayName) {
          return LABEL_KEYS.entrySet().stream()
              .filter(entry -> StudioBundle.get(entry.getValue()).equals(displayName))
              .map(Map.Entry::getKey)
              .findFirst()
              .orElse(DEFAULT);
        }
      });
    }

    bindComboBox(newDocumentCombo, (element, value) ->
        getContent().setOpenNewDocumentPreProcessing(emptyToNull(value)));
    bindComboBox(existingDocumentCombo, (element, value) ->
        getContent().setOpenExistingDocumentPreProcessing(emptyToNull(value)));
  }

  /** Hides this panel entirely for model types other than {@link FormModel}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull FormModel model) {
    this.model = model;
    String newValue = getContent().getOpenNewDocumentPreProcessing();
    String existingValue = getContent().getOpenExistingDocumentPreProcessing();
    setFieldValue(newDocumentCombo, newValue == null ? DEFAULT : newValue);
    setFieldValue(existingDocumentCombo, existingValue == null ? DEFAULT : existingValue);
  }

  private static String emptyToNull(String value) {
    return value == null || DEFAULT.equals(value) || "NONE".equals(value) ? null : value;
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
