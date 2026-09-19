package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.ButtonStyling;
import de.a12.studio.ui.components.IconComboController;
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
import java.util.function.Supplier;

/**
 * Edits a {@link Button}'s {@code buttonStyling.priority}, {@code buttonStyling.icon}, {@code
 * buttonStyling.destructive} and {@code buttonStyling.labelHidden} - SME's {@code I_Button-form.json} "Visual
 * Settings" section (the {@code label}/{@code description}/{@code style} parts of {@code buttonStyling} are
 * edited by the dialog's separate Label/Description/Styles panels instead). Embedded in {@link
 * de.a12.studio.ui.editors.formmodel.dialogs.FormButtonDialogController}. Not tied to a single {@code Element},
 * so it follows the model-header pattern (a plain {@link #setButton} entry point) rather than {@link
 * #setElement}.
 * <p>
 * The panel really edits a {@link ButtonStyling}, which is what a {@link Button} and a Row Action ({@link
 * de.a12.studio.models.formmodel.RowAction}, SME's {@code I_SectionButtonStyling-form.json}) both carry, so
 * {@link #setStyling} takes it directly; {@link #setButton} is the shortcut for the Button case. Like {@link
 * StylesPanelController#setCustom}, {@code reader} must tolerate the styling not existing yet (return {@code
 * null}) and {@code writer} may lazily create it.
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
  private IconComboController iconComboController;

  @FXML
  private CheckBox destructiveField;

  @FXML
  private CheckBox hideLabelField;

  private Supplier<ButtonStyling> reader;

  private Supplier<ButtonStyling> writer;

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
    bindComboBox(priorityCombo, (el, value) -> writer.get().setPriority("PRIMARY".equals(value) ? "PRIMARY" : "SECONDARY"));

    iconComboController.setOnChange(name -> {
      writer.get().setIconName(name);
      commitHeaderChange();
    });
    bindCheckBox(destructiveField, (el, value) -> writer.get().setDestructive(value ? Boolean.TRUE : null));
    bindCheckBox(hideLabelField, (el, value) -> writer.get().setLabelHidden(value ? Boolean.TRUE : null));
  }

  public void setButton(@NonNull Button button) {
    setStyling(button::getButtonStyling, button::getOrCreateButtonStyling);
  }

  public void setStyling(@NonNull Supplier<ButtonStyling> reader, @NonNull Supplier<ButtonStyling> writer) {
    this.reader = reader;
    this.writer = writer;
    ButtonStyling styling = reader.get();
    setFieldValue(priorityCombo, styling != null && "PRIMARY".equals(styling.getPriority()) ? "PRIMARY" : "SECONDARY");
    iconComboController.setValue(styling != null ? styling.getIconName() : null);
    setFieldValue(destructiveField, styling != null && Boolean.TRUE.equals(styling.getDestructive()));
    setFieldValue(hideLabelField, styling != null && Boolean.TRUE.equals(styling.getLabelHidden()));
  }

}
