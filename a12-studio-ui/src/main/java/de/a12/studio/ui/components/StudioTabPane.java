package de.a12.studio.ui.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import java.util.function.BiConsumer;

/**
 * A {@link TabPane} whose tab header can be shown in one of two layouts, switchable at runtime:
 * <ul>
 *   <li><b>single row</b> ({@link #multiRowHeaderProperty()} {@code false}): the regular JavaFX
 *   {@link TabPaneSkin} - one row of tab headers, with an overflow menu once they no longer fit;</li>
 *   <li><b>multi row</b> (default, {@code true}): {@link MultiRowTabPaneSkin} - the headers wrap onto as many rows as
 *   the available width needs, so every open tab stays visible and one click away.</li>
 * </ul>
 * Both layouts are styled by the same {@code stylesheet-tab-pane.css} rules ({@code .tab-pane .tab}, ...), so a
 * tab looks the same either way; only the multi-row one carries the extra {@code multi-row-tabs} style class.
 * The multi-row layout only supports {@link Side#TOP} headers - for any other side this falls back to the
 * regular skin, whatever {@link #multiRowHeaderProperty()} says.
 *
 * <p>Tabs can be dragged by their header from one {@code StudioTabPane} to another. The pane only reports the drop
 * (see {@link #setOnTabDropped}); moving the tab is up to whoever owns the panes, since that is where the
 * bookkeeping for the tab lives. While a tab is dragged over a pane it can be dropped on, the pane carries the
 * {@code tab-drop-target} pseudo class.
 */
public class StudioTabPane extends TabPane {

  private static final String MULTI_ROW_STYLE_CLASS = "multi-row-tabs";

  /**
   * Key under which a skin that can't tell {@link Tab#getStyleableNode()} (see {@link MultiRowTabPaneSkin}) stores
   * a header's tab in the header node's properties, so a header found under the mouse can be mapped back to its tab.
   */
  static final String TAB_HEADER_KEY = StudioTabPane.class.getName() + ".tab";

  private static final PseudoClass DROP_TARGET_PSEUDO_CLASS = PseudoClass.getPseudoClass("tab-drop-target");

  /**
   * A custom format on purpose: text controls inside the editors accept any dragboard with a string, so a tab
   * dragged over one would otherwise get its title dropped into it.
   */
  private static final DataFormat TAB_FORMAT = new DataFormat("application/x-a12-studio-tab");

  /** The tab being dragged, if any. Drag and drop between the panes stays within the application, so no serializing. */
  private static Tab draggedTab;

  private final BooleanProperty multiRowHeader = new SimpleBooleanProperty(this, "multiRowHeader", true);

  private BiConsumer<Tab, StudioTabPane> onTabDropped;

