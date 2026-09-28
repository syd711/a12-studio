package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.ui.components.DialogHeaderController;
import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * A floating, undecorated window that shows the Content Model preview {@code WebView} on its own (see "Open in a
 * Separate Window" on {@link ContentModelEditorController}'s preview toolbar). The {@code WebView} node itself is
 * moved into this window rather than a second one being loaded, so the loaded page, its JS selection bridge and
 * the preview server session keep working unchanged; closing the window (its header's close button, or the OS
 * close) hands the node back via {@code onClosed}.
 */
@Slf4j
class ContentPreviewWindow {

  private static final double DEFAULT_WIDTH = 900;
  private static final double DEFAULT_HEIGHT = 700;
  private static final double MIN_WIDTH = 400;
  private static final double MIN_HEIGHT = 300;
  private static final int SHADOW_MARGIN = 14;

  private final Stage stage = WidgetFactory.createStage();

  ContentPreviewWindow(@NonNull Window owner, @NonNull String title, @NonNull Node content, @NonNull Consumer<ContentPreviewWindow> onClosed) {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/de/a12/studio/ui/components/scene-dialog-header.fxml"));
    loader.setResources(StudioBundle.getBundle());
    Parent header;
    try {
      header = loader.load();
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not load preview window header FXML", e);
    }
    DialogHeaderController headerController = loader.getController();
    headerController.setStage(stage);
    headerController.setTitle(title);

    BorderPane root = new BorderPane();
    root.getStyleClass().add("root");
    root.setTop(header);
    root.setCenter(content);
    root.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.35), 12, 0, 0, 2));
    root.getStylesheets().add(getClass().getResource("/de/a12/studio/ui/stylesheet.css").toExternalForm());
    WidgetFactory.applyFontSize(root);

    // Same shadow-margin wrapping trick as WidgetFactory.createDialogStage: the DropShadow above is
    // rasterized on top of root's own bounds, so the Scene needs extra transparent room around it.
    StackPane shadowWrapper = new StackPane(root);
    shadowWrapper.setPadding(new Insets(SHADOW_MARGIN));
    shadowWrapper.setBackground(Background.EMPTY);
    shadowWrapper.setPickOnBounds(false);

    Scene scene = new Scene(shadowWrapper, DEFAULT_WIDTH + SHADOW_MARGIN * 2, DEFAULT_HEIGHT + SHADOW_MARGIN * 2, Color.TRANSPARENT);
    stage.setScene(scene);
    stage.initOwner(owner);
    stage.setTitle(title);
    stage.setMinWidth(MIN_WIDTH + SHADOW_MARGIN * 2);
    stage.setMinHeight(MIN_HEIGHT + SHADOW_MARGIN * 2);
    FXResizeHelper.install(stage, 30, 6, SHADOW_MARGIN);
    stage.setOnHidden(event -> onClosed.accept(this));
  }

  void show() {
    stage.show();
    stage.toFront();
  }

  /** Closes the window; triggers the {@code onClosed} callback given to the constructor. */
  void close() {
    stage.close();
  }
}
