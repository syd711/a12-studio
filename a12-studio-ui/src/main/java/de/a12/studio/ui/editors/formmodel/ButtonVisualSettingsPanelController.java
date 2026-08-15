package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.Button;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Edits a {@link Button}'s {@code buttonStyling.priority}, {@code buttonStyling.icon}, {@code
 * buttonStyling.destructive} and {@code buttonStyling.labelHidden} - SME's {@code I_Button-form.json} "Visual
 * Settings" section (the {@code label}/{@code description}/{@code style} parts of {@code buttonStyling} are
 * edited by the dialog's separate Label/Description/Styles panels instead). Embedded in {@link
 * de.a12.studio.ui.editors.formmodel.dialogs.FormButtonDialogController}. Not tied to a single {@code Element},
 * so it follows the model-header pattern (a plain {@link #setButton} entry point) rather than {@link
 * #setElement}.
 */
public class ButtonVisualSettingsPanelController extends AbstractPropertyEditor implements Initializable {

  private static final Map<String, String> PRIORITY_LABELS = new LinkedHashMap<>();
  static {
    PRIORITY_LABELS.put("SECONDARY", "Secondary (default)");
    PRIORITY_LABELS.put("PRIMARY", "Primary");
  }

  @FXML
  private ComboBox<String> priorityCombo;

  @FXML
  private TextField iconField;

  @FXML
  private CheckBox destructiveField;

  @FXML
  private CheckBox hideLabelField;

  private Button button;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    priorityCombo.getItems().addAll(PRIORITY_LABELS.keySet());
    priorityCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return PRIORITY_LABELS.getOrDefault(value, value);
      }

      @Override
      public String fromString(String displayName) {
        return PRIORITY_LABELS.entrySet().stream()
            .filter(entry -> entry.getValue().equals(displayName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse("SECONDARY");
      }
    });
    bindComboBox(priorityCombo, (el, value) -> button.setPrimary("PRIMARY".equals(value)));

    bindTextField(iconField, (el, value) -> button.setIconName(value.isEmpty() ? null : value));
    bindCheckBox(destructiveField, (el, value) -> button.setDestructive(value ? Boolean.TRUE : null));
    bindCheckBox(hideLabelField, (el, value) -> button.getOrCreateButtonStyling().setLabelHidden(value ? Boolean.TRUE : null));
  }

  public void setButton(@NonNull Button button) {
    this.button = button;
    setFieldValue(priorityCombo, Boolean.TRUE.equals(button.getPrimary()) ? "PRIMARY" : "SECONDARY");
    setFieldValue(iconField, button.getIconName());
    setFieldValue(destructiveField, Boolean.TRUE.equals(button.getDestructive()));
    setFieldValue(hideLabelField, button.getButtonStyling() != null && Boolean.TRUE.equals(button.getButtonStyling().getLabelHidden()));
  }
}
