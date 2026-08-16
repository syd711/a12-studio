package de.a12.studio.ui.editors.propertyeditors;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import org.jspecify.annotations.NonNull;

/**
 * Same as {@link LocalizedTextPanelController}, but edits each locale's text in a {@link TextArea} instead of
 * a {@link javafx.scene.control.TextField}, for texts that are expected to span multiple lines.
 */
public class LocalizedTextAreaPanelController extends LocalizedTextPanelController {

  @Override
  protected TextInputControl createLocaleField(@NonNull String id) {
    TextArea textArea = new TextArea();
    textArea.setId(id);
    textArea.setPrefRowCount(3);
    textArea.setWrapText(true);
    return textArea;
  }
}
