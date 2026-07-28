package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeColumn;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.util.converter.IntegerStringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Edits a {@link TreeModel}: the tree's columns, its node types (Document Model + element mapped per
 * column), and the general configuration (hierarchical column, expansion strategy). The header's
 * {@code modelReferences} track the node Document Models (purpose "document-model-for-tree").
 */
public class TreeModelEditorController extends AbstractEditorController implements Initializable {

  private static final List<String> EXPANSION_STRATEGIES = List.of("level_by_level", "expand_all");

  @FXML
  private TableView<TreeColumn> columnsTable;
  @FXML
  private TableColumn<TreeColumn, String> columnNameColumn;
  @FXML
  private TableColumn<TreeColumn, Integer> columnWidthColumn;
  @FXML
  private TableColumn<TreeColumn, Boolean> columnFixedWidthColumn;
  @FXML
  private TableColumn<TreeColumn, String> columnPinDirectionColumn;

  @FXML
  private ListView<TreeNode> nodesList;
  @FXML
  private ComboBox<String> nodeDocumentModelField;
  @FXML
  private CheckBox nodeDndField;
  @FXML
  private GridPane nodeColumnMappingGrid;

  @FXML
  private ComboBox<String> hierarchicalColumnField;
  @FXML
  private ComboBox<String> expansionStrategyField;

