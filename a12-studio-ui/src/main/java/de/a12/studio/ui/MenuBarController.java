package de.a12.studio.ui;

import de.a12.studio.commons.util.StudioFolderChooser;
import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the app's main menu bar (File/Edit/Help).
 */
public class MenuBarController implements Initializable {

  @FXML
  private MenuBar menuBar;

  @FXML
  private Menu recentProjectsMenu;
  private Project project;

  @FXML
  private void onNew() {
  }

  @FXML
  private void onOpen() {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setInitialDirectory(LocalUISettings.getLastFolderSelection());
    chooser.setTitle("Choose Project Workspace");

    File file = chooser.showOpenDialog(Studio.stage);
    if (file != null) {
      openProject(file);
    }
  }

  @FXML
  private void onProjectOpen(javafx.event.ActionEvent event) {
    MenuItem menuItem = (MenuItem) event.getSource();
    File file = (File) menuItem.getUserData();
    if (file != null) {
      openProject(file);
    }
  }

  private void openProject(File file) {
    LocalUISettings.saveProject(file);
    project = new Project();
    project.load(file);

    StudioEventManager.getInstance().fireProjectOpenEvent(project);

    refreshRecentProjectsMenu();
  }

  private void refreshRecentProjectsMenu() {
    recentProjectsMenu.getItems().clear();

    List<String> recentProjects = LocalUISettings.getRecentProjects();
    if (recentProjects.isEmpty()) {
      MenuItem emptyItem = new MenuItem("No projects found.");
      emptyItem.setDisable(true);
      recentProjectsMenu.getItems().add(emptyItem);
      return;
    }

    for (String path : recentProjects) {
      File file = new File(path);
      MenuItem menuItem = new MenuItem(file.getAbsolutePath());
      menuItem.setUserData(file);
      menuItem.setOnAction(this::onProjectOpen);
      recentProjectsMenu.getItems().add(menuItem);
    }
  }

  @FXML
  private void onCloseProject() {
    StudioEventManager.getInstance().fireProjectClosedEvent(project);
  }

  @FXML
  private void onCloseAllProjects() {
  }

  @FXML
  private void onCloseOtherProjects() {
  }

  @FXML
  private void onExit() {
    System.exit(0);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    refreshRecentProjectsMenu();

    Platform.runLater(() -> {
      File lastFolderSelection = LocalUISettings.getLastFolderSelection();
      if(lastFolderSelection != null) {
        openProject(lastFolderSelection);
      }
    });
  }
}
