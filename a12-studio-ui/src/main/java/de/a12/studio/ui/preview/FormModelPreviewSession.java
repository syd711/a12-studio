package de.a12.studio.ui.preview;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeBackend;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

/**
 * Previews a Form Model with the real Form Engine: the live Form Model, against its data-binding Document Model (or
 * the Document Model a bound Combination Model stands for) expanded by the {@link SmeBackend}.
 */
public final class FormModelPreviewSession extends FormEnginePreviewSession {

  private final ProjectItem formItem;

  private String cachedDocumentRevision;
  private String cachedDocumentModel;
  private String cachedValidationCode;

  public FormModelPreviewSession(@NonNull ProjectItem formItem) {
    this.formItem = formItem;
  }

  @Override
  public String getTitle() {
    return formItem.getModel().getId() + " - Form Model Preview";
  }

  @Override
  public synchronized Snapshot snapshot(@Nullable String knownFormRevision, @Nullable String knownDocumentRevision)
      throws PreviewAppException {
    FormModel formModel = (FormModel) formItem.getModel();
    String formJson = serialize(formModel);

    ExpansionInput input = expansionInput(dataBindingId(formModel), formItem, null);
    String documentRevision = input.revision();
    if (!documentRevision.equals(cachedDocumentRevision)) {
      SmeBackend backend = SmeBackend.getInstance();
      JsonNode expanded = input.expand(backend);
      cachedValidationCode = backend.generateValidationCode(expanded);
      cachedDocumentModel = expanded.toString();
      cachedDocumentRevision = documentRevision;
    }

    Rendering rendering = new Rendering(getTitle(), formJson, cachedDocumentModel, cachedValidationCode);
    return toSnapshot(rendering, revision(formJson), documentRevision, knownFormRevision, knownDocumentRevision);
  }

  private static String dataBindingId(FormModel formModel) throws PreviewAppException {
    return formModel.getModelReferences().stream()
        .filter(reference -> reference.getModelType() == ModelType.DOCUMENT
            && ModelReference.PURPOSE_DATA_BINDING.equals(reference.getPurpose()))
        .map(ModelReference::getReference)
        .findFirst()
        .orElseThrow(() -> new PreviewAppException("The Form Model has no Document Model selected as its data binding."));
  }
}
