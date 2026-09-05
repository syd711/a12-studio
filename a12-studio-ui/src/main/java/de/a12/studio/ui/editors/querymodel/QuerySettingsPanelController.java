package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.ui.editors.propertyeditors.TargetModelPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Tab ("Settings") of the Query Model editor: the query's target {@link DocumentModel}, edited via the shared
 * {@link TargetModelPanelController} (not wired through {@code AbstractPropertyEditor} - same reasoning as {@link
 * de.a12.studio.ui.editors.mappingmodel.MappingModelEditorController}, which this class's save/reference-sync
 * responsibility mirrors). Before this tab existed, there was no UI path at all to set or change {@code
 * content.targetDocumentModel} - only hand-editing the JSON file.
 *
 * <p>{@code content.projectionName} isn't exposed here: every real Query Model file uses the constant
 * {@code "document"} (SME's own Query Model treats it as a read-only field for the same reason - see
 * docs/sme-reference-comparison.md "Query Model" section), so it's defaulted automatically instead of inventing
 * an editable field for something that isn't meant to vary.
 */
public class QuerySettingsPanelController {

  @FXML
  private TargetModelPanelController targetModelPanelController;

  private ProjectItem projectItem;
  private QueryModel model;

  private Runnable onTargetModelChanged = () -> {
  };

  @FXML
  private void initialize() {
    targetModelPanelController.setOnChange(this::handleTargetModelSelectionChanged);
  }

  /** Invoked after the target Document Model changes and has been saved, so the owning editor can reload the
   * Model Tree tab, which mirrors that target. */
  public void setOnTargetModelChanged(@NonNull Runnable onTargetModelChanged) {
    this.onTargetModelChanged = onTargetModelChanged;
  }

  public void load(@NonNull ProjectItem projectItem, @NonNull QueryModel model) {
    this.projectItem = projectItem;
    this.model = model;
    targetModelPanelController.load(documentModelOptions(), model.getContent().getTargetDocumentModel());
  }

  private List<DocumentModel> documentModelOptions() {
    return ProjectDocumentModels.getOtherDocumentModels(projectItem);
  }

  private void handleTargetModelSelectionChanged() {
    String targetId = targetModelPanelController.getValue();
    model.getContent().setTargetDocumentModel(targetId);
    if (model.getContent().getProjectionName() == null || model.getContent().getProjectionName().isBlank()) {
      model.getContent().setProjectionName("document");
    }
    syncModelReferences(targetId);

    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
    onTargetModelChanged.run();
  }

  /** Mirrors {@code MappingModelEditorController#syncModelReferences}: rebuilds the header's DOCUMENT-type
   * references to match the current target, replacing rather than patching since there's at most one. */
  private void syncModelReferences(String targetId) {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(reference -> reference.getModelType() == ModelType.DOCUMENT);
    if (targetId != null) {
      ModelReference reference = new ModelReference();
      reference.setModelType(ModelType.DOCUMENT);
      reference.setReference(targetId);
      references.add(reference);
    }
  }
}
