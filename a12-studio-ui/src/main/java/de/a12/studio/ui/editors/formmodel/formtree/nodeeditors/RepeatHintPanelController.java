package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.GroupConfigEntry;
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
 * "Hint" property editor for a selected {@link AbstractRepeat} node: two plain
 * {@link LocalizedTextPanelController} sub-editors — "Group Configuration" (model-wide
 * {@link GroupConfigEntry#getHint()}) and "Repeat" (per-repeat {@link AbstractRepeat#getHint()} override).
 */
public class RepeatHintPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML private LocalizedTextPanelController groupConfigHintController;
  @FXML private LocalizedTextPanelController repeatHintController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    groupConfigHintController.configureCustom("groupConfigHint", StudioBundle.get("group_configuration"));
    repeatHintController.configureCustom("repeatHint", StudioBundle.get("repeat"));
  }

  public void setRepeat(@NonNull AbstractRepeat repeat, @Nullable FormModelContent content) {
    GroupConfigEntry entry = GroupConfigEntryHelper.findOrCreate(repeat, content);
    groupConfigHintController.setCustom(() -> texts(entry.getHint()), () -> ensure(entry));
    repeatHintController.setCustom(() -> texts(repeat.getHint()), () -> ensureRepeat(repeat));
  }

  private static List<Label> texts(@Nullable TextContainer c) { return c != null ? c.getText() : List.of(); }
  private static List<Label> ensure(@NonNull GroupConfigEntry e) { if (e.getHint()==null) e.setHint(new TextContainer()); return e.getHint().getText(); }
  private static List<Label> ensureRepeat(@NonNull AbstractRepeat r) { if (r.getHint()==null) r.setHint(new TextContainer()); return r.getHint().getText(); }
}
