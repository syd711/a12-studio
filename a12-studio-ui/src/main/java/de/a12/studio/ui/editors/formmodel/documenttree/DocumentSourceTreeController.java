package de.a12.studio.ui.editors.formmodel.documenttree;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.formmodel.DependentConfig;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.editors.formmodel.FormModelEditorController;
import de.a12.studio.ui.editors.formmodel.formtree.FormDependencyBadges;
import de.a12.studio.ui.editors.formmodel.formtree.FormDependencyBadges.Entry;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
import java.util.HashMap;
import java.util.Map;
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

  @FXML
  private Button openDocumentModelButton;

  private ModelRoot modelRoot;

  private String documentModelId;

  // Every Document Model in the project, needed by ElementViewModel to resolve an Include group's children
  // from the Document Model it references (see ElementViewModel#getChildren), same as
  // DocumentModelElementsTreeController#otherDocumentModels.
  private List<DocumentModel> otherDocumentModels = List.of();

  private Label placeholderLabel;

  private ProjectItem formModelProjectItem;

  /** SME's dependency marks of a Document Model element: which master fields it depends on and which fields
   * it triggers, by element id; see {@link #refreshDependencyMarks()}. */
  public record Marks(List<Entry> dependentOn, List<Entry> masterOf) {
  }

  private Map<String, Marks> marksById = Map.of();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    searchController.setOnSearch(this::applyFilter);
    searchController.installShortcut(tree);
    tree.setShowRoot(false);
    tree.setCellFactory(view -> {
      FormSourceElementTreeCell cell = new FormSourceElementTreeCell(id -> marksById.get(id));
      setupDragSource(cell);
      return cell;
    });
  }

  public void load(@Nullable DocumentModel model, @NonNull ProjectItem formModelProjectItem) {
    this.modelRoot = model != null && model.getContent() != null ? model.getContent().getModelRoot() : null;
    this.documentModelId = model != null ? model.getId() : null;
    this.formModelProjectItem = formModelProjectItem;
    this.otherDocumentModels = ProjectDocumentModels.getOtherDocumentModelsWithCombinations(formModelProjectItem);
    boolean hasModel = modelRoot != null;
    tree.setVisible(hasModel);
    tree.setManaged(hasModel);
    openDocumentModelButton.setDisable(documentModelId == null);
    if (!hasModel) {
      showPlaceholder();
      return;
    }
    hidePlaceholder();
    applyFilter(searchController.getText());
  }

  @FXML
  private void onOpenDocumentModel() {
    if (documentModelId != null) {
      ProjectDocumentModels.openModelInEditor(documentModelId);
    }
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
    refreshDependencyMarks();
  }

  /**
   * Recomputes the "D"/"T" marks (SME's {@code dependencySuffix.tsx}, Document Model case) from the Form Model's
   * field and group configuration - a field's {@code dependentField}/{@code dependentEnumeration} and a group's
   * {@code dependentGroup} name a master field - and repaints the rows. Called after every rebuild and whenever
   * the Form Model is saved, since the dependencies are edited elsewhere in the editor.
   */
  public void refreshDependencyMarks() {
    marksById = computeMarks();
    tree.refresh();
  }

  private Map<String, Marks> computeMarks() {
    if (formModelProjectItem == null || !(formModelProjectItem.getModel() instanceof FormModel formModel)
        || formModel.getContent() == null || tree.getRoot() == null) {
      return Map.of();
    }
    Map<String, String> pathById = new HashMap<>();
    collectPaths(tree.getRoot(), pathById);
    Map<String, List<Entry>> dependentOn = new HashMap<>();
    Map<String, List<Entry>> masterOf = new HashMap<>();
    for (FieldConfigEntry entry : formModel.getContent().getFieldConfiguration().getField()) {
      link(entry.getElementRef(), entry.getDependentField(), "Field", pathById, dependentOn, masterOf);
      if (entry.getDependentEnumeration() != null) {
        link(entry.getElementRef(), entry.getDependentEnumeration().getMasterField(), "Enumeration", pathById, dependentOn, masterOf);
      }
    }
    for (GroupConfigEntry entry : formModel.getContent().getGroupConfiguration().getGroup()) {
      link(entry.getGroupRef(), entry.getDependentGroup(), "Group", pathById, dependentOn, masterOf);
    }
    Map<String, Marks> marks = new HashMap<>();
    java.util.Set<String> ids = new java.util.HashSet<>(dependentOn.keySet());
    ids.addAll(masterOf.keySet());
    for (String id : ids) {
      marks.put(id, new Marks(dependentOn.getOrDefault(id, List.of()), masterOf.getOrDefault(id, List.of())));
    }
    return marks;
  }

  private static void link(String elementRef, DependentConfig dependentConfig, String kind, Map<String, String> pathById,
      Map<String, List<Entry>> dependentOn, Map<String, List<Entry>> masterOf) {
    if (dependentConfig != null) {
      link(elementRef, dependentConfig.getMasterField(), kind, pathById, dependentOn, masterOf);
    }
  }

  private static void link(String elementRef, String masterField, String kind, Map<String, String> pathById,
      Map<String, List<Entry>> dependentOn, Map<String, List<Entry>> masterOf) {
    if (elementRef == null || masterField == null) {
      return;
    }
    dependentOn.computeIfAbsent(elementRef, id -> new ArrayList<>())
        .add(new Entry(pathById.getOrDefault(masterField, FormDependencyBadges.UNKNOWN_PATH), kind));
    masterOf.computeIfAbsent(masterField, id -> new ArrayList<>())
        .add(new Entry(pathById.getOrDefault(elementRef, FormDependencyBadges.UNKNOWN_PATH), kind));
  }

  private static void collectPaths(TreeItem<ElementViewModel> item, Map<String, String> pathById) {
    if (item.getValue() != null) {
      List<String> names = new ArrayList<>();
      for (TreeItem<ElementViewModel> current = item; current != null && current.getValue() != null; current = current.getParent()) {
        names.add(0, current.getValue().getName());
      }
      pathById.putIfAbsent(item.getValue().getElement().getId(), String.join("/", names));
    }
    item.getChildren().forEach(child -> collectPaths(child, pathById));
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
