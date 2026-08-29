package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.editors.documentmodel.commands.MoveNodeCommand;
import de.a12.studio.ui.events.ElementValidatedEvent;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.commandstack.CommandStack;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class DocumentModelElementsTreeController implements Initializable, StudioEventListener {

  private static final String TABLE_SETTINGS_ID = ModelType.DOCUMENT.getValue();

  private static final String NAME_COLUMN_ID = "name";

  private static final String TYPE_COLUMN_ID = "type";

  // Identifies a tree row reorder/reparent drag; the dragboard needs some content to be considered a valid
  // drag source; the actual dragged node is tracked directly via the draggedTreeItem field below since it's
  // an in-process, same-tree drag (no need to serialize the element itself onto the dragboard).
  private static final DataFormat ELEMENT_DRAG_FORMAT = new DataFormat("application/x-a12-document-model-element");

  private static final List<String> DROP_STYLE_CLASSES =
      List.of("tree-row-drop-above", "tree-row-drop-below", "tree-row-drop-into");

  @FXML
  private ToolBar modelTreeToolbarBar;

  @FXML
  private Button undoButton;

  @FXML
  private Button redoButton;

  @FXML
  private MenuButton modelTreeAddButton;

  @FXML
  private Button deleteButton;

  @FXML
  private SearchFieldController searchController;

  @FXML
  private TreeTableView<ElementViewModel> elementsTreeTable;

  @FXML
  private TreeTableColumn<ElementViewModel, String> nameColumn;

  @FXML
  private TreeTableColumn<ElementViewModel, String> typeColumn;

  private ProjectItem projectItem;
  private ModelRoot modelRoot;

  // Every other Document Model in the project, needed by ElementViewModel to resolve an Include group's
  // children from the Document Model it references (see ElementViewModel#getChildren).
  private List<DocumentModel> otherDocumentModels = List.of();

  private final CommandStack commandStack = new CommandStack();

  private DocumentModelActions documentModelActions;

  private Consumer<List<Element>> selectionListener;

  private TreeItem<ElementViewModel> draggedTreeItem;

  public void load(@NonNull DocumentModel model) {
    load(projectItem, model.getContent().getModelRoot());
  }

  public void setSelectionListener(Consumer<List<Element>> selectionListener) {
    this.selectionListener = selectionListener;
  }

  public void load(ProjectItem projectItem, @NonNull ModelRoot modelRoot) {
    this.projectItem = projectItem;
    this.modelRoot = modelRoot;
    this.otherDocumentModels = ProjectDocumentModels.getOtherDocumentModels(projectItem);
    this.documentModelActions =
        new DocumentModelActions(projectItem, modelRoot, commandStack, elementsTreeTable, this::onModelChanged);
    modelTreeAddButton.getItems().addAll(documentModelActions.createAddMenuItems());
    applyFilter(searchController.getText());
    StudioEventManager.getInstance().addListener(this);
  }

  /**
   * Unregisters this tree controller once its owning tab is closed, mirroring {@link
   * de.a12.studio.ui.editors.AbstractEditorController#modelClosed}. This controller registers itself directly
   * (rather than inheriting from {@code AbstractEditorController}) since it isn't a top-level tab editor.
   */
  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    if (event.getItem().equals(projectItem)) {
      StudioEventManager.getInstance().removeListener(this);
    }
  }

  @Override
  public void elementValidated(@NonNull ElementValidatedEvent event) {
    TreeItem<ElementViewModel> treeItem = findTreeItem(elementsTreeTable.getRoot(), event.getElementId());
    if (treeItem == null) {
      return;
    }
    treeItem.getValue().setErrorMessages(event.getError().map(error -> List.of(error.message())).orElse(List.of()));
    elementsTreeTable.refresh();
  }

  public List<Element> getAncestors(@NonNull Element element) {
    List<Element> ancestors = new ArrayList<>();
    TreeItem<ElementViewModel> treeItem = findTreeItem(elementsTreeTable.getRoot(), element.getId());
    if (treeItem == null) {
      return ancestors;
    }

    TreeItem<ElementViewModel> parent = treeItem.getParent();
    while (parent != null && parent.getValue() != null) {
      ancestors.add(0, parent.getValue().getElement());
      parent = parent.getParent();
    }
    return ancestors;
  }

  private TreeItem<ElementViewModel> findTreeItem(TreeItem<ElementViewModel> treeItem, @NonNull String elementId) {
    if (treeItem == null) {
      return null;
    }
    if (treeItem.getValue() != null && elementId.equals(treeItem.getValue().getElement().getId())) {
      return treeItem;
    }
    for (TreeItem<ElementViewModel> child : treeItem.getChildren()) {
      TreeItem<ElementViewModel> found = findTreeItem(child, elementId);
      if (found != null) {
        return found;
      }
    }
    return null;
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
   * Refreshes the tree after a command executed elsewhere (undo/redo, {@link DocumentModelActions},
   * drag-and-drop) changed the model: updates the undo/redo buttons, rebuilds the filtered tree, then
   * (once the tree reflects the change) re-selects {@code elementToSelect} if one was given, and persists.
   */
  private void onModelChanged(Element elementToSelect) {
    updateUndoRedoState();
    applyFilter(searchController.getText());
    if (elementToSelect != null) {
      selectElement(elementToSelect);
    }
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  private void notifySelectionChanged() {
    if (selectionListener == null) {
      return;
    }

    List<Element> selectedElements = new ArrayList<>();
    for (TreeItem<ElementViewModel> treeItem : elementsTreeTable.getSelectionModel().getSelectedItems()) {
      if (treeItem != null && treeItem.getValue() != null) {
        selectedElements.add(treeItem.getValue().getElement());
      }
    }
    selectionListener.accept(selectedElements);
  }

  private void applyFilter(String filter) {
    if (modelRoot == null) {
      return;
    }

    String term = filter == null ? "" : filter.trim().toLowerCase();
    TreeItem<ElementViewModel> root = new TreeItem<>();
    for (GroupElement group : modelRoot.getRootGroups()) {
      TreeItem<ElementViewModel> treeItem = term.isEmpty() ? toTreeItem(group) : toFilteredTreeItem(group, term);
      if (treeItem != null) {
        root.getChildren().add(treeItem);
      }
    }
    elementsTreeTable.setRoot(root);
    applyValidationState(root);
    expandAll(root);
  }

  /**
   * Re-validates the whole document and marks each errored element's tree row, so structural changes made
   * elsewhere in the tree (e.g. deleting a field another group uses as its index field, undo/redo, initial
   * load of a document that was already invalid on disk) are reflected immediately. This rebuilds every
   * {@link ElementViewModel} in the tree (see {@link #toTreeItem}), which would otherwise always start out
   * with {@code hasError == false} until the next individual field edit re-triggers {@link #elementValidated}.
   */
  private void applyValidationState(@NonNull TreeItem<ElementViewModel> root) {
    markErrors(root, errorMessagesByElementId());
  }

  private Map<String, List<String>> errorMessagesByElementId() {
    if (!(projectItem.getModel() instanceof DocumentModel documentModel)) {
      return Map.of();
    }
    try {
      List<ModelValidationError> errors = Studio.getValidationService().validate(documentModel);
      Map<String, List<String>> messagesById = new HashMap<>();
      for (ModelValidationError error : errors) {
        if (error.elementId() != null) {
          messagesById.computeIfAbsent(error.elementId(), id -> new ArrayList<>()).add(error.message());
        }
      }
      return messagesById;
    }
    catch (Exception e) {
      log.warn("Failed to validate document '{}': {}", projectItem.getPath(), e.getMessage(), e);
      return Map.of();
    }
  }

  private void markErrors(@NonNull TreeItem<ElementViewModel> treeItem, @NonNull Map<String, List<String>> errorMessagesById) {
    if (treeItem.getValue() != null) {
      treeItem.getValue().setErrorMessages(errorMessagesById.getOrDefault(treeItem.getValue().getElement().getId(), List.of()));
    }
    for (TreeItem<ElementViewModel> child : treeItem.getChildren()) {
      markErrors(child, errorMessagesById);
    }
  }

  private void expandAll(@NonNull TreeItem<ElementViewModel> treeItem) {
    treeItem.setExpanded(true);
    for (TreeItem<ElementViewModel> child : treeItem.getChildren()) {
      expandAll(child);
    }
  }

  private TreeItem<ElementViewModel> toTreeItem(@NonNull Element element) {
    ElementViewModel viewModel = new ElementViewModel(element, otherDocumentModels);
    TreeItem<ElementViewModel> treeItem = new TreeItem<>(viewModel);
    for (ElementViewModel child : viewModel.getChildren()) {
      treeItem.getChildren().add(toTreeItem(child.getElement()));
    }
    return treeItem;
  }

  private TreeItem<ElementViewModel> toFilteredTreeItem(@NonNull Element element, @NonNull String term) {
    ElementViewModel viewModel = new ElementViewModel(element, otherDocumentModels);
    List<TreeItem<ElementViewModel>> matchingChildren = new ArrayList<>();
    for (ElementViewModel child : viewModel.getChildren()) {
      TreeItem<ElementViewModel> filteredChild = toFilteredTreeItem(child.getElement(), term);
      if (filteredChild != null) {
        matchingChildren.add(filteredChild);
      }
    }

    boolean selfMatches = viewModel.getName() != null && viewModel.getName().toLowerCase().contains(term);
    if (!selfMatches && matchingChildren.isEmpty()) {
      return null;
    }

    TreeItem<ElementViewModel> treeItem = new TreeItem<>(viewModel);
    treeItem.getChildren().addAll(matchingChildren);
    return treeItem;
  }

  /**
   * Whether {@code element} is a group with fixed children (attachment, multi-select, include), or a
   * descendant of one. Such groups have a fixed set of children, so nothing may be added inside them.
   */
  private boolean isWithinFixedChildrenGroup(@NonNull Element element) {
    return new ElementViewModel(element).hasFixedChildren() || hasFixedChildrenAncestor(element);
  }

  /**
   * Whether any ancestor of {@code element} (not {@code element} itself) is a group with fixed
   * children (attachment, multi-select, include).
   */
  private boolean hasFixedChildrenAncestor(@NonNull Element element) {
    for (Element ancestor : getAncestors(element)) {
      if (new ElementViewModel(ancestor).hasFixedChildren()) {
        return true;
      }
    }
    return false;
  }

  private void onDeleteKeyPressed() {
    documentModelActions.confirmAndDeleteSelection();
  }

  @FXML
  private void onDeleteButton() {
    documentModelActions.confirmAndDeleteSelection();
  }

  private enum DropLocation {ABOVE, BELOW, INTO}

  private record DropPosition(TreeItem<ElementViewModel> targetItem, DropLocation location) {

  }

  /**
   * The list a tree item's element currently lives in: {@link ModelRoot#getRootGroups()} for a top-level
   * group, or its parent group's {@link de.a12.studio.models.documentmodel.GroupConfig#getElements()} otherwise.
   */
  private List<? extends Element> siblingsOf(@NonNull TreeItem<ElementViewModel> item) {
    TreeItem<ElementViewModel> parentItem = item.getParent();
    if (parentItem == null || parentItem.getValue() == null) {
      return modelRoot.getRootGroups();
    }
    Element parentElement = parentItem.getValue().getElement();
    if (parentElement instanceof GroupElement parentGroup && parentGroup.getGroup() != null) {
      return parentGroup.getGroup().getElements();
    }
    return List.of();
  }

  private boolean isSameOrDescendant(@NonNull TreeItem<ElementViewModel> item, @NonNull TreeItem<ElementViewModel> ancestorCandidate) {
    TreeItem<ElementViewModel> current = item;
    while (current != null) {
      if (current == ancestorCandidate) {
        return true;
      }
      current = current.getParent();
    }
    return false;
  }

  private boolean isAttachmentOrMultiSelectGroup(@NonNull Element element) {
    return element instanceof GroupElement groupElement && groupElement.getGroup() != null
        && (GroupConfig.USAGE_TYPE_ATTACHMENT.equals(groupElement.getGroup().getUsageType())
            || GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(groupElement.getGroup().getUsageType()));
  }

  /**
   * Whether {@code target} is a valid drop location for {@code dragged}, and if so where: reparented as a
   * child ("into", only offered for the middle 50% of a non-fixed-children group's row) or reordered as a
   * sibling directly before/after {@code target} (the top/bottom 25%+50% of its row, or the whole row for a
   * non-group target). Returns {@code null} when the drop would be invalid: onto itself or one of its own
   * descendants (would create a cycle), into a fixed-children group (attachment/multi-select/include, see
   * {@link ElementViewModel#hasFixedChildren}), next to a row inside one, directly after an attachment or
   * multi-select group (see {@link #isAttachmentOrMultiSelectGroup}) - checked against the resulting sibling
   * list position rather than just {@code target} itself, since hovering over the *next* row's top half lands
   * in that same gap - or a non-{@link GroupElement} landing directly in the root list (which only holds
   * top-level groups, see {@link ModelRoot#getRootGroups}).
   */
  private DropPosition resolveDropPosition(@NonNull TreeItem<ElementViewModel> dragged, @NonNull TreeItem<ElementViewModel> target,
                                            double relativeY, double rowHeight) {
    if (isSameOrDescendant(target, dragged)) {
      return null;
    }

    Element draggedElement = dragged.getValue().getElement();
    Element targetElement = target.getValue().getElement();
    double fraction = rowHeight <= 0 ? 0.5 : relativeY / rowHeight;

    boolean canDropInto = targetElement instanceof GroupElement targetGroup && targetGroup.getGroup() != null
        && !new ElementViewModel(targetElement).hasFixedChildren() && !hasFixedChildrenAncestor(targetElement);
    if (canDropInto && fraction > 0.25 && fraction < 0.75) {
      return new DropPosition(target, DropLocation.INTO);
    }

    if (hasFixedChildrenAncestor(targetElement)) {
      return null;
    }
    List<? extends Element> siblings = siblingsOf(target);
    if (siblings == modelRoot.getRootGroups() && !(draggedElement instanceof GroupElement)) {
      return null;
    }

    DropLocation location = fraction < 0.5 ? DropLocation.ABOVE : DropLocation.BELOW;
    int targetElementIndex = siblings.indexOf(targetElement);
    int insertIndex = location == DropLocation.BELOW ? targetElementIndex + 1 : targetElementIndex;
    if (insertIndex > 0 && isAttachmentOrMultiSelectGroup(siblings.get(insertIndex - 1))) {
      return null;
    }
    return new DropPosition(target, location);
  }

  private void moveElement(@NonNull TreeItem<ElementViewModel> draggedItem, @NonNull DropPosition position) {
    Element element = draggedItem.getValue().getElement();
    List<? extends Element> sourceSiblings = siblingsOf(draggedItem);

    List<? extends Element> targetSiblings;
    int targetIndex;
    if (position.location() == DropLocation.INTO) {
      GroupElement targetGroup = (GroupElement) position.targetItem().getValue().getElement();
      targetSiblings = targetGroup.getGroup().getElements();
      targetIndex = targetSiblings.size();
    }
    else {
      targetSiblings = siblingsOf(position.targetItem());
      int targetElementIndex = targetSiblings.indexOf(position.targetItem().getValue().getElement());
      targetIndex = position.location() == DropLocation.BELOW ? targetElementIndex + 1 : targetElementIndex;
    }

    commandStack.execute(new MoveNodeCommand(sourceSiblings, targetSiblings, element, targetIndex));
    onModelChanged(element);
  }

  /**
   * Handles dropping a top-level group onto the empty area below the last row (no {@link TreeTableRow} covers
   * that space, so {@link #setupTreeDragAndDrop} handles it directly on the table instead of a row): moves it
   * to the end of {@link ModelRoot#getRootGroups()}. Non-group elements can't land here since the root list is
   * typed to hold only groups.
   */
  private void moveElementToRootEnd(@NonNull TreeItem<ElementViewModel> draggedItem, @NonNull GroupElement draggedGroup) {
    List<? extends Element> sourceSiblings = siblingsOf(draggedItem);
    List<GroupElement> rootGroups = modelRoot.getRootGroups();

    commandStack.execute(new MoveNodeCommand(sourceSiblings, rootGroups, draggedGroup, rootGroups.size()));
    onModelChanged(draggedGroup);
  }

  private void showDropIndicator(@NonNull TreeTableRow<ElementViewModel> row, @NonNull DropPosition position) {
    String showClass = switch (position.location()) {
      case ABOVE -> "tree-row-drop-above";
      case BELOW -> "tree-row-drop-below";
      case INTO -> "tree-row-drop-into";
    };
    for (String styleClass : DROP_STYLE_CLASSES) {
      if (!styleClass.equals(showClass)) {
        row.getStyleClass().remove(styleClass);
      }
    }
    if (!row.getStyleClass().contains(showClass)) {
      row.getStyleClass().add(showClass);
    }
  }

  private void clearDropIndicator(@NonNull TreeTableRow<ElementViewModel> row) {
    row.getStyleClass().removeAll(DROP_STYLE_CLASSES);
  }

  /**
   * Wires up reorder/reparent drag-and-drop for one tree row: grabbing anywhere on the row starts the drag
   * (no dedicated handle, unlike e.g. {@code ModulesPanelController}, since rows here nest to arbitrary depth
   * and a per-row handle would fight with expand/collapse disclosure arrows). Hovering over another row shows
   * exactly where the dragged element will land - as a sibling above/below it, or reparented into it - via
   * {@link #showDropIndicator}, and {@link #resolveDropPosition} vetoes drops that would be invalid.
   */
  private void setupRowDragAndDrop(@NonNull TreeTableRow<ElementViewModel> row) {
    row.setOnDragDetected(event -> {
      if (row.isEmpty() || row.getTreeItem() == null
          || hasFixedChildrenAncestor(row.getTreeItem().getValue().getElement())) {
        return;
      }
      draggedTreeItem = row.getTreeItem();
      Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(ELEMENT_DRAG_FORMAT, draggedTreeItem.getValue().getElement().getId());
      dragboard.setContent(content);
      event.consume();
    });

    row.setOnDragOver(event -> {
      if (row.isEmpty() || row.getTreeItem() == null || draggedTreeItem == null
          || !event.getDragboard().hasContent(ELEMENT_DRAG_FORMAT)) {
        return;
      }
      DropPosition position = resolveDropPosition(draggedTreeItem, row.getTreeItem(), event.getY(), row.getHeight());
      if (position != null) {
        event.acceptTransferModes(TransferMode.MOVE);
        showDropIndicator(row, position);
      }
      else {
        clearDropIndicator(row);
      }
      event.consume();
    });

    row.setOnDragExited(event -> clearDropIndicator(row));

    row.setOnDragDropped(event -> {
      if (row.isEmpty() || row.getTreeItem() == null || draggedTreeItem == null) {
        return;
      }
      DropPosition position = resolveDropPosition(draggedTreeItem, row.getTreeItem(), event.getY(), row.getHeight());
      boolean success = position != null;
      if (success) {
        moveElement(draggedTreeItem, position);
      }
      clearDropIndicator(row);
      event.setDropCompleted(success);
      event.consume();
    });

    row.setOnDragDone(event -> draggedTreeItem = null);
  }

  /**
   * Lets a top-level group be dropped onto the empty area below the last row, where no {@link TreeTableRow}
   * exists to handle the drag itself (see {@link #moveElementToRootEnd}). Per-row handlers in {@link
   * #setupRowDragAndDrop} consume drag-over/dropped events themselves, so this table-level handler only ever
   * sees events that fell through from that empty area.
   */
  /**
   * Whether a root-level group may be dropped after the current last root group: false if the dragged
   * element isn't a group at all (root only holds groups, see {@link ModelRoot#getRootGroups}), or if the
   * root list currently ends with an attachment/multi-select group - mirrors the "nothing lands directly
   * after one of those" rule enforced for in-tree drops by {@link #resolveDropPosition}.
   */
  private boolean canAppendToRootEnd(@NonNull Element draggedElement) {
    if (!(draggedElement instanceof GroupElement)) {
      return false;
    }
    List<GroupElement> rootGroups = modelRoot.getRootGroups();
    return rootGroups.isEmpty() || !isAttachmentOrMultiSelectGroup(rootGroups.get(rootGroups.size() - 1));
  }

  private void setupTreeDragAndDrop() {
    elementsTreeTable.setOnDragOver(event -> {
      if (draggedTreeItem != null && canAppendToRootEnd(draggedTreeItem.getValue().getElement())
          && event.getDragboard().hasContent(ELEMENT_DRAG_FORMAT)) {
        event.acceptTransferModes(TransferMode.MOVE);
      }
      event.consume();
    });

    elementsTreeTable.setOnDragDropped(event -> {
      boolean success = draggedTreeItem != null && canAppendToRootEnd(draggedTreeItem.getValue().getElement());
      if (success) {
        moveElementToRootEnd(draggedTreeItem, (GroupElement) draggedTreeItem.getValue().getElement());
      }
      event.setDropCompleted(success);
      event.consume();
    });
  }

  private void selectElement(@NonNull Element element) {
    TreeItem<ElementViewModel> treeItem = findTreeItem(elementsTreeTable.getRoot(), element.getId());
    if (treeItem == null) {
      return;
    }
    elementsTreeTable.getSelectionModel().clearSelection();
    elementsTreeTable.getSelectionModel().select(treeItem);
    int row = elementsTreeTable.getRow(treeItem);
    if (row >= 0) {
      elementsTreeTable.scrollTo(row);
    }
  }

  private void applyColumnWidth(@NonNull TreeTableColumn<ElementViewModel, String> column,
                                BaseTableSettings tableSettings, @NonNull String columnId) {
    if (tableSettings == null) {
      return;
    }
    double width = tableSettings.getColumnWidth(columnId);
    if (width > 0) {
      column.setPrefWidth(width);
    }
  }

  private void saveColumnWidth(@NonNull String columnId, double width) {
    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    if (tableSettings == null) {
      return;
    }
    tableSettings.getColumnWith().put(columnId, width);
    tableSettings.save();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    modelTreeAddButton.setDisable(true);
    deleteButton.setDisable(true);
    updateUndoRedoState();
    searchController.setOnSearch(this::applyFilter);

    elementsTreeTable.setShowRoot(false);
    elementsTreeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    elementsTreeTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<ElementViewModel>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<ElementViewModel>> observable, TreeItem<ElementViewModel> oldValue, TreeItem<ElementViewModel> newValue) {
        modelTreeAddButton.setDisable(newValue == null || isWithinFixedChildrenGroup(newValue.getValue().getElement()));
        deleteButton.setDisable(newValue == null || hasFixedChildrenAncestor(newValue.getValue().getElement()));
      }
    });
    elementsTreeTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TreeItem<ElementViewModel>>) change -> notifySelectionChanged());
    elementsTreeTable.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        onDeleteKeyPressed();
      }
    });
    elementsTreeTable.setRowFactory(treeTable -> {
      TreeTableRow<ElementViewModel> row = new TreeTableRow<>() {
        @Override
        protected void updateItem(ElementViewModel item, boolean empty) {
          super.updateItem(item, empty);
          boolean fixedChildLeaf = !empty && item != null && hasFixedChildrenAncestor(item.getElement());
          if (fixedChildLeaf) {
            if (!getStyleClass().contains("fixed-child-row")) {
              getStyleClass().add("fixed-child-row");
            }
          }
          else {
            getStyleClass().remove("fixed-child-row");
          }
        }
      };
      setupRowDragAndDrop(row);
      // Built fresh on every request (rather than once in updateItem and cached via setContextMenu) so its
      // items - notably the multi-selection-only "Create Overview Model from Selection" entry, see
      // DocumentModelActions#createElementMenuItems - reflect the tree's actual selection at click time; a row
      // is only re-populated via updateItem on row reuse/scroll, not on every subsequent multi-selection change.
      row.setOnContextMenuRequested(event -> {
        ElementViewModel item = row.getItem();
        if (row.isEmpty() || item == null || hasFixedChildrenAncestor(item.getElement())) {
          return;
        }
        documentModelActions.createContextMenu(item.getElement()).show(row, event.getScreenX(), event.getScreenY());
        event.consume();
      });
      return row;
    });
    setupTreeDragAndDrop();
    nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getName()));
    nameColumn.setCellFactory(column -> new ElementNameTreeCell());


    typeColumn.setCellValueFactory(param -> {
      String type = param.getValue().getValue().getType();
      if (type != null) {
        type = type.replaceAll("Type", "");
        if (type.equalsIgnoreCase("Rule")) {
          type = "Validation Rule";
        }
      }

      return new ReadOnlyStringWrapper(type);
    });

    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    applyColumnWidth(nameColumn, tableSettings, NAME_COLUMN_ID);
    applyColumnWidth(typeColumn, tableSettings, TYPE_COLUMN_ID);

    nameColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(NAME_COLUMN_ID, newValue.doubleValue()));
    typeColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(TYPE_COLUMN_ID, newValue.doubleValue()));
  }
}
