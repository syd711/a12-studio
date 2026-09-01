package de.a12.studio.ui.previewapp;

import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Floating console window that streams {@link PreviewAppProcess} stdout/stderr.
 *
 * <p>Converted from a raw {@link Stage} to a proper studio dialog (custom window header,
 * resizable via {@link de.a12.studio.ui.util.FXResizeHelper}) that matches the look of every
 * other dialog in the application.
 *
 * <p>The instance stays alive (hidden, not disposed) across start/stop cycles so scrollback is
 * preserved. The shared {@link TextArea} can be reparented into the docked console panel inside
 * the main view via {@link #getController()}, and back again when undocked.
 */
@Slf4j
public class PreviewAppLogWindow {

  private static PreviewAppLogWindow instance;

  private final Stage stage;
  private final PreviewAppConsoleController controller;

  private PreviewAppLogWindow(Window owner) {
    FXMLLoader loader = new FXMLLoader(
        PreviewAppLogWindow.class.getResource("scene-preview-app-console.fxml"));
    loader.setResources(StudioBundle.getBundle());

    Parent root;
    try {
      root = loader.load();
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not load preview app console FXML", e);
    }
    controller = loader.getController();

    root.getStyleClass().add("root");
    root.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.35), 12, 0, 0, 2));

    stage = WidgetFactory.createStage();
    stage.initOwner(owner);

    // Position/size persistence key matches the FXML base name.
    String stateId = "scene-preview-app-console";

    // Wire the header's stage reference (title is set dynamically by the controller).
    var header = root.lookup("#header");
    if (header != null && header.getUserData() instanceof de.a12.studio.ui.components.DialogHeaderController dhc) {
      dhc.setStage(stage);
    }

    // Wrap in shadow padding just like WidgetFactory.createDialogStage does. A StackPane with an
    // explicit Background.EMPTY (not a BorderPane with an inline "-fx-background-color:
    // transparent" style) is required here: JavaFX auto-adds the "root" style class to whatever
    // node ends up as the actual Scene root, and stylesheet.css's ".root"/".main" rule paints that
    // node with -fx-surface-color - only an explicitly-set Background (which CSS won't override)
    // keeps the shadow-margin area transparent instead of showing as a thick surface-colored frame.
    int shadowMargin = 14;
    StackPane shadowWrapper = new StackPane(root);
    shadowWrapper.setPadding(new Insets(shadowMargin));
    shadowWrapper.setBackground(Background.EMPTY);
    shadowWrapper.setPickOnBounds(false);

    Scene scene = new Scene(shadowWrapper, 960 + shadowMargin * 2, 580 + shadowMargin * 2, Color.TRANSPARENT);
    stage.setScene(scene);
    stage.setResizable(true);
    stage.setMinWidth(480 + shadowMargin * 2);
    stage.setMinHeight(280 + shadowMargin * 2);
    stage.setTitle(StudioBundle.get("preview_app_console"));

    // Restore saved size/position.
    var saved = de.a12.studio.ui.util.localsettings.LocalUISettings.getPosition(stateId);
    if (saved != null && saved.getX() >= 0) {
      stage.setX(saved.getX());
      stage.setY(saved.getY());
      if (saved.getWidth() > 0) stage.setWidth(saved.getWidth());
      if (saved.getHeight() > 0) stage.setHeight(saved.getHeight());
    }

    // Persist position/size changes.
    var debouncer = new de.a12.studio.ui.util.Debouncer();
    stage.xProperty().addListener((o, ov, nv) -> persistPosition(stateId, debouncer));
    stage.yProperty().addListener((o, ov, nv) -> persistPosition(stateId, debouncer));
    stage.widthProperty().addListener((o, ov, nv) -> persistPosition(stateId, debouncer));
    stage.heightProperty().addListener((o, ov, nv) -> persistPosition(stateId, debouncer));

    // Install edge-resize (same margin as WidgetFactory.makeResizable).
    de.a12.studio.ui.util.FXResizeHelper.install(stage, 30, 6, shadowMargin);

    stage.setOnCloseRequest(event -> {
      event.consume();
      stage.hide();
    });
  }

  private static void persistPosition(String stateId, Debouncer debouncer) {
    debouncer.debounce(stateId, () -> {
      if (instance != null) {
        LocalUISettings.saveLocation(stateId,
            (int) instance.stage.getX(), (int) instance.stage.getY(),
            (int) instance.stage.getWidth(), (int) instance.stage.getHeight());
      }
    }, 50);
  }

  /** Returns the controller, which exposes the shared TextArea and dock-action wiring. */
  public PreviewAppConsoleController getController() {
    return controller;
  }

  /** Shows the floating window (creates the singleton on first call). */
  public static synchronized void show(Window owner) {
    if (instance == null) {
      instance = new PreviewAppLogWindow(owner);
    }
    instance.stage.show();
    instance.stage.toFront();
  }

  public static synchronized void hide() {
    if (instance != null) {
      instance.stage.hide();
    }
  }

  public static synchronized boolean isShowing() {
    return instance != null && instance.stage.isShowing();
  }

  /** Provides access to the singleton controller (may return null before first {@link #show}). */
  public static synchronized PreviewAppConsoleController getInstanceController(Window owner) {
    if (instance == null) {
      instance = new PreviewAppLogWindow(owner);
    }
    return instance.controller;
  }
}
