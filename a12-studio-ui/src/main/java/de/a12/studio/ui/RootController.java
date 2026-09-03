package de.a12.studio.ui;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.plugin.manager.IFileDropHandler;
import de.a12.studio.plugin.manager.PluginManager;
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
import de.a12.studio.ui.util.WidgetFactory;
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
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
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

  @FXML
  private StackPane fileDropOverlay;

  @FXML
  private VBox fileDropZone;

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

    // Install file-drop handlers on the root stack so the overlay covers the entire window.
    installFileDropHandlers();
  }

  // ---------------------------------------------------------------------------
  // File-drop support
  // ---------------------------------------------------------------------------

  private void installFileDropHandlers() {
    rootStack.setOnDragOver(this::onDragOver);
    rootStack.setOnDragEntered(this::onDragEntered);
    rootStack.setOnDragExited(this::onDragExited);
    rootStack.setOnDragDropped(this::onDragDropped);
  }

  private void onDragOver(@NonNull DragEvent event) {
    Dragboard db = event.getDragboard();
    if (db.hasFiles() && hasHandlerForAnyFile(db.getFiles())) {
      event.acceptTransferModes(TransferMode.COPY);
    }
    event.consume();
  }

  private void onDragEntered(@NonNull DragEvent event) {
    Dragboard db = event.getDragboard();
    if (db.hasFiles() && hasHandlerForAnyFile(db.getFiles())) {
      showDropOverlay(true);
    }
    event.consume();
  }

  private void onDragExited(@NonNull DragEvent event) {
    showDropOverlay(false);
    event.consume();
  }

  private void onDragDropped(@NonNull DragEvent event) {
    showDropOverlay(false);
    Dragboard db = event.getDragboard();
    List<File> files = db.hasFiles() ? List.copyOf(db.getFiles()) : List.of();
    boolean handled = hasHandlerForAnyFile(files);
    // Tell the OS the drop finished before opening any handler dialog. A handler that
    // pops a modal dialog (e.g. showAndWait) starts a nested event loop; if that happened
    // synchronously here, the native drag ghost/cursor stays on screen (on Windows) until
    // the dialog closes, since the drop-completed signal never gets a chance to reach Glass.
    event.setDropCompleted(handled);
    event.consume();
    if (handled) {
      Platform.runLater(() -> {
        for (File file : files) {
          dispatchDroppedFile(file);
        }
      });
    }
  }

  /**
   * Returns {@code true} if at least one of the given files can be handled by the built-in
   * AI handler or a registered plugin drop handler.
   */
  private boolean hasHandlerForAnyFile(@NonNull List<File> files) {
    for (File file : files) {
      if (canAiHandleFile(file) || findPluginHandler(file) != null) {
        return true;
      }
    }
    return false;
  }

  /**
   * Dispatches a single dropped file to the first willing handler.
   * Returns {@code true} if the file was accepted.
   */
  private boolean dispatchDroppedFile(@NonNull File file) {
    // 1. Check built-in AI handler first (currently a stub – always returns false).
    if (canAiHandleFile(file)) {
      handleFileWithAi(file);
      return true;
    }

    // 2. Try plugin handlers in load order.
    IFileDropHandler handler = findPluginHandler(file);
    if (handler != null) {
      ProjectItem target = resolveDropTarget();
      if (target == null) {
        log.warn("Cannot handle dropped file '{}': no project open or no folder selected.", file.getName());
        WidgetFactory.showAlert(Studio.stage, "No project open",
            "Please open a project before dropping files.");
        return false;
      }
      handler.handle(Studio.stage, target, file);
      return true;
    }

    log.debug("No handler found for dropped file: {}", file.getName());
    return false;
  }

  /**
   * Built-in AI file handler stub. Returns {@code false} until AI drop support is implemented.
   */
  private boolean canAiHandleFile(@NonNull File file) {
    // TODO: implement AI-based file handling
    return false;
  }

  private void handleFileWithAi(@NonNull File file) {
    // TODO: implement AI-based file handling
    log.info("AI file handling not yet implemented for: {}", file.getName());
  }

  /**
   * Finds the first plugin {@link IFileDropHandler} that accepts the given file,
   * or {@code null} if none matches.
   */
  private IFileDropHandler findPluginHandler(@NonNull File file) {
    for (IFileDropHandler handler : PluginManager.getInstance().getFileDropHandlers()) {
      if (handler.canHandle(file)) {
        return handler;
      }
    }
    return null;
  }

  /**
   * Returns the drop target folder: the currently selected project item if it is a folder,
   * the project root if a project is open but nothing is selected, or {@code null} if no
   * project is open.
   */
  private ProjectItem resolveDropTarget() {
    if (project == null) {
      return null;
    }
    ProjectItem selected = Studio.getSelectedProjectItem();
    if (selected != null && selected.isFolder()) {
      return selected;
    }
    // Fall back to the project root folder.
    return project.getRoot();
  }

  private void showDropOverlay(boolean show) {
    fileDropOverlay.setVisible(show);
    fileDropOverlay.setManaged(show);
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
