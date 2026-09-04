package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.editors.formmodel.MultiColumnSectionEditorPanelController;
import de.a12.studio.ui.editors.formmodel.documenttree.DocumentSourceTreeController;
import de.a12.studio.ui.editors.formmodel.formtree.commands.AddNodeCommand;
import de.a12.studio.ui.editors.formmodel.formtree.commands.MoveNodeCommand;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.FormNodeEditorControlGridPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.FormNodeEditorConfirmControlPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.FormNodeEditorControlPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.FormNodeEditorRepeatPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.FormNodeEditorRowPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.FormNodeEditorScreenPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.FormNodeEditorSectionPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.HideConditionPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.TabErrorBadge;
import de.a12.studio.ui.util.commandstack.Command;
import de.a12.studio.ui.util.commandstack.CommandStack;
import de.a12.studio.ui.util.commandstack.CompositeCommand;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The right-hand "Form Model" structural tree of the Overview tab ({@link FormModelEditorController#loadOverview}):
 * Screens -> Sections/Control Grids/Repeats -> Rows -> Cells, with a search filter, a context menu ({@link
 * FormModelActions}) mirroring the SME reference's Add/Delete/Cut/Copy/Paste/Move actions, and drag-and-drop -
 * both reordering/reparenting within this tree, and accepting Fields/Groups dropped from {@link
 * DocumentSourceTreeController}'s tree to create the matching {@link Control}/{@link InlineRepeat}.
 */
@Slf4j
public class FormModelTreeController implements Initializable {

  // Identifies an in-tree reorder/reparent drag; the dragged node itself is tracked via draggedTreeItem (same
  // controller, same tree), mirroring DocumentModelElementsTreeController's ELEMENT_DRAG_FORMAT.
  private static final DataFormat INTERNAL_DRAG_FORMAT = new DataFormat("application/x-a12-form-model-tree-node");

  private static final List<String> DROP_STYLE_CLASSES =
      List.of("tree-row-drop-above", "tree-row-drop-below", "tree-row-drop-into");

  private static final String TABLE_SETTINGS_ID = ModelType.FORM.getValue();
  private static final String TREE_DIVIDER_ID = "treeEditorDivider";


  @FXML
  private Button undoButton;

  @FXML
  private Button redoButton;

  @FXML
  private SearchFieldController searchController;

  @FXML
  private ErrorContainerController errorContainerController;

  @FXML
  private SplitPane treeEditorSplitPane;

  @FXML
  private TreeView<FormElementViewModel> tree;

  @FXML
  private Label noSelectionLabel;
  @FXML
  private Node rowEditor;
  @FXML
  private FormNodeEditorRowPanelController rowEditorController;
  @FXML
  private Node multiColumnSectionEditor;
  @FXML
  private MultiColumnSectionEditorPanelController multiColumnSectionEditorController;
  @FXML
  private Node screenEditor;
  @FXML
  private FormNodeEditorScreenPanelController screenEditorController;
  @FXML
  private Node sectionEditor;
  @FXML
  private FormNodeEditorSectionPanelController sectionEditorController;
  @FXML
  private Node controlGridEditor;
  @FXML
  private FormNodeEditorControlGridPanelController controlGridEditorController;
  @FXML
  private Node controlEditor;
  @FXML
  private FormNodeEditorControlPanelController controlEditorController;
  @FXML
  private Node confirmControlEditor;
  @FXML
  private FormNodeEditorConfirmControlPanelController confirmControlEditorController;
  @FXML
  private Node repeatEditor;
  @FXML
  private FormNodeEditorRepeatPanelController repeatEditorController;

  private ProjectItem projectItem;
  private FormModelContent content;
  private FormModelActions actions;
  private final CommandStack commandStack = new CommandStack();

  // Kept so node editors (Section, Row, ControlGrid, Screen) can populate their Hide Condition
  // field combos with the boolean fields available from the linked Document Model.
  private @Nullable DocumentModel documentModel;

  private Map<String, Element> documentElementsById = Map.of();

  // Resolves Control/Repeat/Repeat Overview Column display names (see FormElementViewModel#getName), following
  // includes into other Document Models in the project - unlike documentElementsById above, which only indexes
  // this Form Model's own linked Document Model and is used purely for local drag-and-drop lookups.
  private @Nullable ElementIndex elementIndex;

  private TreeItem<FormElementViewModel> draggedTreeItem;

  private Runnable onNodeSelected;

  private enum DropLocation {ABOVE, BELOW, INTO}

  private record DropTarget(TreeItem<FormElementViewModel> targetItem, DropLocation location) {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    errorContainerController.addStyleClass("error-container-no-radius");

    searchController.setOnSearch(this::applyFilter);
    tree.setShowRoot(false);
    tree.setCellFactory(view -> {
      FormModelTreeCell cell = new FormModelTreeCell(this::createContextMenu);
      setupCellDragAndDrop(cell);
      return cell;
    });
    tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      updateEditorPane(newValue);
      if (newValue != null && onNodeSelected != null) {
        onNodeSelected.run();
      }
    });
    updateEditorPane(null);
    updateUndoRedoState();

    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    applyDividerPosition(tableSettings);
    treeEditorSplitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) ->
        saveDividerPosition(newValue.doubleValue()));
  }

  private void applyDividerPosition(BaseTableSettings tableSettings) {
    if (tableSettings == null) {
      return;
    }
    double position = tableSettings.getDividerPosition(TREE_DIVIDER_ID);
    if (position >= 0) {
      treeEditorSplitPane.setDividerPosition(0, position);
    }
  }

  private void saveDividerPosition(double position) {
    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    if (tableSettings == null) {
      return;
    }
    tableSettings.getDividerPositions().put(TREE_DIVIDER_ID, position);
    tableSettings.save();
  }

  /**
   * Registers a callback invoked whenever a node is selected in this tree (not on deselection), used by
   * {@link de.a12.studio.ui.editors.formmodel.FormModelEditorController} to auto-collapse the "Document/
   * Relationship Model" sidebar panel while it isn't pinned.
   */
  public void setOnNodeSelected(@Nullable Runnable onNodeSelected) {
    this.onNodeSelected = onNodeSelected;
  }

  @FXML
  private void onUndo() {
    commandStack.undo();
    onModelChanged(null);
  }

  @FXML
  private void onRedo() {
    commandStack.redo();
    onModelChanged(null);
  }

  private void updateUndoRedoState() {
    undoButton.setDisable(!commandStack.canUndo());
    redoButton.setDisable(!commandStack.canRedo());
  }

  /**
   * Shows/populates the right-hand editor pane for whichever node is currently selected in the tree - a
   * {@link Row}, {@link MultiColumnSection}, {@link Screen}, {@link Section} or {@link ControlGrid} each get
   * their own dedicated editor pane ({@link FormNodeEditorRowPanelController}/{@link
   * MultiColumnSectionEditorPanelController}/{@link FormNodeEditorScreenPanelController}/{@link
   * FormNodeEditorSectionPanelController}/{@link FormNodeEditorControlGridPanelController}); any other node type
   * (or no selection) falls back to {@link #noSelectionLabel}.
   */
  private void updateEditorPane(@Nullable TreeItem<FormElementViewModel> selectedItem) {
    Object node = selectedItem != null && selectedItem.getValue() != null ? selectedItem.getValue().getNode() : null;

    boolean isRow = node instanceof Row;
    boolean isMultiColumnSection = node instanceof MultiColumnSection;
    boolean isScreen = node instanceof Screen;
    boolean isSection = node instanceof Section;
    boolean isControlGrid = node instanceof ControlGrid;
    boolean isConfirmControl = node instanceof Control && isConfirmField((Control) node);
    boolean isControl = node instanceof Control && !isConfirmControl;
    boolean isRepeat = node instanceof AbstractRepeat;

    setVisible(rowEditor, isRow);
    setVisible(multiColumnSectionEditor, isMultiColumnSection);
    setVisible(screenEditor, isScreen);
    setVisible(sectionEditor, isSection);
    setVisible(controlGridEditor, isControlGrid);
    setVisible(controlEditor, isControl);
    setVisible(confirmControlEditor, isConfirmControl);
    setVisible(repeatEditor, isRepeat);
    setVisible(noSelectionLabel, !(isRow || isMultiColumnSection || isScreen || isSection
        || isControlGrid || isControl || isConfirmControl || isRepeat));

    if (isRow) {
      rowEditorController.setRow((Row) node, elementIndex, containerHideConditionScope(selectedItem));
    }
    else if (isMultiColumnSection) {
      multiColumnSectionEditorController.setSection((MultiColumnSection) node);
    }
    else if (isScreen) {
      screenEditorController.setScreen((Screen) node, screenIds());
    }
    else if (isSection) {
      sectionEditorController.setSection((Section) node, elementIndex, containerHideConditionScope(selectedItem));
    }
    else if (isControlGrid) {
      controlGridEditorController.setControlGrid((ControlGrid) node, elementIndex, containerHideConditionScope(selectedItem));
    }
    else if (isControl) {
      controlEditorController.setControl((Control) node, documentModel, elementIndex, content);
    }
    else if (isConfirmControl) {
      confirmControlEditorController.setControl((Control) node, documentModel, elementIndex, content);
    }
    else if (isRepeat) {
      repeatEditorController.setRepeat((AbstractRepeat) node, documentModel, content,
          elementIndex, containerHideConditionScope(selectedItem));
    }
  }

  /**
   * Where a Section/Row/ControlGrid/Repeat node should look for hide-condition master fields: anchored at the
   * Document Model group the nearest ancestor {@link AbstractRepeat} iterates over ({@code groupRef}), or -
   * when this node isn't nested in any Repeat - {@link HideConditionPanelController.MasterFieldScope#root()},
   * mirroring the SME reference's {@code resolveClosestRepeatGroupOrRoot}.
   */
  private HideConditionPanelController.@NonNull MasterFieldScope containerHideConditionScope(
      @Nullable TreeItem<FormElementViewModel> selectedItem) {
    AbstractRepeat ancestorRepeat = findAncestorRepeat(selectedItem);
    return ancestorRepeat == null
        ? HideConditionPanelController.MasterFieldScope.root()
        : HideConditionPanelController.MasterFieldScope.anchoredOrUnbound(ancestorRepeat.getGroupRef(), elementIndex);
  }

  // Walks strictly upward from the selected tree item (never including it) to find the nearest enclosing Repeat.
  private static @Nullable AbstractRepeat findAncestorRepeat(@Nullable TreeItem<FormElementViewModel> item) {
    TreeItem<FormElementViewModel> current = item == null ? null : item.getParent();
    while (current != null) {
      Object node = current.getValue() != null ? current.getValue().getNode() : null;
      if (node instanceof AbstractRepeat repeat) {
        return repeat;
      }
      current = current.getParent();
    }
    return null;
  }

  private static void setVisible(@NonNull Node node, boolean visible) {
    node.setVisible(visible);
    node.setManaged(visible);
  }

  private List<String> screenIds() {
    return content.getScreens().stream().map(Screen::getId).collect(Collectors.toList());
  }

  /**
   * Refreshes every visible tree cell's displayed label without rebuilding the tree structure - called by
   * {@link FormModelEditorController#modelSaved} whenever any property editor (including the right-hand editor
   * panes wired above) saves a change, so a Name/Label edit is reflected in the tree immediately even though
   * those panes commit directly rather than going through {@link FormModelActions}' {@code onModelChanged}.
   */
  public void refreshTreeLabels() {
    tree.refresh();
  }

  public void setModel(@NonNull FormModel model, @Nullable DocumentModel documentModel, @NonNull ProjectItem projectItem) {
    this.projectItem = projectItem;
    this.content = ensureContent(model);
    this.documentModel = documentModel;
    this.documentElementsById = indexDocumentModel(documentModel);
    this.elementIndex = hasModelRoot(documentModel)
        ? new ElementIndex(documentModel, ProjectDocumentModels.getOtherDocumentModels(projectItem))
        : null;
    this.actions = new FormModelActions(content, commandStack, this::onModelChanged);
    tree.setContextMenu(createContextMenu(null));
    applyFilter(searchController.getText());
  }

  private static boolean hasModelRoot(@Nullable DocumentModel documentModel) {
    return documentModel != null && documentModel.getContent() != null && documentModel.getContent().getModelRoot() != null;
  }

  private static FormModelContent ensureContent(@NonNull FormModel model) {
    FormModelContent content = model.getContent();
    if (content == null) {
      content = new FormModelContent();
      model.setContent(content);
    }
    return content;
  }

  private static Map<String, Element> indexDocumentModel(@Nullable DocumentModel documentModel) {
    Map<String, Element> elementsById = new HashMap<>();
    if (hasModelRoot(documentModel)) {
      for (GroupElement group : documentModel.getContent().getModelRoot().getRootGroups()) {
        indexElement(group, elementsById);
      }
    }
    return elementsById;
  }

  private static void indexElement(@NonNull Element element, @NonNull Map<String, Element> elementsById) {
    elementsById.put(element.getId(), element);
    if (element instanceof GroupElement group && group.getGroup() != null) {
      for (Element child : group.getGroup().getElements()) {
        indexElement(child, elementsById);
      }
    }
  }

  private ContextMenu createContextMenu(@Nullable FormElementViewModel selected) {
    return actions.createContextMenu(selected);
  }

  @FXML
  private void onExpandAll() {
    setExpandedRecursive(tree.getRoot(), true);
  }

  @FXML
  private void onCollapseAll() {
    TreeItem<FormElementViewModel> root = tree.getRoot();
    if (root == null) {
      return;
    }
    // Root is hidden (showRoot=false) but must stay expanded, otherwise its
    // top-level children would be hidden along with it.
    root.setExpanded(true);
    for (TreeItem<FormElementViewModel> child : root.getChildren()) {
      setExpandedRecursive(child, false);
    }
  }

  private void setExpandedRecursive(TreeItem<FormElementViewModel> item, boolean expanded) {
    if (item == null) {
      return;
    }
    item.setExpanded(expanded);
    for (TreeItem<FormElementViewModel> child : item.getChildren()) {
      setExpandedRecursive(child, expanded);
    }
  }

  private void onModelChanged(@Nullable Object nodeToSelect) {
    updateUndoRedoState();
    applyFilter(searchController.getText());
    if (nodeToSelect != null) {
      selectNode(nodeToSelect);
    }
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  private void applyFilter(String filter) {
    if (content == null) {
      return;
    }
    String term = filter == null ? "" : filter.trim().toLowerCase();
    TreeItem<FormElementViewModel> root = new TreeItem<>();
    for (Screen screen : content.getScreens()) {
      FormElementViewModel viewModel = new FormElementViewModel(screen, null, elementIndex);
      TreeItem<FormElementViewModel> item = term.isEmpty() ? toTreeItem(viewModel) : toFilteredTreeItem(viewModel, term);
      if (item != null) {
        root.getChildren().add(item);
      }
    }
    tree.setRoot(root);
    setExpandedRecursive(root, true);
    applyValidationState(root);
  }

  /**
   * Marks every tree row whose element has a validation problem (see {@link FormElementViewModel#hasError()} /
   * {@link FormModelTreeCell}), mirroring {@code DocumentModelElementsTreeController#applyValidationState}, and
   * reflects the aggregate error state onto this panel's own {@link #errorContainerController} so the enclosing
   * "Overview" {@link javafx.scene.control.Tab} picks up a {@link TabErrorBadge} even though the actual
   * per-row errors live on virtualized {@link javafx.scene.control.TreeCell}s that {@link TabErrorBadge}'s
   * scene-graph walk would otherwise miss whenever an errored row is scrolled out of view. Re-run on every
   * {@link #applyFilter} call - the tree is always fully rebuilt there (on load and after every edit via
   * {@link #onModelChanged}), so there's no need for a separate per-edit validation hook here.
   */
  private void applyValidationState(@NonNull TreeItem<FormElementViewModel> root) {
    Map<String, List<String>> errorMessagesById = errorMessagesByElementId();
    markErrors(root, errorMessagesById);

    if (errorMessagesById.isEmpty()) {
      errorContainerController.hide();
    }
    else {
      errorContainerController.show("ERROR", StudioBundle.get("form_model_tree.validation_error_message"));
    }
    TabErrorBadge.refresh(tree);
  }

  private Map<String, List<String>> errorMessagesByElementId() {
    if (!(projectItem.getModel() instanceof FormModel formModel)) {
      return Map.of();
    }
    try {
      List<ModelValidationError> errors = Studio.getValidationService().validate(formModel);
      Map<String, List<String>> messagesById = new HashMap<>();
      for (ModelValidationError error : errors) {
        if (error.elementId() != null) {
          messagesById.computeIfAbsent(error.elementId(), id -> new ArrayList<>()).add(error.message());
        }
      }
      return messagesById;
    }
    catch (Exception e) {
      log.warn("Failed to validate form model '{}': {}", projectItem.getPath(), e.getMessage(), e);
      return Map.of();
    }
  }

  private void markErrors(@NonNull TreeItem<FormElementViewModel> treeItem, @NonNull Map<String, List<String>> errorMessagesById) {
    if (treeItem.getValue() != null) {
      treeItem.getValue().setErrorMessages(errorMessagesById.getOrDefault(treeItem.getValue().getId(), List.of()));
    }
    for (TreeItem<FormElementViewModel> child : treeItem.getChildren()) {
      markErrors(child, errorMessagesById);
    }
  }

  private TreeItem<FormElementViewModel> toTreeItem(@NonNull FormElementViewModel viewModel) {
    TreeItem<FormElementViewModel> item = new TreeItem<>(viewModel);
    for (FormElementViewModel child : viewModel.getChildren()) {
      item.getChildren().add(toTreeItem(child));
    }
    return item;
  }

  private TreeItem<FormElementViewModel> toFilteredTreeItem(@NonNull FormElementViewModel viewModel, @NonNull String term) {
    List<TreeItem<FormElementViewModel>> matchingChildren = new ArrayList<>();
    for (FormElementViewModel child : viewModel.getChildren()) {
      TreeItem<FormElementViewModel> filtered = toFilteredTreeItem(child, term);
      if (filtered != null) {
        matchingChildren.add(filtered);
      }
    }
    boolean selfMatches = viewModel.getName() != null && viewModel.getName().toLowerCase().contains(term);
    if (!selfMatches && matchingChildren.isEmpty()) {
      return null;
    }
    TreeItem<FormElementViewModel> item = new TreeItem<>(viewModel);
    item.getChildren().addAll(matchingChildren);
    return item;
  }

  private void selectNode(@NonNull Object node) {
    TreeItem<FormElementViewModel> item = findTreeItem(tree.getRoot(), node);
    if (item == null) {
      return;
    }
    tree.getSelectionModel().select(item);
  }

  private TreeItem<FormElementViewModel> findTreeItem(TreeItem<FormElementViewModel> item, @NonNull Object node) {
    if (item == null) {
      return null;
    }
    if (item.getValue() != null && item.getValue().getNode() == node) {
      return item;
    }
    for (TreeItem<FormElementViewModel> child : item.getChildren()) {
      TreeItem<FormElementViewModel> found = findTreeItem(child, node);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // ---- Drag and drop ----

  private void setupCellDragAndDrop(@NonNull FormModelTreeCell cell) {
    cell.setOnDragDetected(event -> {
      if (cell.isEmpty() || cell.getTreeItem() == null) {
        return;
      }
      draggedTreeItem = cell.getTreeItem();
      Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent clipboardContent = new ClipboardContent();
      clipboardContent.put(INTERNAL_DRAG_FORMAT, draggedTreeItem.getValue().getId());
      dragboard.setContent(clipboardContent);
      event.consume();
    });

    cell.setOnDragOver(event -> {
      if (cell.isEmpty() || cell.getTreeItem() == null) {
        return;
      }
      Dragboard dragboard = event.getDragboard();
      DropTarget position = null;
      TransferMode transferMode = null;
      if (draggedTreeItem != null && dragboard.hasContent(INTERNAL_DRAG_FORMAT)) {
        position = resolveInternalDropPosition(draggedTreeItem, cell.getTreeItem(), event.getY(), cell.getHeight());
        transferMode = TransferMode.MOVE;
      }
      else if (dragboard.hasContent(DocumentSourceTreeController.SOURCE_ELEMENT_DRAG_FORMAT)) {
        position = resolveExternalDropPosition(dragboard, cell.getTreeItem(), event.getY(), cell.getHeight());
        transferMode = TransferMode.COPY;
      }
      if (position != null) {
        event.acceptTransferModes(transferMode);
        showDropIndicator(cell, position.location());
      }
      else {
        clearDropIndicator(cell);
      }
      event.consume();
    });

    cell.setOnDragExited(event -> clearDropIndicator(cell));

    cell.setOnDragDropped(event -> {
      boolean success = false;
      if (!cell.isEmpty() && cell.getTreeItem() != null) {
        Dragboard dragboard = event.getDragboard();
        if (draggedTreeItem != null && dragboard.hasContent(INTERNAL_DRAG_FORMAT)) {
          DropTarget position = resolveInternalDropPosition(draggedTreeItem, cell.getTreeItem(), event.getY(), cell.getHeight());
          if (position != null) {
            moveNode(draggedTreeItem, position);
            success = true;
          }
        }
        else if (dragboard.hasContent(DocumentSourceTreeController.SOURCE_ELEMENT_DRAG_FORMAT)) {
          DropTarget position = resolveExternalDropPosition(dragboard, cell.getTreeItem(), event.getY(), cell.getHeight());
          if (position != null) {
            dropDocumentElement(dragboard, position);
            success = true;
          }
        }
      }
      clearDropIndicator(cell);
      event.setDropCompleted(success);
      event.consume();
    });

    cell.setOnDragDone(event -> draggedTreeItem = null);
  }

  private void showDropIndicator(@NonNull FormModelTreeCell cell, @NonNull DropLocation location) {
    String showClass = switch (location) {
      case ABOVE -> "tree-row-drop-above";
      case BELOW -> "tree-row-drop-below";
      case INTO -> "tree-row-drop-into";
    };
    for (String styleClass : DROP_STYLE_CLASSES) {
      if (!styleClass.equals(showClass)) {
        cell.getStyleClass().remove(styleClass);
      }
    }
    if (!cell.getStyleClass().contains(showClass)) {
      cell.getStyleClass().add(showClass);
    }
  }

  private void clearDropIndicator(@NonNull FormModelTreeCell cell) {
    cell.getStyleClass().removeAll(DROP_STYLE_CLASSES);
  }

  private boolean isSameOrDescendant(@NonNull TreeItem<FormElementViewModel> item,
                                      @NonNull TreeItem<FormElementViewModel> ancestorCandidate) {
    TreeItem<FormElementViewModel> current = item;
    while (current != null) {
      if (current == ancestorCandidate) {
        return true;
      }
      current = current.getParent();
    }
    return false;
  }

  /**
   * Where an in-tree drag would land: reparented as a child of {@code target} ("into", offered only for the
   * middle 50% of a row that can structurally contain the dragged node's type) or reordered as a sibling
   * directly before/after {@code target} (top/bottom half otherwise) - {@code null} if the drop would be
   * invalid (onto itself/a descendant, or a type the target/its parent can't contain).
   */
  private DropTarget resolveInternalDropPosition(@NonNull TreeItem<FormElementViewModel> dragged,
                                                  @NonNull TreeItem<FormElementViewModel> target,
                                                  double relativeY, double rowHeight) {
    if (isSameOrDescendant(target, dragged)) {
      return null;
    }
    Object draggedNode = dragged.getValue().getNode();
    Object targetNode = target.getValue().getNode();
    double fraction = rowHeight <= 0 ? 0.5 : relativeY / rowHeight;

    boolean canDropInto = FormModelNodeTypes.canContain(targetNode, draggedNode.getClass());
    if (canDropInto && fraction > 0.25 && fraction < 0.75) {
      return new DropTarget(target, DropLocation.INTO);
    }

    List<Object> siblings = actions.siblingsOf(target.getValue());
    if (siblings == null) {
      return null;
    }
    Object targetParent = target.getValue().getParentNode();
    boolean canDropAsSibling = targetParent == null ? draggedNode instanceof Screen
        : FormModelNodeTypes.canContain(targetParent, draggedNode.getClass());
    if (!canDropAsSibling) {
      return null;
    }

    DropLocation location = fraction < 0.5 ? DropLocation.ABOVE : DropLocation.BELOW;
    return new DropTarget(target, location);
  }

  /**
   * Moves a dragged node to its drop location as a single undoable step: reused across every combination of
   * source/target container ({@link FormModelActions#siblingsOf} returns {@code null} for the single-slot
   * {@link de.a12.studio.models.formmodel.EmbeddedRepeat}/{@link de.a12.studio.models.formmodel.DetachedRepeat}
   * parents) - a plain {@link MoveNodeCommand} when both ends are real sibling lists (also handles same-list
   * reordering), else a {@link CompositeCommand} pairing a detach (from wherever the node currently lives) with
   * an attach (to the target list or single slot).
   */
  private void moveNode(@NonNull TreeItem<FormElementViewModel> draggedItem, @NonNull DropTarget position) {
    FormElementViewModel draggedVm = draggedItem.getValue();
    Object node = draggedVm.getNode();
    List<Object> sourceSiblings = actions.siblingsOf(draggedVm);

    Command command;
    if (position.location() == DropLocation.INTO) {
      Object targetNode = position.targetItem().getValue().getNode();
      List<Object> targetSiblings = FormModelActions.childListOf(targetNode);
      command = sourceSiblings != null && targetSiblings != null
          ? new MoveNodeCommand(sourceSiblings, targetSiblings, node, targetSiblings.size())
          : new CompositeCommand(actions.createDetachCommand(draggedVm), actions.createAttachCommand(targetNode, node));
    }
    else {
      List<Object> targetSiblings = actions.siblingsOf(position.targetItem().getValue());
      int targetIndex = targetSiblings.indexOf(position.targetItem().getValue().getNode());
      int insertIndex = position.location() == DropLocation.BELOW ? targetIndex + 1 : targetIndex;
      command = sourceSiblings != null
          ? new MoveNodeCommand(sourceSiblings, targetSiblings, node, insertIndex)
          : new CompositeCommand(actions.createDetachCommand(draggedVm), new AddNodeCommand(targetSiblings, node, insertIndex));
    }

    commandStack.execute(command);
    actions.notifyChanged(node);
  }

  private Element resolveDraggedDocumentElement(@NonNull Dragboard dragboard) {
    Object elementId = dragboard.getContent(DocumentSourceTreeController.SOURCE_ELEMENT_DRAG_FORMAT);
    return elementId instanceof String id ? documentElementsById.get(id) : null;
  }

  /**
   * Returns true when the given {@link Control}'s {@link Control#getElementRef()} resolves to a
   * {@link de.a12.studio.models.documentmodel.ConfirmFieldType} field in the currently loaded
   * Document Model. Used by {@link #updateEditorPane} to route confirm-type controls to the
   * dedicated confirm-field editor with its Dependencies tab.
   */
  private boolean isConfirmField(@NonNull Control control) {
    if (control.getElementRef() == null) return false;
    Element element = documentElementsById.get(control.getElementRef());
    return element instanceof FieldElement field
        && field.getField() != null
        && field.getField().getFieldType() instanceof de.a12.studio.models.documentmodel.ConfirmFieldType;
  }

  private static boolean isRepeatableGroup(@Nullable Element element) {
    return element instanceof GroupElement group && group.getGroup() != null
        && group.getGroup().getRepeatability() != null && group.getGroup().getRepeatability() > 1;
  }

  /**
   * Where a Field/Group dragged from the Document Model tree would land, mirroring the SME reference at a
   * pragmatic level: a Row or Cell only accepts Fields (as a new sibling {@link Control} cell, or reparented
   * into a Row); a Control Grid ("into") accepts either; a Screen/Section/Multi-Column Section ("into") only
   * accepts repeatable Groups (a plain Field must go through a Control Grid/Row, as in SME).
   */
  private DropTarget resolveExternalDropPosition(@NonNull Dragboard dragboard, @NonNull TreeItem<FormElementViewModel> target,
                                                  double relativeY, double rowHeight) {
    Element element = resolveDraggedDocumentElement(dragboard);
    if (element == null) {
      return null;
    }
    Object targetNode = target.getValue().getNode();
    double fraction = rowHeight <= 0 ? 0.5 : relativeY / rowHeight;

    if (targetNode instanceof Row && element instanceof FieldElement) {
      return new DropTarget(target, DropLocation.INTO);
    }
    if (targetNode instanceof Cell && element instanceof FieldElement) {
      List<Object> siblings = actions.siblingsOf(target.getValue());
      if (siblings == null) {
        return null;
      }
      DropLocation location = fraction < 0.5 ? DropLocation.ABOVE : DropLocation.BELOW;
      return new DropTarget(target, location);
    }
    if (targetNode instanceof ControlGrid && (element instanceof FieldElement || isRepeatableGroup(element))) {
      return new DropTarget(target, DropLocation.INTO);
    }
    if ((targetNode instanceof Screen || targetNode instanceof Section || targetNode instanceof MultiColumnSection)
        && isRepeatableGroup(element) && fraction > 0.25 && fraction < 0.75) {
      return new DropTarget(target, DropLocation.INTO);
    }
    return null;
  }

  private void dropDocumentElement(@NonNull Dragboard dragboard, @NonNull DropTarget position) {
    Element element = resolveDraggedDocumentElement(dragboard);
    if (element == null) {
      return;
    }
    Object targetNode = position.targetItem().getValue().getNode();

    if (targetNode instanceof Row row) {
      Control control = FormModelElementFactory.newControl(element.getId());
      commandStack.execute(new AddNodeCommand(FormModelActions.childListOf(row), control, row.getCell().size()));
      actions.notifyChanged(control);
    }
    else if (targetNode instanceof Cell) {
      List<Object> siblings = actions.siblingsOf(position.targetItem().getValue());
      if (siblings == null) {
        return;
      }
      Control control = FormModelElementFactory.newControl(element.getId());
      int targetIndex = siblings.indexOf(targetNode);
      int insertIndex = position.location() == DropLocation.BELOW ? targetIndex + 1 : targetIndex;
      commandStack.execute(new AddNodeCommand(siblings, control, insertIndex));
      actions.notifyChanged(control);
    }
    else if (targetNode instanceof ControlGrid grid) {
      if (element instanceof GroupElement) {
        // A repeat can't live inside a Control Grid's Row list (Rows only hold Cells), so it lands as a
        // sibling ScreenElement right after the Control Grid instead, matching the SME reference.
        InlineRepeat repeat = FormModelElementFactory.newInlineRepeat(element.getId());
        List<Object> siblings = actions.siblingsOf(position.targetItem().getValue());
        if (siblings == null) {
          return;
        }
        int index = siblings.indexOf(targetNode);
        commandStack.execute(new AddNodeCommand(siblings, repeat, index + 1));
        actions.notifyChanged(repeat);
      }
      else {
        Row row = FormModelElementFactory.newRow();
        Control control = FormModelElementFactory.newControl(element.getId());
        row.getCell().add(control);
        commandStack.execute(new AddNodeCommand(FormModelActions.childListOf(grid), row, grid.getRow().size()));
        actions.notifyChanged(control);
      }
    }
    else if (targetNode instanceof Screen || targetNode instanceof Section || targetNode instanceof MultiColumnSection) {
      InlineRepeat repeat = FormModelElementFactory.newInlineRepeat(element.getId());
      Command command = actions.createAttachCommand(targetNode, repeat);
      if (command == null) {
        return;
      }
      commandStack.execute(command);
      actions.notifyChanged(repeat);
    }
  }
}
