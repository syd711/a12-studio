package de.a12.studio.ui.preferences;

import de.a12.studio.ui.StudioKeyEventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class PreferenceShortcutsController implements Initializable {

  @FXML
  private GridPane shortcutsGrid;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    int row = 0;
    for (StudioKeyEventHandler.Shortcut shortcut : StudioKeyEventHandler.SHORTCUTS) {
      Label keyLabel = new Label(shortcut.keys());
      keyLabel.getStyleClass().add("shortcut-key");

      Label descriptionLabel = new Label(shortcut.description());
      descriptionLabel.getStyleClass().add("shortcut-description");

      shortcutsGrid.addRow(row++, keyLabel, descriptionLabel);
    }
  }
}
