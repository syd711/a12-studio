package de.a12.studio.ui.editors.maindetailmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Edits a {@link MasterDetailModel}: whether it presents an {@link de.a12.studio.models.overviewmodel.OverviewModel}
 * or a {@link de.a12.studio.models.treemodel.TreeModel} as the "master" list (and which one), the preferred
 * detail form width, and a {@link FormMapping} per Document Model the chosen master model references
 * (mirroring SME's {@code formMappingMiddleware}) — one row lets the user assign which Form Model edits that
 * Document Model's records. For a Tree-type master model, also maintains two further tree-only mappings
 * (mirroring SME's {@code syncRelationshipEditors}/{@code syncLinkDocumentEditors}): {@code
 * relationshipEditors} (one row per Document Model referenced by a tree node carrying an {@code
 * event_add_link} action) and {@code linkDocumentEditors} (one row per link Document Model declared by a
 * Relationship Model the tree model references).
 */
public class MainDetailModelEditorController extends AbstractEditorController implements Initializable {

  private static final String EVENT_ADD_LINK = "event_add_link";

  @FXML
  private MainModelReferencePanelController masterModelReferenceController;

  @FXML
  private FormWidthPanelController formWidthPanelController;

  @FXML
  private MainDetailFormMappingPanelController mainDetailFormMappingPanelController;

  @FXML
  private RelationshipEditorsPanelController relationshipEditorsPanelController;

  @FXML
  private LinkDocumentEditorsPanelController linkDocumentEditorsPanelController;

  private MasterDetailModel model;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    masterModelReferenceController.setOnChange(() -> {
      refreshFormMapping();
      commitChange();
    });
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((MasterDetailModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull MasterDetailModel model) {
    this.model = model;

    masterModelReferenceController.load(model, overviewModelOptions(), treeModelOptions());

    formWidthPanelController.setModel(model);
    refreshFormMapping();
  }

  /**
   * Reloads this editor whenever a Document Model is saved in a different tab, so the Form Mapping panel's
   * Document Model list (see {@link #refreshFormMapping}) doesn't go stale while this tab stays open.
   */
  @Override
  protected void onDocumentModelChangedElsewhere() {
    load(model);
  }

  private List<String> overviewModelOptions() {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.OVERVIEW).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  private List<String> treeModelOptions() {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.TREE).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  /**
   * Refreshes the Form Mapping panel with the Document Models the currently selected master model (Overview
   * or Tree) references, then, for a Tree-type master model, refreshes the two tree-only mapping panels and
   * shows them; for an Overview-type master model, hides those two panels and clears their content so they
   * don't linger in the saved file.
   */
  private void refreshFormMapping() {
    mainDetailFormMappingPanelController.load(model, projectItem, referencedDocumentModelIds());

    boolean treeMode = "tree".equals(model.getContent().getType());
    relationshipEditorsPanelController.setVisible(treeMode);
    linkDocumentEditorsPanelController.setVisible(treeMode);
    if (treeMode) {
      TreeModel treeModel = findTypedModel(model.getContent().getTreeModel(), ModelType.TREE, TreeModel.class).orElse(null);
      relationshipEditorsPanelController.load(model, projectItem, relationshipEditorDocumentModelIds(treeModel));
      linkDocumentEditorsPanelController.load(model, projectItem, linkDocumentEditorDocumentModelIds(treeModel));
    }
    else {
      model.getContent().setRelationshipEditors(null);
      model.getContent().setLinkDocumentEditors(null);
    }
  }

  /**
   * The Document Model ids referenced (via {@link ModelReference#PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW} or
   * {@link ModelReference#PURPOSE_DOCUMENT_MODEL_FOR_TREE}) by whichever master model is currently selected.
   */
  private List<String> referencedDocumentModelIds() {
    if ("tree".equals(model.getContent().getType())) {
      return referencedDocumentModelIds(model.getContent().getTreeModel(), ModelType.TREE, ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_TREE);
    }
    return referencedDocumentModelIds(model.getContent().getOverviewModel(), ModelType.OVERVIEW, ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW);
  }

  private List<String> referencedDocumentModelIds(String masterModelId, ModelType masterModelType, String purpose) {
    if (masterModelId == null) {
      return List.of();
    }
    return findModel(masterModelId, masterModelType)
        .map(masterModel -> masterModel.getModelReferences().stream()
            .filter(reference -> reference.getModelType() == ModelType.DOCUMENT && purpose.equals(reference.getPurpose()))
            .map(ModelReference::getReference)
            .toList())
        .orElse(List.of());
  }

  /**
   * The Document Model ids referenced by every node of {@code treeModel} that carries an {@code
   * event_add_link} action (SME: {@code TreeAPI.getNodesWithAddLinkAction}), i.e. the nodes a "child" can be
   * linked into, each of which needs a Form Model containing Relationship Binding views.
   */
  private List<String> relationshipEditorDocumentModelIds(TreeModel treeModel) {
    if (treeModel == null) {
      return List.of();
    }
    return treeModel.getContent().getNodes().stream()
        .filter(node -> node.getActions().stream().map(TreeNodeAction::getEvent).anyMatch(EVENT_ADD_LINK::equals))
        .map(TreeNode::getDocumentModelRef)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
  }

  /**
   * The link Document Model ids declared by every Relationship Model {@code treeModel} references (SME: {@code
   * syncLinkDocumentEditors}), i.e. the "Additional Link Fields" that need a Form Model to edit them.
   */
  private List<String> linkDocumentEditorDocumentModelIds(TreeModel treeModel) {
    if (treeModel == null) {
      return List.of();
    }
    return treeModel.getModelReferences().stream()
        .filter(reference -> reference.getModelType() == ModelType.RELATIONSHIP)
        .map(ModelReference::getReference)
        .distinct()
        .map(id -> findTypedModel(id, ModelType.RELATIONSHIP, RelationshipModel.class))
        .flatMap(Optional::stream)
        .map(relationshipModel -> relationshipModel.getContent().getLinkDocumentModelValue())
        .filter(Objects::nonNull)
        .distinct()
        .toList();
  }

  private Optional<A12Model<?>> findModel(String id, ModelType modelType) {
    if (id == null) {
      return Optional.empty();
    }
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, modelType).stream()
        .filter(otherModel -> id.equals(otherModel.getId()))
        .findFirst();
  }

  private <T extends A12Model<?>> Optional<T> findTypedModel(String id, ModelType modelType, Class<T> type) {
    return findModel(id, modelType).filter(type::isInstance).map(type::cast);
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.MASTERDETAIL;
  }
}
