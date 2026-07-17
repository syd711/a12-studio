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
import javafx.scene.input.KeyCode;
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
    scene.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
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

  private void onKeyPressed(@NonNull KeyEvent event) {
    if (event.isControlDown() && event.getCode() == KeyCode.S) {
      ProjectItem projectItem = rootController.getSelectedProjectItem();
      if (projectItem != null) {
        StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
      }
      event.consume();
    }
  }

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    Project project = event.getProject();
    stage.setTitle("A12 Studio - " + project.getName());
    rootController.setTitle("A12 Studio - " + project.getName() + " (" + project.getRoot().getPath() + ")");
  }
}
