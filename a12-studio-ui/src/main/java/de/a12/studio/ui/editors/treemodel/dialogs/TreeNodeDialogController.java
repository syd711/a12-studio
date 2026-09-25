package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeColumn;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Add/edit dialog for a single {@link TreeNode} (node type), opened from {@link
 * de.a12.studio.ui.editors.treemodel.TreeNodeTypesPanelController} by clicking a node type row or its Add
 * button. Edits the Document Model, whether drag &amp; drop is allowed and, per tree column, the Document
 * Model field the column shows. Works on a copy of the node's column mappings and, on OK, builds a brand new
 * {@link TreeNode} carrying only those three edited values (see {@link #getResult()}) rather than mutating
 * the one passed to {@link #init}, mirroring {@link TreeColumnDialogController}; the caller copies them onto
 * the existing node (edit, so its actions, child relationship configurations, ... stay untouched) or gives
 * the new node an id and appends it (add). Picking a different Document Model keeps the chosen fields - the
 * combos just list the new model's fields - so a mapping to a field the new model lacks stays visible instead
 * of being silently dropped.
 */
public class TreeNodeDialogController implements DialogController {

  @FXML
  private ComboBox<String> documentModelField;

  @FXML
  private CheckBox dragDropField;

  @FXML
  private GridPane columnMappingGrid;

  @FXML
  private Label noColumnsLabel;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private TreeModel model;

  private ProjectItem projectItem;

  private TreeNode existing;

  private boolean initialDragDrop;

  // Working copy of the node's column mappings, edited by the mapping combos.
  private List<TreeNodeColumn> mappings = new ArrayList<>();

  private TreeNode built;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    okButton.disableProperty().bind(documentModelField.valueProperty().isNull());
    documentModelField.valueProperty().addListener((observable, oldValue, newValue) -> rebuildColumnMappingRows());
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    TreeNode node = new TreeNode();
    node.setDocumentModelRef(documentModelField.getValue());

    // A new node still gets a (SME-mandatory) configuration; an existing one keeps its other keys, and "dnd"
    // is only written when the user actually changed it, so an absent key stays absent.
    Map<String, Object> configuration = new LinkedHashMap<>();
    if (existing != null && existing.getConfiguration() != null) {
      configuration.putAll(existing.getConfiguration());
    }
    if (dragDropField.isSelected() != initialDragDrop) {
      configuration.put("dnd", dragDropField.isSelected());
    }
    node.setConfiguration(configuration);
    node.setColumns(mappings);

    built = node;
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  void init(Stage stage, TreeModel model, ProjectItem projectItem, TreeNode existing) {
    this.stage = stage;
    this.model = model;
    this.projectItem = projectItem;
    this.existing = existing;

    mappings = new ArrayList<>();
    if (existing != null) {
      existing.getColumns().forEach(mapping -> mappings.add(copyOf(mapping)));
    }
    initialDragDrop = existing != null && existing.getConfiguration() != null
        && Boolean.TRUE.equals(existing.getConfiguration().get("dnd"));
    dragDropField.setSelected(initialDragDrop);

    documentModelField.getItems().setAll(documentModelOptions());
    documentModelField.setValue(existing != null ? existing.getDocumentModelRef() : null);
    rebuildColumnMappingRows();
  }

  Optional<TreeNode> getResult() {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      return Optional.ofNullable(built);
    }
    return Optional.empty();
  }

  /** One row per tree column, mapping the column to a field of the selected Document Model. */
  private void rebuildColumnMappingRows() {
    columnMappingGrid.getChildren().clear();
    List<TreeColumn> columns = model.getContent().getColumns();
    noColumnsLabel.setVisible(columns.isEmpty());
    noColumnsLabel.setManaged(columns.isEmpty());

    List<String> fieldOptions = fieldOptionsFor(documentModelField.getValue());
    int row = 0;
    for (TreeColumn column : columns) {
      Label columnLabel = new Label(column.getName());
      columnLabel.getStyleClass().add("field-label");

      ComboBox<String> elementField = new ComboBox<>();
      elementField.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(elementField, Priority.ALWAYS);
      elementField.getItems().setAll(fieldOptions);
      elementField.setValue(mappedElementRef(column.getId()));
      elementField.valueProperty().addListener((observable, oldValue, newValue) -> setMappedElementRef(column.getId(), newValue));

      columnMappingGrid.addRow(row++, columnLabel, elementField);
    }
  }

  private String mappedElementRef(String columnId) {
    return mappings.stream()
        .filter(mapping -> columnId != null && columnId.equals(mapping.getColumnRef()))
        .map(TreeNodeColumn::getElementRef)
        .findFirst()
        .orElse(null);
  }

  private void setMappedElementRef(String columnId, String elementRef) {
    if (columnId == null) {
      return;
    }
    TreeNodeColumn mapping = mappings.stream()
        .filter(existingMapping -> columnId.equals(existingMapping.getColumnRef()))
        .findFirst()
        .orElse(null);
    if (mapping == null) {
      mapping = new TreeNodeColumn();
      mapping.setColumnRef(columnId);
      mappings.add(mapping);
    }
    mapping.setElementRef(elementRef);
  }

  /** Copies everything on the mapping, including its per-node display-mode override. */
  private static TreeNodeColumn copyOf(TreeNodeColumn source) {
    TreeNodeColumn copy = new TreeNodeColumn();
    copy.setColumnRef(source.getColumnRef());
    copy.setElementRef(source.getElementRef());
    if (source.getConfiguration() != null) {
      TreeNodeColumn.Configuration configuration = new TreeNodeColumn.Configuration();
      configuration.setAttachmentDisplayMode(source.getConfiguration().getAttachmentDisplayMode());
      configuration.setMultiSelectDisplayMode(source.getConfiguration().getMultiSelectDisplayMode());
      copy.setConfiguration(configuration);
    }
    return copy;
  }

  private List<String> documentModelOptions() {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.DOCUMENT).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
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

  private static List<String> collectFieldIds(DocumentModel documentModel) {
    List<String> ids = new ArrayList<>();
    if (documentModel.getContent() != null && documentModel.getContent().getModelRoot() != null
        && documentModel.getContent().getModelRoot().getRootGroups() != null) {
      for (GroupElement group : documentModel.getContent().getModelRoot().getRootGroups()) {
        collectFieldIds(group, ids);
      }
    }
    return ids;
  }

  private static void collectFieldIds(GroupElement group, List<String> ids) {
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
}
