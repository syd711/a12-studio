package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import javafx.scene.control.Label;
import org.jspecify.annotations.NonNull;

/** An explanation in a panel, like the info texts of SME's settings: a wrapped hint over the full row width, no setting. */
public class NoteRow extends SettingRow {

  private final Label note = new Label();

  public NoteRow() {
    note.getStyleClass().add("content-setting-hint");
    note.setWrapText(true);
    addBelow(note);
  }

  public String getText() {
    return note.getText();
  }

  public void setText(String text) {
    note.setText(text);
  }

  @Override
  protected void load(@NonNull ContentProps props) {
  }
}
