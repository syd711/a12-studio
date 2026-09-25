package de.a12.studio.ui.preview;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeBackend;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

/**
 * What the Content Model preview page ({@link PreviewServer}, {@code /sme/?content=...}) renders with the real Content
 * Engine: the live Content Model plus, if it is bound to one, the expanded Document Model it takes its data from -
 * the inputs SME's own preview window is fed over {@code postMessage} (see {@code
 * client/src/modules/contentModel/preview} in SME). A session reads the live in-memory model on every {@link
 * #snapshot}, so edits made in the editor show up without saving.
 *
 * <p>Like {@link FormEnginePreviewSession}, the expensive part (Document Model expansion by the {@link SmeBackend}) is
 * cached and only redone when its inputs change; the page passes the revisions it already has, and the snapshot leaves
 * out whatever is unchanged. The validation code the Content Engine needs is not part of the snapshot: the client
 * bundle asks the backend for it itself, see {@link PreviewServer}.
 */
public final class ContentModelPreviewSession {

  private static final String META_DATA_GROUP = "__meta";

  /**
   * @param contentModelRevision  identifies {@code contentModel}
   * @param documentModelRevision identifies the Document Model, {@code "none"} if the Content Model is not bound to one
   * @param hasDocumentModel      whether the Content Model is bound to a Document Model at all
   * @param contentModel          the serialized Content Model, or {@code null} if the caller's revision is current
   * @param documentModel         the serialized expanded Document Model (the input of the validation code
   *                              generation), or {@code null} if the caller's revision is current
   * @param documentModelWithoutMetaData the same without the metadata groups, which is what the Content Engine's own
   *                              (deserialized) Document Model is made from, or {@code null} if the caller's revision is current
   */
  public record Snapshot(String contentModelRevision, String documentModelRevision, boolean hasDocumentModel,
      @Nullable String contentModel, @Nullable String documentModel, @Nullable String documentModelWithoutMetaData) {
  }

  private static final String NO_DOCUMENT_MODEL = "none";

  private final ProjectItem contentItem;

  private String cachedDocumentRevision;
  private String cachedDocumentModel;
  private String cachedDocumentModelWithoutMetaData;

  public ContentModelPreviewSession(@NonNull ProjectItem contentItem) {
    this.contentItem = contentItem;
  }

  /** The id of the Document Model the Content Model is bound to, or {@code null}. */
  static @Nullable String documentModelId(@NonNull ContentModel contentModel) {
    return contentModel.getModelReferences().stream()
        .filter(reference -> reference.getModelType() == ModelType.DOCUMENT
            && ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_CONTENT_MODEL.equals(reference.getPurpose()))
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
  }

  /**
   * The current state of the session.
   *
   * @param knownContentRevision  the Content Model revision the page has, or {@code null}
   * @param knownDocumentRevision the Document Model revision the page has, or {@code null}
   * @throws PreviewAppException  if the models cannot be prepared (no A12 installation, the Document Model contains errors, ...)
   */
  public synchronized Snapshot snapshot(@Nullable String knownContentRevision, @Nullable String knownDocumentRevision)
      throws PreviewAppException {
    ContentModel contentModel = (ContentModel) contentItem.getModel();
    String contentJson = FormEnginePreviewSession.serialize(contentModel);
    String contentRevision = FormEnginePreviewSession.revision(contentJson);
    @Nullable String contentToSend = contentRevision.equals(knownContentRevision) ? null : contentJson;

    String documentModelId = documentModelId(contentModel);
    if (documentModelId == null) {
      return new Snapshot(contentRevision, NO_DOCUMENT_MODEL, false, contentToSend, null, null);
    }

    FormEnginePreviewSession.ExpansionInput input =
        FormEnginePreviewSession.expansionInput(documentModelId, contentItem, null);
    String documentRevision = input.revision();
    if (!documentRevision.equals(cachedDocumentRevision)) {
      JsonNode expanded = input.expand(SmeBackend.getInstance());
      cachedDocumentModel = expanded.toString();
      cachedDocumentModelWithoutMetaData = withoutMetaDataGroups(expanded).toString();
      cachedDocumentRevision = documentRevision;
    }

    boolean sendDocument = !documentRevision.equals(knownDocumentRevision);
    return new Snapshot(contentRevision, documentRevision, true, contentToSend,
        sendDocument ? cachedDocumentModel : null, sendDocument ? cachedDocumentModelWithoutMetaData : null);
  }

  /**
   * A copy of {@code expandedDocumentModel} without its metadata groups - the {@code __meta} group the expansion adds
   * for the kernel's validator - mirroring what SME does before it hands the model to the Content Engine.
   */
  static JsonNode withoutMetaDataGroups(@NonNull JsonNode expandedDocumentModel) {
    JsonNode copy = expandedDocumentModel.deepCopy();
    removeMetaDataGroups(copy);
    return copy;
  }

  private static void removeMetaDataGroups(JsonNode node) {
    if (node.isArray()) {
      ArrayNode array = (ArrayNode) node;
      for (int i = array.size() - 1; i >= 0; i--) {
        JsonNode element = array.get(i);
        if (element.isObject() && META_DATA_GROUP.equals(element.path("name").asString(null))) {
          array.remove(i);
        }
      }
    }
    node.forEach(ContentModelPreviewSession::removeMetaDataGroups);
  }
}
