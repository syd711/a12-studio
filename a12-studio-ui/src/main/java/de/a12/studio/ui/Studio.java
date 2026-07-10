package de.a12.studio.ui;

import de.a12.studio.commons.util.FXResizeHelper;
import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.system.UpdateController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

/**
 * App bootstrap - undecorated window shell (custom draggable/resizable header, empty main
 * region). Trimmed port of vpin-studio's de.mephisto.vpin.ui.Studio: no splash screen, no
 * REST-client connection bootstrap, no single-instance check - a12-studio-server has none of
 * that machinery yet.
 */
public class Studio extends Application implements StudioEventListener {

  public static Stage stage;
  private RootController rootController;

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

    Scene scene = new Scene(root, width, height, Color.TRANSPARENT);
    stage.setTitle("A12 Studio");
    stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-150.png")));
    stage.setScene(scene);
    stage.setMinWidth(width);
    stage.setMinHeight(height);
    stage.setResizable(true);
    stage.initStyle(StageStyle.TRANSPARENT);
    stage.setX((screenBounds.getWidth() / 2) - (width / 2));
    stage.setY((screenBounds.getHeight() / 2) - (height / 2));

    FXResizeHelper.install(stage, 30, 6);
    stage.show();

    // Windows denies focus/z-order to windows created by a background process (e.g. launched
    // from IDEA), so the stage can open behind the IDE. Toggling always-on-top forces it front.
    stage.setAlwaysOnTop(true);
    stage.toFront();
    stage.requestFocus();
    stage.setAlwaysOnTop(false);

    new UpdateController(stage).checkForUpdateAsync();
  }


  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    Project project = event.getProject();
    stage.setTitle("A12 Studio - " + project.getName());
    rootController.setTitle("A12 Studio - " + project.getName());
  }
}
