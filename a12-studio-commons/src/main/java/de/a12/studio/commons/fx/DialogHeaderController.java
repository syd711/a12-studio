package de.a12.studio.commons.fx;

import de.a12.studio.commons.util.FXResizeHelper;
import de.a12.studio.commons.util.WidgetFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 *
 */
public class DialogHeaderController implements Initializable {

  private double xOffset;
  private double yOffset;

  @FXML
  protected BorderPane header;

  @FXML
  protected Label titleLabel;

  private final BooleanProperty dirty = new SimpleBooleanProperty(false);

  private Stage stage;

  @FXML
  private void onCloseClick() {
    if (isDirty()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "You have unsaved changes.", "Do you really want to close and lose them ?");
      if (!result.isPresent() || !result.get().equals(ButtonType.OK)) {
        // stay here
        return;
      }
    }
    // else close the stage
    DialogController dialogController = getDialogController();
    if (dialogController != null) {
      dialogController.onDialogCancel();
    }
    stage.close();
  }

  protected DialogController getDialogController() {
    Object userData = stage.getUserData();
    if (userData instanceof DialogController) {
      return (DialogController) userData;
    }
    else if (userData instanceof FXResizeHelper) {
      // for FXResizeHelper, the DialogController has been wrapped into the FXResizeHelper
      userData = ((FXResizeHelper) userData).getUserData();
      if (userData instanceof DialogController) {
        return (DialogController) userData;
      }
    }
    return null;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    header.setUserData(this);
    header.setOnMousePressed(event -> {
      xOffset = stage.getX() - event.getScreenX();
      yOffset = stage.getY() - event.getScreenY();
    });
    header.setOnMouseDragged(event -> {
      stage.setX(event.getScreenX() + xOffset);
      stage.setY(event.getScreenY() + yOffset);
    });
    // update title when the dirty flag changes
    dirty.addListener((pbs, o, v) -> updateTitle());
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public Stage getStage() {
    return stage;
  }

  public void setTitle(String title) {
    titleLabel.setText(title);
  }

  //---------------------------------------------
  // Dirty management

  private static final String dirtySuffix = " (*)";

  public boolean isDirty() {
    return this.dirty.get();
  }

  public void setDirty(boolean dirty) {
    this.dirty.set(dirty);
  }

  public BooleanProperty dirtyProperty() {
    return dirty;
  }

  protected void updateTitle() {
    String title = titleLabel.getText();
    if (dirty.get() && !title.endsWith(dirtySuffix)) {
      titleLabel.setText(title + dirtySuffix);
    }
    else if (!dirty.get() && title.endsWith(dirtySuffix)) {
      titleLabel.setText(title.substring(0, title.length() - dirtySuffix.length()));
    }
  }
}
