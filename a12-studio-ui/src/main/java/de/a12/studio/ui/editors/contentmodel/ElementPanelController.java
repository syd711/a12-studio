package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.jspecify.annotations.NonNull;

/** The identity of the selected element: its (read-only) id. */
public class ElementPanelController extends AbstractContentSettingsPanel {

  @FXML
  private TextField elementIdField;

  @FXML
  private void onCopyId(ActionEvent event) {
    ClipboardContent content = new ClipboardContent();
    content.putString(elementIdField.getText());
    Clipboard.getSystemClipboard().setContent(content);
  }

  @Override
  protected boolean appliesTo(String type) {
    return true;
  }

  @Override
  protected void populate(@NonNull ContentElement element) {
    elementIdField.setText(element.getId() != null ? element.getId() : "");
  }
}
