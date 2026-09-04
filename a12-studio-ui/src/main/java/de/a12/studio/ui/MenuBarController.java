package de.a12.studio.ui;

import de.a12.studio.ui.components.FileSearchDialogController;
import de.a12.studio.ui.components.ProgressDialog;
import de.a12.studio.ui.components.StudioFolderChooser;
import de.a12.studio.ui.newproject.NewProjectDialogController;
import de.a12.studio.ui.previewapp.PreviewAppDeployer;
import de.a12.studio.ui.previewapp.PreviewAppLogWindow;
import de.a12.studio.ui.previewapp.PreviewAppProcess;
import de.a12.studio.ui.previewapp.PreviewAppStatusMonitor;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.A12Settings;
import de.a12.studio.models.projects.settings.AiSettings;
import de.a12.studio.ui.events.SettingsChangedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.updater.Dialogs;
import de.a12.studio.ui.updater.UpdaterService;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.SystemUtil;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Controller for the app's main menu bar (File/Edit/Help).
 */
public class MenuBarController implements Initializable, StudioEventListener {

  @FXML
  private MenuBar menuBar;

  @FXML
  private Button updateBtn;

  @FXML
  private Button claudeConsoleBtn;

  @FXML
  private StackPane deployBtnContainer;

  @FXML
  private Button deployBtn;

  @FXML
  private ProgressIndicator deploySpinner;

  @FXML
  private Button launchPreviewAppBtn;

  @FXML
  private Button stopPreviewAppBtn;

  @FXML
  private Button previewAppLogBtn;

  @FXML
  private Button searchBtn;

  @FXML
  private Menu recentProjectsMenu;

  @FXML
  private MenuItem newMenuItem;

  @FXML
  private MenuItem openMenuItem;

  private Project project;

  private final UpdaterService updaterService = new UpdaterService();
  private String newVersion;

  @FXML
  private void onNew() {
    NewProjectDialogController.show(Studio.stage).ifPresent(this::openProject);
  }

  @FXML
  private void onOpen() {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle(StudioBundle.get("choose_project_workspace"));

    File file = chooser.showOpenDialog(Studio.stage);
    if (file != null) {
      openProject(file);
    }
  }

  private void openProject(File file) {
    LocalUISettings.saveProject(file);

    ProgressDialog.createProgressDialog(Studio.stage,
        new OpenProjectProgressModel(file, loadedProject -> project = loadedProject, () -> {
          refreshRecentProjectsMenu();
          refreshProjectDependentButtons();
        }));
  }

  private void refreshRecentProjectsMenu() {
    recentProjectsMenu.getItems().clear();

    List<String> recentProjects = LocalUISettings.getRecentProjects();
    if (project != null) {
      String openProjectPath = project.getFolder().getAbsolutePath();
      recentProjects = recentProjects.stream()
          .filter(path -> !new File(path).getAbsolutePath().equals(openProjectPath))
          .toList();
    }
    if (recentProjects.isEmpty()) {
      MenuItem emptyItem = new MenuItem(StudioBundle.get("no_projects_found"));
      emptyItem.setDisable(true);
      recentProjectsMenu.getItems().add(emptyItem);
      return;
    }

    double rowWidth = calculateRecentProjectRowWidth(recentProjects);
    for (String path : recentProjects) {
      recentProjectsMenu.getItems().add(createRecentProjectMenuItem(new File(path), rowWidth));
    }

    recentProjectsMenu.getItems().add(new SeparatorMenuItem());
    MenuItem clearItem = new MenuItem(StudioBundle.get("clear_recent_projects"));
    clearItem.setOnAction(event -> onClearRecentProjects());
    recentProjectsMenu.getItems().add(clearItem);
  }

