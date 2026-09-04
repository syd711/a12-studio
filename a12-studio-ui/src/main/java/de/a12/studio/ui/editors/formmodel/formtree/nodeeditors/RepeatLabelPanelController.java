package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * "Label" property editor for a selected {@link AbstractRepeat} node: two {@link LocalizedTextTypePanelController}
 * sub-editors — "Group Configuration" (the model-wide {@link GroupConfigEntry#getLabel()} keyed by
 * {@link AbstractRepeat#getGroupRef()}) and "Repeat" (the per-repeat {@link AbstractRepeat#getLabel()} override).
 * Both support per-locale text and expression values.
 */
public class RepeatLabelPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private LocalizedTextTypePanelController groupConfigLabelController;

  @FXML
  private LocalizedTextTypePanelController repeatLabelController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    groupConfigLabelController.configureCustom("groupConfigLabel", StudioBundle.get("group_configuration"));
    repeatLabelController.configureCustom("repeatLabel", StudioBundle.get("repeat"));
  }

  public void setRepeat(@NonNull AbstractRepeat repeat, @Nullable FormModelContent content) {
    GroupConfigEntry entry = GroupConfigEntryHelper.findOrCreate(repeat, content);
    groupConfigLabelController.setCustom(entry::getLabel, entry::setLabel);
    repeatLabelController.setCustom(repeat::getLabel, repeat::setLabel);
  }
}
