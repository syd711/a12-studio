package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * "Label" property editor for a selected {@link Control} node: two {@link LocalizedTextTypePanelController}
 * sub-editors (both supporting per-locale text and expression values via the type-switcher combo):
 * <ul>
 *   <li><b>Field Configuration</b> — the model-wide {@link FieldConfigEntry#getLabel()} for the bound field,
 *       stored in {@link FormModelContent#getFieldConfiguration()}, applying to every Control that references
 *       this field unless a Control-level override is set.</li>
 *   <li><b>Control</b> — the per-Control {@link Control#getLabel()} override, which takes precedence.</li>
 * </ul>
 */
public class ControlLabelPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private LocalizedTextTypePanelController fieldConfigLabelController;

  @FXML
  private LocalizedTextTypePanelController controlLabelController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    fieldConfigLabelController.configureCustom("fieldConfigLabel", "Field Configuration");
    controlLabelController.configureCustom("controlLabel", "Control");
  }

  public void setControl(@NonNull Control control, @Nullable FormModelContent content) {
    FieldConfigEntry entry = FieldConfigEntryHelper.findOrCreate(control, content);
    fieldConfigLabelController.setCustom(entry::getLabel, entry::setLabel);
    controlLabelController.setCustom(control::getLabel, control::setLabel);
  }
}
