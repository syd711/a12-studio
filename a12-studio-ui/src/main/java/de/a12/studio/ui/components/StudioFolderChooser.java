package de.a12.studio.ui.components;

import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class StudioFolderChooser {

  private DirectoryChooser folderChooser;

  public StudioFolderChooser() {
    try {
      folderChooser = new DirectoryChooser();
      File lastFolderSelection = LocalUISettings.getLastFolderSelection();
      if (lastFolderSelection != null && lastFolderSelection.exists() && !lastFolderSelection.isFile()) {
        folderChooser.setInitialDirectory(lastFolderSelection);
      }
      else {
        folderChooser.setInitialDirectory(new File("./"));
      }
    }
    catch (Exception e) {
      log.error("Error creating folder chooser: " + e.getMessage(), e);
    }
  }

  public void setTitle(String title) {
    this.folderChooser.setTitle(title);
  }

  public void setInitialDirectory(File folder) {
    if (folder.exists()) {
      folderChooser.setInitialDirectory(folder);
    }
  }

  public File showOpenDialog(Stage stage) {
    try {
      File file = folderChooser.showDialog(stage);
      if (file != null) {
        LocalUISettings.saveLastFolderLocation(file);
      }

      return file;
    }
    catch (Exception e) {
      log.error("Error saving file location: " + e.getMessage(), e);
    }
    return null;
  }
}
