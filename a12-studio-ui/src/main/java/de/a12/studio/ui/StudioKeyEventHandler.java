package de.a12.studio.ui;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.FileSearchDialogController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.updater.Dialogs;
import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.StudioVersion;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.List;

/**
 * Global keyboard shortcuts for the main studio window: saving the active model, resizing the
 * window to fixed presets, snapping/maximizing the window with Win+arrow keys, and opening the
 * release notes dialog.
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
      new Shortcut("Ctrl+Shift+N", "Search files"),
      new Shortcut("Ctrl+Shift+F", "Find in files"),
      new Shortcut("Ctrl+Alt+U", "Show update info"),
      new Shortcut("Ctrl+Alt+H", "Resize window to 1920x1080"),
      new Shortcut("Ctrl+Alt+W", "Resize window to 2560x1440"),
      new Shortcut("Win+Left", "Snap window to the left half of the screen"),
      new Shortcut("Win+Right", "Snap window to the right half of the screen"),
      new Shortcut("Win+Up", "Maximize the window"),
      new Shortcut("Win+Down", "Restore, then minimize the window")
  );

  private final Stage stage;

  // the Windows key isn't reported as a modifier (like Ctrl/Alt/Shift) on KeyEvent, so its
  // pressed state has to be tracked manually across KEY_PRESSED/KEY_RELEASED events
  private boolean windowsKeyDown;

  public StudioKeyEventHandler(Stage stage) {
    this.stage = stage;
    stage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
      if (!isFocused) {
        windowsKeyDown = false;
      }
    });
  }

  @Override
  public void handle(KeyEvent ke) {
    if (ke.getEventType() == KeyEvent.KEY_RELEASED) {
      if (ke.getCode() == KeyCode.WINDOWS) {
        windowsKeyDown = false;
      }
      return;
    }

    if (ke.getCode() == KeyCode.WINDOWS) {
      windowsKeyDown = true;
      return;
    }

    if (windowsKeyDown && handleWindowsArrowShortcut(ke)) {
      return;
    }

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
    else if (ke.getCode() == KeyCode.N && ke.isShiftDown() && ke.isControlDown()) {
      FileSearchDialogController.show(stage, Studio.getCurrentProject());
      ke.consume();
    }
    else if (ke.getCode() == KeyCode.F && ke.isShiftDown() && ke.isControlDown()) {
      FileSearchDialogController.show(stage, Studio.getCurrentProject(), FileSearchDialogController.SearchMode.FIND_IN_FILES);
      ke.consume();
    }
  }

  /**
   * Handles Win+Left/Right/Up/Down the same way a regular (decorated) Windows app would via
   * Aero Snap. Returns true if the key was handled and consumed.
   */
  private boolean handleWindowsArrowShortcut(KeyEvent ke) {
    if (!(stage.getUserData() instanceof FXResizeHelper helper)) {
      return false;
    }

    // Windows sometimes drops the extended-key flag on Win+Arrow combos, which makes the OS
    // report the dedicated arrow keys as their numpad (KP_*) equivalents instead - handle both.
    switch (ke.getCode()) {
      case LEFT, KP_LEFT -> helper.snapLeft();
      case RIGHT, KP_RIGHT -> helper.snapRight();
      case UP, KP_UP -> helper.maximize();
      case DOWN, KP_DOWN -> helper.restoreOrMinimize();
      default -> {
        return false;
      }
    }
    ke.consume();
    return true;
  }

  private void resize(double width, double height) {
    stage.setWidth(width);
    stage.setHeight(height);
  }
}
