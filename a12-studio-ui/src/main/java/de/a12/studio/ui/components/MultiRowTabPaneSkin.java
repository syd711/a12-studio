package de.a12.studio.ui.components;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link TabPane} skin that wraps the tab headers onto as many rows as the available width needs, instead of
 * squeezing them into one row behind an overflow menu like the regular {@code TabPaneSkin}. Used by
 * {@link StudioTabPane} when its multi-row header option is on.
 *
 * <p>The scene graph mirrors the regular skin's style classes ({@code tab-header-area},
 * {@code tab-header-background}, {@code tab}, {@code tab-label}, {@code tab-close-button},
 * {@code tab-content-area}, and the {@code :selected} pseudo class on a tab), so the descendant-selector rules in
 * {@code stylesheet-tab-pane.css}/{@code stylesheet-model-colors.css} apply unchanged. Rows keep a stable order -
 * selecting a tab never moves it to another row - and every header of every row gets the same height, so
 * selecting a tab (whose bottom border is thicker) doesn't shift the rows below it. Every row has a rule
 * ({@code tab-header-row-line}) under it, spanning the whole strip.
 *
 * <p>Supports {@code Side.TOP} only, and the tab behavior the studio relies on: selection by click, the tab's
 * text/graphic/style/style classes/tooltip/context menu/disabled state, the closing policy with the
 * {@code onCloseRequest}/{@code onClosed} events, and lazily set tab content. It does not implement keyboard
 * navigation between headers.
 */
class MultiRowTabPaneSkin extends SkinBase<TabPane> {

  private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

  private static final double TAB_HGAP = 2;
  private static final double TAB_VGAP = 2;
  /** Thickness of the rule under every header row; its color comes from the {@code tab-header-row-line} style class. */
  private static final double ROW_LINE_HEIGHT = 1;

  private final Map<Tab, TabHeader> headers = new HashMap<>();
  private final Map<Tab, TabContent> contents = new HashMap<>();

  private final HeaderArea headerArea = new HeaderArea();
  private final StackPane contentArea = new StackPane();

  MultiRowTabPaneSkin(TabPane tabPane) {
    super(tabPane);

    contentArea.getStyleClass().add("tab-content-area");
    Rectangle clip = new Rectangle();
    clip.widthProperty().bind(contentArea.widthProperty());
    clip.heightProperty().bind(contentArea.heightProperty());
    contentArea.setClip(clip);

    getChildren().setAll(contentArea, headerArea);

    syncTabs();
    registerListChangeListener(tabPane.getTabs(), change -> syncTabs());
    registerChangeListener(tabPane.getSelectionModel().selectedItemProperty(), observable -> updateContentVisibility());
    registerChangeListener(tabPane.tabMinHeightProperty(), observable -> headerArea.requestLayout());
    registerChangeListener(tabPane.tabMinWidthProperty(), observable -> headerArea.requestLayout());
    registerChangeListener(tabPane.tabMaxWidthProperty(), observable -> headerArea.requestLayout());
  }

  /** Brings {@link #headers}/{@link #contents} in line with the tab list: creates what's new, disposes what's gone. */
  private void syncTabs() {
    List<Tab> tabs = getSkinnable().getTabs();

    headers.entrySet().removeIf(entry -> {
      if (tabs.contains(entry.getKey())) {
        return false;
      }
      entry.getValue().dispose();
      return true;
    });
    contents.entrySet().removeIf(entry -> {
      if (tabs.contains(entry.getKey())) {
        return false;
      }
      contentArea.getChildren().remove(entry.getValue());
      entry.getValue().dispose();
      return true;
    });

    List<TabHeader> orderedHeaders = new ArrayList<>();
    for (Tab tab : tabs) {
      orderedHeaders.add(headers.computeIfAbsent(tab, TabHeader::new));
      if (!contents.containsKey(tab)) {
        TabContent content = new TabContent(tab);
        contents.put(tab, content);
        contentArea.getChildren().add(content);
      }
    }
    headerArea.setHeaders(orderedHeaders);
    headerArea.setVisible(!tabs.isEmpty());
    headerArea.setManaged(!tabs.isEmpty());
    updateContentVisibility();
  }

