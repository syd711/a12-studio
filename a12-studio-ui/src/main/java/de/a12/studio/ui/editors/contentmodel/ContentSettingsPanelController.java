package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.editors.contentmodel.fields.SettingGroup;
import de.a12.studio.ui.editors.contentmodel.fields.SettingRow;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The controller of every purely declarative property panel: the FXML lists {@link SettingRow}s (optionally inside
 * {@link SettingGroup}s that scope them to element types) and this class shows the selected element's values in
 * them and shows only the rows/groups that apply to its type. The panel appears for a type when at least one of its
 * rows does. Rows may also depend on other props ({@code showWhen}/{@code enabledWhen}), re-evaluated after every
 * edit. Panels needing behavior of their own (lists, dependent value ranges) have controllers of their own.
 */
public class ContentSettingsPanelController extends AbstractContentSettingsPanel {

  private final List<Entry> entries = new ArrayList<>();

  /** A row, with the group it sits in (if any); the group's types apply unless the row declares its own. */
  private record Entry(SettingRow row, SettingGroup group) {
    boolean appliesTo(String type) {
      if (group != null && !group.appliesTo(type)) {
        return false;
      }
      return row.appliesTo(type);
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    collect(getRootPane().getContent(), null);
    entries.forEach(entry -> entry.row().setOnEdit(this::changed));
  }

  private void collect(Node node, SettingGroup group) {
    if (node instanceof SettingRow row) {
      entries.add(new Entry(row, group));
      return;
    }
    SettingGroup nextGroup = node instanceof SettingGroup settingGroup ? settingGroup : group;
    if (node instanceof Parent parent) {
      parent.getChildrenUnmodifiable().forEach(child -> collect(child, nextGroup));
    }
  }

  @Override
  protected boolean appliesTo(String type) {
    return entries.stream().anyMatch(entry -> entry.appliesTo(type));
  }

  @Override
  protected void populate(@NonNull ContentElement element) {
    ContentProps props = new ContentProps(element);
    String type = element.getType();
    updateFromModel(() -> {
      for (Entry entry : entries) {
        if (entry.appliesTo(type)) {
          entry.row().show(props);
        }
        else {
          entry.row().clear();
        }
      }
    });
    applyConditions();
  }

  @Override
  protected void changed() {
    applyConditions();
    super.changed();
  }

  /** Shows/hides and enables/disables the rows whose {@code showWhen}/{@code enabledWhen} depend on other props. */
  protected void applyConditions() {
    ContentElement element = currentElement();
    if (element == null) {
      return;
    }
    ContentProps props = new ContentProps(element);
    String type = element.getType();
    for (Entry entry : entries) {
      SettingRow row = entry.row();
      boolean shown = entry.appliesTo(type) && row.isShownFor(props);
      row.setVisible(shown);
      row.setManaged(shown);
      row.setDisable(!row.isEnabledFor(props));
      if (entry.group() != null) {
        boolean groupShown = entry.group().appliesTo(type);
        entry.group().setVisible(groupShown);
        entry.group().setManaged(groupShown);
      }
    }
  }
}
