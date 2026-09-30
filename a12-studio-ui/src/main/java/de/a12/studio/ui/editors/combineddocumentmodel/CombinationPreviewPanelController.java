package de.a12.studio.ui.editors.combineddocumentmodel;

import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelElements;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The "Preview" tab of the Combination Model editor: a read-only tree of the merged Document Model this
 * Combination Model produces, built by {@link CombinedDocumentModelElements#resolveForFieldReferences} (the
 * same approximate, kernel-free join every field-reference picker already resolves against - see that
 * class's own javadoc for what it does and does not apply: Addition steps only, in order; Selection/Decoration
 * steps are not reflected here either, for the same reason). This is the "scoped slice... buildable today
 * without the kernel dependency" the doc's Combined Document Model section called for, not the real
 * kernel-backed expand/merge preview SME has.
 *
 * <p>Closely mirrors {@code formmodel.documenttree.DocumentSourceTreeController} (read-only Document Model
 * tree, search filter, expand/collapse) minus its drag-and-drop (nothing to drop this onto) and "open
 * Document Model" button (the merged model is synthetic, in-memory only - there is no file to open).
 */
public class CombinationPreviewPanelController implements Initializable {

  @FXML
  private SearchFieldController searchController;

  @FXML
  private StackPane treeContainer;

  @FXML
  private TreeView<ElementViewModel> tree;

  private ProjectItem projectItem;

  private DocumentModel mergedModel;

  // Needed by ElementViewModel to resolve an Include group's children, same as DocumentSourceTreeController.
  private List<DocumentModel> otherDocumentModels = List.of();

  private Label placeholderLabel;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    searchController.setOnSearch(this::applyFilter);
    tree.setShowRoot(false);
    tree.setCellFactory(view -> new CombinationPreviewElementTreeCell());
  }

  public void load(@NonNull CombinedDocumentModel model, @NonNull ProjectItem projectItem) {
    this.projectItem = projectItem;
    this.mergedModel = CombinedDocumentModelElements.resolveForFieldReferences(projectItem, model.getId());
    this.otherDocumentModels = ProjectDocumentModels.getOtherDocumentModelsWithCombinations(projectItem);
    refresh();
  }

  /** Rebuilds the tree from the current model state - call after any change that may affect the merge (the
   * Base Model, a step's referenced model, or a referenced Document Model saved in another tab). */
  public void refresh() {
    if (projectItem == null) {
      return;
    }
    boolean hasModel = mergedModel != null && mergedModel.getContent() != null
        && mergedModel.getContent().getModelRoot() != null;
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
      placeholderLabel = new Label(StudioBundle.get("combined_document_model_editor.preview_unavailable"));
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
    if (mergedModel == null || mergedModel.getContent() == null || mergedModel.getContent().getModelRoot() == null) {
      return;
    }
    String term = filter == null ? "" : filter.trim().toLowerCase();
    TreeItem<ElementViewModel> root = new TreeItem<>();
    List<GroupElement> rootGroups = mergedModel.getContent().getModelRoot().getRootGroups();
    if (rootGroups != null) {
      for (GroupElement group : rootGroups) {
        TreeItem<ElementViewModel> item = term.isEmpty() ? toTreeItem(group) : toFilteredTreeItem(group, term);
        if (item != null) {
          root.getChildren().add(item);
        }
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
    // Root is hidden (showRoot=false) but must stay expanded, otherwise its top-level children would be hidden too.
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
}
