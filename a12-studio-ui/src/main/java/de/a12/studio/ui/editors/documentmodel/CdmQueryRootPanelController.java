package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.composeddocumentmodel.CdmRelationshipStep;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModelResolver;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Edits a Document Model's Composed Document Model (CDM) annotations - {@code cdm.queryRoot} (the root
 * Document Model this CDM queries from) and its relationship chain ({@code cdm.relationship}/{@code
 * cdm.sourceRole}/{@code cdm.targetRole}/{@code cdm.targetDocumentModel}, one set per traversed relationship) -
 * following {@link de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController}'s pattern of filtering
 * a dedicated annotation family out of the raw {@link de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController}
 * and editing it with real widgets instead. Not bound to a single {@link de.a12.studio.models.documentmodel.Element}
 * (these annotations live on the model header), so only {@link #setModel} is used.
 * <p>
 * Setting a query root here turns a plain {@link DocumentModel} into a {@link
 * de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel} - but, like the {@code additive-document}
 * annotation, only on the model's *next* load (see {@link ComposedDocumentModelResolver}'s javadoc): this
 * panel edits the raw annotation list of whatever {@link A12Model} it was given, whatever its current runtime
 * type.
 */
public class CdmQueryRootPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> queryRootCombo;
  @FXML
  private GridPane stepsGrid;
  @FXML
  private Label emptyStepsLabel;

  private A12Model<?> model;
  private ProjectItem projectItem;
  private List<DocumentModel> otherDocumentModels = List.of();
  private List<RelationshipModel> allRelationshipModels = List.of();
  private List<CdmRelationshipStep> steps = new ArrayList<>();

  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    queryRootCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      ComposedDocumentModelResolver.setQueryRootId(model, newValue);
      commitHeaderChange();
    });
  }

  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull A12Model<?> model, @NonNull ProjectItem projectItem) {
    this.model = model;
    this.projectItem = projectItem;
    this.otherDocumentModels = ProjectDocumentModels.getOtherDocumentModels(projectItem);
    this.allRelationshipModels = ProjectDocumentModels.getRelationshipModelsConnectedTo(projectItem, null);
    this.steps = new ArrayList<>(ComposedDocumentModelResolver.getRelationshipSteps(model));

    updatingFromModel = true;
    try {
      queryRootCombo.getItems().setAll(candidateDocumentModelIds());
      queryRootCombo.setValue(ComposedDocumentModelResolver.getQueryRootId(model).orElse(null));
    }
    finally {
      updatingFromModel = false;
    }
    rebuildStepRows();
  }

  private List<String> candidateDocumentModelIds() {
    List<String> ids = otherDocumentModels.stream().map(DocumentModel::getId).collect(Collectors.toCollection(ArrayList::new));
    String current = ComposedDocumentModelResolver.getQueryRootId(model).orElse(null);
    if (current != null && !current.isBlank() && !ids.contains(current)) {
      ids.add(current);
    }
    ids.sort(Comparator.naturalOrder());
    return ids;
  }

  @FXML
  private void onAddStep() {
    steps.add(new CdmRelationshipStep(null, null, null, null));
    rebuildStepRows();
    persistSteps();
  }

  private void rebuildStepRows() {
    stepsGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    boolean empty = steps.isEmpty();
    emptyStepsLabel.setVisible(empty);
    emptyStepsLabel.setManaged(empty);
    stepsGrid.setVisible(!empty);
    stepsGrid.setManaged(!empty);

    for (int index = 0; index < steps.size(); index++) {
      addStepRow(steps.get(index), index);
    }
  }

  private void addStepRow(CdmRelationshipStep step, int index) {
    String contextDocumentModelId = contextDocumentModelId(index);

    ComboBox<String> relationshipCombo = new ComboBox<>();
    relationshipCombo.setMaxWidth(Double.MAX_VALUE);
    relationshipCombo.getItems().setAll(relevantRelationshipIds(contextDocumentModelId, step.getRelationshipName()));
    relationshipCombo.setValue(step.getRelationshipName());

    ComboBox<String> sourceRoleCombo = new ComboBox<>();
    sourceRoleCombo.setMaxWidth(Double.MAX_VALUE);
    ComboBox<String> targetRoleCombo = new ComboBox<>();
    targetRoleCombo.setMaxWidth(Double.MAX_VALUE);
    populateRoleOptions(step.getRelationshipName(), sourceRoleCombo, step.getSourceRole());
    populateRoleOptions(step.getRelationshipName(), targetRoleCombo, step.getTargetRole());

    relationshipCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      step.setRelationshipName(newValue);
      populateRoleOptions(newValue, sourceRoleCombo, null);
      populateRoleOptions(newValue, targetRoleCombo, null);
      step.setSourceRole(null);
      applyStep(step);
    });
    sourceRoleCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      step.setSourceRole(newValue);
      applyStep(step);
    });
    targetRoleCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      step.setTargetRole(newValue);
      applyStep(step);
    });

    stepsGrid.addRow(index + 1, relationshipCombo, sourceRoleCombo, targetRoleCombo, createActionsBox(step, index));
  }

  /**
   * Recomputes {@code step.targetDocumentModel} from the relationship/target-role currently selected, then
   * persists every step - a role selection always changes which Document Model a step resolves to, so the
   * whole chain's downstream "connected to" context (see {@link #contextDocumentModelId}) may shift too;
   * simplest to just rebuild.
   */
  private void applyStep(CdmRelationshipStep step) {
    step.setTargetDocumentModel(findRelationship(step.getRelationshipName())
        .flatMap(relationship -> findCharacteristic(relationship, step.getTargetRole()))
        .map(EntityCharacteristic::getDocumentModel)
        .orElse(null));
    persistSteps();
    rebuildStepRows();
  }

  private void persistSteps() {
    ComposedDocumentModelResolver.setRelationshipSteps(model, steps);
    commitHeaderChange();
  }

  /**
   * The Document Model id step {@code index} is traversed *from*: the query root for the first step, or the
   * previous step's resolved target Document Model otherwise.
   */
  private String contextDocumentModelId(int index) {
    if (index == 0) {
      return ComposedDocumentModelResolver.getQueryRootId(model).orElse(null);
    }
    return steps.get(index - 1).getTargetDocumentModel();
  }

  private List<String> relevantRelationshipIds(String contextDocumentModelId, String currentRelationshipName) {
    List<RelationshipModel> candidates = contextDocumentModelId == null
        ? allRelationshipModels
        : ProjectDocumentModels.getRelationshipModelsConnectedTo(projectItem, contextDocumentModelId);
    List<String> ids = candidates.stream().map(RelationshipModel::getId).collect(Collectors.toCollection(ArrayList::new));
    if (currentRelationshipName != null && !currentRelationshipName.isBlank() && !ids.contains(currentRelationshipName)) {
      ids.add(currentRelationshipName);
    }
    ids.sort(Comparator.naturalOrder());
    return ids;
  }

  private void populateRoleOptions(String relationshipName, ComboBox<String> combo, String valueToSelect) {
    List<String> roles = findRelationship(relationshipName)
        .map(relationship -> relationship.getContent().getEntityCharacteristics().stream()
            .map(EntityCharacteristic::getRole)
            .toList())
        .orElse(List.of());
    combo.getItems().setAll(roles);
    combo.setValue(valueToSelect);
  }

  private Optional<RelationshipModel> findRelationship(String id) {
    return allRelationshipModels.stream().filter(relationship -> relationship.getId().equals(id)).findFirst();
  }

  private Optional<EntityCharacteristic> findCharacteristic(RelationshipModel relationship, String role) {
    if (role == null) {
      return Optional.empty();
    }
    return relationship.getContent().getEntityCharacteristics().stream()
        .filter(characteristic -> role.equals(characteristic.getRole()))
        .findFirst();
  }

  private HBox createActionsBox(CdmRelationshipStep step, int index) {
    var moveButtonsBox = RowFactory.createMoveButtonsBox(index, steps.size(), (fromIndex, toIndex) -> {
      Collections.swap(steps, fromIndex, toIndex);
      persistSteps();
      rebuildStepRows();
    });

    var deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("cdm_query_root_panel.delete_step"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        steps.remove(step);
        persistSteps();
        rebuildStepRows();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }
}
