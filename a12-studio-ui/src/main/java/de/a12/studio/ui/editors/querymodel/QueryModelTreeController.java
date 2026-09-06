package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.editors.querymodel.dialogs.Dialogs;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Tab 1 ("Model Tree") of the Query Model editor: the query's document graph, rooted at the target {@link
 * DocumentModel} and extended by zero or more relationship-traversal hops ({@link QueryLink}, added/removed via
 * this tree's row context menu - see {@link #onAddRelationship}/{@link #onRemoveRelationship} - scoped to
 * relationships actually connected to the row's own Document Model, not every relationship in the project). Each
 * node in the graph (the target Document Model itself, or a relationship hop) gets an "In Result" checkbox
 * column reading/writing its own {@code fields} list ({@link QueryModelContent#getFields()} for the root,
 * {@link QueryLink#getFields()} for a hop - see {@link QueryTreeRow#getFieldsScope()}).
 *
 * <p>Unlike SME's Query Model, "Filter Definition" is still collapsed to a single expression on the whole query
 * ({@link QueryModelContent#getFilterDefinition()}) rather than one per graph node - per-node filtering (backed
 * by {@link de.a12.studio.models.querymodel.ql.QueryLanguageEmitter}/{@code Formatter}, already built) is a
 * separate, not-yet-done piece; see {@link QueryTreeRow#hasFilterDefinition()} and
 * docs/sme-reference-comparison.md "Query Model" section.
 */
public class QueryModelTreeController implements Initializable {

  @FXML
  private SearchFieldController searchController;

  @FXML
  private Button addRelationshipButton;

  @FXML
  private Button removeRelationshipButton;

  @FXML
  private TreeTableView<QueryTreeRow> elementsTreeTable;

  @FXML
  private TreeTableColumn<QueryTreeRow, String> nameColumn;

  @FXML
  private TreeTableColumn<QueryTreeRow, QueryTreeRow> inResultColumn;

  @FXML
  private TreeTableColumn<QueryTreeRow, QueryTreeRow> filterDefinitionColumn;

  private ProjectItem projectItem;
  private QueryModel model;
  private DocumentModel targetDocumentModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    elementsTreeTable.setShowRoot(true);
    nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue().getName()));
    nameColumn.setCellFactory(column -> new QueryTreeNameCell());

    inResultColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));
    inResultColumn.setCellFactory(column -> new InResultCell());

    filterDefinitionColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));
    filterDefinitionColumn.setCellFactory(column -> new FilterDefinitionCell());

    elementsTreeTable.setRowFactory(table -> createTreeTableRow());
    elementsTreeTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateActionButtonsState());

    searchController.setOnSearch(term -> rebuildTree());
  }

  public void load(@NonNull ProjectItem projectItem, @NonNull QueryModel model) {
    this.projectItem = projectItem;
    this.model = model;
    resolveTargetDocumentModel();
    rebuildTree();
  }

  private QueryModelContent content() {
    return model.getContent();
  }

  private void resolveTargetDocumentModel() {
    String targetId = content().getTargetDocumentModel();
    targetDocumentModel = ProjectDocumentModels.getOtherDocumentModels(projectItem).stream()
        .filter(dm -> dm.getId().equals(targetId))
        .findFirst()
        .orElse(null);
  }

  private void rebuildTree() {
    TreeItem<QueryTreeRow> root = new TreeItem<>(QueryTreeRow.rootLabel(StudioBundle.get("model_tree")));
    root.setExpanded(true);
    if (targetDocumentModel != null) {
      TreeItem<QueryTreeRow> targetItem = buildTargetDocumentModelItem(searchTerm());
      if (targetItem != null) {
        root.getChildren().add(targetItem);
      }
    }
    elementsTreeTable.setRoot(root);
    updateActionButtonsState();
  }

  private String searchTerm() {
    String text = searchController.getText();
    return text == null ? "" : text.trim().toLowerCase();
  }

  private TreeItem<QueryTreeRow> buildTargetDocumentModelItem(@NonNull String term) {
    ElementIndex elementIndex = new ElementIndex(targetDocumentModel);
    QueryTreeRow row = QueryTreeRow.targetDocumentModel(targetDocumentModel.getId());
    row.setFieldsScope(content().getFields());

    List<String> allFieldPaths = new ArrayList<>();
    List<TreeItem<QueryTreeRow>> children = new ArrayList<>();
    List<GroupElement> rootGroups = targetDocumentModel.getContent().getModelRoot().getRootGroups();
    for (GroupElement group : rootGroups) {
      ElementViewModel elementViewModel = new ElementViewModel(group);
      allFieldPaths.addAll(QueryTreeRow.collectDescendantFieldPaths(elementViewModel, ev -> elementIndex.getPath(ev.getElement())));
      TreeItem<QueryTreeRow> childItem = buildElementItem(elementViewModel, elementIndex, content().getFields(), term);
      if (childItem != null) {
        children.add(childItem);
      }
    }
    for (QueryLink link : content().getLinks()) {
      TreeItem<QueryTreeRow> linkItem = buildRelationshipLinkItem(link, term);
      if (linkItem != null) {
        children.add(linkItem);
        allFieldPaths.addAll(linkItem.getValue().getDescendantFieldPaths());
      }
    }
    row.setDescendantFieldPaths(allFieldPaths);

    boolean selfMatches = term.isEmpty() || row.getName().toLowerCase().contains(term);
    if (!selfMatches && children.isEmpty()) {
      return null;
    }
    TreeItem<QueryTreeRow> item = new TreeItem<>(row);
    item.getChildren().addAll(children);
    item.setExpanded(true);
    return item;
  }

  /** Builds the row for one relationship hop, resolving its target Document Model (via {@link
   * QueryTraversalOption#resolveTargetDocumentModel}) to render that model's own field/group subtree - scoped to
   * {@code link.getFields()}, exactly like the root target Document Model is scoped to {@code content.fields} -
   * plus any further nested hops ({@link QueryLink#getLinks()}), recursively. An unresolved relationship/role
   * (deleted elsewhere) renders as a childless, checkbox-less row instead of failing the whole tree. */
  private TreeItem<QueryTreeRow> buildRelationshipLinkItem(@NonNull QueryLink link, @NonNull String term) {
    DocumentModel linkedDocumentModel = QueryTraversalOption.resolveTargetDocumentModel(projectItem, link.getRelationshipModel(), link.getTargetRole());
    QueryTreeRow row = QueryTreeRow.relationshipLink(link, linkedDocumentModel != null ? linkedDocumentModel.getId() : null);

    List<TreeItem<QueryTreeRow>> children = new ArrayList<>();
    List<String> allFieldPaths = new ArrayList<>();
    if (linkedDocumentModel != null && linkedDocumentModel.getContent() != null && linkedDocumentModel.getContent().getModelRoot() != null) {
      row.setFieldsScope(link.getFields());
      ElementIndex elementIndex = new ElementIndex(linkedDocumentModel);
      List<GroupElement> rootGroups = linkedDocumentModel.getContent().getModelRoot().getRootGroups();
      if (rootGroups != null) {
        for (GroupElement group : rootGroups) {
          ElementViewModel elementViewModel = new ElementViewModel(group);
          allFieldPaths.addAll(QueryTreeRow.collectDescendantFieldPaths(elementViewModel, ev -> elementIndex.getPath(ev.getElement())));
          TreeItem<QueryTreeRow> childItem = buildElementItem(elementViewModel, elementIndex, link.getFields(), term);
          if (childItem != null) {
            children.add(childItem);
          }
        }
      }
    }
    for (QueryLink nestedLink : link.getLinks()) {
      TreeItem<QueryTreeRow> nestedItem = buildRelationshipLinkItem(nestedLink, term);
      if (nestedItem != null) {
        children.add(nestedItem);
        allFieldPaths.addAll(nestedItem.getValue().getDescendantFieldPaths());
      }
    }
    row.setDescendantFieldPaths(allFieldPaths);

    boolean selfMatches = term.isEmpty() || row.getName().toLowerCase().contains(term);
    if (!selfMatches && children.isEmpty()) {
      return null;
    }
    TreeItem<QueryTreeRow> item = new TreeItem<>(row);
    item.getChildren().addAll(children);
    item.setExpanded(true);
    return item;
  }

  private TreeItem<QueryTreeRow> buildElementItem(@NonNull ElementViewModel elementViewModel, @NonNull ElementIndex elementIndex,
                                                    @NonNull List<String> fieldsScope, @NonNull String term) {
    QueryTreeRow row = QueryTreeRow.element(elementViewModel, elementIndex.getPath(elementViewModel.getElement()));
    row.setFieldsScope(fieldsScope);
    row.setDescendantFieldPaths(QueryTreeRow.collectDescendantFieldPaths(elementViewModel, ev -> elementIndex.getPath(ev.getElement())));

    List<TreeItem<QueryTreeRow>> matchingChildren = new ArrayList<>();
    for (ElementViewModel child : projectableChildren(elementViewModel)) {
      TreeItem<QueryTreeRow> childItem = buildElementItem(child, elementIndex, fieldsScope, term);
      if (childItem != null) {
        matchingChildren.add(childItem);
      }
    }

    boolean selfMatches = term.isEmpty() || (row.getName() != null && row.getName().toLowerCase().contains(term));
    if (!selfMatches && matchingChildren.isEmpty()) {
      return null;
    }
    TreeItem<QueryTreeRow> item = new TreeItem<>(row);
    item.getChildren().addAll(matchingChildren);
    item.setExpanded(true);
    return item;
  }

  /** Only Fields and Groups are projectable query tree nodes - Rule/Computation elements are skipped, mirroring
   * {@link QueryTreeRow#collectDescendantFieldPaths}. */
  private static List<ElementViewModel> projectableChildren(@NonNull ElementViewModel elementViewModel) {
    List<ElementViewModel> children = new ArrayList<>();
    for (ElementViewModel child : elementViewModel.getChildren()) {
      if (child.getElement() instanceof FieldElement || child.getElement() instanceof GroupElement) {
        children.add(child);
      }
    }
    return children;
  }

  private void toggleInResult(@NonNull QueryTreeRow row) {
    List<String> fields = row.getFieldsScope();
    if (row.isField()) {
      String path = row.getPath();
      if (fields.contains(path)) {
        fields.remove(path);
      }
      else {
        fields.add(path);
      }
    }
    else {
      boolean turnOn = row.inResultState() != QueryTreeRow.InResultState.ALL;
      for (String path : row.getDescendantFieldPaths()) {
        if (turnOn) {
          if (!fields.contains(path)) {
            fields.add(path);
          }
        }
        else {
          fields.remove(path);
        }
      }
    }
    commitChange();
    elementsTreeTable.refresh();
  }

  @FXML
  private void onExpandAll() {
    setExpandedRecursive(elementsTreeTable.getRoot(), true);
  }

  @FXML
  private void onCollapseAll() {
    setExpandedRecursive(elementsTreeTable.getRoot(), false);
  }

  private void setExpandedRecursive(TreeItem<QueryTreeRow> item, boolean expanded) {
    if (item == null) {
      return;
    }
    item.setExpanded(expanded);
    for (TreeItem<QueryTreeRow> child : item.getChildren()) {
      setExpandedRecursive(child, expanded);
    }
  }

  private void onEditFilterDefinition() {
    if (Dialogs.showFilterDefinition(Studio.stage, content())) {
      elementsTreeTable.refresh();
    }
  }

  private TreeTableRow<QueryTreeRow> createTreeTableRow() {
    TreeTableRow<QueryTreeRow> row = new TreeTableRow<>();
    row.setOnContextMenuRequested(event -> {
      QueryTreeRow value = row.getItem();
      if (value == null || value.getKind() == QueryTreeRow.Kind.ELEMENT) {
        return;
      }
      ContextMenu contextMenu = createContextMenu(row.getTreeItem());
      if (!contextMenu.getItems().isEmpty()) {
        contextMenu.show(row, event.getScreenX(), event.getScreenY());
      }
    });
    return row;
  }

  private ContextMenu createContextMenu(@NonNull TreeItem<QueryTreeRow> treeItem) {
    ContextMenu contextMenu = new ContextMenu();
    String sourceDocumentModelId = sourceDocumentModelIdFor(treeItem.getValue());
    if (sourceDocumentModelId != null) {
      MenuItem addItem = new MenuItem(StudioBundle.get("query_model_tree.add_relationship"));
      addItem.setOnAction(event -> onAddRelationship(treeItem, sourceDocumentModelId));
      contextMenu.getItems().add(addItem);
    }
    if (treeItem.getValue().isRelationshipLink()) {
      MenuItem removeItem = new MenuItem(StudioBundle.get("query_model_tree.remove_relationship"));
      removeItem.setOnAction(event -> onRemoveRelationship(treeItem));
      contextMenu.getItems().add(removeItem);
    }
    return contextMenu;
  }

  /** The Document Model a new relationship added under {@code row} should be scoped to (see {@link
   * QueryTraversalOption#optionsConnectedTo}): the target Document Model's own id for the root row, or a
   * relationship-link row's already-resolved target - null (no "Add Relationship" action at all) for an
   * unresolved link, since there's nothing to scope candidates by. */
  private String sourceDocumentModelIdFor(@NonNull QueryTreeRow row) {
    if (row.getKind() == QueryTreeRow.Kind.TARGET_DOCUMENT_MODEL) {
      return targetDocumentModel.getId();
    }
    if (row.getKind() == QueryTreeRow.Kind.RELATIONSHIP_LINK && row.getLink() != null) {
      DocumentModel resolved = QueryTraversalOption.resolveTargetDocumentModel(projectItem, row.getLink().getRelationshipModel(), row.getLink().getTargetRole());
      return resolved != null ? resolved.getId() : null;
    }
    return null;
  }

  private void onAddRelationship(@NonNull TreeItem<QueryTreeRow> treeItem, @NonNull String sourceDocumentModelId) {
    Optional<QueryTraversalOption> selected = Dialogs.showAddRelationship(Studio.stage, projectItem, sourceDocumentModelId);
    if (selected.isEmpty()) {
      return;
    }
    QueryLink newLink = new QueryLink();
    newLink.setRelationshipModel(selected.get().relationshipModel());
    newLink.setTargetRole(selected.get().targetRole());
    linksListFor(treeItem.getValue()).add(newLink);

    commitChange();
    rebuildTree();
  }

  private void onRemoveRelationship(@NonNull TreeItem<QueryTreeRow> treeItem) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        StudioBundle.get("query_model_tree.remove_relationship_confirm"), null, null, StudioBundle.get("remove"));
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }

    TreeItem<QueryTreeRow> parentItem = treeItem.getParent();
    if (parentItem == null || parentItem.getValue() == null) {
      return;
    }
    linksListFor(parentItem.getValue()).remove(treeItem.getValue().getLink());

    commitChange();
    rebuildTree();
  }

  /** The list a relationship hop is (or should be) stored in: {@code content.links} for the target Document
   * Model row, or the link's own {@code links} for a relationship-link row (a nested/multi-hop traversal). */
  private List<QueryLink> linksListFor(@NonNull QueryTreeRow row) {
    if (row.getKind() == QueryTreeRow.Kind.RELATIONSHIP_LINK && row.getLink() != null) {
      return row.getLink().getLinks();
    }
    return content().getLinks();
  }

  private void updateActionButtonsState() {
    TreeItem<QueryTreeRow> selected = elementsTreeTable.getSelectionModel().getSelectedItem();
    QueryTreeRow row = selected != null ? selected.getValue() : null;
    addRelationshipButton.setDisable(row == null || sourceDocumentModelIdFor(row) == null);
    removeRelationshipButton.setDisable(row == null || !row.isRelationshipLink());
  }

  @FXML
  private void onAddRelationshipButton() {
    TreeItem<QueryTreeRow> selected = elementsTreeTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    String sourceDocumentModelId = sourceDocumentModelIdFor(selected.getValue());
    if (sourceDocumentModelId != null) {
      onAddRelationship(selected, sourceDocumentModelId);
    }
  }

  @FXML
  private void onRemoveRelationshipButton() {
    TreeItem<QueryTreeRow> selected = elementsTreeTable.getSelectionModel().getSelectedItem();
    if (selected != null && selected.getValue().isRelationshipLink()) {
      onRemoveRelationship(selected);
    }
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  private class InResultCell extends TreeTableCell<QueryTreeRow, QueryTreeRow> {
    private final CheckBox checkBox = new CheckBox();

    InResultCell() {
      checkBox.setAllowIndeterminate(true);
      checkBox.setOnAction(event -> {
        QueryTreeRow row = getItem();
        if (row != null) {
          toggleInResult(row);
        }
      });
    }

    @Override
    protected void updateItem(QueryTreeRow row, boolean empty) {
      super.updateItem(row, empty);
      if (empty || row == null || !row.hasInResultCheckbox()) {
        setGraphic(null);
        return;
      }
      QueryTreeRow.InResultState state = row.inResultState();
      if (state == QueryTreeRow.InResultState.NOT_APPLICABLE) {
        setGraphic(null);
        return;
      }
      checkBox.setIndeterminate(state == QueryTreeRow.InResultState.MIXED);
      checkBox.setSelected(state == QueryTreeRow.InResultState.ALL);
      setGraphic(checkBox);
    }
  }

  private class FilterDefinitionCell extends TreeTableCell<QueryTreeRow, QueryTreeRow> {

    @Override
    protected void updateItem(QueryTreeRow row, boolean empty) {
      super.updateItem(row, empty);
      if (empty || row == null || !row.hasFilterDefinition()) {
        setText(null);
        setGraphic(null);
        setOnMouseClicked(null);
        return;
      }
      String filterDefinition = content().getFilterDefinition();
      setText(filterDefinition == null || filterDefinition.isBlank()
          ? StudioBundle.get("edit_filter_definition") : summarize(filterDefinition));
      setGraphic(null);
      setCursor(Cursor.HAND);
      setOnMouseClicked(event -> onEditFilterDefinition());
    }

    private String summarize(@NonNull String filterDefinition) {
      String singleLine = filterDefinition.strip().replaceAll("\\s+", " ");
      return singleLine.length() > 80 ? singleLine.substring(0, 80) + "…" : singleLine;
    }
  }
}
