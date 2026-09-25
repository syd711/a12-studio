package de.a12.studio.ui.tabs;

import de.a12.studio.ui.components.DialogHeaderController;
import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.Icons;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Window header of a {@link DetachedTabWindow}: the same look as the main window's header (see
 * {@link de.a12.studio.ui.HeaderController}) with minimize/maximize/close, plus a button that docks the tab back
 * into the tab pane.
 */
public class DetachedTabHeaderController extends DialogHeaderController {

  @FXML
  private Button maximizeBtn;

  private Runnable onDockAction;

  public void setOnDockAction(Runnable action) {
    this.onDockAction = action;
  }

  @FXML
  private void onDockClick() {
    if (onDockAction != null) {
      onDockAction.run();
    }
  }

  @FXML
  private void onHideClick() {
    getStage().setIconified(true);
  }

  @FXML
  private void onMaximize() {
    toggleMaximized(null);
  }

  @FXML
  private void onMouseClick(MouseEvent event) {
    if (event.getClickCount() == 2) {
      toggleMaximized(event);
    }
  }

  private void toggleMaximized(MouseEvent event) {
    FXResizeHelper helper = (FXResizeHelper) getStage().getUserData();
    boolean maximized = helper.switchWindowedMode(event);

    FontIcon icon = new FontIcon(maximized ? Icons.WINDOW_RESTORE : Icons.WINDOW_MAXIMIZE);
    icon.getStyleClass().add("header-icon");
    icon.setIconSize(16);
    maximizeBtn.setGraphic(icon);

    if (header.getScene() != null) {
      if (maximized) {
        header.getScene().getRoot().getStyleClass().add("maximized");
      }
      else {
        header.getScene().getRoot().getStyleClass().remove("maximized");
      }
    }
  }
}
