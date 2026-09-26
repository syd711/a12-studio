package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementDefaults;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.contentmodel.commands.ElementStateCommand;
import de.a12.studio.ui.editors.contentmodel.commands.InsertElementCommand;
import de.a12.studio.ui.editors.contentmodel.commands.MoveElementCommand;
import de.a12.studio.ui.editors.contentmodel.commands.RemoveElementCommand;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preview.PreviewLauncher;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.commandstack.Command;
import de.a12.studio.ui.util.commandstack.CommandStack;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Edits a {@link ContentModel}: the element tree on the left (add/remove/reorder), the live preview in the middle,
 * and on the right, for the selected element, the same settings SME's setting panel offers for its type, as a stack
 * of {@link ContentSettingsPanel}s (each shows itself only for the types it has settings for). The Lexical {@code
 * tree}/{@code html} payloads inside props are deliberately edited as opaque JSON in the "Raw properties" panel —
 * the studio does not reinterpret them.
 *
 * <p>The center shows the Content Model rendered by the real Content Engine, as SME's preview window does: the
 * installed Simple Model Editor client is loaded into a {@code WebView} and fed the live model by the {@link
 * de.a12.studio.ui.preview.PreviewServer} (see {@link de.a12.studio.ui.preview.ContentModelPreviewSession}).
 */
@Slf4j
public class ContentModelEditorController extends AbstractEditorController implements Initializable {

  private static final int TREE_ICON_SIZE = 14;
  // Edits made in the panels are applied to the model at once; the file is written once typing pauses.
  private static final int SAVE_DEBOUNCE_MS = 300;
  private static final String SAVE_KEY = "save";
  // Panel edits on the same element closer together than this (typing, dragging a slider) undo as one step.
  private static final long COALESCE_MS = 1000;
  private static final double ZOOM_STEP = 0.1;
  private static final double MIN_ZOOM = 0.3;
  private static final double MAX_ZOOM = 3.0;

  // Static so Copy/Cut in one Content Model tab and Paste in another (or a later reopen of the same tab) work,
  // like a system clipboard. Holds a JSON snapshot, not the live element, so every paste is a fresh clone.
  private static String clipboardJson;

  @FXML
  private TreeView<ContentElement> elementsTree;

  @FXML
  private VBox settingsBox;

  @FXML
  private Button undoButton;

  @FXML
  private Button redoButton;

  @FXML
  private Button addButton;

  @FXML
  private Button deleteButton;

  @FXML
  private Button moveUpButton;

  @FXML
  private Button moveDownButton;

  @FXML
  private Button cutButton;

  @FXML
  private Button copyButton;

  @FXML
  private Button pasteButton;

  @FXML
  private Button duplicateButton;

  @FXML
  private ElementPanelController elementPanelController;

  @FXML
  private RawPropsPanelController rawPropsPanelController;

  @FXML
  private WebView previewWebView;

  @FXML
  private Button zoomOutButton;

  @FXML
  private Button zoomInButton;

  @FXML
  private Button openInBrowserButton;

  @FXML
  private Label previewUnavailableLabel;

  private final List<ContentSettingsPanel> panels = new ArrayList<>();
  private final Debouncer saveDebouncer = new Debouncer();
  private boolean savePending;
  private ContentModel model;

