package de.a12.studio.ui;

import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import de.a12.studio.ui.util.Icons;
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
 *
 */
public class HeaderController implements Initializable {
  private final Debouncer debouncer = new Debouncer();

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
  private void onDragDone() {
    debouncer.debounce("position", () -> {
      int y = (int) getStage().getY();
      int x = (int) getStage().getX();
      int width = (int) getStage().getWidth();
      int height = (int) getStage().getHeight();
      if (width > 0 && height > 0) {
        LocalUISettings.saveLocation(x, y, width, height);
      }
    }, 500);
  }

  @FXML
  private void onMaximize() {
    FXResizeHelper helper = (FXResizeHelper) getStage().getUserData();
    boolean isMaximize = helper.switchWindowedMode(event);
    refreshWindowMaximizedState(isMaximize);
  }

  private void refreshWindowMaximizedState(boolean isMaximized) {
    FontIcon icon = new FontIcon(isMaximized ? Icons.WINDOW_RESTORE : Icons.WINDOW_MAXIMIZE);
    icon.getStyleClass().add("header-icon");
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
        stage.xProperty().addListener((observable, oldValue, newValue) -> onDragDone());
        stage.yProperty().addListener((observable, oldValue, newValue) -> onDragDone());
        stage.widthProperty().addListener((observable, oldValue, newValue) -> onDragDone());
        stage.heightProperty().addListener((observable, oldValue, newValue) -> onDragDone());

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
