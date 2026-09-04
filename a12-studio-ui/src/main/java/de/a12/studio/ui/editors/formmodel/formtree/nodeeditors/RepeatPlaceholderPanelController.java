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
 * "Placeholder" property editor for a selected {@link AbstractRepeat} node: two plain
 * {@link LocalizedTextPanelController} sub-editors — "Group Configuration" (model-wide
 * {@link GroupConfigEntry#getPlaceholder()}) and "Repeat" (per-repeat {@link AbstractRepeat#getPlaceholder()}).
 */
public class RepeatPlaceholderPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML private LocalizedTextPanelController groupConfigPlaceholderController;
  @FXML private LocalizedTextPanelController repeatPlaceholderController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    groupConfigPlaceholderController.configureCustom("groupConfigPlaceholder", StudioBundle.get("group_configuration"));
    repeatPlaceholderController.configureCustom("repeatPlaceholder", StudioBundle.get("repeat"));
  }

  public void setRepeat(@NonNull AbstractRepeat repeat, @Nullable FormModelContent content) {
    GroupConfigEntry entry = GroupConfigEntryHelper.findOrCreate(repeat, content);
    groupConfigPlaceholderController.setCustom(() -> texts(entry.getPlaceholder()), () -> ensure(entry));
    repeatPlaceholderController.setCustom(() -> texts(repeat.getPlaceholder()), () -> ensureRepeat(repeat));
  }

  private static List<Label> texts(@Nullable TextContainer c) { return c != null ? c.getText() : List.of(); }
  private static List<Label> ensure(@NonNull GroupConfigEntry e) { if (e.getPlaceholder()==null) e.setPlaceholder(new TextContainer()); return e.getPlaceholder().getText(); }
  private static List<Label> ensureRepeat(@NonNull AbstractRepeat r) { if (r.getPlaceholder()==null) r.setPlaceholder(new TextContainer()); return r.getPlaceholder().getText(); }
}
