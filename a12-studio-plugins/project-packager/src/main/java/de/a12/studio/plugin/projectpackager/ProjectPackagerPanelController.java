package de.a12.studio.plugin.projectpackager;

import de.a12.studio.models.projects.Project;
import de.a12.studio.ui.components.StudioFolderChooser;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;

public class ProjectPackagerPanelController {

  @FXML
  private TextField targetFolderField;

  @FXML
  private Label statusLabel;

  private ProjectPackagerSettings settings;

  public void setProject(Project project) {
    this.settings = ProjectPackagerSettings.load(project.getFolder());

    targetFolderField.setText(settings.getTargetFolder());
    updateStatus();
  }

  @FXML
  private void onBrowse() {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle(StudioBundle.get("project_packager.choose_target_folder"));
    String current = settings.getTargetFolder();
    if (current != null && !current.isBlank()) {
      chooser.setInitialDirectory(new File(current));
    }
    File selected = chooser.showOpenDialog((Stage) targetFolderField.getScene().getWindow());
    if (selected == null) {
      return;
    }
    settings.setTargetFolder(selected.getAbsolutePath());
    settings.save();
    targetFolderField.setText(selected.getAbsolutePath());
    updateStatus();
  }

  private void updateStatus() {
    String target = settings.getTargetFolder();
    if (target == null || target.isBlank()) {
      statusLabel.setText(StudioBundle.get("project_packager.no_target_folder"));
    }
    else {
      statusLabel.setText(StudioBundle.get("project_packager.target_folder_configured"));
    }
  }
}
