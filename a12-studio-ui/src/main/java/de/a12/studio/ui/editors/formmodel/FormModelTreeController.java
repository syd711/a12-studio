package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
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
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The right-hand "Form Model" structural tree of the Overview tab ({@link FormModelEditorController#loadOverview}):
 * Screens -> Sections/Control Grids/Repeats -> Rows -> Cells, with a search filter, a context menu ({@link
 * FormModelActions}) mirroring the SME reference's Add/Delete/Cut/Copy/Paste/Move actions, and drag-and-drop -
 * both reordering/reparenting within this tree, and accepting Fields/Groups dropped from {@link
 * DocumentSourceTreeController}'s tree to create the matching {@link Control}/{@link InlineRepeat}.
 */
public class FormModelTreeController implements Initializable {

  // Identifies an in-tree reorder/reparent drag; the dragged node itself is tracked via draggedTreeItem (same
  // controller, same tree), mirroring DocumentModelElementsTreeController's ELEMENT_DRAG_FORMAT.
  private static final DataFormat INTERNAL_DRAG_FORMAT = new DataFormat("application/x-a12-form-model-tree-node");

  private static final List<String> DROP_STYLE_CLASSES =
      List.of("tree-row-drop-above", "tree-row-drop-below", "tree-row-drop-into");

  @FXML
  private SearchFieldController searchController;

  @FXML
  private TreeView<FormElementViewModel> tree;

  private ProjectItem projectItem;
  private FormModelContent content;
  private FormModelActions actions;

  private Map<String, Element> documentElementsById = Map.of();

  // Resolves Control/Repeat/Repeat Overview Column display names (see FormElementViewModel#getName), following
  // includes into other Document Models in the project - unlike documentElementsById above, which only indexes
  // this Form Model's own linked Document Model and is used purely for local drag-and-drop lookups.
  private @Nullable ElementIndex elementIndex;

  private TreeItem<FormElementViewModel> draggedTreeItem;

  private enum DropLocation {ABOVE, BELOW, INTO}

  private record DropTarget(TreeItem<FormElementViewModel> targetItem, DropLocation location) {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    searchController.setOnSearch(this::applyFilter);
    tree.setShowRoot(false);
    tree.setCellFactory(view -> {
      FormModelTreeCell cell = new FormModelTreeCell(this::createContextMenu);
      setupCellDragAndDrop(cell);
      return cell;
    });
  }

  public void setModel(@NonNull FormModel model, @Nullable DocumentModel documentModel, @NonNull ProjectItem projectItem) {
    this.projectItem = projectItem;
    this.content = ensureContent(model);
    this.documentElementsById = indexDocumentModel(documentModel);
    this.elementIndex = hasModelRoot(documentModel)
        ? new ElementIndex(documentModel, ProjectDocumentModels.getOtherDocumentModels(projectItem))
        : null;
    this.actions = new FormModelActions(content, this::onModelChanged);
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
    setExpandedRecursive(tree.getRoot(), false);
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
    int row = tree.getRow(item);
    if (row >= 0) {
      tree.scrollTo(row);
    }
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

  private void moveNode(@NonNull TreeItem<FormElementViewModel> draggedItem, @NonNull DropTarget position) {
    FormElementViewModel draggedVm = draggedItem.getValue();
    Object node = draggedVm.getNode();
    actions.removeNode(draggedVm);

    if (position.location() == DropLocation.INTO) {
      actions.insertAsChild(position.targetItem().getValue().getNode(), node);
    }
    else {
      List<Object> targetSiblings = actions.siblingsOf(position.targetItem().getValue());
      int targetIndex = targetSiblings.indexOf(position.targetItem().getValue().getNode());
      int insertIndex = position.location() == DropLocation.BELOW ? targetIndex + 1 : targetIndex;
      targetSiblings.add(Math.max(0, Math.min(insertIndex, targetSiblings.size())), node);
    }
    actions.notifyChanged(node);
  }

  private Element resolveDraggedDocumentElement(@NonNull Dragboard dragboard) {
    Object elementId = dragboard.getContent(DocumentSourceTreeController.SOURCE_ELEMENT_DRAG_FORMAT);
    return elementId instanceof String id ? documentElementsById.get(id) : null;
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
      row.getCell().add(control);
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
      siblings.add(Math.max(0, Math.min(insertIndex, siblings.size())), control);
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
        siblings.add(index + 1, repeat);
        actions.notifyChanged(repeat);
      }
      else {
        Row row = FormModelElementFactory.newRow();
        Control control = FormModelElementFactory.newControl(element.getId());
        row.getCell().add(control);
        grid.getRow().add(row);
        actions.notifyChanged(control);
      }
    }
    else if (targetNode instanceof Screen || targetNode instanceof Section || targetNode instanceof MultiColumnSection) {
      InlineRepeat repeat = FormModelElementFactory.newInlineRepeat(element.getId());
      actions.insertAsChild(targetNode, repeat);
      actions.notifyChanged(repeat);
    }
  }
}
