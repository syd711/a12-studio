package de.a12.studio.ui.editors.combineddocumentmodel;

import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelElements;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.util.JsonSettings;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.kernel.ProjectKernelModels;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
 * Combination Model produces, expanded by the A12 kernel through {@link ProjectKernelModels#expand} (the real
 * join, as SME shows it). If the kernel cannot expand the model, it falls back to {@link
 * CombinedDocumentModelElements#resolveForFieldReferences}, the approximate kernel-free join every
 * field-reference picker resolves against (Addition steps only, in order).
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
  private SplitPane split;

  // The read-only element details under the tree, only for a preview that asks for them (see showElementDetails).
  private VBox detailsPane;
  private GridPane detailsGrid;
  private TextArea detailsJson;

  @FXML
  private TreeView<ElementViewModel> tree;

  private ProjectItem projectItem;

  private DocumentModel mergedModel;

  // Needed by ElementViewModel to resolve an Include group's children, same as DocumentSourceTreeController.
  private List<DocumentModel> otherDocumentModels = List.of();

  private Label placeholderLabel;

  // Overrides the Combination Model's own "no base model" text, see showDocumentModel.
  private String placeholderText;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    searchController.setOnSearch(this::applyFilter);
    searchController.installShortcut(tree);
    tree.setShowRoot(false);
    tree.setCellFactory(view -> new CombinationPreviewElementTreeCell());
    tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showDetailsOf(newValue));
  }

  /**
   * Adds a read-only view of the selected element under the tree, like SME's Transformer Model preview, whose element
   * editor is the Document Model's own one made read-only: what the element is (name, kind, type, id, annotations) and
   * its full configuration as JSON (a group without its children).
   */
  public void showElementDetails() {
    if (detailsPane != null) {
      return;
    }
    detailsGrid = new GridPane();
    detailsGrid.setHgap(12);
    detailsGrid.setVgap(4);
    detailsJson = new TextArea();
    detailsJson.setEditable(false);
    detailsJson.getStyleClass().add("code-area");
    detailsJson.setPrefRowCount(8);
    detailsJson.setStyle("-fx-font-family: monospace;");
    detailsPane = new VBox(8, detailsGrid, detailsJson);
    detailsPane.setPadding(new javafx.geometry.Insets(8));
    javafx.scene.layout.VBox.setVgrow(detailsJson, javafx.scene.layout.Priority.ALWAYS);
    split.getItems().add(detailsPane);
    split.setDividerPositions(0.6);
    showDetailsOf(tree.getSelectionModel().getSelectedItem());
  }

  private void showDetailsOf(@Nullable TreeItem<ElementViewModel> selected) {
    if (detailsPane == null) {
      return;
    }
    detailsGrid.getChildren().clear();
    if (selected == null || selected.getValue() == null) {
      detailsJson.clear();
      detailsGrid.add(new Label(StudioBundle.get("combination_preview.select_element")), 0, 0);
      return;
    }
    ElementViewModel viewModel = selected.getValue();
    Element element = viewModel.getElement();
    int row = 0;
    row = addDetail(row, "combination_preview.name", element.getName());
    row = addDetail(row, "combination_preview.kind", viewModel.isGroup() ? "Group" : element.getType() == null ? null : element.getType().getValue());
    row = addDetail(row, "combination_preview.type", viewModel.getType());
    row = addDetail(row, "combination_preview.id", element.getId());
    for (var annotation : element.getAnnotations()) {
      row = addDetail(row, "combination_preview.annotation", annotation.getName() + (annotation.getValue() == null ? "" : " = " + annotation.getValue()));
    }
    try {
      JsonNode json = JsonSettings.objectMapper.valueToTree(element);
      if (json instanceof ObjectNode object && object.path("Group") instanceof ObjectNode group) {
        group.remove("elements");
      }
      detailsJson.setText(JsonSettings.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
    }
    catch (RuntimeException e) {
      detailsJson.setText(String.valueOf(e.getMessage()));
    }
  }

  private int addDetail(int row, String key, @Nullable String value) {
    if (value == null || value.isBlank()) {
      return row;
    }
    Label label = new Label(StudioBundle.get(key));
    label.getStyleClass().add("field-label");
    Label text = new Label(value);
    text.setWrapText(true);
    detailsGrid.add(label, 0, row);
    detailsGrid.add(text, 1, row);
    return row + 1;
  }

  public void load(@NonNull CombinedDocumentModel model, @NonNull ProjectItem projectItem) {
    this.projectItem = projectItem;
    // The kernel does the real join (additions, selections, decorations); the approximate Addition-only merge is
    // the fallback for whatever the kernel cannot expand (e.g. a half-edited model with an unset base).
    this.mergedModel = ProjectKernelModels.expand(projectItem, model.getId())
        .orElseGet(() -> CombinedDocumentModelElements.resolveForFieldReferences(projectItem, model.getId()));
    this.otherDocumentModels = ProjectDocumentModels.getOtherDocumentModelsWithCombinations(projectItem);
    refresh();
  }

  /**
   * Shows a Document Model that is already at hand instead of expanding a Combination Model - the Transformer Model
   * editor's Preview tab reuses this panel for the Document Model its transformation generated. A {@code null}
   * model shows {@code placeholder} (why there is none) in its place.
   */
  public void showDocumentModel(@Nullable DocumentModel documentModel, @NonNull ProjectItem projectItem, @NonNull String placeholder) {
    this.projectItem = projectItem;
    this.mergedModel = documentModel;
    this.otherDocumentModels = ProjectDocumentModels.getOtherDocumentModelsWithCombinations(projectItem);
    if (placeholderLabel != null) {
      placeholderLabel.setText(placeholder);
    }
    placeholderText = placeholder;
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
      placeholderLabel = new Label(placeholderText != null ? placeholderText
          : StudioBundle.get("combined_document_model_editor.preview_unavailable"));
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
