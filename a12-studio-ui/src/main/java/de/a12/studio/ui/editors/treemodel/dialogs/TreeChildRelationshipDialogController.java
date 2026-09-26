package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModelHeterogeneity;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.treemodel.TreeChildRelationshipConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeColumn;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Add/edit dialog for one {@link TreeChildRelationshipConfiguration} of a node type, opened from {@link
 * de.a12.studio.ui.editors.treemodel.TreeChildRelationshipsPanelController}: the relationship model that yields
 * the node's children, the role the node itself plays in it (Parent Role) and - if the relationship has a link
 * Document Model - which of its fields each tree column shows on the child nodes. Relationship model and Parent
 * Role are written to the configuration it is given (a working copy, see {@link
 * Dialogs#showChildRelationshipForEdit}) as they change; the column mapping is edited on a separate list and only
 * written back on OK, so a configuration that had no {@code columns} key keeps having none unless something is mapped.
 */
public class TreeChildRelationshipDialogController implements DialogController {

  @FXML
  private ComboBox<String> relationshipModelField;

  @FXML
  private ComboBox<String> parentRoleField;

  @FXML
  private VBox columnMappingBox;

  @FXML
  private GridPane columnMappingGrid;

  @FXML
  private Label noColumnsLabel;

  @FXML
  private Button okButton;

  private Stage stage;

  private TreeModel model;

  private ProjectItem projectItem;

  private String nodeDocumentModelId;

  private TreeChildRelationshipConfiguration configuration;

  // Working copy of the column mapping, edited by the mapping combos and written back on OK.
  private List<TreeNodeColumn> mappings = new ArrayList<>();

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  // Set while the combos are being repopulated, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @FXML
  private void initialize() {
    okButton.disableProperty().bind(relationshipModelField.valueProperty().isNull().or(parentRoleField.valueProperty().isNull()));
    relationshipModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      configuration.setRelationshipModelRef(newValue);
      mappings.clear();
      refreshRelationshipDependents(true);
    });
    parentRoleField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        configuration.setParentRole(newValue);
      }
    });
  }

  void init(Stage stage, TreeModel model, ProjectItem projectItem, TreeNode node, TreeChildRelationshipConfiguration configuration) {
    this.stage = stage;
    this.model = model;
    this.projectItem = projectItem;
    this.nodeDocumentModelId = node.getDocumentModelRef();
    this.configuration = configuration;

    mappings = new ArrayList<>();
    if (configuration.getColumns() != null) {
      configuration.getColumns().forEach(mapping -> mappings.add(ColumnMappingEditor.copyOf(mapping)));
    }

    updatingFromModel = true;
    try {
      List<String> relationships = new ArrayList<>(ProjectDocumentModels.getRelationshipModelsConnectedTo(projectItem, nodeDocumentModelId).stream()
          .map(A12Model::getId)
          .toList());
      // A reference that no longer resolves stays selectable, so opening the dialog never silently changes it.
      if (configuration.getRelationshipModelRef() != null && !relationships.contains(configuration.getRelationshipModelRef())) {
        relationships.add(configuration.getRelationshipModelRef());
      }
      relationshipModelField.getItems().setAll(relationships);
      relationshipModelField.setValue(configuration.getRelationshipModelRef());
    }
    finally {
      updatingFromModel = false;
    }
    refreshRelationshipDependents(false);
  }

  /**
   * Re-reads what depends on the chosen relationship: the roles offered for Parent Role (all roles of the
   * relationship) and the link Document Model's fields for the column mapping. {@code relationshipChanged}
   * replaces a Parent Role that belonged to the previous relationship with the role the node's own Document Model
   * plays, when that is unambiguous.
   */
  private void refreshRelationshipDependents(boolean relationshipChanged) {
    RelationshipModel relationship = findRelationship(configuration.getRelationshipModelRef());

    updatingFromModel = true;
    try {
      List<String> roles = rolesOf(relationship);
      String parentRole = relationshipChanged ? defaultParentRole(relationship, roles) : configuration.getParentRole();
      // A role that no longer exists stays selectable, for the same reason as an unresolved relationship above.
      if (parentRole != null && !roles.contains(parentRole)) {
        roles.add(parentRole);
      }
      parentRoleField.getItems().setAll(roles);
      parentRoleField.setValue(parentRole);
      configuration.setParentRole(parentRole);
    }
    finally {
      updatingFromModel = false;
    }

    String linkDocumentModel = relationship != null && relationship.getContent() != null
        ? relationship.getContent().getLinkDocumentModelValue() : null;
    // Nothing to map without a link Document Model, unless an old mapping exists that should stay visible.
    boolean mappable = linkDocumentModel != null || !mappings.isEmpty();
    columnMappingBox.setVisible(mappable);
    columnMappingBox.setManaged(mappable);
    if (mappable) {
      ColumnMappingEditor.populate(columnMappingGrid, noColumnsLabel, model.getContent().getColumns(),
          ColumnMappingEditor.fieldOptionsFor(projectItem, linkDocumentModel),
          ColumnMappingEditor.elementIndexFor(projectItem, linkDocumentModel), mappings, () -> {
          });
    }
  }

  private RelationshipModel findRelationship(String id) {
    if (id == null) {
      return null;
    }
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.RELATIONSHIP).stream()
        .filter(candidate -> id.equals(candidate.getId()))
        .map(RelationshipModel.class::cast)
        .findFirst()
        .orElse(null);
  }

  private static List<String> rolesOf(RelationshipModel relationship) {
    List<String> roles = new ArrayList<>();
    if (relationship != null && relationship.getContent() != null) {
      for (EntityCharacteristic entity : relationship.getContent().getEntityCharacteristics()) {
        if (entity.getRole() != null && !roles.contains(entity.getRole())) {
          roles.add(entity.getRole());
        }
      }
    }
    return roles;
  }

  /** The role played by the node's Document Model (or one of its super types), if that is unambiguous. */
  private String defaultParentRole(RelationshipModel relationship, List<String> roles) {
    if (relationship == null || relationship.getContent() == null || nodeDocumentModelId == null) {
      return null;
    }
    List<String> acceptable = new ArrayList<>(List.of(nodeDocumentModelId));
    acceptable.addAll(DocumentModelHeterogeneity.reachableSuperTypes(ProjectDocumentModels.getOtherDocumentModels(projectItem), nodeDocumentModelId));
    List<String> matching = relationship.getContent().getEntityCharacteristics().stream()
        .filter(entity -> entity.getRole() != null && acceptable.contains(entity.getDocumentModel()))
        .map(EntityCharacteristic::getRole)
        .distinct()
        .toList();
    return matching.size() == 1 && roles.contains(matching.get(0)) ? matching.get(0) : null;
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    mappings.removeIf(mapping -> mapping.getElementRef() == null);
    boolean hadColumnsKey = configuration.getColumns() != null;
    configuration.setColumns(mappings.isEmpty() && !hadColumnsKey ? null : mappings);
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  Optional<TreeChildRelationshipConfiguration> getResult() {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      return Optional.of(configuration);
    }
    return Optional.empty();
  }
}