  /** Only the selected tab's content takes part in layout; the others stay in the scene graph, hidden, so they keep their state. */
  private void updateContentVisibility() {
    Tab selected = getSkinnable().getSelectionModel().getSelectedItem();
    contents.forEach((tab, content) -> {
      boolean isSelected = tab == selected;
      content.setVisible(isSelected);
      content.setManaged(isSelected);
    });
  }

  /** Same close protocol as the regular skin: close request (vetoable), removal, then the tab's {@code onClosed} handler. */
  private void closeTab(Tab tab) {
    Event closeRequest = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
    Event.fireEvent(tab, closeRequest);
    if (closeRequest.isConsumed()) {
      return;
    }
    getSkinnable().getTabs().remove(tab);
    EventHandler<Event> onClosed = tab.getOnClosed();
    if (onClosed != null) {
      onClosed.handle(new Event(Tab.CLOSED_EVENT));
    }
  }

  @Override
  protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
    double headerHeight = headerArea.isManaged() ? Math.min(headerArea.prefHeight(contentWidth), contentHeight) : 0;
    headerArea.resizeRelocate(contentX, contentY, contentWidth, headerHeight);
    contentArea.resizeRelocate(contentX, contentY + headerHeight, contentWidth, Math.max(0, contentHeight - headerHeight));
  }

  @Override
  protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
    return leftInset + rightInset + Math.max(headerArea.minWidth(-1), contentArea.minWidth(-1));
  }

  @Override
  protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
    return topInset + bottomInset + headerHeight(-1) + contentArea.minHeight(-1);
  }

  @Override
  protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
    return leftInset + rightInset + Math.max(headerArea.prefWidth(-1), contentArea.prefWidth(-1));
  }

  @Override
  protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
    double contentWidth = width < 0 ? -1 : width - leftInset - rightInset;
    return topInset + bottomInset + headerHeight(contentWidth) + contentArea.prefHeight(contentWidth);
  }

  @Override
  protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
    return Double.MAX_VALUE;
  }

  @Override
  protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
    return Double.MAX_VALUE;
  }

  private double headerHeight(double width) {
    return headerArea.isManaged() ? headerArea.prefHeight(width) : 0;
  }

  @Override
  public void dispose() {
    if (getSkinnable() == null) {
      return;
    }
    headers.values().forEach(TabHeader::dispose);
    contents.values().forEach(TabContent::dispose);
    headers.clear();
    contents.clear();
    headerArea.setHeaders(List.of());
    contentArea.getChildren().clear();
    getChildren().removeAll(contentArea, headerArea);
    super.dispose();
  }

  /** The strip behind all tab headers: paints the background, and lays the headers out in wrapping rows. */
  private final class HeaderArea extends Region {

    private final Region background = new Region();
    private final List<Region> rowLines = new ArrayList<>();

    HeaderArea() {
      getStyleClass().add("tab-header-area");
      background.getStyleClass().add("tab-header-background");
      background.setManaged(false);
      getChildren().add(background);
    }

    void setHeaders(List<TabHeader> tabHeaders) {
      List<Node> nodes = new ArrayList<>();
      nodes.add(background);
      nodes.addAll(rowLines);
      nodes.addAll(tabHeaders);
      if (!getChildren().equals(nodes)) {
        getChildren().setAll(nodes);
      }
    }

    private List<TabHeader> tabHeaders() {
      List<TabHeader> result = new ArrayList<>();
      for (Node child : getChildren()) {
        if (child instanceof TabHeader header) {
          result.add(header);
        }
      }
      return result;
    }

    /** Every header gets the tallest header's height (at least {@code tabMinHeight}), so all rows are equally tall. */
    private double rowHeight(List<TabHeader> tabHeaders) {
      double height = getSkinnable().getTabMinHeight();
      for (TabHeader header : tabHeaders) {
        height = Math.max(height, header.prefHeight(-1));
      }
      return height;
    }

    private double headerWidth(TabHeader header, double availableWidth) {
      TabPane tabPane = getSkinnable();
      double width = Math.max(header.prefWidth(-1), tabPane.getTabMinWidth());
      width = Math.min(width, tabPane.getTabMaxWidth());
      return Math.max(0, Math.min(width, availableWidth));
    }

    /**
     * Fills rows left to right, starting a new row whenever the next header wouldn't fit any more (a header
     * that alone is wider than a row is shrunk to the row instead). Returns the number of rows; positions the
     * headers only if {@code apply}, so the same pass serves the preferred-height computation. Each row is
     * followed by {@link #ROW_LINE_HEIGHT} of space for its rule (see {@link #layoutRowLines}).
     */
    private int flow(List<TabHeader> tabHeaders, double availableWidth, boolean apply) {
      Insets insets = getInsets();
      double rowHeight = rowHeight(tabHeaders);
      int rows = 1;
      double x = 0;
      double y = insets.getTop();
      for (TabHeader header : tabHeaders) {
        double width = headerWidth(header, availableWidth);
        if (x > 0 && x + width > availableWidth) {
          rows++;
          x = 0;
          y += rowHeight + ROW_LINE_HEIGHT + TAB_VGAP;
        }
        if (apply) {
          header.resizeRelocate(snapPositionX(insets.getLeft() + x), snapPositionY(y), width, rowHeight);
        }
        x += width + TAB_HGAP;
      }
      return rows;
    }

    private double availableWidth(double width) {
      Insets insets = getInsets();
      return width < 0 ? Double.MAX_VALUE : Math.max(0, width - insets.getLeft() - insets.getRight());
    }

    @Override
    protected void layoutChildren() {
      background.resizeRelocate(0, 0, getWidth(), getHeight());
      List<TabHeader> tabHeaders = tabHeaders();
      int rows = flow(tabHeaders, availableWidth(getWidth()), true);
      layoutRowLines(tabHeaders.isEmpty() ? 0 : rows, rowHeight(tabHeaders));
    }

    /** One rule under each row, across the whole strip; created/dropped here since the row count is only known once laid out. */
    private void layoutRowLines(int rows, double rowHeight) {
      while (rowLines.size() < rows) {
        Region line = new Region();
        line.getStyleClass().add("tab-header-row-line");
        line.setManaged(false);
        line.setMouseTransparent(true);
        rowLines.add(line);
      }
      while (rowLines.size() > rows) {
        rowLines.remove(rowLines.size() - 1);
      }
      setHeaders(tabHeaders());
      double y = getInsets().getTop() + rowHeight;
      for (Region line : rowLines) {
        line.resizeRelocate(0, snapPositionY(y), getWidth(), ROW_LINE_HEIGHT);
        y += ROW_LINE_HEIGHT + TAB_VGAP + rowHeight;
      }
    }

    @Override
    protected double computePrefHeight(double width) {
      List<TabHeader> tabHeaders = tabHeaders();
      Insets insets = getInsets();
      if (tabHeaders.isEmpty()) {
        return insets.getTop() + insets.getBottom();
      }
      int rows = flow(tabHeaders, availableWidth(width), false);
      return insets.getTop() + rows * (rowHeight(tabHeaders) + ROW_LINE_HEIGHT) + (rows - 1) * TAB_VGAP + insets.getBottom();
    }

    /** As wide as the widest header, not all headers side by side: the rows wrap, so that is all that is ever required. */
    @Override
    protected double computePrefWidth(double height) {
      Insets insets = getInsets();
      double widest = 0;
      for (TabHeader header : tabHeaders()) {
        widest = Math.max(widest, headerWidth(header, Double.MAX_VALUE));
      }
      return insets.getLeft() + widest + insets.getRight();
    }

    @Override
    protected double computeMinWidth(double height) {
      Insets insets = getInsets();
      return insets.getLeft() + insets.getRight();
    }
  }

  /** One tab's header: icon + title + close button, kept in sync with the {@link Tab} it stands for. */
  private final class TabHeader extends HBox {

    private final Tab tab;
    private final StackPane closeButton = new StackPane();
    private final List<Runnable> disposers = new ArrayList<>();
    private Tooltip installedTooltip;

    TabHeader(Tab tab) {
      this.tab = tab;
      TabPane tabPane = getSkinnable();

      setAlignment(Pos.CENTER_LEFT);
      getProperties().put(StudioTabPane.TAB_HEADER_KEY, tab);
      Label label = new Label();
      label.getStyleClass().add("tab-label");
      label.textProperty().bind(tab.textProperty());
      label.graphicProperty().bind(tab.graphicProperty());
      closeButton.getStyleClass().add("tab-close-button");
      getChildren().addAll(label, closeButton);

      idProperty().bind(tab.idProperty());
      styleProperty().bind(tab.styleProperty());
      disableProperty().bind(tab.disableProperty());
      disposers.add(() -> {
        label.textProperty().unbind();
        label.graphicProperty().unbind();
        idProperty().unbind();
        styleProperty().unbind();
        disableProperty().unbind();
      });

      listen(tab.getStyleClass(), this::updateStyleClasses);
      listen(tab.selectedProperty(), this::updateSelected);
      listen(tab.selectedProperty(), this::updateCloseButton);
      listen(tab.closableProperty(), this::updateCloseButton);
      listen(tabPane.tabClosingPolicyProperty(), this::updateCloseButton);
      listen(tab.tooltipProperty(), this::updateTooltip);
      updateStyleClasses();
      updateSelected();
      updateCloseButton();
      updateTooltip();

      setOnMousePressed(event -> {
        if (event.getButton() == MouseButton.PRIMARY && !tab.isDisable()) {
          tabPane.getSelectionModel().select(tab);
        }
      });
      setOnContextMenuRequested(event -> {
        if (tab.getContextMenu() != null) {
          tab.getContextMenu().show(this, event.getScreenX(), event.getScreenY());
          event.consume();
        }
      });
      // Consumed on press already, not just on click: the press would otherwise reach the header and select
      // the very tab that is about to be closed - which for a still-lazy restored tab means building its editor.
      closeButton.setOnMousePressed(Event::consume);
      closeButton.setOnMouseClicked(event -> {
        if (event.getButton() == MouseButton.PRIMARY) {
          closeTab(tab);
        }
        event.consume();
      });
    }

    private void listen(Observable observable, Runnable action) {
      InvalidationListener listener = ignored -> action.run();
      observable.addListener(listener);
      disposers.add(() -> observable.removeListener(listener));
    }

    private void updateStyleClasses() {
      List<String> styleClasses = new ArrayList<>();
      styleClasses.add("tab");
      styleClasses.addAll(tab.getStyleClass());
      getStyleClass().setAll(styleClasses);
    }

    private void updateSelected() {
      pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, tab.isSelected());
    }

    private void updateCloseButton() {
      boolean visible = tab.isClosable() && switch (getSkinnable().getTabClosingPolicy()) {
        case ALL_TABS -> true;
        case SELECTED_TAB -> tab.isSelected();
        case UNAVAILABLE -> false;
      };
      closeButton.setVisible(visible);
      closeButton.setManaged(visible);
    }

    private void updateTooltip() {
      if (installedTooltip != null) {
        Tooltip.uninstall(this, installedTooltip);
      }
      installedTooltip = tab.getTooltip();
      if (installedTooltip != null) {
        Tooltip.install(this, installedTooltip);
      }
    }

    void dispose() {
      if (installedTooltip != null) {
        Tooltip.uninstall(this, installedTooltip);
        installedTooltip = null;
      }
      disposers.forEach(Runnable::run);
      disposers.clear();
    }
  }

  /**
   * Holds one tab's content; follows {@link Tab#contentProperty()}, which is often set only after the tab was added.
   * Always resizes the content to fill the whole area (like the regular skin's content region does) rather than
   * aligning it within, which a {@code StackPane} would do for content whose max size is below the area's.
   */
  private static final class TabContent extends Pane {

    private final Tab tab;
    private final InvalidationListener contentListener = ignored -> updateContent();

    TabContent(Tab tab) {
      this.tab = tab;
      tab.contentProperty().addListener(contentListener);
      updateContent();
    }

    @Override
    protected void layoutChildren() {
      Insets insets = getInsets();
      double width = getWidth() - insets.getLeft() - insets.getRight();
      double height = getHeight() - insets.getTop() - insets.getBottom();
      for (Node child : getChildren()) {
        child.resizeRelocate(insets.getLeft(), insets.getTop(), width, height);
      }
    }

    private void updateContent() {
      Node content = tab.getContent();
      if (content == null) {
        getChildren().clear();
      }
      else if (getChildren().size() != 1 || getChildren().get(0) != content) {
        getChildren().setAll(content);
      }
    }

    void dispose() {
      tab.contentProperty().removeListener(contentListener);
      getChildren().clear();
    }
  }
}
