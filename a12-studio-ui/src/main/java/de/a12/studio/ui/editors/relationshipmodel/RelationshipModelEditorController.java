package de.a12.studio.ui.editors.relationshipmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.relationshipmodel.dialogs.Dialogs;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    relatedEntitiesController.setOnChange(this::onEntitiesChanged);
  }

  /**
   * Entity edits can change a role's document model (kept in sync via {@link #syncModelReferences}) and/or an
   * entity's multiplicity, which the Link Document Model panel's many-to-many warning depends on - so it must
   * be re-checked here too, not just when that panel's own fields change.
   */
  private void onEntitiesChanged() {
    syncModelReferences();
    linkDocumentModelController.refreshValidation();
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

  /**
   * Generates a Document Model per entity into a user-chosen folder, mirroring SME's relationship model
   * "Generate Document Models" action (see {@link RelationshipDocumentModelGenerator}). Any Document Model
   * previously generated for this relationship - found project-wide by id, regardless of which folder it was
   * generated into - is deleted and regenerated after the user confirms.
   */
  @FXML
  private void onGenerateDocumentModels(ActionEvent event) {
    Optional<ProjectItem> folder = Dialogs.showGenerateDocumentModelsFolder(Studio.stage, projectItem.getParent());
    if (folder.isEmpty()) {
      return;
    }

    try {
      RelationshipDocumentModelGenerator.validate(model, projectItem);

      List<ProjectItem> existing = RelationshipDocumentModelGenerator.findExistingGeneratedModels(model.getId(), projectItem);
      if (!existing.isEmpty()) {
        String existingNames = existing.stream().map(ProjectItem::getName).reduce((a, b) -> a + "\n" + b).orElse("");
        Optional<ButtonType> result = WidgetFactory.showYesNoConfirmation(Studio.stage,
            StudioBundle.get("generate_document_models_overwrite_confirm"), existingNames);
        if (result.isEmpty() || result.get() != ButtonType.YES) {
          return;
        }
        RelationshipDocumentModelGenerator.deleteAll(existing);
      }

      RelationshipDocumentModelGenerator.generate(model, projectItem, folder.get());
    }
    catch (IllegalStateException | IOException e) {
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
    }
  }
}
