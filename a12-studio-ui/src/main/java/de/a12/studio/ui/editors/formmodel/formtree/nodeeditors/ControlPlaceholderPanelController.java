package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Placeholder" property editor for a selected {@link Control} node: two plain
 * {@link LocalizedTextPanelController} sub-editors (per-locale text only):
 * <ul>
 *   <li><b>Field Configuration</b> — the model-wide {@link FieldConfigEntry#getPlaceholder()} for the bound
 *       field, applying to every Control that references it unless overridden.</li>
 *   <li><b>Control</b> — the per-Control {@link Control#getPlaceholder()} override.</li>
 * </ul>
 */
public class ControlPlaceholderPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private LocalizedTextPanelController fieldConfigPlaceholderController;

  @FXML
  private LocalizedTextPanelController controlPlaceholderController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    fieldConfigPlaceholderController.configureCustom("fieldConfigPlaceholder", "Field Configuration");
    controlPlaceholderController.configureCustom("controlPlaceholder", "Control");
  }

  public void setControl(@NonNull Control control, @Nullable FormModelContent content) {
    FieldConfigEntry entry = FieldConfigEntryHelper.findOrCreate(control, content);

    fieldConfigPlaceholderController.setCustom(
        () -> texts(entry.getPlaceholder()),
        () -> ensureEntryPlaceholder(entry).getText());

    controlPlaceholderController.setCustom(
        () -> texts(control.getPlaceholder()),
        () -> ensureControlPlaceholder(control).getText());
  }

  private static List<Label> texts(@Nullable TextContainer c) {
    return c != null ? c.getText() : List.of();
  }

  private static TextContainer ensureEntryPlaceholder(@NonNull FieldConfigEntry entry) {
    if (entry.getPlaceholder() == null) {
      entry.setPlaceholder(new TextContainer());
    }
    return entry.getPlaceholder();
  }

  private static TextContainer ensureControlPlaceholder(@NonNull Control control) {
    if (control.getPlaceholder() == null) {
      control.setPlaceholder(new TextContainer());
    }
    return control.getPlaceholder();
  }
}
