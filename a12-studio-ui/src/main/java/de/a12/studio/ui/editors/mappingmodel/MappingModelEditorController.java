package de.a12.studio.ui.editors.mappingmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.mappingmodel.MappingTarget;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.propertyeditors.TargetModelPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link MappingModel}: currently just its Target, the {@link DocumentModel} the mapping writes to.
 * More fields (Source, PreComputationFragment, StructuralMappingModel) are added later.
 */
public class MappingModelEditorController extends AbstractEditorController implements Initializable {

  @FXML
  private TargetModelPanelController targetModelPanelController;

  private MappingModel model;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    targetModelPanelController.setOnChange(this::onTargetModelChanged);
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((MappingModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull MappingModel model) {
    this.model = model;
    targetModelPanelController.load(documentModelOptions(), currentTargetDmId());
  }

  private List<DocumentModel> documentModelOptions() {
    return ProjectDocumentModels.getOtherDocumentModels(projectItem);
  }

  private String currentTargetDmId() {
    MappingTarget target = model.getContent().getTarget();
    return target != null ? target.getDmId() : null;
  }

  private void onTargetModelChanged() {
    String selectedId = targetModelPanelController.getValue();
    syncTargetReference(selectedId);
    applyTarget(selectedId);
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
   * Rebuilds the header's Document Model reference for the Target: drops the reference to the previously
   * selected Document Model (if any) and adds one for the newly selected Document Model (unless it's already
   * referenced, e.g. by a future Source entry pointing at the same model).
   */
  private void syncTargetReference(String newDmId) {
    List<ModelReference> references = model.getModelReferences();
    String previousDmId = currentTargetDmId();
    if (previousDmId != null && !previousDmId.equals(newDmId)) {
      references.removeIf(reference -> reference.getModelType() == ModelType.DOCUMENT && previousDmId.equals(reference.getReference()));
    }
    if (newDmId != null && references.stream().noneMatch(reference -> reference.getModelType() == ModelType.DOCUMENT && newDmId.equals(reference.getReference()))) {
      ModelReference reference = new ModelReference();
      reference.setModelType(ModelType.DOCUMENT);
      reference.setReference(newDmId);
      references.add(reference);
    }
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
