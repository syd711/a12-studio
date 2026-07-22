package de.a12.studio.ui;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.updater.Dialogs;
import de.a12.studio.ui.util.StudioVersion;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Global keyboard shortcuts for the main studio window: saving the active model, resizing the
 * window to fixed presets, and opening the release notes dialog.
 */
public class StudioKeyEventHandler implements EventHandler<KeyEvent> {

  private final Stage stage;

  public StudioKeyEventHandler(Stage stage) {
    this.stage = stage;
  }

  @Override
  public void handle(KeyEvent ke) {
    if (ke.getCode() == KeyCode.U && ke.isAltDown() && ke.isControlDown()) {
      Dialogs.openUpdateInfoDialog(StudioVersion.get());
      ke.consume();
    }
    else if (ke.getCode() == KeyCode.H && ke.isAltDown() && ke.isControlDown()) {
      resize(1920, 1080);
      ke.consume();
    }
    else if (ke.getCode() == KeyCode.W && ke.isAltDown() && ke.isControlDown()) {
      resize(2560, 1440);
      ke.consume();
    }
    else if (ke.getCode() == KeyCode.S && ke.isControlDown()) {
      ProjectItem projectItem = Studio.getSelectedProjectItem();
      if (projectItem != null) {
        StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
      }
      ke.consume();
    }
    else if (ke.getCode() == KeyCode.W && ke.isControlDown()) {
      Studio.closeSelectedTab();
      ke.consume();
    }
  }

  private void resize(double width, double height) {
    stage.setWidth(width);
    stage.setHeight(height);
  }
}
