package de.a12.studio.ui;

import de.a12.studio.commons.util.StudioFolderChooser;
import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.dataservices.projects.settings.JsonSettings;
import de.a12.studio.ui.events.SettingsChangedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.updater.Dialogs;
import de.a12.studio.ui.updater.UpdaterService;
import de.a12.studio.ui.util.StudioVersion;
import de.a12.studio.ui.util.SystemUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the app's main menu bar (File/Edit/Help).
 */
public class MenuBarController implements Initializable, StudioEventListener {

  // No published a12-release-line concept exists yet for the Java stack; update by hand until one does.
  private static final String A12_RELEASE_VERSION = "2025.06";

  @FXML
  private MenuBar menuBar;

  @FXML
  private MenuButton versionMenuButton;

  @FXML
  private Button updateBtn;

  @FXML
  private Button claudeConsoleBtn;

  @FXML
  private Menu recentProjectsMenu;
  private Project project;

  private final UpdaterService updaterService = new UpdaterService();
  private String newVersion;

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
  private void onExit() {
    System.exit(0);
  }

  @FXML
  private void onPreferences() {
    StudioEventManager.getInstance().firePreferencesOpenRequestedEvent();
  }

  @FXML
  private void onOpenClaudeConsole() {
    if (project != null) {
      SystemUtil.openClaudeConsole(project.getFolder(), resolveClaudeCommand());
    }
  }

  private static String resolveClaudeCommand() {
    JsonSettings settings = JsonSettings.load();
    if (settings.getClaudePathMode() == JsonSettings.ClaudePathMode.CONFIGURE_PATH
        && settings.getClaudeExecutablePath() != null
        && !settings.getClaudeExecutablePath().isEmpty()) {
      return settings.getClaudeExecutablePath();
    }
    return "claude";
  }

  @Override
  public void settingsChanged(@NonNull SettingsChangedEvent event) {
    if (event.getSettings().getSettingsType().equals(JsonSettings.SettingsType.AI)) {
      refreshClaudeConsoleButton(event.getSettings());
    }
  }

  private void refreshClaudeConsoleButton(JsonSettings settings) {
    boolean visible = settings.isAddClaudeConsoleButton();
    claudeConsoleBtn.setVisible(visible);
    claudeConsoleBtn.setManaged(visible);
  }

  @FXML
  private void onAbout() {
    SystemUtil.openUrl("https://github.com/syd711/a12-studio");
  }

  @FXML
  private void onUpdate() {
    Dialogs.openUpdateInfoDialog(newVersion);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    refreshRecentProjectsMenu();
    initializeVersionMenu();
    updateBtn.managedProperty().bind(updateBtn.visibleProperty());
    runUpdateCheck();

    StudioEventManager.getInstance().addListener(this);
    refreshClaudeConsoleButton(JsonSettings.load());

    Platform.runLater(() -> {
      File lastFolderSelection = LocalUISettings.getLastFolderSelection();
      if (lastFolderSelection != null) {
        openProject(lastFolderSelection);
      }
    });
  }

  private void runUpdateCheck() {
    Thread t = new Thread(() -> {
      String latestVersion = updaterService.checkForNewerVersion();
      if (latestVersion != null) {
        Platform.runLater(() -> {
          newVersion = latestVersion;
          updateBtn.getTooltip().setText("Version " + newVersion + " available");
          updateBtn.setVisible(true);
        });
      }
    });
    t.setName("Update Check");
    t.setDaemon(true);
    t.start();
  }

  private void initializeVersionMenu() {
    String studioVersion = StudioVersion.get();
    versionMenuButton.setText(studioVersion);

    versionMenuButton.getItems().addAll(
        versionHeaderItem("A12 Studio Version"),
        versionValueItem(studioVersion),
        new SeparatorMenuItem(),
        versionHeaderItem("A12 Release Version"),
        versionValueItem(A12_RELEASE_VERSION),
        new SeparatorMenuItem(),
        versionHeaderItem("Model Versions"));

    for (ModelType modelType : ModelType.values()) {
      versionMenuButton.getItems().add(versionValueItem(modelType.getDisplayName() + " " + modelType.getCurrentVersion()));
    }
  }

  private static CustomMenuItem versionHeaderItem(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("version-menu-header");
    return versionMenuItem(label);
  }

  private static CustomMenuItem versionValueItem(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("version-menu-value");
    return versionMenuItem(label);
  }

  private static CustomMenuItem versionMenuItem(Label label) {
    CustomMenuItem item = new CustomMenuItem(label, false);
    item.setHideOnClick(false);
    // Marks read-only rows so CSS can suppress the standard hover/focus highlight for just this menu.
    item.getStyleClass().add("version-menu-item");
    return item;
  }
}
