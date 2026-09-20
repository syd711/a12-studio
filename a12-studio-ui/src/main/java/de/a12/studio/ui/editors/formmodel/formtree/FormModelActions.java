package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.ButtonGroup;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.ExpressionCell;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.HeaderFooterBox;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.NavigationButton;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.formmodel.TextCell;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.formtree.commands.AddNodeCommand;
import de.a12.studio.ui.editors.formmodel.formtree.commands.DeleteNodeCommand;
import de.a12.studio.ui.editors.formmodel.formtree.commands.ReplaceNodeCommand;
import de.a12.studio.ui.editors.formmodel.formtree.commands.SetSingleChildCommand;
import de.a12.studio.ui.editors.formmodel.formtree.commands.SwapCommand;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.commandstack.Command;
import de.a12.studio.ui.util.commandstack.CommandStack;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Builds the Form Model tree's context menu and carries out its actions: Add (per {@link
 * FormModelNodeTypes#allowedChildTypes}), Delete, Duplicate, Cut/Copy/Paste, Move Up/Down - every mutation
 * going through the shared {@link CommandStack} passed in at construction, so it's undoable via the toolbar's
 * Undo/Redo buttons. The same actions ({@link #cut}/{@link #copy}/{@link #paste}/{@link #duplicate}/{@link
 * #move}/{@link #confirmAndDelete}, plus {@link #createAddMenuItems}/{@link #createConvertMenuItems} for the
 * "Add"/"Convert" menus) are package-visible so {@link FormModelTreeController} can wire the equivalent toolbar
 * buttons to the exact same code path instead of duplicating it. Also exposes the underlying list-mutation
 * building blocks ({@link #siblingsOf}, {@link #createAttachCommand}, {@link #createDetachCommand}) to {@link
 * FormModelTreeController} so drag-and-drop builds the exact same kind of commands instead of duplicating the
 * parent-type dispatch.
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
  private final CommandStack commandStack;
  private final Consumer<Object> onModelChanged;

  private final FormModelIncludeActions includes;

  FormModelActions(@NonNull FormModelContent content, @NonNull CommandStack commandStack, @NonNull Consumer<Object> onModelChanged,
      @NonNull ProjectItem projectItem, @Nullable DocumentModel documentModel) {
    this.content = content;
    this.commandStack = commandStack;
    this.onModelChanged = onModelChanged;
    this.includes = new FormModelIncludeActions(content, commandStack, onModelChanged, projectItem, documentModel);
  }

  ContextMenu createContextMenu(@Nullable FormElementViewModel selected) {
    ContextMenu contextMenu = new ContextMenu();
    if (selected == null) {
      contextMenu.getItems().addAll(createAddMenuItems(null));
      return contextMenu;
    }

    List<MenuItem> addItems = createAddMenuItems(selected);
    if (!addItems.isEmpty()) {
      Menu addMenu = new Menu("_Add");
      addMenu.getItems().addAll(addItems);
      contextMenu.getItems().add(addMenu);
      contextMenu.getItems().add(new SeparatorMenuItem());
    }

    List<MenuItem> convertItems = createConvertMenuItems(selected);
    if (!convertItems.isEmpty()) {
      contextMenu.getItems().addAll(convertItems);
      contextMenu.getItems().add(new SeparatorMenuItem());
    }

    List<Object> siblings = siblingsOf(selected);
    boolean reorderable = siblings != null;

    // An include in the grid slot of an Embedded Repeat has no siblings, but can be refreshed all the same.
    if (includes.isRefreshable(selected.getNode()) && (siblings != null || selected.getParentNode() instanceof EmbeddedRepeat)) {
      MenuItem refreshIncludeItem = createMenuItem(StudioBundle.get("form_model_tree.refresh_include"), Icons.RELOAD);
      refreshIncludeItem.setOnAction(event -> includes.refresh(selected, siblings));
      contextMenu.getItems().add(refreshIncludeItem);
      contextMenu.getItems().add(new SeparatorMenuItem());
    }

    MenuItem cutItem = createMenuItem(StudioBundle.get("form_model_tree.cut"), Icons.CUT);
    cutItem.setOnAction(event -> cut(selected));
    contextMenu.getItems().add(cutItem);

    MenuItem copyItem = createMenuItem(StudioBundle.get("form_model_tree.copy"), Icons.COPY);
    copyItem.setOnAction(event -> copy(selected));
    contextMenu.getItems().add(copyItem);

    MenuItem pasteItem = createMenuItem(StudioBundle.get("form_model_tree.paste"), Icons.PASTE);
    pasteItem.setDisable(!canPasteInto(selected.getNode()));
    pasteItem.setOnAction(event -> paste(selected));
    contextMenu.getItems().add(pasteItem);

    MenuItem duplicateItem = createMenuItem(StudioBundle.get("form_model_tree.duplicate"), Icons.COPY);
    duplicateItem.setDisable(!reorderable);
    duplicateItem.setOnAction(event -> duplicate(selected));
    contextMenu.getItems().add(duplicateItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem moveUpItem = createMenuItem(StudioBundle.get("form_model_tree.move_up"), Icons.ARROW_UP);
    moveUpItem.setDisable(!reorderable);
    moveUpItem.setOnAction(event -> move(selected, -1));
    contextMenu.getItems().add(moveUpItem);

    MenuItem moveDownItem = createMenuItem(StudioBundle.get("form_model_tree.move_down"), Icons.ARROW_DOWN);
    moveDownItem.setDisable(!reorderable);
    moveDownItem.setOnAction(event -> move(selected, 1));
    contextMenu.getItems().add(moveDownItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem deleteItem = createMenuItem(StudioBundle.get("delete"), Icons.TRASH);
    deleteItem.setOnAction(event -> confirmAndDelete(selected));
    contextMenu.getItems().add(deleteItem);

    return contextMenu;
  }

  /**
   * The "Add" items valid for {@code selected} (per {@link FormModelNodeTypes#allowedChildTypes}) - shared by
   * {@link #createContextMenu}'s "Add" submenu and {@link FormModelTreeController}'s toolbar "Add" {@link
   * javafx.scene.control.MenuButton}, which is repopulated from this on every selection change since (unlike
   * {@code DocumentModelActions#createAddMenuItems}) the available types genuinely differ per selected node type.
   */
  List<MenuItem> createAddMenuItems(@Nullable FormElementViewModel selected) {
    List<MenuItem> items = new ArrayList<>();
    if (selected == null) {
      MenuItem addScreenItem = createMenuItem("_Add Screen", Icons.FORM_SCREEN);
      addScreenItem.setOnAction(event -> addRootScreen());
      items.add(addScreenItem);
      return items;
    }
    for (FormModelNodeTypes.ChildTypeDescriptor descriptor : FormModelNodeTypes.allowedChildTypes(selected.getNode())) {
      MenuItem item = createMenuItem(descriptor.label(), descriptor.icon());
      item.setOnAction(event -> addChild(selected, descriptor));
      items.add(item);
    }
    if (includes.canIncludeInto(selected.getNode())) {
      items.add(new SeparatorMenuItem());
      MenuItem includeItem = createMenuItem(StudioBundle.get("form_model_tree.include_form_model"), Icons.ELEMENT_INCLUDE);
      includeItem.setOnAction(event -> includes.includeInto(selected));
      items.add(includeItem);
    }
    return items;
  }

  /**
   * The repeat-conversion items valid for {@code selected} (every {@link RepeatConverter.RepeatKind} other than
   * its current one), or empty if {@code selected} isn't an {@link AbstractRepeat} - shared by {@link
   * #createContextMenu} and {@link FormModelTreeController}'s toolbar "Convert" {@link
   * javafx.scene.control.MenuButton}, same reasoning as {@link #createAddMenuItems}.
   */
  List<MenuItem> createConvertMenuItems(@Nullable FormElementViewModel selected) {
    if (!(selected != null && selected.getNode() instanceof AbstractRepeat repeat)) {
      return List.of();
    }
    List<MenuItem> items = new ArrayList<>();
    RepeatConverter.RepeatKind currentKind = RepeatConverter.kindOf(repeat);
    for (RepeatConverter.RepeatKind targetKind : RepeatConverter.RepeatKind.values()) {
      if (targetKind == currentKind) {
        continue;
      }
      MenuItem item = createMenuItem(StudioBundle.get(convertLabelKey(targetKind)), convertIcon(targetKind));
      item.setOnAction(event -> convertRepeat(selected, repeat, targetKind));
      items.add(item);
    }
    return items;
  }

  private void addRootScreen() {
    Screen screen = FormModelElementFactory.newScreen();
    List<Object> screens = topLevelSiblings();
    commandStack.execute(new AddNodeCommand(screens, screen, screens.size()));
    onModelChanged.accept(screen);
  }

  private void addChild(@NonNull FormElementViewModel target, FormModelNodeTypes.@NonNull ChildTypeDescriptor descriptor) {
    Object newChild = descriptor.factory().get();
    Command command = createAttachCommand(target.getNode(), newChild);
    if (command == null) {
      return;
    }
    commandStack.execute(command);
    onModelChanged.accept(newChild);
  }

  void move(@NonNull FormElementViewModel item, int delta) {
    List<Object> siblings = siblingsOf(item);
    if (siblings == null) {
      return;
    }
    int index = siblings.indexOf(item.getNode());
    int newIndex = index + delta;
    if (index < 0 || newIndex < 0 || newIndex >= siblings.size()) {
      return;
    }
    commandStack.execute(new SwapCommand(siblings, index, newIndex));
    onModelChanged.accept(item.getNode());
  }

  void duplicate(@NonNull FormElementViewModel item) {
    List<Object> siblings = siblingsOf(item);
    if (siblings == null) {
      return;
    }
    Object clone = cloneNode(item.getNode());
    if (clone == null) {
      return;
    }
    regenerateIds(clone);
    clearIncludeProvenance(clone);
    int index = siblings.indexOf(item.getNode());
    commandStack.execute(new AddNodeCommand(siblings, clone, index + 1));
    onModelChanged.accept(clone);
  }

  void cut(@NonNull FormElementViewModel item) {
    if (!copyToClipboard(item.getNode())) {
      return;
    }
    Command command = createDetachCommand(item);
    if (command != null) {
      commandStack.execute(command);
    }
    onModelChanged.accept(null);
  }

  void copy(@NonNull FormElementViewModel item) {
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

  boolean canPasteInto(@NonNull Object target) {
    return clipboardType != null && FormModelNodeTypes.canContain(target, clipboardType);
  }

  void paste(@NonNull FormElementViewModel target) {
    if (!canPasteInto(target.getNode())) {
      return;
    }
    try {
      Object clone = JsonSettings.objectMapper.readValue(clipboardJson, clipboardType);
      regenerateIds(clone);
      clearIncludeProvenance(clone);
      Command command = createAttachCommand(target.getNode(), clone);
      if (command == null) {
        return;
      }
      commandStack.execute(command);
      onModelChanged.accept(clone);
    }
    catch (Exception e) {
      log.warn("Failed to paste form model node: {}", e.getMessage(), e);
    }
  }

  private static String convertLabelKey(RepeatConverter.@NonNull RepeatKind targetKind) {
    return switch (targetKind) {
      case INLINE -> "form_model_tree.convert_to_inline_repeat";
      case EMBEDDED -> "form_model_tree.convert_to_embedded_repeat";
      case DETACHED -> "form_model_tree.convert_to_detached_repeat";
    };
  }

  private static String convertIcon(RepeatConverter.@NonNull RepeatKind targetKind) {
    return switch (targetKind) {
      case INLINE -> Icons.FORM_INLINE_REPEAT;
      case EMBEDDED -> Icons.FORM_EMBEDDED_REPEAT;
      case DETACHED -> Icons.FORM_DETACHED_REPEAT;
    };
  }

  /**
   * Converts {@code repeat} to a different {@link AbstractRepeat} subtype in place - swaps it out for the
   * {@link RepeatConverter#convert} result at the same position in its parent's sibling list (a repeat is
   * always a real {@link Screen}/{@link Section}/{@link MultiColumnSection} child, never a single-slot child,
   * so {@link #siblingsOf} is never {@code null} here).
   */
  private void convertRepeat(@NonNull FormElementViewModel item, @NonNull AbstractRepeat repeat,
                              RepeatConverter.@NonNull RepeatKind targetKind) {
    List<Object> siblings = siblingsOf(item);
    if (siblings == null) {
      return;
    }
    AbstractRepeat converted = RepeatConverter.convert(repeat, targetKind);
    commandStack.execute(new ReplaceNodeCommand(siblings, repeat, converted));
    onModelChanged.accept(converted);
  }

  void confirmAndDelete(@NonNull FormElementViewModel item) {
    boolean hasChildren = !item.getChildren().isEmpty();
    String help = hasChildren ? "Child elements will be deleted as well." : null;
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        StudioBundle.get("delete_the_selected_element_s_confirm"), help, null, StudioBundle.get("delete"));
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }
    if (item.getNode() instanceof Screen screen) {
      removeNavigationButtonsTargeting(screen.getId());
    }
    Command command = createDetachCommand(item);
    if (command != null) {
      commandStack.execute(command);
    }
    onModelChanged.accept(null);
  }

  /**
   * Removes every {@link NavigationButton} whose {@code target} matches {@code screenId} from all
   * {@link HeaderFooterBox}es in the model (model-level subHeaderBox/footerBox and every per-screen
   * subHeaderBox/footerBox).  Called before the screen is detached so the model is left consistent.
   */
  private void removeNavigationButtonsTargeting(@NonNull String screenId) {
    List<HeaderFooterBox> boxes = new ArrayList<>();
    boxes.add(content.getSubHeaderBox());
    boxes.add(content.getFooterBox());
    for (Screen screen : content.getScreens()) {
      boxes.add(screen.getSubHeaderBox());
      boxes.add(screen.getFooterBox());
    }
    for (HeaderFooterBox box : boxes) {
      if (box == null) {
        continue;
      }
      removeFromGroup(box.getMajorButtons(), screenId);
      removeFromGroup(box.getMinorButtons(), screenId);
    }
  }

  private static void removeFromGroup(@Nullable ButtonGroup group, @NonNull String screenId) {
    if (group == null) {
      return;
    }
    Iterator<Button> it = group.getButton().iterator();
    while (it.hasNext()) {
      Button button = it.next();
      if (button instanceof NavigationButton navBtn && screenId.equals(navBtn.getTarget())) {
        it.remove();
      }
    }
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
   * Makes the copy a plain element: a duplicate or pasted copy of an included element is no part of the include any
   * more. Left in, its neighbor(s) would share the include id, so refreshing the include would treat the two as one
   * and replace both.
   */
  static void clearIncludeProvenance(@NonNull Object node) {
    for (ScreenElement element : FormModelWalker.find(node, ScreenElement.class, child -> true)) {
      element.setIncludeId(null);
      element.setFormModelRef(null);
      element.setHostDocumentModelPath(null);
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
  List<Object> siblingsOf(@NonNull FormElementViewModel item) {
    Object parent = item.getParentNode();
    if (parent == null) {
      return topLevelSiblings();
    }
    Object node = item.getNode();
    if ((parent instanceof EmbeddedRepeat && node instanceof ControlGrid)
        || (parent instanceof DetachedRepeat && node instanceof Screen)) {
      return null;
    }
    return childListOf(parent);
  }

  @SuppressWarnings("unchecked")
  private List<Object> topLevelSiblings() {
    return (List<Object>) (List<?>) content.getScreens();
  }

  /**
   * The mutable child list {@code parent} exposes ({@link ScreenElement}s for a {@link Screen}/{@link
   * Section}/{@link MultiColumnSection}, {@link Row}s for a {@link ControlGrid}, {@link Cell}s for a {@link
   * Row}), or {@code null} for a single-slot parent ({@link EmbeddedRepeat}/{@link DetachedRepeat}) or a leaf
   * that can't contain children at all. The unchecked cast is safe: every list here only ever receives objects
   * whose concrete type was chosen by {@link FormModelNodeTypes}, which already restricts factories/clipboard
   * pastes to the correct type for each parent. Package-private so {@link FormModelTreeController}'s
   * drag-and-drop can resolve the same child lists {@link #createAttachCommand}/{@link #createDetachCommand} do.
   */
  @SuppressWarnings("unchecked")
  static List<Object> childListOf(@NonNull Object parent) {
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
    if (parent instanceof AbstractRepeat repeat) {
      return (List<Object>) (List<?>) repeat.getRepeatOverviewColumn();
    }
    return null;
  }

  /**
   * Builds a command that adds {@code child} as the last child of {@code parent} - a real sibling list for most
   * parent types, or the single {@link EmbeddedRepeat}/{@link DetachedRepeat} slot - undoing by removing/clearing
   * it again. Returns {@code null} for a parent that can't take a child (shouldn't happen given the callers
   * already checked via {@link FormModelNodeTypes#canContain}/{@link FormModelNodeTypes#allowedChildTypes}).
   */
  Command createAttachCommand(@NonNull Object parent, @NonNull Object child) {
    if (parent instanceof EmbeddedRepeat repeat && child instanceof ControlGrid grid) {
      return new SetSingleChildCommand<>(repeat::setControlGrid, grid, repeat.getControlGrid());
    }
    if (parent instanceof DetachedRepeat repeat && child instanceof Screen screen) {
      return new SetSingleChildCommand<>(repeat::setDetailScreen, screen, repeat.getDetailScreen());
    }
    List<Object> children = childListOf(parent);
    return children == null ? null : new AddNodeCommand(children, child, children.size());
  }

  /**
   * Builds a command that removes {@code item}'s node from wherever it currently lives (list slot, or single
   * {@link EmbeddedRepeat}/{@link DetachedRepeat} slot), undoing by re-inserting/restoring it.
   */
  Command createDetachCommand(@NonNull FormElementViewModel item) {
    Object parent = item.getParentNode();
    Object node = item.getNode();
    if (parent == null) {
      return new DeleteNodeCommand(topLevelSiblings(), node);
    }
    if (parent instanceof EmbeddedRepeat repeat && node instanceof ControlGrid) {
      return new SetSingleChildCommand<ControlGrid>(repeat::setControlGrid, null, repeat.getControlGrid());
    }
    if (parent instanceof DetachedRepeat repeat && node instanceof Screen) {
      return new SetSingleChildCommand<Screen>(repeat::setDetailScreen, null, repeat.getDetailScreen());
    }
    List<Object> siblings = childListOf(parent);
    return siblings == null ? null : new DeleteNodeCommand(siblings, node);
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