  private void onClearRecentProjects() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(
        Studio.stage, StudioBundle.get("clear_all_recent_projects"), null, null, "Clear");
    if (result.isPresent() && result.get() == ButtonType.OK) {
      LocalUISettings.clearRecentProjects();
      refreshRecentProjectsMenu();
    }
  }

  private static final Font RECENT_PROJECT_FONT = Font.font(14);
  // Space reserved for the remove button, its padding and the row's own spacing/padding,
  // on top of the measured text width.
  private static final double RECENT_PROJECT_ROW_CHROME_WIDTH = 60;
  private static final double RECENT_PROJECT_ROW_MIN_WIDTH = 240;
  private static final double RECENT_PROJECT_ROW_MAX_WIDTH = 720;

  private double calculateRecentProjectRowWidth(List<String> paths) {
    Text measurer = new Text();
    measurer.setFont(RECENT_PROJECT_FONT);

    double maxTextWidth = 0;
    for (String path : paths) {
      measurer.setText(new File(path).getAbsolutePath());
      maxTextWidth = Math.max(maxTextWidth, measurer.getLayoutBounds().getWidth());
    }

    double rowWidth = maxTextWidth + RECENT_PROJECT_ROW_CHROME_WIDTH;
    return Math.min(Math.max(rowWidth, RECENT_PROJECT_ROW_MIN_WIDTH), RECENT_PROJECT_ROW_MAX_WIDTH);
  }

  private CustomMenuItem createRecentProjectMenuItem(File file, double rowWidth) {
    String path = file.getAbsolutePath();

    Label label = new Label(path);
    label.setMaxWidth(Double.MAX_VALUE);
    // Truncates from the front so the project folder name (the useful part) stays visible;
    // only kicks in for paths longer than RECENT_PROJECT_ROW_MAX_WIDTH allows.
    label.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
    label.setTooltip(WidgetFactory.createTooltip(path));
    HBox.setHgrow(label, Priority.ALWAYS);
    label.setOnMouseClicked(event -> {
      closeMenuBarChain();
      openProject(file);
    });

    FontIcon removeIcon = WidgetFactory.createIcon(Icons.TRASH);
    // Reuses the existing ".menu-item:hover .menu-icon" rule so the icon turns white
    // on the same blue selection highlight as every other menu icon.
    removeIcon.getStyleClass().add("menu-icon");
    Button removeBtn = new Button();
    removeBtn.setGraphic(removeIcon);
    removeBtn.getStyleClass().add("recent-project-remove-btn");
    removeBtn.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("remove_from_recent_projects")));
    removeBtn.setOnAction(event -> {
      event.consume();
      LocalUISettings.removeRecentProject(path);
      refreshRecentProjectsMenu();
    });

    HBox row = new HBox(8, label, removeBtn);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("recent-project-item");
    // CustomMenuItem content isn't stretched to the menu's width by default, so without
    // an explicit width the hgrow above has nothing to grow into and the button ends up
    // right after the text instead of pinned to the row's right edge.
    row.setPrefWidth(rowWidth);
    row.setMinWidth(rowWidth);
    row.setMaxWidth(rowWidth);

    // hideOnClick=false so removing an entry doesn't collapse the whole menu; the
    // label click handler above closes it explicitly when a project is opened instead.
    return new CustomMenuItem(row, false);
  }

  private void closeMenuBarChain() {
    Menu topMenu = recentProjectsMenu;
    while (topMenu.getParentMenu() != null) {
      topMenu = topMenu.getParentMenu();
    }
    topMenu.hide();
  }

  @FXML
  private void onCloseProject() {
    PreviewAppProcess.getInstance().stop();
    StudioEventManager.getInstance().fireProjectClosedEvent(project);
    project = null;
    refreshRecentProjectsMenu();
    refreshProjectDependentButtons();
  }

  @FXML
  private void onExit() {
    PreviewAppProcess.getInstance().stop();
    System.exit(0);
  }

  @FXML
  private void onPreferences() {
    StudioEventManager.getInstance().firePreferencesOpenRequestedEvent();
  }

  @FXML
  private void onSearch() {
    if (project != null) {
      FileSearchDialogController.show(Studio.stage, project);
    }
  }

  @FXML
  private void onOpenClaudeConsole() {
    if (project != null) {
      SystemUtil.openClaudeConsole(project.getFolder(), resolveClaudeCommand());
    }
  }

  private static String resolveClaudeCommand() {
    AiSettings settings = AiSettings.load();
    if (settings.getClaudePathMode() == AiSettings.ClaudePathMode.CONFIGURE_PATH
        && settings.getClaudeExecutablePath() != null
        && !settings.getClaudeExecutablePath().isEmpty()) {
      return settings.getClaudeExecutablePath();
    }
    return "claude";
  }

  @FXML
  private void onDeploy() {
    if (project == null) {
      return;
    }
    deployBtn.setDisable(true);
    deploySpinner.setVisible(true);
    PreviewAppDeployer.deploy(project, () -> {
      deploySpinner.setVisible(false);
      refreshDeployButtonState(PreviewAppStatusMonitor.getInstance().getStatus());
    });
  }

  @FXML
  private void onLaunchPreviewApp() {
    if (project != null) {
      PreviewAppProcess.getInstance().start(project);
      RootController root = Studio.getRootController();
      if (!root.isConsoleDocked() && !PreviewAppLogWindow.isShowing()) {
        root.dockConsole();
      }
    }
  }

  @FXML
  private void onStopPreviewApp() {
    PreviewAppProcess.getInstance().stop();
  }

  @FXML
  private void onOpenPreviewAppLog() {
    RootController root = Studio.getRootController();
    if (root.isConsoleDocked()) {
      // Console is embedded in the main view (possibly minimized) - just reveal it again
      // instead of opening the floating dialog.
      root.showDockedConsole();
      return;
    }
    // Ensure the dock button in the console header is wired to RootController.dockConsole()
    // before the window is made visible.
    PreviewAppLogWindow.getInstanceController(Studio.stage)
        .setOnDockAction(() -> root.dockConsole());
    PreviewAppLogWindow.show(Studio.stage);
  }

  /** Re-applies the visibility of every menu-bar button that only makes sense while a project is open
   *  (preview app start/stop/console, search, AI console). Call whenever the open project changes or
   *  the settings the individual buttons also depend on change. */
  private void refreshProjectDependentButtons() {
    refreshPreviewAppButtonsVisibility();
    refreshClaudeConsoleButton(AiSettings.load());
    boolean visible = project != null;
    searchBtn.setVisible(visible);
    searchBtn.setManaged(visible);
  }

  private void refreshPreviewAppButtonsVisibility() {
    String installationPath = A12Settings.load().getInstallationPath();
    boolean visible = project != null && installationPath != null
        && A12Settings.isValidInstallationFolder(new File(installationPath));
    deployBtnContainer.setVisible(visible);
    deployBtnContainer.setManaged(visible);
    launchPreviewAppBtn.setVisible(visible);
    launchPreviewAppBtn.setManaged(visible);
    stopPreviewAppBtn.setVisible(visible);
    stopPreviewAppBtn.setManaged(visible);
    previewAppLogBtn.setVisible(visible);
    previewAppLogBtn.setManaged(visible);
  }

  /** Deploy is only meaningful while the Preview App server is actually reachable locally. */
  private void refreshDeployButtonState(PreviewAppStatusMonitor.Status status) {
    deployBtn.setDisable(status != PreviewAppStatusMonitor.Status.RUNNING || PreviewAppDeployer.isDeploying());
  }

  private void refreshPreviewAppButtonsState(PreviewAppProcess.State state) {
    boolean running = state == PreviewAppProcess.State.RUNNING;
    boolean busy = state == PreviewAppProcess.State.STARTING || state == PreviewAppProcess.State.STOPPING;
    launchPreviewAppBtn.setDisable(running || busy);
    stopPreviewAppBtn.setDisable(!running && !busy);
  }

  @Override
  public void settingsChanged(@NonNull SettingsChangedEvent event) {
    if (event.getSettings().getSettingsType().equals(AiSettings.SettingsType.AI)) {
      refreshClaudeConsoleButton((AiSettings) event.getSettings());
    }
    if (event.getSettings().getSettingsType().equals(AiSettings.SettingsType.A12_INSTALLATION)) {
      refreshPreviewAppButtonsVisibility();
    }
  }

  private void refreshClaudeConsoleButton(AiSettings settings) {
    boolean visible = project != null && settings.isAddClaudeConsoleButton();
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
    newMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    openMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));

    LocalUISettings.pruneMissingRecentProjects();
    refreshRecentProjectsMenu();
    updateBtn.managedProperty().bind(updateBtn.visibleProperty());
    runUpdateCheck();

    StudioEventManager.getInstance().addListener(this);
    refreshProjectDependentButtons();

    refreshPreviewAppButtonsState(PreviewAppProcess.getInstance().getState());
    PreviewAppProcess.getInstance().stateProperty().addListener(
        (observable, oldState, newState) -> refreshPreviewAppButtonsState(newState));

    PreviewAppStatusMonitor.getInstance().start();
    refreshDeployButtonState(PreviewAppStatusMonitor.getInstance().getStatus());
    PreviewAppStatusMonitor.getInstance().statusProperty().addListener(
        (observable, oldStatus, newStatus) -> refreshDeployButtonState(newStatus));

    Platform.runLater(() -> {
      List<String> recentProjects = LocalUISettings.getRecentProjects();
      if (!recentProjects.isEmpty()) {
        openProject(new File(recentProjects.get(0)));
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
}
