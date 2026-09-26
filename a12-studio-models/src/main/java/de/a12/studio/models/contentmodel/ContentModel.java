package de.a12.studio.models.contentmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ContentModel extends A12Model<ContentModelContent> {

  /** The alias SME gives the Document Model reference it writes ({@code ExportTransformations}). */
  public static final String DOCUMENT_MODEL_ALIAS = "DM";

  /**
   * The id of the Document Model the model is bound to. Like SME's import ({@code
   * ImportTransformations.transformTechnicalFields}) this is the first header reference to a Document Model,
   * whatever its purpose - older or hand-written files do not all use {@link
   * ModelReference#PURPOSE_DOCUMENT_MODEL_FOR_CONTENT_MODEL}.
   */
  @JsonIgnore
  public @Nullable String getDocumentModelId() {
    return getModelReferences().stream()
        .filter(ContentModel::isDocumentReference)
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
  }

  /**
   * Binds the model to a Document Model, or unbinds it with {@code null} / a blank id, in the shape SME's export
   * ({@code ExportTransformations}) writes: exactly one Document Model reference, first in the list, with purpose
   * {@link ModelReference#PURPOSE_DOCUMENT_MODEL_FOR_CONTENT_MODEL} and alias {@link #DOCUMENT_MODEL_ALIAS}; every
   * other reference to a Document Model is dropped, references to other model types are kept. Without a Document
   * Model there is nothing a base group could point into, so unbinding also clears {@code
   * content.configuration.baseGroupId}, as SME's export does. Changing to another Document Model leaves the base
   * group alone: it is then reported as not found, like SME's settings form does.
   */
  @JsonIgnore
  public void setDocumentModelId(@Nullable String documentModelId) {
    List<ModelReference> references = getModelReferences();
    references.removeIf(ContentModel::isDocumentReference);
    if (documentModelId != null && !documentModelId.isBlank()) {
      ModelReference reference = new ModelReference();
      reference.setPurpose(ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_CONTENT_MODEL);
      reference.setModelType(ModelType.DOCUMENT);
      reference.setAlias(DOCUMENT_MODEL_ALIAS);
      reference.setReference(documentModelId);
      references.addFirst(reference);
    }
    else if (getContent() != null && getContent().getConfiguration() != null) {
      getContent().getConfiguration().setBaseGroupId(null);
    }
  }

  private static boolean isDocumentReference(ModelReference reference) {
    return reference.getModelType() == ModelType.DOCUMENT;
  }
}
