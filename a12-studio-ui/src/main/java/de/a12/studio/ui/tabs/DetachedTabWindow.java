package de.a12.studio.ui.tabs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * A separate window showing the editor of a tab that was taken out of the {@link
 * de.a12.studio.ui.components.StudioTabPane} ("Open Tab in New Window"). The window owns the already built
 * editor content from then on and is not tied to the tab pane in any way; the tab pane only keeps a reference
 * to it (see {@link TabPaneController}) so events about the model (delete, rename, revert, ...) still reach it,
 * and so the window's dock button can put the tab back.
 *
 * <p>Built like the main window: an undecorated, transparent stage with the custom window header (see {@link
 * DetachedTabHeaderController}, which adds a dock button to the main header's buttons) and edge resizing via
 * {@link FXResizeHelper}.
 */
@Slf4j
class DetachedTabWindow {

  private static final double DEFAULT_WIDTH = 1100;
  private static final double DEFAULT_HEIGHT = 750;
  private static final double MIN_WIDTH = 640;
  private static final double MIN_HEIGHT = 420;
  private static final double BOTTOM_EDGE_HEIGHT = 10;

  private final Stage stage = WidgetFactory.createStage();
  private final BorderPane root = new BorderPane();
  private final DetachedTabHeaderController headerController;
  private ProjectItem item;

  /**
   * @param onDock   called when the user clicks the header's dock button
   * @param onHidden called once the window has been closed, by the user or through {@link #close()}; the window
   *                 is passed along because {@link #getItem()} changes when the model is renamed
   */
  DetachedTabWindow(@NonNull ProjectItem item, @NonNull Parent content, @NonNull Consumer<DetachedTabWindow> onDock,
                    @NonNull Consumer<DetachedTabWindow> onHidden) {
    this.item = item;

    FXMLLoader loader = new FXMLLoader(getClass().getResource("detached-tab-header.fxml"));
    loader.setResources(StudioBundle.getBundle());
    try {
      root.setTop(loader.load());
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not load detached tab header FXML", e);
    }
    headerController = loader.getController();
    headerController.setStage(stage);
    headerController.setTitle(title());
    headerController.setOnDockAction(() -> onDock.accept(this));

    root.setCenter(content);
    root.setBottom(createBottomEdge());
    root.getStylesheets().add(getClass().getResource("/de/a12/studio/ui/stylesheet.css").toExternalForm());
    WidgetFactory.applyFontSize(root);

    Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT, Color.TRANSPARENT);
    scene.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);

    stage.setScene(scene);
    stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-180.png")));
    stage.setTitle(title());
    stage.setMinWidth(MIN_WIDTH);
    stage.setMinHeight(MIN_HEIGHT);
    stage.setOnHidden(event -> onHidden.accept(this));
    FXResizeHelper.install(stage, 30, 6);
  }

  /**
   * A thin strip styled like the main window's footer bar. The editor content is square, so without it the
   * content paints over the root's rounded bottom corners and border; the strip has the rounded bottom corners
   * (and drops them while maximized, see stylesheet.css) instead, like the footer does in the main window.
   */
  private static Region createBottomEdge() {
    Region edge = new Region();
    edge.getStyleClass().add("footer-bar");
    edge.setMinHeight(BOTTOM_EDGE_HEIGHT);
    edge.setPrefHeight(BOTTOM_EDGE_HEIGHT);
    edge.setMaxHeight(BOTTOM_EDGE_HEIGHT);
    return edge;
  }

  ProjectItem getItem() {
    return item;
  }

  void show() {
    stage.show();
    stage.toFront();
  }

  void focus() {
    if (stage.isIconified()) {
      stage.setIconified(false);
    }
    stage.toFront();
    stage.requestFocus();
  }

  void close() {
    stage.close();
  }

  /** Detaches the editor content from this window, e.g. to move it back into a tab. */
  Parent takeContent() {
    Parent content = (Parent) root.getCenter();
    root.setCenter(null);
    return content;
  }

  /** Swaps in freshly built editor content, e.g. after the model was reverted or refactored. */
  void setContent(@NonNull Parent content) {
    root.setCenter(content);
  }

  /** Follows a rename of the shown model. */
  void setItem(@NonNull ProjectItem item) {
    this.item = item;
    stage.setTitle(title());
    headerController.setTitle(title());
  }

  private String title() {
    return item.getDisplayName();
  }

  /**
   * The main window's global shortcuts are not installed here, and the ones that go through
   * {@link Studio#getSelectedProjectItem()} would act on the main window's tab anyway, so only the two that
   * make sense for a single-model window are handled, on this window's own model.
   */
  private void onKeyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.S && event.isControlDown()) {
      item.save();
      StudioEventManager.getInstance().fireModelSavedEvent(item);
      event.consume();
    }
    else if (event.getCode() == KeyCode.W && event.isControlDown()) {
      close();
      event.consume();
    }
  }
}
