package de.a12.studio.ui.editors.maindetailmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link MasterDetailModel}: whether it presents an {@link de.a12.studio.models.overviewmodel.OverviewModel}
 * or a {@link de.a12.studio.models.treemodel.TreeModel} as the "master" list (and which one), the preferred
 * detail form width, and a {@link FormMapping} per Document Model the chosen master model references
 * (mirroring SME's {@code formMappingMiddleware}) — one row lets the user assign which Form Model edits that
 * Document Model's records.
 */
public class MainDetailModelEditorController extends AbstractEditorController implements Initializable {

  @FXML
  private MainModelReferencePanelController masterModelReferenceController;

  @FXML
  private FormWidthPanelController formWidthPanelController;

  @FXML
  private MainDetailFormMappingPanelController mainDetailFormMappingPanelController;

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
   * or Tree, per {@code content.type}) references.
   */
  private void refreshFormMapping() {
    mainDetailFormMappingPanelController.load(model, projectItem, referencedDocumentModelIds());
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
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, masterModelType).stream()
        .filter(masterModel -> masterModelId.equals(masterModel.getId()))
        .findFirst()
        .map(masterModel -> masterModel.getModelReferences().stream()
            .filter(reference -> reference.getModelType() == ModelType.DOCUMENT && purpose.equals(reference.getPurpose()))
            .map(ModelReference::getReference)
            .toList())
        .orElse(List.of());
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
