package de.a12.studio.ui;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.FileSearchDialogController;
import de.a12.studio.ui.components.RecentFilesDialogController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.updater.Dialogs;
import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.StudioBundle;
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
      new Shortcut(StudioBundle.get("ctrl_n"), StudioBundle.get("new_project")),
      new Shortcut(StudioBundle.get("ctrl_o"), StudioBundle.get("open_project")),
      new Shortcut(StudioBundle.get("ctrl_s"), StudioBundle.get("save_the_active_model")),
      new Shortcut(StudioBundle.get("ctrl_w"), StudioBundle.get("close_the_selected_tab")),
      new Shortcut(StudioBundle.get("ctrl_tab"), StudioBundle.get("select_the_next_tab")),
      new Shortcut(StudioBundle.get("ctrl_shift_tab"), StudioBundle.get("select_the_previous_tab")),
      new Shortcut(StudioBundle.get("ctrl_alt_p"), StudioBundle.get("open_preferences")),
      new Shortcut(StudioBundle.get("ctrl_shift_n"), StudioBundle.get("search_files")),
      new Shortcut(StudioBundle.get("ctrl_shift_f"), StudioBundle.get("find_in_files")),
      new Shortcut(StudioBundle.get("ctrl_e"), StudioBundle.get("recent_files")),
      new Shortcut(StudioBundle.get("ctrl_alt_u"), StudioBundle.get("show_update_info")),
      new Shortcut(StudioBundle.get("ctrl_alt_h"), StudioBundle.get("resize_window_to_1920x1080")),
      new Shortcut(StudioBundle.get("ctrl_alt_w"), StudioBundle.get("resize_window_to_2560x1440")),
      new Shortcut(StudioBundle.get("win_left"), StudioBundle.get("snap_window_to_the_left_half_of_the_screen")),
      new Shortcut(StudioBundle.get("win_right"), StudioBundle.get("snap_window_to_the_right_half_of_the_screen")),
      new Shortcut(StudioBundle.get("win_up"), StudioBundle.get("maximize_the_window")),
      new Shortcut(StudioBundle.get("win_down"), StudioBundle.get("restore_then_minimize_the_window"))
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
        projectItem.save();
        StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
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
    else if (ke.getCode() == KeyCode.E && ke.isControlDown()) {
      RecentFilesDialogController.show(stage, Studio.getCurrentProject());
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
