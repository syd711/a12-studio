package de.a12.studio.ui;

import de.a12.studio.commons.util.FXResizeHelper;
import de.a12.studio.ui.system.UpdateController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * App bootstrap - undecorated window shell (custom draggable/resizable header, empty main
 * region). Trimmed port of vpin-studio's de.mephisto.vpin.ui.Studio: no splash screen, no
 * REST-client connection bootstrap, no single-instance check - a12-studio-server has none of
 * that machinery yet.
 */
public class A12StudioApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(A12StudioApp.class.getResource("scene-root.fxml"));
        Parent root = loader.load();

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double width = 1280;
        double height = 800;

        Scene scene = new Scene(root, width, height, Color.TRANSPARENT);
        stage.setTitle("A12 Studio");
        stage.setScene(scene);
        stage.setMinWidth(1280);
        stage.setMinHeight(700);
        stage.setResizable(true);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setX((screenBounds.getWidth() / 2) - (width / 2));
        stage.setY((screenBounds.getHeight() / 2) - (height / 2));

        FXResizeHelper.install(stage, 30, 6);
        stage.show();

        new UpdateController(stage).checkForUpdateAsync();
    }
}
