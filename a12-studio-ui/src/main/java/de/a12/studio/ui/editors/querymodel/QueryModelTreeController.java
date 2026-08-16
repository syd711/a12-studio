package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.editors.propertyeditors.TargetModelPanelController;
import de.a12.studio.ui.editors.querymodel.dialogs.Dialogs;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Tab 1 ("Model Tree") of the Query Model editor: a read-only tree of the query's target {@link
 * DocumentModel}, with an "In Result" checkbox column (writes {@link QueryModelContent#getFields()}) and a
 * "Filter Definition" column. Unlike SME's Query Model - a multi-Document-Model/Relationship-Model constraint
 * graph a user builds up node by node - this editor only supports a single target Document Model (matching
 * {@code QueryModel.json}'s shape), so "Filter Definition" collapses to a single expression on that one
 * Document Model rather than one per graph node; see {@link QueryTreeRow#hasFilterDefinition()}.
 */
public class QueryModelTreeController implements Initializable {

  @FXML
  private TargetModelPanelController targetModelPanelController;

  @FXML
  private SearchFieldController searchController;

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
  private ElementIndex elementIndex;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    elementsTreeTable.setShowRoot(true);
    nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue().getName()));
    nameColumn.setCellFactory(column -> new QueryTreeNameCell());

    inResultColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));
    inResultColumn.setCellFactory(column -> new InResultCell());

    filterDefinitionColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));
    filterDefinitionColumn.setCellFactory(column -> new FilterDefinitionCell());

    targetModelPanelController.setOnChange(this::onTargetModelChanged);
    searchController.setOnSearch(term -> rebuildTree());
  }

  public void load(@NonNull ProjectItem projectItem, @NonNull QueryModel model) {
    this.projectItem = projectItem;
    this.model = model;
    targetModelPanelController.load(ProjectDocumentModels.getOtherDocumentModels(projectItem), content().getTargetDocumentModel());
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
    elementIndex = targetDocumentModel != null ? new ElementIndex(targetDocumentModel) : null;
  }

  private void onTargetModelChanged() {
    String newTargetId = targetModelPanelController.getValue();
    if (!Objects.equals(newTargetId, content().getTargetDocumentModel())) {
      content().setTargetDocumentModel(newTargetId);
      // Field paths and the filter expression are only meaningful against the Document Model they were
      // picked/written against; switching target drops both rather than silently keeping stale references.
      content().getFields().clear();
      content().setFilterDefinition(null);
      syncTargetModelReference(newTargetId);
      resolveTargetDocumentModel();
      rebuildTree();
      commitChange();
    }
  }

  /**
   * Mirrors {@code MappingModelEditorController#syncModelReferences} for a single reference instead of a list:
   * replaces whatever {@link ModelReference#PURPOSE_DOCUMENT_MODEL_FOR_QUERY} reference the header carries with
   * one pointing at {@code targetId} (or drops it entirely once {@code targetId} is {@code null}).
   */
  private void syncTargetModelReference(String targetId) {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(reference -> reference.getModelType() == ModelType.DOCUMENT
        && ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_QUERY.equals(reference.getPurpose()));
    if (targetId != null) {
      ModelReference reference = new ModelReference();
      reference.setModelType(ModelType.DOCUMENT);
      reference.setPurpose(ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_QUERY);
      reference.setReference(targetId);
      references.add(reference);
    }
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
  }

  private String searchTerm() {
    String text = searchController.getText();
    return text == null ? "" : text.trim().toLowerCase();
  }

  private TreeItem<QueryTreeRow> buildTargetDocumentModelItem(@NonNull String term) {
    QueryTreeRow row = QueryTreeRow.targetDocumentModel(targetDocumentModel.getId());
    List<String> allFieldPaths = new ArrayList<>();
    List<TreeItem<QueryTreeRow>> children = new ArrayList<>();
    List<GroupElement> rootGroups = targetDocumentModel.getContent().getModelRoot().getRootGroups();
    for (GroupElement group : rootGroups) {
      ElementViewModel elementViewModel = new ElementViewModel(group);
      allFieldPaths.addAll(QueryTreeRow.collectDescendantFieldPaths(elementViewModel, this::pathOf));
      TreeItem<QueryTreeRow> childItem = buildElementItem(elementViewModel, term);
      if (childItem != null) {
        children.add(childItem);
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

  private TreeItem<QueryTreeRow> buildElementItem(@NonNull ElementViewModel elementViewModel, @NonNull String term) {
    QueryTreeRow row = QueryTreeRow.element(elementViewModel, pathOf(elementViewModel));
    row.setDescendantFieldPaths(QueryTreeRow.collectDescendantFieldPaths(elementViewModel, this::pathOf));

    List<TreeItem<QueryTreeRow>> matchingChildren = new ArrayList<>();
    for (ElementViewModel child : projectableChildren(elementViewModel)) {
      TreeItem<QueryTreeRow> childItem = buildElementItem(child, term);
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

  private String pathOf(@NonNull ElementViewModel elementViewModel) {
    return elementIndex.getPath(elementViewModel.getElement());
  }

  private void toggleInResult(@NonNull QueryTreeRow row) {
    List<String> fields = content().getFields();
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
      boolean turnOn = row.inResultState(fields) != QueryTreeRow.InResultState.ALL;
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

  private void onEditFilterDefinition() {
    if (Dialogs.showFilterDefinition(Studio.stage, content())) {
      elementsTreeTable.refresh();
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
      QueryTreeRow.InResultState state = row.inResultState(content().getFields());
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
