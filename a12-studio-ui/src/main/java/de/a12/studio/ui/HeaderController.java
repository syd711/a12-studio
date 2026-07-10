package de.a12.studio.ui;

import de.a12.studio.commons.util.FXResizeHelper;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the draggable/resizable window header (title, minimize/maximize/close).
 * Trimmed port of vpin-studio's HeaderResizeableController: no VPin Mania integration,
 * no window-position persistence.
 */
public class HeaderController implements Initializable {

  @FXML
  private Button maximizeBtn;

  @FXML
  private Button minimizeBtn;

  @FXML
  private Label titleLabel;

  @FXML
  private BorderPane header;

  private static MouseEvent event;

  @FXML
  private void onMouseClick(MouseEvent e) {
    if (e.getClickCount() == 2) {
      FXResizeHelper helper = (FXResizeHelper) getStage().getUserData();
      boolean isMaximize = helper.switchWindowedMode(e);
      refreshWindowMaximizedState(isMaximize);
    }
  }

  private Stage getStage() {
    if (header.getScene() != null) {
      return (Stage) header.getScene().getWindow();
    }
    return null;
  }

  @FXML
  private void onCloseClick() {
    Platform.exit();
  }

  @FXML
  private void onMaximize() {
    FXResizeHelper helper = (FXResizeHelper) getStage().getUserData();
    boolean isMaximize = helper.switchWindowedMode(event);
    refreshWindowMaximizedState(isMaximize);
  }

  private void refreshWindowMaximizedState(boolean isMaximized) {
    FontIcon icon = new FontIcon(isMaximized ? "mdi2w-window-restore" : "mdi2w-window-maximize");
    icon.setIconColor(javafx.scene.paint.Color.WHITE);
    icon.setIconSize(16);
    maximizeBtn.setGraphic(icon);
  }

  @FXML
  private void onHideClick() {
    getStage().setIconified(true);
  }

  public void setTitle(String title) {
    titleLabel.setText(title);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    header.setUserData(this);

    Platform.runLater(() -> {
      Stage stage = getStage();
      if (stage != null) {
        header.setOnMouseMoved(new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            HeaderController.event = event;
          }
        });

        boolean isMaximize = FXResizeHelper.isMaximized(stage);
        refreshWindowMaximizedState(isMaximize);
      }
    });
  }
}