  public StudioTabPane() {
    multiRowHeader.addListener((observable, oldValue, newValue) -> refreshSkin());
    sideProperty().addListener((observable, oldValue, newValue) -> refreshSkin());

    addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);
    addEventHandler(MouseEvent.DRAG_DETECTED, this::onDragDetected);
    addEventHandler(DragEvent.DRAG_DONE, event -> draggedTab = null);
    // Filters, not handlers: the editor content below the header sees the drag first otherwise, and could take it.
    addEventFilter(DragEvent.DRAG_OVER, this::onDragOver);
    addEventFilter(DragEvent.DRAG_DROPPED, this::onDragDropped);
    addEventHandler(DragEvent.DRAG_EXITED, event -> pseudoClassStateChanged(DROP_TARGET_PSEUDO_CLASS, false));
  }

  /** Called with the tab and this pane when a tab dragged out of another {@code StudioTabPane} was dropped here. */
  public void setOnTabDropped(BiConsumer<Tab, StudioTabPane> onTabDropped) {
    this.onTabDropped = onTabDropped;
  }

  public BooleanProperty multiRowHeaderProperty() {
    return multiRowHeader;
  }

  public boolean isMultiRowHeader() {
    return multiRowHeader.get();
  }

  public void setMultiRowHeader(boolean multiRowHeader) {
    this.multiRowHeader.set(multiRowHeader);
  }

  @Override
  protected Skin<?> createDefaultSkin() {
    if (usesMultiRowSkin()) {
      getStyleClass().add(MULTI_ROW_STYLE_CLASS);
      return new MultiRowTabPaneSkin(this);
    }
    return new TabPaneSkin(this);
  }

  private boolean usesMultiRowSkin() {
    return isMultiRowHeader() && getSide() == Side.TOP;
  }

  /**
   * Swaps the skin in place if the layout that should be used no longer matches the installed one. Nothing to
   * do while no skin exists yet (the pane hasn't been attached to a scene): {@link #createDefaultSkin()} then
   * picks the right one when it is first needed.
   */
  private void refreshSkin() {
    if (getSkin() == null || usesMultiRowSkin() == getSkin() instanceof MultiRowTabPaneSkin) {
      return;
    }
    getStyleClass().remove(MULTI_ROW_STYLE_CLASS);
    setSkin(createDefaultSkin());
  }

  /** A middle click on a tab header closes that tab, like the header's close button does. */
  private void onMouseClicked(MouseEvent event) {
    if (event.getButton() != MouseButton.MIDDLE) {
      return;
    }
    Node header = findHeader(event.getPickResult().getIntersectedNode());
    Tab tab = header == null ? null : tabOf(header);
    if (tab == null || !tab.isClosable() || getTabClosingPolicy() == TabClosingPolicy.UNAVAILABLE) {
      return;
    }
    closeTab(tab);
    event.consume();
  }

  /** Same close protocol as the tab header's close button: close request (vetoable), removal, then the tab's {@code onClosed} handler. */
  private void closeTab(Tab tab) {
    Event closeRequest = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
    Event.fireEvent(tab, closeRequest);
    if (closeRequest.isConsumed()) {
      return;
    }
    getTabs().remove(tab);
    EventHandler<Event> onClosed = tab.getOnClosed();
    if (onClosed != null) {
      onClosed.handle(new Event(Tab.CLOSED_EVENT));
    }
  }

  private void onDragDetected(MouseEvent event) {
    if (event.getButton() != MouseButton.PRIMARY) {
      return;
    }
    Node header = findHeader(event.getPickResult().getIntersectedNode());
    Tab tab = header == null ? null : tabOf(header);
    if (tab == null) {
      return;
    }
    Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
    ClipboardContent content = new ClipboardContent();
    content.put(TAB_FORMAT, "tab");
    dragboard.setContent(content);
    dragboard.setDragView(header.snapshot(null, null));
    draggedTab = tab;
    event.consume();
  }

  private void onDragOver(DragEvent event) {
    if (acceptsDrop(event)) {
      event.acceptTransferModes(TransferMode.MOVE);
      pseudoClassStateChanged(DROP_TARGET_PSEUDO_CLASS, true);
      event.consume();
    }
  }

  private void onDragDropped(DragEvent event) {
    if (!acceptsDrop(event)) {
      return;
    }
    Tab tab = draggedTab;
    draggedTab = null;
    pseudoClassStateChanged(DROP_TARGET_PSEUDO_CLASS, false);
    event.setDropCompleted(true);
    event.consume();
    if (onTabDropped != null) {
      onTabDropped.accept(tab, this);
    }
  }

  /** Only a tab of another pane can be dropped: there is no reordering within one. */
  private boolean acceptsDrop(DragEvent event) {
    return draggedTab != null && draggedTab.getTabPane() != this && event.getDragboard().hasContent(TAB_FORMAT);
  }

  /**
   * The tab header of this pane that {@code node} is part of, or {@code null} - also for the header's close button,
   * which isn't a drag handle. Only headers in this pane's own strip count: an editor's content may well contain a
   * tab pane of its own, whose headers must not start a drag of the outer tab.
   */
  private Node findHeader(Node node) {
    Node header = null;
    for (; node != null && node != this; node = node.getParent()) {
      if (node.getStyleClass().contains("tab-close-button")) {
        return null;
      }
      if (node.getStyleClass().contains("tab")) {
        header = node;
      }
      if (node.getStyleClass().contains("tab-header-area") && node.getParent() == this) {
        return header != null && tabOf(header) != null ? header : null;
      }
    }
    return null;
  }

  /**
   * The tab a header node of this pane stands for. {@link MultiRowTabPaneSkin} tags its headers with their tab;
   * the regular skin doesn't ({@link Tab#getStyleableNode()} stays {@code null}), but lays its headers out as
   * siblings in tab order, so a header's position among the "tab" siblings is its tab's index.
   */
  private Tab tabOf(Node header) {
    if (header.getProperties().get(TAB_HEADER_KEY) instanceof Tab tab) {
      return tab;
    }
    if (header.getParent() == null) {
      return null;
    }
    int index = 0;
    for (Node sibling : header.getParent().getChildrenUnmodifiable()) {
      if (sibling == header) {
        return index < getTabs().size() ? getTabs().get(index) : null;
      }
      if (sibling.getStyleClass().contains("tab")) {
        index++;
      }
    }
    return null;
  }
}
