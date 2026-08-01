package de.a12.studio.ui.previewapp;

import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Small, non-modal window streaming {@link PreviewAppProcess}'s stdout/stderr live, mirroring the
 * per-service log window of the standalone "A12 Preview App Control" Electron tool. Stays alive
 * (hidden, not disposed) across start/stop cycles so it doesn't lose scrollback.
 */
public class PreviewAppLogWindow {

  private static PreviewAppLogWindow instance;

  private final Stage stage;

  private final TextArea textArea;

  private PreviewAppLogWindow(Window owner) {
    textArea = new TextArea();
    textArea.setEditable(false);
    textArea.setWrapText(false);
    textArea.setStyle("-fx-font-family: monospace;");

    stage = new Stage();
    stage.initOwner(owner);
    stage.setScene(new Scene(new BorderPane(textArea), 900, 550));
    stage.setOnCloseRequest(event -> {
      event.consume();
      stage.hide();
    });

    PreviewAppProcess process = PreviewAppProcess.getInstance();
    for (String line : process.getLogLines()) {
      textArea.appendText(line + "\n");
    }
    process.getLogLines().addListener((ListChangeListener<String>) change -> {
      while (change.next()) {
        if (change.wasRemoved() && process.getLogLines().isEmpty()) {
          textArea.clear();
        }
        if (change.wasAdded()) {
          for (String line : change.getAddedSubList()) {
            textArea.appendText(line + "\n");
          }
        }
      }
    });

    updateTitle(process.getState());
    process.stateProperty().addListener((observable, oldState, newState) -> updateTitle(newState));
  }

  private void updateTitle(PreviewAppProcess.State state) {
    stage.setTitle("Preview App Log (" + state + ")");
  }

  public static synchronized void show(Window owner) {
    if (instance == null) {
      instance = new PreviewAppLogWindow(owner);
    }
    instance.stage.show();
    instance.stage.toFront();
  }
}
