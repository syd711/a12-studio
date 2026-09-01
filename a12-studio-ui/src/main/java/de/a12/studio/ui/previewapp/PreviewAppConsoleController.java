package de.a12.studio.ui.previewapp;

import de.a12.studio.ui.components.ConsoleDialogHeaderController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Preview App console view. Owns the {@link TextArea} that streams
 * stdout/stderr from {@link PreviewAppProcess}, and is shared between the floating dialog
 * and the docked panel – the TextArea node itself is reparented so scrollback is never lost.
 *
 * <p>The header's dock button wires back to whatever dock action the caller installs via
 * {@link #setOnDockAction(Runnable)}.
 */
public class PreviewAppConsoleController implements Initializable {

  @FXML
  private TextArea logArea;

  @FXML
  private ConsoleDialogHeaderController headerController;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    PreviewAppProcess process = PreviewAppProcess.getInstance();

    // Replay existing log lines so the view is populated when first opened.
    for (String line : process.getLogLines()) {
      logArea.appendText(line + "\n");
    }

    process.getLogLines().addListener((ListChangeListener<String>) change -> {
      while (change.next()) {
        if (change.wasRemoved() && process.getLogLines().isEmpty()) {
          logArea.clear();
        }
        if (change.wasAdded()) {
          for (String line : change.getAddedSubList()) {
            logArea.appendText(line + "\n");
          }
        }
      }
    });

    updateTitle(process.getState());
    process.stateProperty().addListener((obs, oldState, newState) -> updateTitle(newState));
  }

  private void updateTitle(PreviewAppProcess.State state) {
    String base = StudioBundle.get("preview_app_console");
    headerController.setTitle(base + " (" + state + ")");
  }

  /**
   * Wires the dock button in the header to the given action (called when the user
   * wants to dock or undock the console).
   */
  public void setOnDockAction(Runnable action) {
    headerController.setOnDockAction(action);
  }

  /**
   * Returns the underlying TextArea so callers can reparent it into the docked panel
   * without losing any scrollback content.
   */
  public TextArea getLogArea() {
    return logArea;
  }
}
