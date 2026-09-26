package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.editors.contentmodel.ContentReferences;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

/**
 * What a Message Group Container collects by itself while it collects automatically (SME's "Automatically Collected
 * Fields / Groups"): the fields or the groups, by path, of the form elements below it. Read-only; empty and hidden
 * when there is nothing to show. Which of the two it shows is set with {@code groups}.
 */
public class CollectedRow extends SettingRow {

  private final VBox lines = new VBox(2);

  private boolean groups;

  public CollectedRow() {
    addBelow(lines);
  }

  public boolean isGroups() {
    return groups;
  }

  public void setGroups(boolean groups) {
    this.groups = groups;
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    lines.getChildren().clear();
    ContentReferences references = context() != null ? context().references() : null;
    if (references == null || !references.isBound()) {
      return;
    }
    ContentReferences.Collected collected = references.autoCollected(props.getElement());
    for (ContentReferences.Choice choice : groups ? collected.groups() : collected.fields()) {
      Label line = new Label(choice.label());
      line.getStyleClass().add("content-setting-hint");
      lines.getChildren().add(line);
    }
    if (lines.getChildren().isEmpty()) {
      Label none = new Label(StudioBundle.get("content_settings.mgc_nothing_collected"));
      none.getStyleClass().add("content-setting-hint");
      lines.getChildren().add(none);
    }
  }
}
