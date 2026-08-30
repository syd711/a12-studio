package de.a12.studio.ui.editors.mappingmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.mappingmodel.MappingSource;
import de.a12.studio.models.mappingmodel.MappingTarget;
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
 * Edits a {@link MappingModel}: currently its Source models and Target, the {@link DocumentModel} the mapping
 * writes to. More fields (PreComputationFragment, StructuralMappingModel) are added later.
 */
public class MappingModelEditorController extends AbstractEditorController implements Initializable {

  @FXML
  private SourceModelsPanelController sourceModelsPanelController;

  @FXML
  private TargetModelPanelController targetModelPanelController;

  private MappingModel model;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    targetModelPanelController.setOnChange(this::onTargetModelChanged);
    sourceModelsPanelController.setOnChange(this::onSourceModelsChanged);
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((MappingModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull MappingModel model) {
    this.model = model;
    sourceModelsPanelController.setModel(model);
    targetModelPanelController.load(documentModelOptions(), currentTargetDmId());
  }

  /**
   * Refreshes the Source/Target Document Model pickers whenever a Document Model is saved in a different tab
   * (added, renamed, or removed elsewhere), so the option lists don't go stale while this tab stays open.
   */
  @Override
  protected void onDocumentModelChangedElsewhere() {
    load(model);
  }

  private List<DocumentModel> documentModelOptions() {
    return ProjectDocumentModels.getOtherDocumentModels(projectItem);
  }

  private String currentTargetDmId() {
    MappingTarget target = model.getContent().getTarget();
    return target != null ? target.getDmId() : null;
  }

  private void onTargetModelChanged() {
    applyTarget(targetModelPanelController.getValue());
    syncModelReferences();
    commitChange();
    updateSettingsErrorBadge();
  }

  /**
   * Invoked after every add/remove/reorder/edit in {@link #sourceModelsPanelController} that may have changed
   * a Source's dmId (see {@link SourceModelsPanelController#setOnChange}).
   */
  private void onSourceModelsChanged() {
    syncModelReferences();
    commitChange();
    updateSettingsErrorBadge();
  }

  private void applyTarget(String dmId) {
    MappingTarget target = model.getContent().getTarget();
    if (target == null) {
      target = new MappingTarget();
      model.getContent().setTarget(target);
    }
    target.setDmId(dmId);
  }

  /**
   * Rebuilds the header's Document Model references to match every dmId currently referenced from content
   * (the Target and every Source), so a shared Document Model stays referenced as long as any of them still
   * points at it, and one no longer used by either drops out. Replaces the whole DOCUMENT-type subset rather
   * than patching it incrementally, since with multiple Sources (and the Target) potentially sharing a dmId,
   * an incremental add/remove can't tell "no longer used by the field that just changed" apart from "no longer
   * used at all".
   */
  private void syncModelReferences() {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(reference -> reference.getModelType() == ModelType.DOCUMENT);
    for (String dmId : currentDmIds()) {
      ModelReference reference = new ModelReference();
      reference.setModelType(ModelType.DOCUMENT);
      reference.setReference(dmId);
      references.add(reference);
    }
  }

  private List<String> currentDmIds() {
    List<String> dmIds = new ArrayList<>();
    String targetDmId = currentTargetDmId();
    if (targetDmId != null) {
      dmIds.add(targetDmId);
    }
    for (MappingSource source : model.getContent().getSource()) {
      if (source.getDmId() != null && !dmIds.contains(source.getDmId())) {
        dmIds.add(source.getDmId());
      }
    }
    return dmIds;
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.MAPPING;
  }
}
