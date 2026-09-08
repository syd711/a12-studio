package de.a12.studio.ui.editors.combineddocumentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.propertyeditors.TargetModelPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link CombinedDocumentModel}: its Base Model and its ordered Combination Steps, each an Addition,
 * Selection or Decoration referencing an Additive/Selection/Decoration model. Real semantic validation (DM
 * expansion, rule-contradiction solving, "Validate model up to this step") has no backing implementation in
 * a12-studio yet - see {@code docs/sme-reference-comparison.md} - so this editor only wires up structural
 * validation (see {@code de.a12.studio.modelsvalidation.validators.combination}).
 */
public class CombinedDocumentModelEditorController extends AbstractEditorController implements Initializable {

  @FXML
  private TargetModelPanelController baseModelPanelController;

  @FXML
  private CombinationStepsPanelController combinationStepsPanelController;

  private CombinedDocumentModel model;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    // Unlike Mapping's Target/Query's target document model, a Combined Document Model's Base Model is
    // optional - SME has no "missing" rule for it, only an invalid-reference-if-filled one.
    baseModelPanelController.setRequired(false);
    baseModelPanelController.setOnChange(this::onBaseModelChanged);
    combinationStepsPanelController.setOnChange(this::onCombinationStepsChanged);
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((CombinedDocumentModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull CombinedDocumentModel model) {
    this.model = model;
    baseModelPanelController.load(documentModelOptions(), model.getContent().getBaseModelId());
    combinationStepsPanelController.setModel(model);
  }

  /**
   * Refreshes the Base Model picker and the Combination Steps' validation state whenever a Document Model is
   * saved in a different tab (added, renamed, or removed elsewhere), so neither goes stale while this tab
   * stays open - same reasoning as {@code MappingModelEditorController#onDocumentModelChangedElsewhere}.
   */
  @Override
  protected void onDocumentModelChangedElsewhere() {
    load(model);
  }

  private List<DocumentModel> documentModelOptions() {
    return ProjectDocumentModels.getOtherDocumentModels(projectItem);
  }

  private void onBaseModelChanged() {
    model.getContent().setBaseModelId(baseModelPanelController.getValue());
    syncModelReferences();
    commitChange();
    updateSettingsErrorBadge();
  }

  /**
   * Invoked after every add/remove/reorder/edit in {@link #combinationStepsPanelController} that may have
   * changed a step's referenced models (see {@link CombinationStepsPanelController#setOnChange}).
   */
  private void onCombinationStepsChanged() {
    syncModelReferences();
    commitChange();
    updateSettingsErrorBadge();
  }

  /**
   * Rebuilds the header's model references to match every id currently referenced from content (the Base
   * Model and every step's Additive/Decoration/Selection model), so a shared model stays referenced as long
   * as anything still points at it, and one no longer used drops out. Replaces the whole
   * DOCUMENT/SELECTION-type subset rather than patching it incrementally - same reasoning, and same untagged
   * (no alias/purpose) shape, as {@code MappingModelEditorController#syncModelReferences}: several steps can
   * share a dmId/smId, so an incremental add/remove can't tell "no longer used by the field that just
   * changed" apart from "no longer used at all".
   */
  private void syncModelReferences() {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(reference -> reference.getModelType() == ModelType.DOCUMENT || reference.getModelType() == ModelType.SELECTION);
    for (String dmId : currentDocumentModelIds()) {
      references.add(reference(ModelType.DOCUMENT, dmId));
    }
    for (String smId : currentSelectionModelIds()) {
      references.add(reference(ModelType.SELECTION, smId));
    }
  }

  private static ModelReference reference(ModelType modelType, String referenceId) {
    ModelReference reference = new ModelReference();
    reference.setModelType(modelType);
    reference.setReference(referenceId);
    return reference;
  }

  private List<String> currentDocumentModelIds() {
    List<String> dmIds = new ArrayList<>();
    addIfAbsent(dmIds, model.getContent().getBaseModelId());
    for (CombinationStep step : model.getContent().getCombinationSteps()) {
      if (step.getAdditiveModel() != null) {
        addIfAbsent(dmIds, step.getAdditiveModel().getDmId());
      }
      if (step.getDecorationModel() != null) {
        addIfAbsent(dmIds, step.getDecorationModel().getDmId());
      }
    }
    return dmIds;
  }

  private List<String> currentSelectionModelIds() {
    List<String> smIds = new ArrayList<>();
    for (CombinationStep step : model.getContent().getCombinationSteps()) {
      if (step.getSelectionModel() != null) {
        addIfAbsent(smIds, step.getSelectionModel().getSmId());
      }
    }
    return smIds;
  }

  private static void addIfAbsent(List<String> ids, String id) {
    if (id != null && !id.isBlank() && !ids.contains(id)) {
      ids.add(id);
    }
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.COMBINATION;
  }
}
