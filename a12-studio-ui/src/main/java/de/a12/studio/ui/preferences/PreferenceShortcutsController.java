package de.a12.studio.ui.preferences;

import de.a12.studio.ui.StudioKeyEventHandler;
import de.a12.studio.ui.util.StudioBundle;
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
    row = addSection(row, StudioBundle.get("general_shortcuts"), StudioKeyEventHandler.Category.GENERAL);
    row = addSection(row, StudioBundle.get("editor_shortcuts"), StudioKeyEventHandler.Category.EDITOR);
  }

  private int addSection(int row, String title, StudioKeyEventHandler.Category category) {
    Label sectionLabel = new Label(title);
    sectionLabel.getStyleClass().add("shortcut-section-title");
    shortcutsGrid.add(sectionLabel, 0, row, 2, 1);
    row++;

    for (StudioKeyEventHandler.Shortcut shortcut : StudioKeyEventHandler.SHORTCUTS) {
      if (shortcut.category() != category) {
        continue;
      }
      Label keyLabel = new Label(shortcut.keys());
      keyLabel.getStyleClass().add("shortcut-key");

      Label descriptionLabel = new Label(shortcut.description());
      descriptionLabel.getStyleClass().add("shortcut-description");

      shortcutsGrid.addRow(row++, keyLabel, descriptionLabel);
    }
    return row;
  }
}
