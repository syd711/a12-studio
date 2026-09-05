package de.a12.studio.ui.previewapp;

import de.a12.studio.models.projects.Project;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the docked preview app console panel embedded at the bottom of the main view.
 * Owns its own {@link TextArea} bound to {@link PreviewAppProcess}'s log lines (independent of
 * the one in {@link PreviewAppConsoleController}'s floating dialog, so scrollback is preserved
 * in both), plus the docked toolbar's deploy/launch/stop/minimize/undock buttons.
 *
 * <p>Minimize and undock are wired back to the owning {@link de.a12.studio.ui.RootController}
 * via {@link #setOnMinimize(Runnable)}/{@link #setOnUndock(Runnable)}, since only the root
 * controller owns the {@code SplitPane} the panel docks into.
 */
public class PreviewAppConsoleDockedController implements Initializable {

  @FXML
  private BorderPane root;

  @FXML
  private TextArea dockedLogArea;

  @FXML
  private Button dockedDeployBtn;

  @FXML
  private ProgressIndicator dockedDeploySpinner;

  @FXML
  private Button dockedLaunchBtn;

  @FXML
  private Button dockedStopBtn;

  @FXML
  private Label dockedStateLabel;

  private Project project;
  private Runnable onMinimize;
  private Runnable onUndock;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
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
    dockedDeploySpinner.setVisible(true);
    PreviewAppDeployer.deploy(project, () -> {
      dockedDeploySpinner.setVisible(false);
      refreshDockedDeployButton(PreviewAppStatusMonitor.getInstance().getStatus());
    });
  }

  @FXML
  private void onMinimizeConsole() {
    if (onMinimize != null) {
      onMinimize.run();
    }
  }

  @FXML
  private void onUndockConsole() {
    if (onUndock != null) {
      onUndock.run();
    }
  }

  /** The currently open project, used by the launch/deploy actions. */
  public void setProject(Project project) {
    this.project = project;
  }

  /** Called when the user clicks the minimize button. */
  public void setOnMinimize(Runnable onMinimize) {
    this.onMinimize = onMinimize;
  }

  /** Called when the user clicks the undock button. */
  public void setOnUndock(Runnable onUndock) {
    this.onUndock = onUndock;
  }

  /** The panel's root node, added to / removed from the owning {@code SplitPane}'s items. */
  public BorderPane getRoot() {
    return root;
  }
}
