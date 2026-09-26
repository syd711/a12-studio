package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementDefaults;
import de.a12.studio.models.contentmodel.ContentElementFactory;
import de.a12.studio.models.contentmodel.ContentInsertion;
import de.a12.studio.models.contentmodel.ContentModule;
import de.a12.studio.models.contentmodel.ContentTableColumns;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.contentmodel.commands.ElementStateCommand;
import de.a12.studio.ui.editors.contentmodel.commands.InsertElementCommand;
import de.a12.studio.ui.editors.contentmodel.commands.MoveElementCommand;
import de.a12.studio.ui.editors.contentmodel.commands.RemoveElementCommand;
import de.a12.studio.ui.editors.contentmodel.dialogs.Dialogs;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preview.PreviewLauncher;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.commandstack.Command;
import de.a12.studio.ui.util.commandstack.CommandStack;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

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
  // Used to center a row in the tree if the number of rows it shows is not known.
  private static final int TREE_ROW_ESTIMATE = 20;

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
  // JavaScript only holds a weak reference to what it is handed, so the bridge is kept here for the editor's lifetime.
  private final PreviewSelectionBridge selectionBridge = new PreviewSelectionBridge();

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
        FontIcon icon = WidgetFactory.createIcon(ContentElementIcons.iconFor(element.getType()), TREE_ICON_SIZE, null);
        icon.getStyleClass().add("tree-icon");
        setGraphic(icon);
      }
    });
    elementsTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (!refreshing) {
        showElement(newValue != null ? newValue.getValue() : null);
        updateActionState();
        syncPreviewSelection();
      }
    });
    previewWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == Worker.State.SUCCEEDED) {
        installSelectionBridge();
        syncPreviewSelection();
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
    syncPreviewSelection();
  }

  /** Lets the preview page report clicks on its elements (see {@code content-model-bootstrap.js}). */
  private void installSelectionBridge() {
    try {
      JSObject window = (JSObject) previewWebView.getEngine().executeScript("window");
      window.setMember("studioSelectionBridge", selectionBridge);
    }
    catch (RuntimeException e) {
      log.warn("The preview page cannot report clicks: {}", e.getMessage());
    }
  }

  /**
   * Has the preview page frame the selected element: the ids from the element up to the root are passed, since
   * elements that render nothing of their own (e.g. table rows) are framed as the closest ancestor that does.
   */
  private void syncPreviewSelection() {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    List<String> path = new ArrayList<>();
    for (; item != null; item = item.getParent()) {
      if (item.getValue().getId() != null) {
        path.add(item.getValue().getId());
      }
    }
    String script = "if (window.studioSelect) { window.studioSelect("
        + path.stream().map(id -> JsonSettings.objectMapper.valueToTree(id).toString()).collect(Collectors.joining(",", "[", "]"))
        + "); }";
    try {
      previewWebView.getEngine().executeScript(script);
    }
    catch (RuntimeException e) {
      log.debug("Preview selection not synchronized: {}", e.getMessage());
    }
  }

  private void selectElementById(String id) {
    TreeItem<ContentElement> item = findItemById(elementsTree.getRoot(), id);
    if (item == null) {
      return;
    }
    for (TreeItem<ContentElement> ancestor = item.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
      ancestor.setExpanded(true);
    }
    elementsTree.getSelectionModel().select(item);
    // The expanded rows are laid out before the scroll position can be worked out.
    Platform.runLater(() -> scrollToCenter(elementsTree.getRow(item)));
  }

  /**
   * Scrolls the tree so that {@code row} is in the middle of it, showing what is above as well as below - {@code
   * TreeView.scrollTo} would put it at the top. A row that is already visible stays where it is.
   */
  private void scrollToCenter(int row) {
    if (row < 0) {
      return;
    }
    int visibleRows = TREE_ROW_ESTIMATE;
    if (elementsTree.lookup(".virtual-flow") instanceof VirtualFlow<?> flow
        && flow.getFirstVisibleCell() != null && flow.getLastVisibleCell() != null) {
      int first = flow.getFirstVisibleCell().getIndex();
      int last = flow.getLastVisibleCell().getIndex();
      // The cells at the edges may be cut off, so they do not count as visible.
      if (row > first && row < last) {
        return;
      }
      visibleRows = last - first + 1;
    }
    elementsTree.scrollTo(Math.max(0, row - visibleRows / 2));
  }

  private static @Nullable TreeItem<ContentElement> findItemById(@Nullable TreeItem<ContentElement> from, String id) {
    if (from == null) {
      return null;
    }
    if (id.equals(from.getValue().getId())) {
      return from;
    }
    for (TreeItem<ContentElement> child : from.getChildren()) {
      TreeItem<ContentElement> found = findItemById(child, id);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /** The object the preview page calls, on the JavaFX thread, when one of the model's elements is clicked. */
  public class PreviewSelectionBridge {
    public void elementClicked(String id) {
      Platform.runLater(() -> selectElementById(id));
    }
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

  /** Asks which of the element types that fit here to add, then appends it as the last child of the selection. */
  @FXML
  public void onAddChild(ActionEvent e) {
    ContentElement parent = selectedElement();
    ContentElement root = model.getContent().getRoot();
    if (parent == null || root == null) {
      return;
    }
    List<ContentModule> insertable = ContentInsertion.insertableModules(root, parent, ContentInsertion.Position.AS_CHILD);
    if (insertable.isEmpty()) {
      return;
    }
    Dialogs.showInsertElement(Studio.stage, typeLabel(parent), insertable).ifPresent(chosen -> addChild(parent, chosen));
  }

  /** Appends a new element of {@code module} as the last child of {@code parent} (undoable, selects the new element). */
  void addChild(@NonNull ContentElement parent, @NonNull ContentModule module) {
    ContentElement table = ContentInsertion.closestTable(model.getContent().getRoot(), parent);
    int tableColumns = table != null ? ContentTableColumns.columns(table).size() : 0;
    execute(new InsertElementCommand(parent, ContentElementFactory.create(module, tableColumns), Integer.MAX_VALUE,
        this::rememberSelection));
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
    syncPreviewSelection();
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
    ContentElement root = model != null ? model.getContent().getRoot() : null;
    addButton.setDisable(!hasSelection || root == null
        || !ContentInsertion.canInsert(root, item.getValue(), ContentInsertion.Position.AS_CHILD));
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

  private ContextMenu createContextMenu() {
    return ContentModelTreeContextMenu.create(this::updateActionState, new ContentModelTreeContextMenu.Actions(
        new ContentModelTreeContextMenu.Action(undoButton, this::onUndo),
        new ContentModelTreeContextMenu.Action(redoButton, this::onRedo),
        new ContentModelTreeContextMenu.Action(addButton, this::onAddChild),
        new ContentModelTreeContextMenu.Action(deleteButton, this::onRemoveElement),
        new ContentModelTreeContextMenu.Action(moveUpButton, this::onMoveUp),
        new ContentModelTreeContextMenu.Action(moveDownButton, this::onMoveDown),
        new ContentModelTreeContextMenu.Action(cutButton, this::onCut),
        new ContentModelTreeContextMenu.Action(copyButton, this::onCopy),
        new ContentModelTreeContextMenu.Action(pasteButton, this::onPaste),
        new ContentModelTreeContextMenu.Action(duplicateButton, this::onDuplicate)));
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
