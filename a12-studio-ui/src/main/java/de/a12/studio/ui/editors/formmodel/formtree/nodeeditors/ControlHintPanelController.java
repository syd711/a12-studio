package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Hint" property editor for a selected {@link Control} node: two plain {@link LocalizedTextPanelController}
 * sub-editors (per-locale text only, no expression support):
 * <ul>
 *   <li><b>Field Configuration</b> — the model-wide {@link FieldConfigEntry#getHint()} for the bound field.</li>
 *   <li><b>Control</b> — the per-Control {@link Control#getHint()} override.</li>
 * </ul>
 */
public class ControlHintPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private LocalizedTextPanelController fieldConfigHintController;

  @FXML
  private LocalizedTextPanelController controlHintController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    fieldConfigHintController.configureCustom("fieldConfigHint", StudioBundle.get("field_configuration"));
    controlHintController.configureCustom("controlHint", StudioBundle.get("control"));
  }

  public void setControl(@NonNull Control control, @Nullable FormModelContent content) {
    FieldConfigEntry entry = FieldConfigEntryHelper.findOrCreate(control, content);

    fieldConfigHintController.setCustom(
        () -> texts(entry.getHint()),
        () -> ensureHint(entry).getText());

    controlHintController.setCustom(
        () -> texts(control.getHint()),
        () -> ensureControlHint(control).getText());
  }

  private static List<Label> texts(@Nullable TextContainer c) {
    return c != null ? c.getText() : List.of();
  }

  private static TextContainer ensureHint(@NonNull FieldConfigEntry entry) {
    if (entry.getHint() == null) {
      entry.setHint(new TextContainer());
    }
    return entry.getHint();
  }

  private static TextContainer ensureControlHint(@NonNull Control control) {
    if (control.getHint() == null) {
      control.setHint(new TextContainer());
    }
    return control.getHint();
  }
}
