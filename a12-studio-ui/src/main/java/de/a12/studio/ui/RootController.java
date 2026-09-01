package de.a12.studio.ui;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.PreferencesOpenRequestedEvent;
import de.a12.studio.ui.events.ProjectClosedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preferences.PreferencesController;
import de.a12.studio.ui.previewapp.PreviewAppDeployer;
import de.a12.studio.ui.previewapp.PreviewAppLogWindow;
import de.a12.studio.ui.previewapp.PreviewAppProcess;
import de.a12.studio.ui.previewapp.PreviewAppStatusMonitor;
import de.a12.studio.ui.projecttree.ProjectTreeController;
import de.a12.studio.ui.tabs.TabPaneController;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RootController implements Initializable, StudioEventListener {

  @FXML
  private StackPane main;

  @FXML
  private SplitPane mainSplitPane;

  @FXML
  private HeaderController headerController;

  @FXML
  private MenuBarController menuBarController;

  @FXML
  private ProjectTreeController projectTreeController;

  @FXML
  private TabPaneController tabPaneController;

  @FXML
  private FooterController footerController;

  @FXML
  private StackPane rootStack;

  // --- Docked console panel ---

  /** Default divider position (0-100) when the console panel is first docked with no saved preference. */
  private static final int DEFAULT_CONSOLE_DIVIDER_POSITION = 65;

  @FXML
  private SplitPane centerSplitPane;

  @FXML
  private BorderPane consoleArea;

  @FXML
  private Button minimizeConsoleBtn;

  @FXML
  private TextArea dockedLogArea;

  @FXML
  private Button dockedDeployBtn;

  @FXML
  private Button dockedLaunchBtn;

  @FXML
  private Button dockedStopBtn;

  @FXML
  private Label dockedStateLabel;

  @FXML
  private Button undockBtn;

  // ---

  private Project project;
  private boolean consoleDocked = false;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);

    mainSplitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
      if (project != null) {
        project.getSettings().getUISettings().setDividerPosition(newValue.doubleValue());
        project.getSettings().getUISettings().save();
      }
    });

    // Wire docked console log area to the process.
    PreviewAppProcess process = PreviewAppProcess.getInstance();
    for (String line : process.getLogLines()) {
      dockedLogArea.appendText(line + "\n");
    }
    process.getLogLines().addListener((ListChangeListener<String>) change -> {
      while (change.next()) {
        if (change.wasRemoved() && process.getLogLines().isEmpty()) {
          dockedLogArea.clear();
        }
        if (change.wasAdded()) {
          for (String line : change.getAddedSubList()) {
            dockedLogArea.appendText(line + "\n");
          }
        }
      }
    });

    updateDockedState(process.getState());
    process.stateProperty().addListener((obs, oldState, newState) -> updateDockedState(newState));

    // Docked deploy button follows the same deploy-enabled rules as the menu bar button.
    PreviewAppStatusMonitor.getInstance().statusProperty().addListener(
        (obs, oldStatus, newStatus) -> refreshDockedDeployButton(newStatus));
    refreshDockedDeployButton(PreviewAppStatusMonitor.getInstance().getStatus());

    // The console panel is only ever present in the SplitPane's items while docked and visible;
    // it starts out included in the FXML purely so FXMLLoader instantiates and injects it.
    centerSplitPane.getItems().remove(consoleArea);

    // Restore the docked console panel's open/minimized state from the last session.
    if (LocalUISettings.getBoolean(LocalUISettings.CONSOLE_VISIBLE, false)) {
      consoleDocked = true;
      setConsolePanelVisible(true);
    }
  }

  private void updateDockedState(PreviewAppProcess.State state) {
    boolean running = state == PreviewAppProcess.State.RUNNING;
    boolean busy = state == PreviewAppProcess.State.STARTING || state == PreviewAppProcess.State.STOPPING;
    dockedLaunchBtn.setDisable(running || busy);
    dockedStopBtn.setDisable(!running && !busy);
    dockedStateLabel.setText(state.name());
  }

  private void refreshDockedDeployButton(PreviewAppStatusMonitor.Status status) {
    dockedDeployBtn.setDisable(status != PreviewAppStatusMonitor.Status.RUNNING || PreviewAppDeployer.isDeploying());
  }

  // --- Docked toolbar actions ---

  @FXML
  private void onDockedLaunch() {
    if (project != null) {
      PreviewAppProcess.getInstance().start(project);
    }
  }

  @FXML
  private void onDockedStop() {
    PreviewAppProcess.getInstance().stop();
  }

  @FXML
  private void onDockedDeploy() {
    if (project == null) return;
    dockedDeployBtn.setDisable(true);
    PreviewAppDeployer.deploy(project,
        () -> refreshDockedDeployButton(PreviewAppStatusMonitor.getInstance().getStatus()));
  }

  @FXML
  private void onUndockConsole() {
    undockConsole();
  }

  @FXML
  private void onMinimizeConsole() {
    setConsolePanelVisible(false);
    LocalUISettings.saveProperty(LocalUISettings.CONSOLE_VISIBLE, "false");
  }

  // --- Dock / undock API ---

  /**
   * Docks the console into the main view bottom area and hides the floating dialog.
   * Called from {@link PreviewAppLogWindow} when the user clicks the dock button in the header.
   */
  public boolean isConsoleDocked() {
    return consoleDocked;
  }

  public void dockConsole() {
    if (consoleDocked) return;
    consoleDocked = true;
    PreviewAppLogWindow.hide();
    setConsolePanelVisible(true);
    LocalUISettings.saveProperty(LocalUISettings.CONSOLE_VISIBLE, "true");
  }

  /**
   * Releases the console back into its floating dialog and collapses the bottom area.
   */
  public void undockConsole() {
    if (!consoleDocked) return;
    consoleDocked = false;
    setConsolePanelVisible(false);
    LocalUISettings.saveProperty(LocalUISettings.CONSOLE_VISIBLE, "false");
    PreviewAppLogWindow.show(Studio.stage);
  }

  /**
   * Re-shows the docked console panel after it was minimized. Called from the header toolbar's
   * console button (MenuBarController.onOpenPreviewAppLog) instead of opening the floating dialog
   * whenever the console is currently docked (visible or minimized).
   */
  public void showDockedConsole() {
    if (!consoleDocked) return;
    setConsolePanelVisible(true);
    LocalUISettings.saveProperty(LocalUISettings.CONSOLE_VISIBLE, "true");
  }

  private void setConsolePanelVisible(boolean visible) {
    if (visible) {
      if (!centerSplitPane.getItems().contains(consoleArea)) {
        centerSplitPane.getItems().add(consoleArea);
        int savedPosition = LocalUISettings.getInt(LocalUISettings.CONSOLE_DIVIDER_POSITION, DEFAULT_CONSOLE_DIVIDER_POSITION);
        centerSplitPane.setDividerPosition(0, savedPosition / 100.0);
        // The Divider object itself is only created by the skin on the next layout pass, so
        // getDividers() is still empty here (mirrors the same wait needed for mainSplitPane
        // above, in projectOpened()).
        Platform.runLater(() -> {
          if (!centerSplitPane.getDividers().isEmpty()) {
            centerSplitPane.setDividerPosition(0, savedPosition / 100.0);
            centerSplitPane.getDividers().get(0).positionProperty().addListener((obs, oldValue, newValue) ->
                LocalUISettings.saveProperty(LocalUISettings.CONSOLE_DIVIDER_POSITION,
                    String.valueOf((int) Math.round(newValue.doubleValue() * 100))));
          }
        });
      }
    }
    else {
      centerSplitPane.getItems().remove(consoleArea);
    }
  }

  // --- Project lifecycle ---

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    this.mainSplitPane.setVisible(true);
    this.mainSplitPane.setManaged(true);
    this.project = event.getProject();
    double dividerPosition = project.getSettings().getUISettings().getDividerPosition();
    Platform.runLater(() -> mainSplitPane.setDividerPositions(dividerPosition));
  }

  @Override
  public void projectClosed(@NonNull ProjectClosedEvent event) {
    this.mainSplitPane.setVisible(false);
    this.mainSplitPane.setManaged(false);
    this.project = null;
  }

  public void setTitle(String s) {
    headerController.setTitle(s);
  }

  public ProjectItem getSelectedProjectItem() {
    return tabPaneController.getSelectedProjectItem();
  }

  public void closeSelectedTab() {
    tabPaneController.closeSelectedTab();
  }

  public void selectNextTab() {
    tabPaneController.selectNextTab();
  }

  public void selectPreviousTab() {
    tabPaneController.selectPreviousTab();
  }

  @Override
  public void preferencesOpenRequested(@NonNull PreferencesOpenRequestedEvent event) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("preferences/scene-preferences.fxml"));
      loader.setResources(StudioBundle.getBundle());
      Parent preferencesRoot = loader.load();
      PreferencesController controller = loader.getController();
      controller.setOnCloseRequested(() -> {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), preferencesRoot);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> rootStack.getChildren().remove(preferencesRoot));
        fadeOut.play();
        if (project != null) {
          projectTreeController.load(project);
        }
      });
      controller.setProjectOpen(project != null);
      controller.showSection(event.getSection());

      preferencesRoot.setOpacity(0);
      rootStack.getChildren().add(preferencesRoot);
      FadeTransition fadeIn = new FadeTransition(Duration.millis(200), preferencesRoot);
      fadeIn.setToValue(1);
      fadeIn.play();
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not load preferences", e);
    }
  }
}
