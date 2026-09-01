package de.a12.studio.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Dialog header for the Preview App console window. Extends the standard {@link DialogHeaderController}
 * with a dock/undock button that lets the user embed the console into the main view.
 */
public class ConsoleDialogHeaderController extends DialogHeaderController {

  @FXML
  private Button dockBtn;

  @FXML
  private FontIcon dockIcon;

  private Runnable onDockAction;

  @FXML
  private void onDockClick() {
    if (onDockAction != null) {
      onDockAction.run();
    }
  }

  /**
   * Sets the action to be executed when the user clicks the dock button.
   */
  public void setOnDockAction(Runnable action) {
    this.onDockAction = action;
  }
}
