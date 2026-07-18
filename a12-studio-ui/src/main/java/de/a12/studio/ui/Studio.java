package de.a12.studio.ui;

import de.a12.studio.commons.util.FXResizeHelper;
import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

/**
 * App bootstrap - undecorated window shell (custom draggable/resizable header, empty main
 * region).
 */
public class Studio extends Application implements StudioEventListener {

  public static Stage stage;
  private static RootController rootController;
  private static Project currentProject;

  @Override
  public void start(Stage stage) throws IOException {
    Studio.stage = stage;

    StudioEventManager.getInstance().addListener(this);

    FXMLLoader loader = new FXMLLoader(Studio.class.getResource("scene-root.fxml"));
    Parent root = loader.load();
    rootController = loader.getController();

    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    double width = 1480;
    double height = 900;

    Rectangle position = LocalUISettings.getPosition();
    if (position.getWidth() > width && position.getHeight() > height) {
      width = position.getWidth();
      height = position.getHeight();
    }

    Scene scene = new Scene(root, width, height, Color.TRANSPARENT);
    scene.addEventHandler(KeyEvent.KEY_PRESSED, new StudioKeyEventHandler(stage));
    stage.setTitle("A12 Studio");
    stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-150.png")));
    stage.setScene(scene);
    stage.setMinWidth(1480);
    stage.setMinHeight(900);
    stage.setResizable(true);
    stage.initStyle(StageStyle.TRANSPARENT);
    if (position.getX() != -1) {
      stage.setX(position.getX());
      stage.setY(position.getY());
    }
    else {
      stage.setX((screenBounds.getWidth() / 2) - (width / 2));
      stage.setY((screenBounds.getHeight() / 2) - (height / 2));
    }

    FXResizeHelper.install(stage, 30, 6);
    stage.show();

    // Windows denies focus/z-order to windows created by a background process (e.g. launched
    // from IDEA), so the stage can open behind the IDE. Toggling always-on-top forces it front.
    stage.setAlwaysOnTop(true);
    stage.toFront();
    stage.requestFocus();
    stage.setAlwaysOnTop(false);
  }

  public static ProjectItem getSelectedProjectItem() {
    return rootController.getSelectedProjectItem();
  }

  public static Project getCurrentProject() {
    return currentProject;
  }

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    // Set before anything else: Studio registers as a listener before the FXML (and its nested
    // controllers, e.g. TabPaneController) is loaded, so this runs before TabPaneController restores
    // previously-open tabs - those tabs load their document model editors immediately and need the
    // project to already be available for cross-model settings validation (e.g. the settings button's
    // error badge).
    currentProject = event.getProject();

    stage.setTitle("A12 Studio - " + currentProject.getName());
    rootController.setTitle("A12 Studio - " + currentProject.getName() + " (" + currentProject.getRoot().getPath() + ")");
  }
}
