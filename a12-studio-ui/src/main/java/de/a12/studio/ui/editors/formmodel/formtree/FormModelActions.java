package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.ExpressionCell;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.formmodel.TextCell;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Builds the Form Model tree's context menu and carries out its actions: Add (per {@link
 * FormModelNodeTypes#allowedChildTypes}), Delete, Duplicate, Cut/Copy/Paste, Move Up/Down. Also exposes the
 * underlying list-mutation building blocks ({@link #siblingsOf}, {@link #insertAsChild}, {@link #removeNode})
 * to {@link FormModelTreeController} so drag-and-drop reuses the exact same mutation logic instead of
 * duplicating it.
 * <p>
 * Node duplication/paste clones via a JSON round-trip through the app's shared {@link JsonSettings#objectMapper}
 * (the same mapper used to load/save model files), then {@link #regenerateIds} walks the clone so it never
 * collides with the id of the node it was copied from.
 */
@Slf4j
class FormModelActions {

  // Static so Copy/Cut in one Form Model tab and Paste in another (or a later reopen of the same tab) work,
  // mirroring how a system clipboard behaves. Holds a JSON snapshot rather than the live object so repeated
  // pastes each get their own fresh clone (see #paste).
  private static String clipboardJson;
  private static Class<?> clipboardType;

  private final FormModelContent content;
  private final Consumer<Object> onModelChanged;

  FormModelActions(@NonNull FormModelContent content, @NonNull Consumer<Object> onModelChanged) {
    this.content = content;
    this.onModelChanged = onModelChanged;
  }

  ContextMenu createContextMenu(@Nullable FormElementViewModel selected) {
    ContextMenu contextMenu = new ContextMenu();
    if (selected == null) {
      MenuItem addScreenItem = createMenuItem("_Add Screen", Icons.FORM_SCREEN);
      addScreenItem.setOnAction(event -> addRootScreen());
      contextMenu.getItems().add(addScreenItem);
      return contextMenu;
    }

    List<FormModelNodeTypes.ChildTypeDescriptor> addTypes = FormModelNodeTypes.allowedChildTypes(selected.getNode());
    if (!addTypes.isEmpty()) {
      Menu addMenu = new Menu("_Add");
      for (FormModelNodeTypes.ChildTypeDescriptor descriptor : addTypes) {
        MenuItem item = createMenuItem(descriptor.label(), descriptor.icon());
        item.setOnAction(event -> addChild(selected, descriptor));
        addMenu.getItems().add(item);
      }
      contextMenu.getItems().add(addMenu);
      contextMenu.getItems().add(new SeparatorMenuItem());
    }

    List<Object> siblings = siblingsOf(selected);
    boolean reorderable = siblings != null;

    MenuItem cutItem = createMenuItem("Cu_t", Icons.CUT);
    cutItem.setOnAction(event -> cut(selected));
    contextMenu.getItems().add(cutItem);

    MenuItem copyItem = createMenuItem("_Copy", Icons.COPY);
    copyItem.setOnAction(event -> copy(selected));
    contextMenu.getItems().add(copyItem);

    MenuItem pasteItem = createMenuItem("_Paste", Icons.PASTE);
    pasteItem.setDisable(!canPasteInto(selected.getNode()));
    pasteItem.setOnAction(event -> paste(selected));
    contextMenu.getItems().add(pasteItem);

    MenuItem duplicateItem = createMenuItem("D_uplicate", Icons.COPY);
    duplicateItem.setDisable(!reorderable);
    duplicateItem.setOnAction(event -> duplicate(selected));
    contextMenu.getItems().add(duplicateItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem moveUpItem = createMenuItem("Move _Up", Icons.ARROW_UP);
    moveUpItem.setDisable(!reorderable);
    moveUpItem.setOnAction(event -> move(selected, -1));
    contextMenu.getItems().add(moveUpItem);

    MenuItem moveDownItem = createMenuItem("Move Do_wn", Icons.ARROW_DOWN);
    moveDownItem.setDisable(!reorderable);
    moveDownItem.setOnAction(event -> move(selected, 1));
    contextMenu.getItems().add(moveDownItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem deleteItem = createMenuItem("_Delete", Icons.TRASH);
    deleteItem.setOnAction(event -> confirmAndDelete(selected));
    contextMenu.getItems().add(deleteItem);

    return contextMenu;
  }

  private void addRootScreen() {
    Screen screen = FormModelElementFactory.newScreen();
    content.getScreens().add(screen);
    onModelChanged.accept(screen);
  }

  private void addChild(@NonNull FormElementViewModel target, FormModelNodeTypes.@NonNull ChildTypeDescriptor descriptor) {
    Object newChild = descriptor.factory().get();
    insertAsChild(target.getNode(), newChild);
    onModelChanged.accept(newChild);
  }

  private void move(@NonNull FormElementViewModel item, int delta) {
    List<Object> siblings = siblingsOf(item);
    if (siblings == null) {
      return;
    }
    int index = siblings.indexOf(item.getNode());
    int newIndex = index + delta;
    if (index < 0 || newIndex < 0 || newIndex >= siblings.size()) {
      return;
    }
    Collections.swap(siblings, index, newIndex);
    onModelChanged.accept(item.getNode());
  }

  private void duplicate(@NonNull FormElementViewModel item) {
    List<Object> siblings = siblingsOf(item);
    if (siblings == null) {
      return;
    }
    Object clone = cloneNode(item.getNode());
    if (clone == null) {
      return;
    }
    regenerateIds(clone);
    int index = siblings.indexOf(item.getNode());
    siblings.add(index + 1, clone);
    onModelChanged.accept(clone);
  }

  private void cut(@NonNull FormElementViewModel item) {
    if (!copyToClipboard(item.getNode())) {
      return;
    }
    removeNode(item);
    onModelChanged.accept(null);
  }

  private void copy(@NonNull FormElementViewModel item) {
    copyToClipboard(item.getNode());
  }

  private boolean copyToClipboard(@NonNull Object node) {
    try {
      clipboardJson = JsonSettings.objectMapper.writeValueAsString(node);
      clipboardType = node.getClass();
      return true;
    }
    catch (Exception e) {
      log.warn("Failed to copy form model node to clipboard: {}", e.getMessage(), e);
      return false;
    }
  }

  private boolean canPasteInto(@NonNull Object target) {
    return clipboardType != null && FormModelNodeTypes.canContain(target, clipboardType);
  }

  private void paste(@NonNull FormElementViewModel target) {
    if (!canPasteInto(target.getNode())) {
      return;
    }
    try {
      Object clone = JsonSettings.objectMapper.readValue(clipboardJson, clipboardType);
      regenerateIds(clone);
      insertAsChild(target.getNode(), clone);
      onModelChanged.accept(clone);
    }
    catch (Exception e) {
      log.warn("Failed to paste form model node: {}", e.getMessage(), e);
    }
  }

  private void confirmAndDelete(@NonNull FormElementViewModel item) {
    boolean hasChildren = !item.getChildren().isEmpty();
    String help = hasChildren ? "Child elements will be deleted as well." : null;
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        StudioBundle.get("delete_the_selected_element_s_confirm"), help, null, StudioBundle.get("delete"));
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }
    removeNode(item);
    onModelChanged.accept(null);
  }

  private Object cloneNode(@NonNull Object node) {
    try {
      String json = JsonSettings.objectMapper.writeValueAsString(node);
      return JsonSettings.objectMapper.readValue(json, node.getClass());
    }
    catch (Exception e) {
      log.warn("Failed to duplicate form model node: {}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * Regenerates the id of {@code node} and every descendant, recursively - so a clone produced by {@link
   * #cloneNode}/{@link #paste} never collides with the id(s) of the subtree it was copied from. Mirrors the
   * {@code <lowercase-type>-<5-hex-digits>} convention from {@link FormModelElementFactory}.
   */
  static void regenerateIds(@NonNull Object node) {
    if (node instanceof Screen screen) {
      screen.setId(FormModelElementFactory.generateId("screen"));
      screen.getScreenElements().forEach(FormModelActions::regenerateIds);
    }
    else if (node instanceof Section section) {
      section.setId(FormModelElementFactory.generateId("section"));
      section.getScreenElements().forEach(FormModelActions::regenerateIds);
    }
    else if (node instanceof MultiColumnSection section) {
      section.setId(FormModelElementFactory.generateId("multicolumnsection"));
      section.getScreenElements().forEach(FormModelActions::regenerateIds);
    }
    else if (node instanceof ControlGrid grid) {
      grid.setId(FormModelElementFactory.generateId("controlgrid"));
      grid.getRow().forEach(FormModelActions::regenerateIds);
    }
    else if (node instanceof Row row) {
      row.setId(FormModelElementFactory.generateId("row"));
      row.getCell().forEach(FormModelActions::regenerateIds);
    }
    else if (node instanceof EmbeddedRepeat repeat) {
      repeat.setId(FormModelElementFactory.generateId("embeddedrepeat"));
      if (repeat.getControlGrid() != null) {
        regenerateIds(repeat.getControlGrid());
      }
    }
    else if (node instanceof DetachedRepeat repeat) {
      repeat.setId(FormModelElementFactory.generateId("detachedrepeat"));
      if (repeat.getDetailScreen() != null) {
        regenerateIds(repeat.getDetailScreen());
      }
    }
    else if (node instanceof InlineRepeat repeat) {
      repeat.setId(FormModelElementFactory.generateId("inlinerepeat"));
    }
    else if (node instanceof CustomScreenElement element) {
      element.setId(FormModelElementFactory.generateId("customscreenelement"));
    }
    else if (node instanceof Control control) {
      control.setId(FormModelElementFactory.generateId("control"));
    }
    else if (node instanceof TextCell cell) {
      cell.setId(FormModelElementFactory.generateId("textcell"));
    }
    else if (node instanceof ExpressionCell cell) {
      cell.setId(FormModelElementFactory.generateId("expressioncell"));
    }
  }

  /**
   * The list {@code item}'s node currently lives in - {@code content.getScreens()} for a top-level {@link
   * Screen}, or its parent's child list otherwise - or {@code null} if the parent is an {@link EmbeddedRepeat}
   * or {@link DetachedRepeat} (a single-slot child has no "siblings" to reorder/insert around).
   */
  @SuppressWarnings("unchecked")
  List<Object> siblingsOf(@NonNull FormElementViewModel item) {
    Object parent = item.getParentNode();
    if (parent == null) {
      return (List<Object>) (List<?>) content.getScreens();
    }
    return childListOf(parent);
  }

  /**
   * The mutable child list {@code parent} exposes ({@link ScreenElement}s for a {@link Screen}/{@link
   * Section}/{@link MultiColumnSection}, {@link Row}s for a {@link ControlGrid}, {@link Cell}s for a {@link
   * Row}), or {@code null} for a single-slot parent ({@link EmbeddedRepeat}/{@link DetachedRepeat}) or a leaf
   * that can't contain children at all. The unchecked cast is safe: every list here only ever receives objects
   * whose concrete type was chosen by {@link FormModelNodeTypes}, which already restricts factories/clipboard
   * pastes to the correct type for each parent.
   */
  @SuppressWarnings("unchecked")
  private static List<Object> childListOf(@NonNull Object parent) {
    if (parent instanceof Screen screen) {
      return (List<Object>) (List<?>) screen.getScreenElements();
    }
    if (parent instanceof Section section) {
      return (List<Object>) (List<?>) section.getScreenElements();
    }
    if (parent instanceof MultiColumnSection section) {
      return (List<Object>) (List<?>) section.getScreenElements();
    }
    if (parent instanceof ControlGrid grid) {
      return (List<Object>) (List<?>) grid.getRow();
    }
    if (parent instanceof Row row) {
      return (List<Object>) (List<?>) row.getCell();
    }
    return null;
  }

  /** Adds {@code child} as the last child of {@code parent}, handling the {@link EmbeddedRepeat}/{@link DetachedRepeat} single-slot case. */
  void insertAsChild(@NonNull Object parent, @NonNull Object child) {
    if (parent instanceof EmbeddedRepeat repeat) {
      repeat.setControlGrid((ControlGrid) child);
      return;
    }
    if (parent instanceof DetachedRepeat repeat) {
      repeat.setDetailScreen((Screen) child);
      return;
    }
    List<Object> children = childListOf(parent);
    if (children != null) {
      children.add(child);
    }
  }

  /** Removes {@code item}'s node from wherever it currently lives (list slot, or single {@link EmbeddedRepeat}/{@link DetachedRepeat} slot). */
  void removeNode(@NonNull FormElementViewModel item) {
    Object parent = item.getParentNode();
    Object node = item.getNode();
    if (parent == null) {
      content.getScreens().remove(node);
      return;
    }
    if (parent instanceof EmbeddedRepeat repeat) {
      repeat.setControlGrid(null);
      return;
    }
    if (parent instanceof DetachedRepeat repeat) {
      repeat.setDetailScreen(null);
      return;
    }
    List<Object> siblings = childListOf(parent);
    if (siblings != null) {
      siblings.remove(node);
    }
  }

  void notifyChanged(@Nullable Object nodeToSelect) {
    onModelChanged.accept(nodeToSelect);
  }

  private static MenuItem createMenuItem(@NonNull String text, @NonNull String icon) {
    MenuItem menuItem = new MenuItem(text);
    FontIcon fontIcon = WidgetFactory.createIcon(icon);
    fontIcon.getStyleClass().add("menu-icon");
    menuItem.setGraphic(fontIcon);
    return menuItem;
  }
}
