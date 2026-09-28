package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementDefaults;
import de.a12.studio.models.contentmodel.ContentElementFactory;
import de.a12.studio.models.contentmodel.ContentInsertion;
import de.a12.studio.models.contentmodel.ContentModule;
import de.a12.studio.models.contentmodel.ContentInsertion.Position;
import de.a12.studio.models.contentmodel.ContentStructure;
import de.a12.studio.models.contentmodel.ContentTableColumns;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.validators.content.ContentRootElementValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.contentmodel.commands.ElementStateCommand;
import de.a12.studio.ui.editors.contentmodel.commands.InsertElementCommand;
import de.a12.studio.ui.editors.contentmodel.commands.MoveElementCommand;
import de.a12.studio.ui.editors.contentmodel.commands.RelocateElementCommand;
import de.a12.studio.ui.editors.contentmodel.commands.RemoveElementCommand;
import de.a12.studio.ui.editors.contentmodel.dialogs.Dialogs;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.ModelSaveEvent;
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
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

  // Identifies a drag of an element within the tree; the dragged element itself is tracked in draggedElement.
  private static final DataFormat DRAG_FORMAT = new DataFormat("application/x-a12-content-model-element");
  private static final List<String> ISSUE_STYLE_CLASSES = List.of("validation-error", "validation-warning");
  private static final List<String> DROP_STYLE_CLASSES = List.of("tree-row-drop-above", "tree-row-drop-below", "tree-row-drop-into");

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
  private SplitPane editorSplitPane;

  @FXML
  private BorderPane previewPane;

  @FXML
  private StackPane previewContainer;

  @FXML
  private WebView previewWebView;

  @FXML
  private Button zoomOutButton;

  @FXML
  private Button zoomInButton;

  @FXML
  private Button openInBrowserButton;

  @FXML
  private Button openInWindowButton;

  @FXML
  private Label previewUnavailableLabel;

  @FXML
  private Label issueSummaryLabel;

  @FXML
  private ErrorContainerController elementIssuesController;

  private final List<ContentSettingsPanel> panels = new ArrayList<>();
  private final Debouncer saveDebouncer = new Debouncer();
  private boolean savePending;
  private ContentModel model;
  // JavaScript only holds a weak reference to what it is handed, so the bridge is kept here for the editor's lifetime.
  private final PreviewSelectionBridge selectionBridge = new PreviewSelectionBridge();
  // Non-null while the preview WebView has been moved into its own floating window (see onOpenInWindow).
  private ContentPreviewWindow previewWindow;

  private final CommandStack commandStack = new CommandStack();
  // Set by the commands while they run: the element the tree should select once the model has changed.
  private ContentElement pendingSelection;
  private boolean refreshing;
  private ContentElement draggedElement;
  // What the pickers of the property column may offer; rebuilt when the binding or a Document Model may have changed.
  private ContentDocumentReferences references;
  // What the validators found, by the id of the element it is about; refreshed by refreshIssues.
  private Map<String, List<ModelValidationError>> issuesById = Map.of();
  // The enabled state of the actions the menu has and the toolbar has no button for, set by updateActionState.
  private boolean addAboveDisabled = true;
  private boolean addBelowDisabled = true;
  private boolean pasteAboveDisabled = true;
  private boolean pasteBelowDisabled = true;
  // The selected element as the panels last left it (JSON), the "before" of the next panel edit.
  private String stateSnapshot;
  private ElementStateCommand lastStateCommand;
  private long lastStateTime;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    elementsTree.setCellFactory(tree -> setupDragAndDrop(new javafx.scene.control.TreeCell<ContentElement>() {
      @Override
      protected void updateItem(ContentElement element, boolean empty) {
        super.updateItem(element, empty);
        setText(empty || element == null ? null : typeLabel(element));
        getStyleClass().removeAll(ISSUE_STYLE_CLASSES);
        setTooltip(null);
        if (empty || element == null) {
          setGraphic(null);
          return;
        }
        markIssues(this, issuesOf(element));
        // "tree-icon" lets the tree stylesheet switch the icon to the inverse color on the selected row.
        FontIcon icon = WidgetFactory.createIcon(ContentElementIcons.iconFor(element.getType()), TREE_ICON_SIZE, null);
        icon.getStyleClass().add("tree-icon");
        setGraphic(icon);
      }
    }));
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
      public ContentReferences references() {
        return references;
      }

      @Override
      public List<String> locales() {
        return model.getLocales().stream().map(locale -> locale.getCode()).toList();
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
  }

  private static String typeLabel(ContentElement element) {
    return element.getType() != null ? element.getType() : "?";
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((ContentModel) model);
    refreshIssues();
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
    openInWindowButton.setDisable(true);
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
   * Moves the preview {@code WebView} out of the editor into its own floating window, so the preview can be seen
   * alongside the editor on a second monitor; closing that window (see {@link ContentPreviewWindow}) automatically
   * moves the WebView back here. The whole preview column (not just the {@code WebView}) is taken out of {@link
   * #editorSplitPane} while it is decoupled, so the tree and settings columns get its space instead of leaving it
   * behind as an empty gap; the divider positions the two remaining columns had are restored once it comes back.
   */
  @FXML
  public void onOpenInWindow(ActionEvent e) {
    if (previewWindow != null) {
      return;
    }
    int previewIndex = editorSplitPane.getItems().indexOf(previewPane);
    double[] dividerPositions = editorSplitPane.getDividerPositions();
    previewContainer.getChildren().remove(previewWebView);
    editorSplitPane.getItems().remove(previewPane);
    openInWindowButton.setDisable(true);
    previewWindow = new ContentPreviewWindow(Studio.stage, projectItem.getDisplayName(), previewWebView, window -> {
      previewWindow = null;
      previewContainer.getChildren().add(0, previewWebView);
      editorSplitPane.getItems().add(previewIndex, previewPane);
      editorSplitPane.setDividerPositions(dividerPositions);
      openInWindowButton.setDisable(false);
    });
    previewWindow.show();
  }

  /**
   * Stops the preview page (it polls the preview server) once the editor's tab is closed, writes an edit that is
   * still waiting for its debounced save, and releases the panels.
   */
  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    if (event.getItem().equals(projectItem)) {
      if (previewWindow != null) {
        // Synchronously restores previewWebView into previewContainer via the onClosed callback below.
        previewWindow.close();
      }
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
    this.references = new ContentDocumentReferences(model, projectItem);
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
    showElementIssues(element);
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
      ContentElement element = selectedElement();
      panels.stream().filter(panel -> panel != source && panel.followsOtherPanels()).forEach(panel -> panel.showElement(element));
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
    addNear(selectedElement(), Position.AS_CHILD);
  }

  /** Asks which of the element types that fit here to add, then inserts it right before the selection. */
  public void onAddAbove(ActionEvent e) {
    addNear(selectedElement(), Position.ABOVE);
  }

  /** Asks which of the element types that fit here to add, then inserts it right after the selection. */
  public void onAddBelow(ActionEvent e) {
    addNear(selectedElement(), Position.BELOW);
  }

  private void addNear(ContentElement target, Position position) {
    ContentElement root = model.getContent().getRoot();
    if (target == null || root == null) {
      return;
    }
    List<ContentModule> insertable = ContentInsertion.insertableModules(root, target, position);
    if (insertable.isEmpty()) {
      return;
    }
    // The dialog names the element the new one is relative to.
    Dialogs.showInsertElement(Studio.stage, typeLabel(target), insertable).ifPresent(chosen -> insertNear(target, chosen, position));
  }

  /** Appends a new element of {@code module} as the last child of {@code parent} (undoable, selects the new element). */
  void addChild(@NonNull ContentElement parent, @NonNull ContentModule module) {
    insertNear(parent, module, Position.AS_CHILD);
  }

  private void insertNear(@NonNull ContentElement target, @NonNull ContentModule module, @NonNull Position position) {
    Placement placement = placement(target, position);
    if (placement == null) {
      return;
    }
    ContentElement table = ContentInsertion.closestTable(model.getContent().getRoot(), placement.parent());
    int tableColumns = table != null ? ContentTableColumns.columns(table).size() : 0;
    execute(new InsertElementCommand(placement.parent(), ContentElementFactory.create(module, tableColumns), placement.index(),
        this::rememberSelection));
  }

  /** Where something inserted at {@code position} relative to {@code target} goes: its parent and its index there. */
  private record Placement(ContentElement parent, int index) {
  }

  private @Nullable Placement placement(@NonNull ContentElement target, @NonNull Position position) {
    if (position == Position.AS_CHILD) {
      return new Placement(target, Integer.MAX_VALUE);
    }
    ContentElement parent = parentOf(target);
    if (parent == null || parent.getChildren() == null) {
      return null;
    }
    return new Placement(parent, parent.getChildren().indexOf(target) + (position == Position.BELOW ? 1 : 0));
  }

  private @Nullable ContentElement parentOf(@NonNull ContentElement element) {
    TreeItem<ContentElement> item = findItem(elementsTree.getRoot(), element);
    return item != null && item.getParent() != null ? item.getParent().getValue() : null;
  }

  @FXML
  public void onRemoveElement(ActionEvent e) {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    if (item == null || item.getParent() == null || !ContentStructure.canRemove(model.getContent().getRoot(), item.getValue())) {
      // The root element cannot be removed (mirrors the content engine's root element rules), nor an element the
      // structure rules of its parent need (a table's head, ...).
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
    if (item == null || item.getParent() == null || !ContentStructure.canMove(model.getContent().getRoot(), item.getValue(), delta)) {
      return;
    }
    execute(new MoveElementCommand(item.getParent().getValue(), item.getValue(), delta, this::rememberSelection));
  }

  @FXML
  public void onCut(ActionEvent e) {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    if (item == null || item.getParent() == null || !ContentStructure.canRemove(model.getContent().getRoot(), item.getValue())
        || !copyToClipboard(item.getValue())) {
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
    pasteAt(Position.AS_CHILD);
  }

  /** Pastes the clipboard element right before the selected element. */
  public void onPasteAbove(ActionEvent e) {
    pasteAt(Position.ABOVE);
  }

  /** Pastes the clipboard element right after the selected element. */
  public void onPasteBelow(ActionEvent e) {
    pasteAt(Position.BELOW);
  }

  private void pasteAt(Position position) {
    ContentElement target = selectedElement();
    ContentElement clone = clipboardJson == null ? null : cloneFromJson(clipboardJson);
    if (target == null || clone == null || !ContentStructure.canPaste(model.getContent().getRoot(), clone, target, position)) {
      return;
    }
    Placement placement = placement(target, position);
    if (placement != null) {
      execute(new InsertElementCommand(placement.parent(), clone, placement.index(), this::rememberSelection));
    }
  }

  /** Inserts a copy of the selected element right after it. */
  @FXML
  public void onDuplicate(ActionEvent e) {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    if (item == null || item.getParent() == null || !ContentStructure.canDuplicate(model.getContent().getRoot(), item.getValue())) {
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

  /**
   * Enables the actions that the structure rules of the element types allow for the selection - the same questions SME's
   * editor asks before it enables Move, Cut, Duplicate and Paste - and disables the rest.
   */
  private void updateActionState() {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    ContentElement root = model != null ? model.getContent().getRoot() : null;
    ContentElement selected = item != null && root != null ? item.getValue() : null;
    boolean isRoot = selected == null || item.getParent() == null;
    ContentElement pasteSource = selected != null && clipboardJson != null ? cloneFromJson(clipboardJson) : null;

    undoButton.setDisable(!commandStack.canUndo());
    redoButton.setDisable(!commandStack.canRedo());
    addButton.setDisable(selected == null || !ContentInsertion.canInsert(root, selected, Position.AS_CHILD));
    addAboveDisabled = isRoot || !ContentInsertion.canInsert(root, selected, Position.ABOVE);
    addBelowDisabled = isRoot || !ContentInsertion.canInsert(root, selected, Position.BELOW);
    boolean removable = !isRoot && ContentStructure.canRemove(root, selected);
    deleteButton.setDisable(!removable);
    cutButton.setDisable(!removable);
    moveUpButton.setDisable(isRoot || !ContentStructure.canMove(root, selected, -1));
    moveDownButton.setDisable(isRoot || !ContentStructure.canMove(root, selected, 1));
    duplicateButton.setDisable(isRoot || !ContentStructure.canDuplicate(root, selected));
    copyButton.setDisable(selected == null);
    pasteButton.setDisable(pasteSource == null || !ContentStructure.canPaste(root, pasteSource, selected, Position.AS_CHILD));
    pasteAboveDisabled = pasteSource == null || isRoot || !ContentStructure.canPaste(root, pasteSource, selected, Position.ABOVE);
    pasteBelowDisabled = pasteSource == null || isRoot || !ContentStructure.canPaste(root, pasteSource, selected, Position.BELOW);
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
        ContentModelTreeContextMenu.Action.of(undoButton, this::onUndo),
        ContentModelTreeContextMenu.Action.of(redoButton, this::onRedo),
        ContentModelTreeContextMenu.Action.of(addButton, this::onAddChild),
        new ContentModelTreeContextMenu.Action(() -> addAboveDisabled, this::onAddAbove),
        new ContentModelTreeContextMenu.Action(() -> addBelowDisabled, this::onAddBelow),
        ContentModelTreeContextMenu.Action.of(deleteButton, this::onRemoveElement),
        ContentModelTreeContextMenu.Action.of(moveUpButton, this::onMoveUp),
        ContentModelTreeContextMenu.Action.of(moveDownButton, this::onMoveDown),
        ContentModelTreeContextMenu.Action.of(cutButton, this::onCut),
        ContentModelTreeContextMenu.Action.of(copyButton, this::onCopy),
        ContentModelTreeContextMenu.Action.of(pasteButton, this::onPaste),
        new ContentModelTreeContextMenu.Action(() -> pasteAboveDisabled, this::onPasteAbove),
        new ContentModelTreeContextMenu.Action(() -> pasteBelowDisabled, this::onPasteBelow),
        ContentModelTreeContextMenu.Action.of(duplicateButton, this::onDuplicate)));
  }

  // ---- Issues ----

  /** A change of this model's settings (Document Model, base group, ...) can change what is wrong with its elements. */
  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    super.modelSaved(event);
    if (projectItem != null && event.getItem().equals(projectItem)) {
      refreshIssues();
    }
  }

  /** Whether the Document Model an element refers to changed is not known; what it refers to may have. */
  @Override
  protected void onDocumentModelChangedElsewhere() {
    refreshIssues();
  }

  /**
   * Asks the validators what is wrong with the model and shows it: on the rows of the elements it is about (colored,
   * with the messages as tooltip), as a count under the tree, in the message box of the selected element's settings and
   * on the settings button for what belongs to the model's settings. Run when the model is loaded and after every save.
   */
  void refreshIssues() {
    if (references != null) {
      references.invalidate();
    }
    issuesById = collectIssues();
    elementsTree.refresh();
    updateIssueSummary();
    showElementIssues(selectedElement());
    // The badge asks the validation service too; there is none before a project is open (and in isolated tests).
    if (Studio.getValidationService() != null) {
      updateSettingsErrorBadge();
    }
  }

  private Map<String, List<ModelValidationError>> collectIssues() {
    ContentElement root = model != null ? model.getContent().getRoot() : null;
    if (root == null || Studio.getValidationService() == null) {
      return Map.of();
    }
    try {
      Set<String> ids = new java.util.HashSet<>();
      collectIds(root, ids);
      Map<String, List<ModelValidationError>> result = new LinkedHashMap<>();
      // Errors before warnings, so an element's messages read in the order of their weight.
      Studio.getValidationService().validate(model).stream()
          .sorted(java.util.Comparator.comparing((ModelValidationError error) -> !Severity.ERROR.name().equals(error.severity())))
          .forEach(error -> {
            // A problem with the root that has no element of its own to point at is reported against the root.
            String id = ContentRootElementValidator.ELEMENT_ID.equals(error.elementId()) ? root.getId() : error.elementId();
            if (id != null && ids.contains(id)) {
              result.computeIfAbsent(id, key -> new ArrayList<>()).add(error);
            }
          });
      return result;
    }
    catch (Exception e) {
      log.warn("Failed to validate content model '{}': {}", projectItem.getPath(), e.getMessage(), e);
      return Map.of();
    }
  }

  private static void collectIds(ContentElement element, Set<String> ids) {
    if (element.getId() != null) {
      ids.add(element.getId());
    }
    if (element.getChildren() != null) {
      element.getChildren().forEach(child -> collectIds(child, ids));
    }
  }

  /** What the validators found about {@code element}, errors first; empty if nothing. */
  List<ModelValidationError> issuesOf(@NonNull ContentElement element) {
    return element.getId() == null ? List.of() : issuesById.getOrDefault(element.getId(), List.of());
  }

  private static boolean hasError(List<ModelValidationError> issues) {
    return issues.stream().anyMatch(issue -> Severity.ERROR.name().equals(issue.severity()));
  }

  private static void markIssues(javafx.scene.control.TreeCell<ContentElement> cell, List<ModelValidationError> issues) {
    if (issues.isEmpty()) {
      return;
    }
    cell.getStyleClass().add(hasError(issues) ? "validation-error" : "validation-warning");
    cell.setTooltip(WidgetFactory.createTooltip(issues.stream().map(issue -> "\u2022 " + issue.message()).collect(Collectors.joining("\n"))));
  }

  private void updateIssueSummary() {
    long errors = issuesById.values().stream().flatMap(List::stream).filter(issue -> Severity.ERROR.name().equals(issue.severity())).count();
    long warnings = issuesById.values().stream().flatMap(List::stream).count() - errors;
    boolean any = errors + warnings > 0;
    issueSummaryLabel.setVisible(any);
    issueSummaryLabel.setManaged(any);
    issueSummaryLabel.setText(StudioBundle.get("content_model_tree.issue_summary", errors, warnings));
    issueSummaryLabel.getStyleClass().removeAll(ISSUE_STYLE_CLASSES);
    issueSummaryLabel.getStyleClass().add(errors > 0 ? "validation-error" : "validation-warning");
  }

  /** The message box above the settings of the selected element lists what is wrong with it. */
  private void showElementIssues(ContentElement element) {
    List<ModelValidationError> issues = element == null ? List.of() : issuesOf(element);
    if (issues.isEmpty()) {
      elementIssuesController.hide();
      return;
    }
    elementIssuesController.show(hasError(issues) ? Severity.ERROR.name() : Severity.WARNING.name(),
        issues.stream().map(ModelValidationError::message).collect(Collectors.joining("\n")));
  }

  /** Goes to the next element, in tree order after the selected one (wrapping around), that has a problem. */
  @FXML
  public void onIssueSummaryClicked(MouseEvent event) {
    ContentElement root = model != null ? model.getContent().getRoot() : null;
    if (root == null || issuesById.isEmpty()) {
      return;
    }
    List<ContentElement> all = new ArrayList<>();
    flatten(root, all);
    int start = all.indexOf(selectedElement());
    for (int step = 1; step <= all.size(); step++) {
      ContentElement candidate = all.get((start + step) % all.size());
      if (candidate.getId() != null && !issuesOf(candidate).isEmpty()) {
        selectElementById(candidate.getId());
        return;
      }
    }
  }

  private static void flatten(ContentElement element, List<ContentElement> all) {
    all.add(element);
    if (element.getChildren() != null) {
      element.getChildren().forEach(child -> flatten(child, all));
    }
  }

  // ---- Drag and drop ----

  /**
   * Lets an element be dragged to another place of the tree: onto the middle of a row to become its last child, onto its
   * top or bottom quarter to go right before or after it. Only places the structure rules allow accept the drop
   * ({@link ContentStructure#canRelocate}); the move is one undoable step.
   */
  private <C extends javafx.scene.control.TreeCell<ContentElement>> C setupDragAndDrop(C cell) {
    cell.setOnDragDetected(event -> {
      ContentElement element = cell.isEmpty() ? null : cell.getItem();
      if (element == null || model == null || element == model.getContent().getRoot()) {
        return;
      }
      draggedElement = element;
      Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(DRAG_FORMAT, String.valueOf(element.getId()));
      dragboard.setContent(content);
      event.consume();
    });
    cell.setOnDragOver(event -> {
      Position position = !cell.isEmpty() && cell.getItem() != null && event.getDragboard().hasContent(DRAG_FORMAT)
          ? dropPosition(cell.getItem(), event.getY(), cell.getHeight())
          : null;
      if (position != null) {
        event.acceptTransferModes(TransferMode.MOVE);
        showDropIndicator(cell, position);
      }
      else {
        clearDropIndicator(cell);
      }
      event.consume();
    });
    cell.setOnDragExited(event -> clearDropIndicator(cell));
    cell.setOnDragDropped(event -> {
      boolean success = false;
      if (!cell.isEmpty() && cell.getItem() != null) {
        Position position = dropPosition(cell.getItem(), event.getY(), cell.getHeight());
        if (position != null) {
          relocate(draggedElement, cell.getItem(), position);
          success = true;
        }
      }
      clearDropIndicator(cell);
      event.setDropCompleted(success);
      event.consume();
    });
    cell.setOnDragDone(event -> draggedElement = null);
    return cell;
  }

  /** Where dropping the dragged element on {@code target} would put it, or null if the rules do not allow any place. */
  private @Nullable Position dropPosition(@NonNull ContentElement target, double relativeY, double rowHeight) {
    ContentElement root = model != null ? model.getContent().getRoot() : null;
    if (root == null || draggedElement == null) {
      return null;
    }
    double fraction = rowHeight <= 0 ? 0.5 : relativeY / rowHeight;
    if (fraction > 0.25 && fraction < 0.75 && ContentStructure.canRelocate(root, draggedElement, target, Position.AS_CHILD)) {
      return Position.AS_CHILD;
    }
    if (target == root) {
      return null;
    }
    Position sibling = fraction < 0.5 ? Position.ABOVE : Position.BELOW;
    return ContentStructure.canRelocate(root, draggedElement, target, sibling) ? sibling : null;
  }

  private void relocate(@NonNull ContentElement element, @NonNull ContentElement target, @NonNull Position position) {
    ContentElement oldParent = parentOf(element);
    Placement placement = placement(target, position);
    if (oldParent == null || placement == null) {
      return;
    }
    ContentElement newParent = placement.parent();
    List<ContentElement> siblings = newParent.getChildren();
    // The index the command works with is the one in the list without the element.
    int index;
    if (position == Position.AS_CHILD) {
      index = siblings == null ? 0 : siblings.size() - (newParent == oldParent ? 1 : 0);
    }
    else {
      index = placement.index();
      if (newParent == oldParent && siblings.indexOf(element) < siblings.indexOf(target)) {
        index--;
      }
    }
    execute(new RelocateElementCommand(oldParent, element, newParent, index, this::rememberSelection));
  }

  private static void showDropIndicator(javafx.scene.control.TreeCell<ContentElement> cell, Position position) {
    String showClass = switch (position) {
      case ABOVE -> "tree-row-drop-above";
      case BELOW -> "tree-row-drop-below";
      case AS_CHILD -> "tree-row-drop-into";
    };
    cell.getStyleClass().removeIf(styleClass -> DROP_STYLE_CLASSES.contains(styleClass) && !styleClass.equals(showClass));
    if (!cell.getStyleClass().contains(showClass)) {
      cell.getStyleClass().add(showClass);
    }
  }

  private static void clearDropIndicator(javafx.scene.control.TreeCell<ContentElement> cell) {
    cell.getStyleClass().removeAll(DROP_STYLE_CLASSES);
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
