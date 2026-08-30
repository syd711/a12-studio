package de.a12.studio.ui.editors.formmodel.documenttree;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.editors.formmodel.FormModelEditorController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The left-hand "Document Model" tree of the Form Model editor's Overview tab ({@link
 * FormModelEditorController#loadOverview}): a read-only view of the Document Model linked via the header's
 * {@link de.a12.studio.models.ModelReference#PURPOSE_DATA_BINDING} reference, with a search filter and
 * drag-and-drop support so Fields and Groups can be dropped onto {@link FormModelTreeController}'s tree to
 * build the form. No context menu, no editing - {@code documentmodel}'s own {@code
 * DocumentModelElementsTreeController} owns the actual Document Model editor.
 */
public class DocumentSourceTreeController implements Initializable {

  // Carries the dragged Element's id; FormModelTreeController resolves it back to an Element using its own
  // index over the same DocumentModel (both controllers are handed the same instance, see
  // FormModelEditorController#loadOverview) - no need to serialize more than the id onto the dragboard.
  public static final DataFormat SOURCE_ELEMENT_DRAG_FORMAT = new DataFormat("application/x-a12-form-model-source-element");

  @FXML
  private SearchFieldController searchController;

  @FXML
  private StackPane treeContainer;

  @FXML
  private TreeView<ElementViewModel> tree;

  private ModelRoot modelRoot;

  // Every Document Model in the project, needed by ElementViewModel to resolve an Include group's children
  // from the Document Model it references (see ElementViewModel#getChildren), same as
  // DocumentModelElementsTreeController#otherDocumentModels.
  private List<DocumentModel> otherDocumentModels = List.of();

  private Label placeholderLabel;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    searchController.setOnSearch(this::applyFilter);
    tree.setShowRoot(false);
    tree.setCellFactory(view -> {
      FormSourceElementTreeCell cell = new FormSourceElementTreeCell();
      setupDragSource(cell);
      return cell;
    });
  }

  public void load(@Nullable DocumentModel model, @NonNull ProjectItem formModelProjectItem) {
    this.modelRoot = model != null && model.getContent() != null ? model.getContent().getModelRoot() : null;
    this.otherDocumentModels = ProjectDocumentModels.getOtherDocumentModels(formModelProjectItem);
    boolean hasModel = modelRoot != null;
    tree.setVisible(hasModel);
    tree.setManaged(hasModel);
    if (!hasModel) {
      showPlaceholder();
      return;
    }
    hidePlaceholder();
    applyFilter(searchController.getText());
  }

  private void showPlaceholder() {
    if (placeholderLabel == null) {
      placeholderLabel = new Label(StudioBundle.get("no_document_model_linked"));
      placeholderLabel.setWrapText(true);
      placeholderLabel.getStyleClass().add("placeholder-label");
      placeholderLabel.setMaxWidth(220);
    }
    if (!treeContainer.getChildren().contains(placeholderLabel)) {
      treeContainer.getChildren().add(placeholderLabel);
    }
  }

  private void hidePlaceholder() {
    if (placeholderLabel != null) {
      treeContainer.getChildren().remove(placeholderLabel);
    }
  }

  private void applyFilter(String filter) {
    if (modelRoot == null) {
      return;
    }
    String term = filter == null ? "" : filter.trim().toLowerCase();
    TreeItem<ElementViewModel> root = new TreeItem<>();
    for (GroupElement group : modelRoot.getRootGroups()) {
      TreeItem<ElementViewModel> item = term.isEmpty() ? toTreeItem(group) : toFilteredTreeItem(group, term);
      if (item != null) {
        root.getChildren().add(item);
      }
    }
    tree.setRoot(root);
    setExpandedRecursive(root, true);
  }

  @FXML
  private void onExpandAll() {
    setExpandedRecursive(tree.getRoot(), true);
  }

  @FXML
  private void onCollapseAll() {
    TreeItem<ElementViewModel> root = tree.getRoot();
    if (root == null) {
      return;
    }
    // Root is hidden (showRoot=false) but must stay expanded, otherwise its
    // top-level children would be hidden along with it.
    root.setExpanded(true);
    for (TreeItem<ElementViewModel> child : root.getChildren()) {
      setExpandedRecursive(child, false);
    }
  }

  private TreeItem<ElementViewModel> toTreeItem(@NonNull Element element) {
    ElementViewModel viewModel = new ElementViewModel(element, otherDocumentModels);
    TreeItem<ElementViewModel> item = new TreeItem<>(viewModel);
    for (ElementViewModel child : viewModel.getChildren()) {
      item.getChildren().add(toTreeItem(child.getElement()));
    }
    return item;
  }

  private TreeItem<ElementViewModel> toFilteredTreeItem(@NonNull Element element, @NonNull String term) {
    ElementViewModel viewModel = new ElementViewModel(element, otherDocumentModels);
    List<TreeItem<ElementViewModel>> matchingChildren = new ArrayList<>();
    for (ElementViewModel child : viewModel.getChildren()) {
      TreeItem<ElementViewModel> filtered = toFilteredTreeItem(child.getElement(), term);
      if (filtered != null) {
        matchingChildren.add(filtered);
      }
    }
    boolean selfMatches = viewModel.getName() != null && viewModel.getName().toLowerCase().contains(term);
    if (!selfMatches && matchingChildren.isEmpty()) {
      return null;
    }
    TreeItem<ElementViewModel> item = new TreeItem<>(viewModel);
    item.getChildren().addAll(matchingChildren);
    return item;
  }

  private void setExpandedRecursive(@Nullable TreeItem<ElementViewModel> item, boolean expanded) {
    if (item == null) {
      return;
    }
    item.setExpanded(expanded);
    for (TreeItem<ElementViewModel> child : item.getChildren()) {
      setExpandedRecursive(child, expanded);
    }
  }

  /** Only Fields and Groups (repeatable or not - {@link FormModelTreeController} decides what a drop creates). */
  private static boolean isDraggable(@NonNull Element element) {
    return element instanceof FieldElement || element instanceof GroupElement;
  }

  private void setupDragSource(@NonNull FormSourceElementTreeCell cell) {
    cell.setOnDragDetected(event -> {
      if (cell.isEmpty() || cell.getTreeItem() == null || cell.getTreeItem().getValue() == null) {
        return;
      }
      Element element = cell.getTreeItem().getValue().getElement();
      if (!isDraggable(element)) {
        return;
      }
      Dragboard dragboard = cell.startDragAndDrop(TransferMode.COPY);
      ClipboardContent content = new ClipboardContent();
      content.put(SOURCE_ELEMENT_DRAG_FORMAT, element.getId());
      dragboard.setContent(content);
      event.consume();
    });
  }
}