  private final CommandStack commandStack = new CommandStack();
  // Set by the commands while they run: the element the tree should select once the model has changed.
  private ContentElement pendingSelection;
  private boolean refreshing;
  // The selected element as the panels last left it (JSON), the "before" of the next panel edit.
  private String stateSnapshot;
  private ElementStateCommand lastStateCommand;
  private long lastStateTime;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    elementsTree.setCellFactory(tree -> new javafx.scene.control.TreeCell<>() {
      @Override
      protected void updateItem(ContentElement element, boolean empty) {
        super.updateItem(element, empty);
        setText(empty || element == null ? null : typeLabel(element));
        if (empty || element == null) {
          setGraphic(null);
          return;
        }
        // "tree-icon" lets the tree stylesheet switch the icon to the inverse color on the selected row.
        FontIcon icon = WidgetFactory.createIcon(iconFor(element.getType()), TREE_ICON_SIZE, null);
        icon.getStyleClass().add("tree-icon");
        setGraphic(icon);
      }
    });
    elementsTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (!refreshing) {
        showElement(newValue != null ? newValue.getValue() : null);
        updateActionState();
      }
    });
    elementsTree.setContextMenu(createContextMenu());
    elementsTree.addEventHandler(KeyEvent.KEY_PRESSED, this::onTreeKeyPressed);
    updateActionState();

    for (Node child : settingsBox.getChildren()) {
      if (child instanceof TitledPane pane && pane.getProperties().get(ContentSettingsPanel.PANEL_KEY) instanceof ContentSettingsPanel panel) {
        panels.add(panel);
        panel.setOnChange(() -> onPanelChanged(panel));
      }
    }
    ContentSettingsPanel.Context context = new ContentSettingsPanel.Context() {
      @Override
      public ContentElement parentOf(@NonNull ContentElement element) {
        TreeItem<ContentElement> item = findItem(elementsTree.getRoot(), element);
        return item != null && item.getParent() != null ? item.getParent().getValue() : null;
      }

      @Override
      public void structureChanged() {
        TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
        if (item != null) {
          item.getChildren().setAll(buildTreeItem(item.getValue()).getChildren());
        }
      }
    };
    panels.forEach(panel -> panel.setContext(context));
    // The element panel include is optional in the FXML; without it there is no type editor to wire up.
    if (elementPanelController != null) {
      elementPanelController.setOnTypeChange(element -> {
        elementsTree.refresh();
        panels.stream().filter(panel -> panel != elementPanelController).forEach(panel -> panel.showElement(element));
      });
    }
  }

  private static String typeLabel(ContentElement element) {
    return element.getType() != null ? element.getType() : "?";
  }

  private static String iconFor(String type) {
    if (type == null) {
      return "mdi2s-shape-outline";
    }
    return switch (type) {
      case "Box" -> "mdi2s-square-outline";
      case "Grid" -> "mdi2v-view-grid-outline";
      case "GridRow", "TableHeadRow", "TableBodyRow" -> "mdi2t-table-row";
      case "GridColumn", "TableBodyCell" -> "mdi2t-table-column";
      case "Paragraph" -> "mdi2f-format-paragraph";
      case "Heading" -> "mdi2f-format-header-1";
      case "UnorderedList" -> "mdi2f-format-list-bulleted";
      case "ListItem" -> "mdi2c-circle-small";
      case "Table" -> "mdi2t-table";
      case "TableHead" -> "mdi2t-table-arrow-up";
      case "TableHeadCell" -> "mdi2t-table-headers-eye";
      case "TableBody" -> "mdi2t-table-large";
      case "TableFoot" -> "mdi2t-table-arrow-down";
      case "MessageBox" -> "mdi2m-message-alert-outline";
      case "Image" -> "mdi2i-image-outline";
      default -> "mdi2s-shape-outline";
    };
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((ContentModel) model);
    updateSettingsErrorBadge();
    startPreview();
  }

  private void startPreview() {
    try {
      previewWebView.getEngine().load(PreviewLauncher.registerContentPreview(projectItem));
      previewUnavailableLabel.setVisible(false);
      previewUnavailableLabel.setManaged(false);
      previewWebView.setVisible(true);
    }
    catch (PreviewAppException e) {
      showPreviewUnavailable(e);
    }
  }

  private void showPreviewUnavailable(PreviewAppException e) {
    previewWebView.setVisible(false);
    previewUnavailableLabel.setText(StudioBundle.get("content_model_editor.preview_unavailable", e.getMessage()));
    previewUnavailableLabel.setVisible(true);
    previewUnavailableLabel.setManaged(true);
    zoomOutButton.setDisable(true);
    zoomInButton.setDisable(true);
    openInBrowserButton.setDisable(true);
  }

  @FXML
  public void onZoomOut(ActionEvent e) {
    applyZoom(previewWebView.getZoom() - ZOOM_STEP);
  }

  @FXML
  public void onZoomIn(ActionEvent e) {
    applyZoom(previewWebView.getZoom() + ZOOM_STEP);
  }

  private void applyZoom(double zoom) {
    previewWebView.setZoom(Math.clamp(Math.round(zoom * 100) / 100.0, MIN_ZOOM, MAX_ZOOM));
  }

  @FXML
  public void onOpenInBrowser(ActionEvent e) {
    try {
      PreviewLauncher.openContentPreview(projectItem);
    }
    catch (PreviewAppException ex) {
      showPreviewUnavailable(ex);
    }
  }

  /**
   * Stops the preview page (it polls the preview server) once the editor's tab is closed, writes an edit that is
   * still waiting for its debounced save, and releases the panels.
   */
  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    if (event.getItem().equals(projectItem)) {
      previewWebView.getEngine().load("about:blank");
      saveDebouncer.shutdown();
      if (savePending) {
        savePending = false;
        commitChange();
      }
      panels.forEach(ContentSettingsPanel::destroy);
    }
    super.modelClosed(event);
  }

  private void load(@NonNull ContentModel model) {
    this.model = model;
    TreeItem<ContentElement> rootItem = buildTreeItem(model.getContent().getRoot());
    rootItem.setExpanded(true);
    elementsTree.setRoot(rootItem);
    elementsTree.getSelectionModel().select(rootItem);
  }

  private TreeItem<ContentElement> buildTreeItem(ContentElement element) {
    return buildTreeItem(element, Set.of());
  }

  private TreeItem<ContentElement> buildTreeItem(ContentElement element, Set<ContentElement> collapsed) {
    TreeItem<ContentElement> item = new TreeItem<>(element);
    if (element.getChildren() != null) {
      for (ContentElement child : element.getChildren()) {
        item.getChildren().add(buildTreeItem(child, collapsed));
      }
    }
    item.setExpanded(!collapsed.contains(element));
    return item;
  }

  private static TreeItem<ContentElement> findItem(TreeItem<ContentElement> from, ContentElement element) {
    if (from == null) {
      return null;
    }
    if (from.getValue() == element) {
      return from;
    }
    for (TreeItem<ContentElement> child : from.getChildren()) {
      TreeItem<ContentElement> found = findItem(child, element);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private ContentElement selectedElement() {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    return item != null ? item.getValue() : null;
  }

  private void showElement(ContentElement element) {
    stateSnapshot = element != null ? ElementStateCommand.capture(element) : null;
    panels.forEach(panel -> panel.showElement(element));
  }

  /**
   * A panel applied an edit to the selected element's props. The panel that made it already shows the result; the
   * raw JSON view is brought up to date, or, when the raw JSON itself was edited, every other panel is.
   */
  private void onPanelChanged(ContentSettingsPanel source) {
    if (source == rawPropsPanelController) {
      ContentElement element = selectedElement();
      panels.stream().filter(panel -> panel != source).forEach(panel -> panel.showElement(element));
    }
    else {
      rawPropsPanelController.refresh();
    }
    recordStateChange();
    scheduleSave();
  }

  private void scheduleSave() {
    savePending = true;
    saveDebouncer.debounce(SAVE_KEY, () -> {
      if (savePending) {
        savePending = false;
        commitChange();
      }
    }, SAVE_DEBOUNCE_MS, true);
  }

  @FXML
  public void onAddChild(ActionEvent e) {
    ContentElement parent = selectedElement();
    if (parent == null) {
      return;
    }
    ContentElement child = new ContentElement();
    child.setId(ContentElementDefaults.newId());
    child.setType("Box");
    child.setNamespace(parent.getNamespace() != null ? parent.getNamespace() : ContentElementDefaults.DEFAULT_NAMESPACE);
    ContentElementDefaults.applyMissing(child);
    execute(new InsertElementCommand(parent, child, Integer.MAX_VALUE, this::rememberSelection));
  }

  @FXML
  public void onRemoveElement(ActionEvent e) {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    if (item == null || item.getParent() == null) {
      // The root element cannot be removed (mirrors the content engine's root element rules).
      return;
    }

    boolean hasChildren = !item.getChildren().isEmpty();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        StudioBundle.get("delete_the_selected_element_s_confirm"),
        hasChildren ? StudioBundle.get("content_model_tree.delete_children_hint") : null, null,
        StudioBundle.get("delete"));
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }
    execute(new RemoveElementCommand(item.getParent().getValue(), item.getValue(), this::rememberSelection));
  }

  @FXML
  public void onMoveUp(ActionEvent e) {
    moveSelected(-1);
  }

  @FXML
  public void onMoveDown(ActionEvent e) {
    moveSelected(1);
  }

  private void moveSelected(int delta) {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    if (item == null || item.getParent() == null) {
      return;
    }
    ContentElement parent = item.getParent().getValue();
    int target = parent.getChildren().indexOf(item.getValue()) + delta;
    if (target < 0 || target >= parent.getChildren().size()) {
      return;
    }
    execute(new MoveElementCommand(parent, item.getValue(), delta, this::rememberSelection));
  }

  @FXML
  public void onCut(ActionEvent e) {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    if (item == null || item.getParent() == null || !copyToClipboard(item.getValue())) {
      return;
    }
    execute(new RemoveElementCommand(item.getParent().getValue(), item.getValue(), this::rememberSelection));
  }

  @FXML
  public void onCopy(ActionEvent e) {
    ContentElement element = selectedElement();
    if (element != null && copyToClipboard(element)) {
      updateActionState();
    }
  }

  /** Pastes the clipboard element as the last child of the selected element. */
  @FXML
  public void onPaste(ActionEvent e) {
    ContentElement parent = selectedElement();
    ContentElement clone = clipboardJson == null ? null : cloneFromJson(clipboardJson);
    if (parent == null || clone == null) {
      return;
    }
    execute(new InsertElementCommand(parent, clone, Integer.MAX_VALUE, this::rememberSelection));
  }

  /** Inserts a copy of the selected element right after it. */
  @FXML
  public void onDuplicate(ActionEvent e) {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    if (item == null || item.getParent() == null) {
      return;
    }
    ContentElement clone;
    try {
      clone = cloneFromJson(ElementStateCommand.capture(item.getValue()));
    }
    catch (Exception ex) {
      log.warn("Failed to duplicate content element: {}", ex.getMessage(), ex);
      return;
    }
    if (clone == null) {
      return;
    }
    ContentElement parent = item.getParent().getValue();
    execute(new InsertElementCommand(parent, clone, parent.getChildren().indexOf(item.getValue()) + 1, this::rememberSelection));
  }

  @FXML
  public void onUndo(ActionEvent e) {
    if (commandStack.canUndo()) {
      lastStateCommand = null;
      pendingSelection = null;
      commandStack.undo();
      afterCommand();
    }
  }

  @FXML
  public void onRedo(ActionEvent e) {
    if (commandStack.canRedo()) {
      lastStateCommand = null;
      pendingSelection = null;
      commandStack.redo();
      afterCommand();
    }
  }

  /** Runs a structural change through the command stack, then brings the tree, the panels and the file up to date. */
  private void execute(Command command) {
    lastStateCommand = null;
    pendingSelection = null;
    commandStack.execute(command);
    afterCommand();
  }

  private void rememberSelection(ContentElement element) {
    pendingSelection = element;
  }

  private void afterCommand() {
    ContentElement toSelect = pendingSelection != null ? pendingSelection : selectedElement();
    pendingSelection = null;
    refreshTree(toSelect);
    commitChange();
  }

  /**
   * Rebuilds the tree items from the model (which the commands changed), keeping collapsed nodes collapsed and
   * selecting {@code toSelect}, or the root if that is gone.
   */
  private void refreshTree(@Nullable ContentElement toSelect) {
    Set<ContentElement> collapsed = Collections.newSetFromMap(new IdentityHashMap<>());
    collectCollapsed(elementsTree.getRoot(), collapsed);
    TreeItem<ContentElement> root = buildTreeItem(model.getContent().getRoot(), collapsed);
    TreeItem<ContentElement> target = toSelect != null ? findItem(root, toSelect) : null;
    if (target == null) {
      target = root;
    }
    for (TreeItem<ContentElement> ancestor = target.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
      ancestor.setExpanded(true);
    }
    refreshing = true;
    try {
      elementsTree.setRoot(root);
      elementsTree.getSelectionModel().select(target);
    }
    finally {
      refreshing = false;
    }
    showElement(target.getValue());
    updateActionState();
  }

  private static void collectCollapsed(@Nullable TreeItem<ContentElement> item, Set<ContentElement> collapsed) {
    if (item == null) {
      return;
    }
    if (!item.isLeaf() && !item.isExpanded()) {
      collapsed.add(item.getValue());
    }
    item.getChildren().forEach(child -> collectCollapsed(child, collapsed));
  }

  /**
   * Records the edit a settings panel just applied to the selected element as an undoable command. Edits that
   * follow each other closely on the same element (typing, dragging a slider) share one command.
   */
  private void recordStateChange() {
    ContentElement element = selectedElement();
    if (element == null || stateSnapshot == null) {
      return;
    }
    String after = ElementStateCommand.capture(element);
    if (after.equals(stateSnapshot)) {
      return;
    }
    long now = System.currentTimeMillis();
    if (lastStateCommand != null && lastStateCommand.getElement() == element && now - lastStateTime < COALESCE_MS) {
      lastStateCommand.setAfter(after);
    }
    else {
      lastStateCommand = new ElementStateCommand(element, stateSnapshot, after, this::rememberSelection);
      commandStack.execute(lastStateCommand);
    }
    lastStateTime = now;
    stateSnapshot = after;
    updateActionState();
  }

  private static boolean copyToClipboard(ContentElement element) {
    try {
      clipboardJson = ElementStateCommand.capture(element);
      return true;
    }
    catch (Exception ex) {
      log.warn("Failed to copy content element to clipboard: {}", ex.getMessage(), ex);
      return false;
    }
  }

  private static @Nullable ContentElement cloneFromJson(String json) {
    try {
      ContentElement clone = JsonSettings.objectMapper.readValue(json, ContentElement.class);
      regenerateIds(clone);
      return clone;
    }
    catch (Exception ex) {
      log.warn("Failed to clone content element: {}", ex.getMessage(), ex);
      return null;
    }
  }

  /** A clone must never share an id with the element it was copied from. */
  private static void regenerateIds(ContentElement element) {
    element.setId(ContentElementDefaults.newId());
    if (element.getChildren() != null) {
      element.getChildren().forEach(ContentModelEditorController::regenerateIds);
    }
  }

  private void updateActionState() {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    boolean hasSelection = item != null;
    boolean isRoot = !hasSelection || item.getParent() == null;
    int index = -1;
    int siblingCount = 0;
    if (!isRoot) {
      List<ContentElement> siblings = item.getParent().getValue().getChildren();
      index = siblings.indexOf(item.getValue());
      siblingCount = siblings.size();
    }
    undoButton.setDisable(!commandStack.canUndo());
    redoButton.setDisable(!commandStack.canRedo());
    addButton.setDisable(!hasSelection);
    deleteButton.setDisable(isRoot);
    moveUpButton.setDisable(isRoot || index <= 0);
    moveDownButton.setDisable(isRoot || index >= siblingCount - 1);
    cutButton.setDisable(isRoot);
    duplicateButton.setDisable(isRoot);
    copyButton.setDisable(!hasSelection);
    pasteButton.setDisable(!hasSelection || clipboardJson == null);
  }

  private void onTreeKeyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.DELETE) {
      onRemoveElement(null);
    }
    else if (event.isShortcutDown() && !event.isAltDown() && !event.isShiftDown()) {
      switch (event.getCode()) {
        case X -> onCut(null);
        case C -> onCopy(null);
        case V -> onPaste(null);
        case D -> onDuplicate(null);
        case Z -> onUndo(null);
        case Y -> onRedo(null);
        default -> {
          return;
        }
      }
    }
    else {
      return;
    }
    event.consume();
  }

  /**
   * The tree's context menu: every toolbar action, each entry enabled exactly when its toolbar button is. The items
   * are created once (a context menu without items never opens); only their enabled state is refreshed on showing.
   */
  private ContextMenu createContextMenu() {
    Map<MenuItem, Button> mirrored = new LinkedHashMap<>();
    ContextMenu menu = new ContextMenu();
    menu.getItems().addAll(
        menuItem(mirrored, "undo", Icons.UNDO, undoButton, this::onUndo),
        menuItem(mirrored, "redo", Icons.REDO, redoButton, this::onRedo),
        new SeparatorMenuItem(),
        menuItem(mirrored, "content_model_tree.add_child", Icons.PLUS, addButton, this::onAddChild),
        new SeparatorMenuItem(),
        menuItem(mirrored, "content_model_tree.delete", Icons.TRASH, deleteButton, this::onRemoveElement),
        new SeparatorMenuItem(),
        menuItem(mirrored, "content_model_tree.move_up", Icons.ARROW_UP, moveUpButton, this::onMoveUp),
        menuItem(mirrored, "content_model_tree.move_down", Icons.ARROW_DOWN, moveDownButton, this::onMoveDown),
        new SeparatorMenuItem(),
        menuItem(mirrored, "content_model_tree.cut", Icons.CUT, cutButton, this::onCut),
        menuItem(mirrored, "content_model_tree.copy", Icons.COPY, copyButton, this::onCopy),
        menuItem(mirrored, "content_model_tree.paste", Icons.PASTE, pasteButton, this::onPaste),
        menuItem(mirrored, "content_model_tree.duplicate", Icons.COPY, duplicateButton, this::onDuplicate));
    menu.setOnShowing(event -> {
      updateActionState();
      mirrored.forEach((item, button) -> item.setDisable(button.isDisable()));
    });
    return menu;
  }

  private static MenuItem menuItem(Map<MenuItem, Button> mirrored, String bundleKey, String icon, Button button,
      EventHandler<ActionEvent> action) {
    MenuItem item = new MenuItem(StudioBundle.get(bundleKey));
    FontIcon fontIcon = WidgetFactory.createIcon(icon);
    fontIcon.getStyleClass().add("menu-icon");
    item.setGraphic(fontIcon);
    item.setOnAction(action);
    mirrored.put(item, button);
    return item;
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.CONTENT;
  }
}
