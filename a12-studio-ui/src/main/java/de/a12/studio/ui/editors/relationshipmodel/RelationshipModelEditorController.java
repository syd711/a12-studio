package de.a12.studio.ui.editors.relationshipmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link RelationshipModel}: the related entities themselves - delegated to {@link
 * RelatedEntitiesPanelController}, mirroring SME's editor (a "Related Entities" list of rows rather than a
 * fixed two-entity form, with the full entity editor shown in a dialog per row) - and the optional Link
 * Document Model / "Duplicates Allowed" flag, delegated to {@link LinkDocumentModelPanelController}. The
 * header's {@code modelReferences} are kept in sync with the entities' Document Models on every change.
 */
public class RelationshipModelEditorController extends AbstractEditorController implements Initializable {

  @FXML
  private RelatedEntitiesPanelController relatedEntitiesController;

  @FXML
  private LinkDocumentModelPanelController linkDocumentModelController;

  private RelationshipModel model;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    relatedEntitiesController.setOnChange(this::syncModelReferences);
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((RelationshipModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull RelationshipModel model) {
    this.model = model;

    List<String> documentModelOptions = entityDocumentModelOptions();
    relatedEntitiesController.setDocumentModelOptions(documentModelOptions);
    relatedEntitiesController.setModel(model);
    linkDocumentModelController.setModel(model, documentModelOptions);
  }

  /**
   * Refreshes the entities' Document Model option lists whenever a Document Model is saved in a different
   * tab (added, renamed, or removed elsewhere), so they don't go stale while this tab stays open.
   */
  @Override
  protected void onDocumentModelChangedElsewhere() {
    load(model);
  }

  private List<String> entityDocumentModelOptions() {
    List<String> options = new ArrayList<>();
    ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.DOCUMENT).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .forEach(options::add);
    return options;
  }

  /**
   * Rebuilds the header's Document Model references from the entities (one reference per entity, alias = role),
   * the shape SME writes for relationship models (see PersonCompany.json).
   */
  private void syncModelReferences() {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(reference -> ModelReference.PURPOSE_DOCUMENT_MODEL.equals(reference.getPurpose()));
    for (EntityCharacteristic entity : model.getContent().getEntityCharacteristics()) {
      if (entity.getDocumentModel() == null || entity.getDocumentModel().isBlank()) {
        continue;
      }
      ModelReference reference = new ModelReference();
      reference.setPurpose(ModelReference.PURPOSE_DOCUMENT_MODEL);
      reference.setModelType(ModelType.DOCUMENT);
      reference.setAlias(entity.getRole());
      reference.setReference(entity.getDocumentModel());
      references.add(reference);
    }
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.RELATIONSHIP;
  }
}
