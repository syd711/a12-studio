package de.a12.studio.ui;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.FileSearchDialogController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.updater.Dialogs;
import de.a12.studio.ui.util.StudioVersion;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.List;

/**
 * Global keyboard shortcuts for the main studio window: saving the active model, resizing the
 * window to fixed presets, and opening the release notes dialog.
 */
public class StudioKeyEventHandler implements EventHandler<KeyEvent> {

  public record Shortcut(String keys, String description) {}

  /**
   * Single source of truth for the shortcuts shown in Preferences > Shortcuts. Whenever a new
   * shortcut is added to {@link #handle(KeyEvent)}, add a matching entry here.
   */
  public static final List<Shortcut> SHORTCUTS = List.of(
      new Shortcut("Ctrl+S", "Save the active model"),
      new Shortcut("Ctrl+W", "Close the selected tab"),
      new Shortcut("Ctrl+Tab", "Select the next tab"),
      new Shortcut("Ctrl+Shift+Tab", "Select the previous tab"),
      new Shortcut("Ctrl+Alt+P", "Open Preferences"),
      new Shortcut("Ctrl+Alt+N", "Search files"),
      new Shortcut("Ctrl+Alt+U", "Show update info"),
      new Shortcut("Ctrl+Alt+H", "Resize window to 1920x1080"),
      new Shortcut("Ctrl+Alt+W", "Resize window to 2560x1440")
  );

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
    else if (ke.getCode() == KeyCode.TAB && ke.isControlDown() && ke.isShiftDown()) {
      Studio.selectPreviousTab();
      ke.consume();
    }
    else if (ke.getCode() == KeyCode.TAB && ke.isControlDown()) {
      Studio.selectNextTab();
      ke.consume();
    }
    else if (ke.getCode() == KeyCode.P && ke.isAltDown() && ke.isControlDown()) {
      StudioEventManager.getInstance().firePreferencesOpenRequestedEvent();
      ke.consume();
    }
    else if (ke.getCode() == KeyCode.N && ke.isAltDown() && ke.isControlDown()) {
      FileSearchDialogController.show(stage, Studio.getCurrentProject());
      ke.consume();
    }
  }

  private void resize(double width, double height) {
    stage.setWidth(width);
    stage.setHeight(height);
  }
}