  private TreeModel model;
  private boolean updatingFromModel;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    initializeColumnsTable();
    initializeNodesList();
    initializeConfigurationFields();
  }

  private void initializeColumnsTable() {
    columnsTable.setEditable(true);

    columnNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
    columnNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    columnNameColumn.setOnEditCommit(event -> {
      event.getRowValue().setName(event.getNewValue());
      commitChange();
      refreshColumnDependentFields();
    });

    columnWidthColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getWidth()));
    columnWidthColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
    columnWidthColumn.setOnEditCommit(event -> {
      event.getRowValue().setWidth(event.getNewValue());
      commitChange();
    });

    columnFixedWidthColumn.setCellValueFactory(data -> {
      SimpleObjectProperty<Boolean> property = new SimpleObjectProperty<>(Boolean.TRUE.equals(data.getValue().getFixedWidth()));
      property.addListener((observable, oldValue, newValue) -> {
        data.getValue().setFixedWidth(newValue);
        commitChange();
      });
      return property;
    });
    columnFixedWidthColumn.setCellFactory(CheckBoxTableCell.forTableColumn(columnFixedWidthColumn));

    columnPinDirectionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPinDirection()));
    columnPinDirectionColumn.setCellFactory(ComboBoxTableCell.forTableColumn("left", "right", ""));
    columnPinDirectionColumn.setOnEditCommit(event -> {
      event.getRowValue().setPinDirection(event.getNewValue() == null || event.getNewValue().isBlank() ? null : event.getNewValue());
      commitChange();
    });
  }

  private void initializeNodesList() {
    nodesList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(TreeNode node, boolean empty) {
        super.updateItem(node, empty);
        setText(empty || node == null ? null : describeNode(node));
      }
    });
    nodesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showNode(newValue));

    nodeDocumentModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      TreeNode node = nodesList.getSelectionModel().getSelectedItem();
      if (updatingFromModel || node == null) {
        return;
      }
      node.setDocumentModelRef(newValue);
      syncModelReferences();
      nodesList.refresh();
      rebuildColumnMappingRows(node);
      commitChange();
    });

    nodeDndField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      TreeNode node = nodesList.getSelectionModel().getSelectedItem();
      if (updatingFromModel || node == null) {
        return;
      }
      if (node.getConfiguration() == null) {
        node.setConfiguration(new java.util.LinkedHashMap<>());
      }
      node.getConfiguration().put("dnd", newValue);
      commitChange();
    });
  }

  private void initializeConfigurationFields() {
    hierarchicalColumnField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setHierarchicalColumnRef(columnIdForName(newValue));
      commitChange();
    });

    expansionStrategyField.getItems().setAll(EXPANSION_STRATEGIES);
    expansionStrategyField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null || newValue == null) {
        return;
      }
      if (ensureConfiguration().getExpansionStrategy() == null) {
        ensureConfiguration().setExpansionStrategy(new de.a12.studio.models.treemodel.ExpansionStrategy());
      }
      ensureConfiguration().getExpansionStrategy().setType(newValue);
      commitChange();
    });
  }

  private de.a12.studio.models.treemodel.TreeConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new de.a12.studio.models.treemodel.TreeConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private String describeNode(TreeNode node) {
    String documentModel = node.getDocumentModelRef() != null ? node.getDocumentModelRef() : "<no document model>";
    return documentModel + (node.getId() != null ? " (" + node.getId() + ")" : "");
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((TreeModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull TreeModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      columnsTable.setItems(FXCollections.observableList(model.getContent().getColumns()));

      nodesList.setItems(FXCollections.observableList(model.getContent().getNodes()));

      nodeDocumentModelField.getItems().setAll(documentModelOptions());

      refreshColumnDependentFields();
      expansionStrategyField.setValue(model.getContent().getConfiguration() != null
          && model.getContent().getConfiguration().getExpansionStrategy() != null
          ? model.getContent().getConfiguration().getExpansionStrategy().getType()
          : null);
    }
    finally {
      updatingFromModel = false;
    }

    if (!nodesList.getItems().isEmpty()) {
      nodesList.getSelectionModel().select(0);
    }
  }

  private void refreshColumnDependentFields() {
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      List<String> columnNames = model.getContent().getColumns().stream().map(TreeColumn::getName).toList();
      hierarchicalColumnField.getItems().setAll(columnNames);
      String hierarchicalRef = model.getContent().getConfiguration() != null
          ? model.getContent().getConfiguration().getHierarchicalColumnRef()
          : null;
      hierarchicalColumnField.setValue(columnNameForId(hierarchicalRef));
    }
    finally {
      updatingFromModel = wasUpdating;
    }
  }

  private String columnNameForId(String columnId) {
    if (columnId == null) {
      return null;
    }
    return model.getContent().getColumns().stream()
        .filter(column -> columnId.equals(column.getId()))
        .map(TreeColumn::getName)
        .findFirst()
        .orElse(null);
  }

  private String columnIdForName(String name) {
    if (name == null) {
      return null;
    }
    return model.getContent().getColumns().stream()
        .filter(column -> name.equals(column.getName()))
        .map(TreeColumn::getId)
        .findFirst()
        .orElse(null);
  }

  private void showNode(TreeNode node) {
    updatingFromModel = true;
    try {
      if (node == null) {
        nodeDocumentModelField.setValue(null);
        nodeDndField.setSelected(false);
        nodeColumnMappingGrid.getChildren().clear();
        return;
      }
      nodeDocumentModelField.setValue(node.getDocumentModelRef());
      nodeDndField.setSelected(node.getConfiguration() != null && Boolean.TRUE.equals(node.getConfiguration().get("dnd")));
      rebuildColumnMappingRows(node);
    }
    finally {
      updatingFromModel = false;
    }
  }

  /** One row per tree column mapping the column to a field of the node's Document Model. */
  private void rebuildColumnMappingRows(TreeNode node) {
    nodeColumnMappingGrid.getChildren().clear();

    List<String> fieldOptions = fieldOptionsFor(node.getDocumentModelRef());
    int row = 0;
    for (TreeColumn column : model.getContent().getColumns()) {
      javafx.scene.control.Label columnLabel = new javafx.scene.control.Label(column.getName());
      columnLabel.getStyleClass().add("field-label");

      ComboBox<String> elementField = new ComboBox<>();
      elementField.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(elementField, javafx.scene.layout.Priority.ALWAYS);
      elementField.getItems().setAll(fieldOptions);
      elementField.setValue(mappedElementRef(node, column.getId()));
      elementField.valueProperty().addListener((observable, oldValue, newValue) -> {
        if (updatingFromModel) {
          return;
        }
        setMappedElementRef(node, column.getId(), newValue);
        commitChange();
      });

      nodeColumnMappingGrid.addRow(row++, columnLabel, elementField);
    }
  }

  private String mappedElementRef(TreeNode node, String columnId) {
    return node.getColumns().stream()
        .filter(mapping -> columnId != null && columnId.equals(mapping.getColumnRef()))
        .map(TreeNodeColumn::getElementRef)
        .findFirst()
        .orElse(null);
  }

  private void setMappedElementRef(TreeNode node, String columnId, String elementRef) {
    TreeNodeColumn mapping = node.getColumns().stream()
        .filter(existing -> columnId != null && columnId.equals(existing.getColumnRef()))
        .findFirst()
        .orElse(null);
    if (mapping == null) {
      mapping = new TreeNodeColumn();
      mapping.setColumnRef(columnId);
      node.getColumns().add(mapping);
    }
    mapping.setElementRef(elementRef);
  }

  /** All field element ids of the given Document Model, walking its root groups recursively. */
  private List<String> fieldOptionsFor(String documentModelId) {
    if (documentModelId == null) {
      return List.of();
    }
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.DOCUMENT).stream()
        .filter(documentModel -> documentModelId.equals(documentModel.getId()))
        .findFirst()
        .map(documentModel -> collectFieldIds((DocumentModel) documentModel))
        .orElse(List.of());
  }

  private List<String> collectFieldIds(DocumentModel documentModel) {
    List<String> ids = new ArrayList<>();
    if (documentModel.getContent() != null && documentModel.getContent().getModelRoot() != null
        && documentModel.getContent().getModelRoot().getRootGroups() != null) {
      for (GroupElement group : documentModel.getContent().getModelRoot().getRootGroups()) {
        collectFieldIds(group, ids);
      }
    }
    return ids;
  }

  private void collectFieldIds(GroupElement group, List<String> ids) {
    if (group.getGroup() == null || group.getGroup().getElements() == null) {
      return;
    }
    for (Element child : group.getGroup().getElements()) {
      if (child instanceof FieldElement field && field.getId() != null) {
        ids.add(field.getId());
      }
      else if (child instanceof GroupElement childGroup) {
        collectFieldIds(childGroup, ids);
      }
    }
  }

  @FXML
  public void onAddColumn(ActionEvent e) {
    TreeColumn column = new TreeColumn();
    column.setId("column-" + UUID.randomUUID().toString().replace("-", "").substring(0, 5));
    column.setName("Column " + (model.getContent().getColumns().size() + 1));
    column.setWidth(1);
    column.setFixedWidth(false);
    columnsTable.getItems().add(column);
    refreshColumnDependentFields();
    commitChange();
  }

  @FXML
  public void onRemoveColumn(ActionEvent e) {
    TreeColumn column = columnsTable.getSelectionModel().getSelectedItem();
    if (column == null) {
      return;
    }
    columnsTable.getItems().remove(column);
    for (TreeNode node : model.getContent().getNodes()) {
      node.getColumns().removeIf(mapping -> column.getId() != null && column.getId().equals(mapping.getColumnRef()));
    }
    refreshColumnDependentFields();
    TreeNode selectedNode = nodesList.getSelectionModel().getSelectedItem();
    if (selectedNode != null) {
      rebuildColumnMappingRows(selectedNode);
    }
    commitChange();
  }

  @FXML
  public void onAddNode(ActionEvent e) {
    TreeNode node = new TreeNode();
    node.setId("node-" + UUID.randomUUID().toString().replace("-", "").substring(0, 5));
    nodesList.getItems().add(node);
    nodesList.getSelectionModel().select(node);
    commitChange();
  }

  @FXML
  public void onRemoveNode(ActionEvent e) {
    TreeNode node = nodesList.getSelectionModel().getSelectedItem();
    if (node == null) {
      return;
    }
    nodesList.getItems().remove(node);
    syncModelReferences();
    commitChange();
  }

  private List<String> documentModelOptions() {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.DOCUMENT).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  /** One header reference per distinct node Document Model, purpose "document-model-for-tree". */
  private void syncModelReferences() {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(reference -> ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_TREE.equals(reference.getPurpose()));
    List<String> seen = new ArrayList<>();
    int index = 1;
    for (TreeNode node : model.getContent().getNodes()) {
      String documentModel = node.getDocumentModelRef();
      if (documentModel == null || documentModel.isBlank() || seen.contains(documentModel)) {
        continue;
      }
      seen.add(documentModel);
      ModelReference reference = new ModelReference();
      reference.setPurpose(ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_TREE);
      reference.setModelType(ModelType.DOCUMENT);
      reference.setAlias("DM" + index++);
      reference.setReference(documentModel);
      references.add(reference);
    }
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.TREE;
  }
}
