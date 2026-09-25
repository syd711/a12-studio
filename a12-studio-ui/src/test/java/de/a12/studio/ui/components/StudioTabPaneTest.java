package de.a12.studio.ui.components;

import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// The tab pane with a real JavaFX toolkit: both header layouts, switching between them, and the multi-row skin's tab handling.
class StudioTabPaneTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
    if (toolkitAvailable) {
      // Every test closes its stage; without this the toolkit would shut down with the last window and hang whatever runs next.
      FxTestSupport.onFx(() -> Platform.setImplicitExit(false));
    }
  }

  /** A pane of {@code tabCount} tabs, {@code width} wide, shown in an off-screen stage and laid out. */
  private static StudioTabPane show(int tabCount, double width, boolean multiRow) throws Exception {
    return FxTestSupport.onFx(() -> {
      StudioTabPane pane = new StudioTabPane();
      pane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
      pane.setMultiRowHeader(multiRow);
      for (int i = 0; i < tabCount; i++) {
        Tab tab = new Tab("Model_Number_" + i, new Label("content " + i));
        pane.getTabs().add(tab);
      }
      // The pane is capped to `width` inside a much wider scene, so a test can resize it through setMaxWidth.
      pane.setMaxWidth(width);
      Scene scene = new Scene(new StackPane(pane), 2000, 300);
      Stage stage = new Stage();
      stage.setScene(scene);
      stage.setX(-10000);
      stage.show();
      layout(pane);
      return pane;
    });
  }

  private static void layout(StudioTabPane pane) {
    // From the scene root, not the pane: resizing the pane is its parent's job.
    pane.getScene().getRoot().applyCss();
    pane.getScene().getRoot().layout();
  }

  private static List<Node> headers(StudioTabPane pane) {
    return new ArrayList<>(pane.lookupAll(".tab-header-area > .tab"));
  }

  private static int rowCount(StudioTabPane pane) {
    Set<Double> rows = new HashSet<>();
    for (Node header : headers(pane)) {
      rows.add(header.getLayoutY());
    }
    return rows.size();
  }

  /** A tab's content is shown or hidden through the wrapper the skin puts around it, not on the content node itself. */
  private static boolean shown(Node content) {
    return content.isVisible() && content.getParent().isVisible();
  }

  private static void close(StudioTabPane pane) throws Exception {
    FxTestSupport.onFx(() -> ((Stage) pane.getScene().getWindow()).close());
  }

  @Test
  void wrapsTheTabHeadersOntoMoreRowsTheNarrowerThePaneIs() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    StudioTabPane pane = show(12, 500, true);
    try {
      FxTestSupport.onFx(() -> {
        assertInstanceOf(MultiRowTabPaneSkin.class, pane.getSkin());
        assertEquals(12, headers(pane).size());
        int rowsAt500 = rowCount(pane);
        assertTrue(rowsAt500 > 1, "12 tabs must not fit one row of 500px, rows=" + rowsAt500);
        for (Node header : headers(pane)) {
          assertTrue(header.getLayoutX() + header.getLayoutBounds().getWidth() <= 500.5, "header sticks out: " + header);
        }

        pane.setMaxWidth(1900);
        layout(pane);
        assertEquals(1, rowCount(pane));

        pane.setMaxWidth(300);
        layout(pane);
        assertTrue(rowCount(pane) > rowsAt500);
      });
    }
    finally {
      close(pane);
    }
  }

  @Test
  void everyHeaderRowHasItsOwnRuleBelowItsTabs() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    StudioTabPane pane = show(12, 500, true);
    try {
      FxTestSupport.onFx(() -> {
        for (double width : new double[]{500, 300, 1900}) {
          pane.setMaxWidth(width);
          layout(pane);
          List<Node> lines = new ArrayList<>(pane.lookupAll(".tab-header-row-line"));
          lines.sort(java.util.Comparator.comparingDouble(Node::getLayoutY));
          assertEquals(rowCount(pane), lines.size(), "one rule per row at width " + width);
          List<Double> rowTops = headers(pane).stream().map(Node::getLayoutY).distinct().sorted().toList();
          for (int row = 0; row < lines.size(); row++) {
            double rowBottom = rowTops.get(row) + headers(pane).get(0).getLayoutBounds().getHeight();
            assertTrue(lines.get(row).getLayoutY() >= rowBottom - 0.5, "rule " + row + " below its tabs, not over them");
            assertTrue(row + 1 == lines.size() || lines.get(row).getLayoutY() < rowTops.get(row + 1), "rule " + row + " above the next row");
            assertEquals(pane.getWidth(), lines.get(row).getLayoutBounds().getWidth(), 0.5, "rule spans the strip");
          }
        }
      });
    }
    finally {
      close(pane);
    }
  }

  @Test
  void multiRowIsTheDefault() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    assertTrue(FxTestSupport.onFx(() -> new StudioTabPane().isMultiRowHeader()));
  }

  @Test
  void singleRowModeKeepsTheRegularSkin() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    StudioTabPane pane = show(3, 500, false);
    try {
      assertInstanceOf(TabPaneSkin.class, pane.getSkin());
      assertFalse(pane.getStyleClass().contains("multi-row-tabs"));
    }
    finally {
      close(pane);
    }
  }

  @Test
  void switchingTheOptionSwapsTheLayoutAndKeepsSelectionAndContent() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    StudioTabPane pane = show(6, 500, false);
    try {
      FxTestSupport.onFx(() -> {
        pane.getSelectionModel().select(2);
        Node content = pane.getTabs().get(2).getContent();

        pane.setMultiRowHeader(true);
        layout(pane);
        assertInstanceOf(MultiRowTabPaneSkin.class, pane.getSkin());
        assertTrue(pane.getStyleClass().contains("multi-row-tabs"));
        assertEquals(6, headers(pane).size());
        assertEquals(2, pane.getSelectionModel().getSelectedIndex());
        assertSame(pane.getScene(), content.getScene());
        assertTrue(shown(content));

        pane.setMultiRowHeader(false);
        layout(pane);
        assertInstanceOf(TabPaneSkin.class, pane.getSkin());
        assertFalse(pane.getStyleClass().contains("multi-row-tabs"));
        assertTrue(pane.lookupAll(".tab-header-area > .tab").isEmpty(), "multi-row headers must be gone");
        assertEquals(2, pane.getSelectionModel().getSelectedIndex());
        assertSame(pane.getScene(), content.getScene());
        assertTrue(shown(content));

        pane.setMultiRowHeader(true);
        layout(pane);
        assertEquals(6, headers(pane).size());
        assertEquals(1, pane.lookupAll(".tab-header-area").size());
      });
    }
    finally {
      close(pane);
    }
  }

  @Test
  void clickingAHeaderSelectsItsTabAndOnlyThatContentIsShown() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    StudioTabPane pane = show(4, 500, true);
    try {
      FxTestSupport.onFx(() -> {
        assertEquals(0, pane.getSelectionModel().getSelectedIndex());
        assertTrue(headers(pane).get(0).getPseudoClassStates().stream().anyMatch(state -> state.getPseudoClassName().equals("selected")));

        Event.fireEvent(headers(pane).get(3), mouse(MouseEvent.MOUSE_PRESSED));
        layout(pane);

        assertEquals(3, pane.getSelectionModel().getSelectedIndex());
        for (int i = 0; i < 4; i++) {
          assertEquals(i == 3, shown(pane.getTabs().get(i).getContent()), "content " + i);
          assertEquals(i == 3, headers(pane).get(i).getPseudoClassStates().stream()
              .anyMatch(state -> state.getPseudoClassName().equals("selected")), "header " + i);
        }
      });
    }
    finally {
      close(pane);
    }
  }

  @Test
  void contentSetAfterTheTabWasAddedShowsUp() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    StudioTabPane pane = show(2, 500, true);
    try {
      FxTestSupport.onFx(() -> {
        Tab lazy = new Tab("Lazy");
        pane.getTabs().add(lazy);
        pane.getSelectionModel().select(lazy);
        layout(pane);
        assertNotNull(lazy);

        Label content = new Label("built later");
        lazy.setContent(content);
        layout(pane);
        assertSame(pane.getScene(), content.getScene());
        assertTrue(shown(content));
      });
    }
    finally {
      close(pane);
    }
  }

  @Test
  void closeButtonFollowsTheClosingPolicyAndHonoursCloseRequests() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    StudioTabPane pane = show(3, 500, true);
    try {
      FxTestSupport.onFx(() -> {
        AtomicInteger closed = new AtomicInteger();
        Tab second = pane.getTabs().get(1);
        second.setOnClosed(event -> closed.incrementAndGet());

        Node closeButton = headers(pane).get(1).lookup(".tab-close-button");
        assertTrue(closeButton.isVisible());

        // A consumed close request vetoes the close.
        second.setOnCloseRequest(Event::consume);
        Event.fireEvent(closeButton, mouse(MouseEvent.MOUSE_CLICKED));
        assertEquals(3, pane.getTabs().size());
        assertEquals(0, closed.get());

        second.setOnCloseRequest(null);
        Event.fireEvent(closeButton, mouse(MouseEvent.MOUSE_CLICKED));
        layout(pane);
        assertEquals(2, pane.getTabs().size());
        assertEquals(1, closed.get());
        assertEquals(2, headers(pane).size());

        // Only the selected tab shows its close button with SELECTED_TAB; none with UNAVAILABLE.
        pane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        layout(pane);
        int selected = pane.getSelectionModel().getSelectedIndex();
        for (int i = 0; i < 2; i++) {
          assertEquals(i == selected, headers(pane).get(i).lookup(".tab-close-button").isVisible());
        }
        pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        layout(pane);
        assertFalse(headers(pane).get(0).lookup(".tab-close-button").isVisible());
      });
    }
    finally {
      close(pane);
    }
  }

  @Test
  void headerFollowsTheTabsTextStyleClassesAndDisabledState() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    StudioTabPane pane = show(2, 500, true);
    try {
      FxTestSupport.onFx(() -> {
        Tab tab = pane.getTabs().get(1);
        tab.setText("Renamed");
        tab.getStyleClass().add("model-tab-form");
        tab.setDisable(true);
        layout(pane);

        Node header = headers(pane).get(1);
        assertEquals("Renamed", ((Label) header.lookup(".tab-label")).getText());
        assertTrue(header.getStyleClass().contains("model-tab-form"));
        assertTrue(header.isDisabled());

        tab.getStyleClass().remove("model-tab-form");
        assertFalse(header.getStyleClass().contains("model-tab-form"));
        assertTrue(header.getStyleClass().contains("tab"));
      });
    }
    finally {
      close(pane);
    }
  }

  private static MouseEvent mouse(javafx.event.EventType<MouseEvent> type) {
    return new MouseEvent(type, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
        false, false, false, false, true, false, false, true, false, false, null);
  }
}
