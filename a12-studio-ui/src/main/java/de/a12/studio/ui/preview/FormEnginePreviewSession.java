package de.a12.studio.ui.preview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeBackend;
import de.a12.studio.ui.util.ProjectDocumentModels;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What the Form Engine preview page ({@link PreviewServer}, {@code /sme/}) renders: a Form Model plus the expanded
 * Document Model and generated validation code it runs against - the three inputs SME's own preview window is fed
 * over {@code postMessage} (see {@code moduleSupport/fmm} in SME). A session reads the live in-memory models on
 * every {@link #snapshot}, so edits made in the editors show up without saving.
 *
 * <p>The expensive part (Document Model expansion and validation-code generation by the {@link SmeBackend}) is
 * cached and only redone when its inputs change; the page passes the revisions it already has, and the snapshot
 * leaves out whatever is unchanged.
 */
public abstract class FormEnginePreviewSession {

  /**
   * @param title                the browser tab title
   * @param formModelRevision    identifies {@code formModel}
   * @param documentModelRevision identifies {@code documentModel} and {@code validationCode}
   * @param formModel            the serialized Form Model, or {@code null} if the caller's revision is current
   * @param documentModel        the serialized expanded Document Model, or {@code null} if the caller's revision is current
   * @param validationCode       the generated validation code, or {@code null} if the caller's revision is current
   */
  public record Snapshot(String title, String formModelRevision, String documentModelRevision,
      @Nullable String formModel, @Nullable String documentModel, @Nullable String validationCode) {
  }

  /**
   * What the backend has to expand: the Document Model {@code targetId} with everything it includes or imports from
   * in {@code models}, or - if {@code combinationModel} is set - that Combination Model with the Document, Selection
   * and Combination Models it references in {@code models}.
   */
  protected record ExpansionInput(String targetId, @Nullable String combinationModel, List<String> models) {

    String revision() {
      List<String> all = new ArrayList<>(List.of(targetId, String.valueOf(combinationModel)));
      all.addAll(models);
      return FormEnginePreviewSession.revision(all.toArray(String[]::new));
    }

    JsonNode expand(SmeBackend backend) throws PreviewAppException {
      return combinationModel != null
          ? backend.expandCombination(combinationModel, models)
          : backend.expand(targetId, models);
    }
  }

  /**
   * @param title               the browser tab title
   * @param formModel           the Form Model to render, serialized
   * @param documentModel       the expanded Document Model, serialized
   * @param validationCode      the validation code for it
   */
  protected record Rendering(String title, String formModel, String documentModel, String validationCode) {
  }

  /** The browser tab title while the page loads. */
  public abstract String getTitle();

  /**
   * The current state of the session.
   *
   * @param knownFormRevision     the Form Model revision the page has, or {@code null}
   * @param knownDocumentRevision the Document Model revision the page has, or {@code null}
   * @throws PreviewAppException  if the models cannot be prepared (no A12 installation, the Document Model contains errors, ...)
   */
  public abstract Snapshot snapshot(@Nullable String knownFormRevision, @Nullable String knownDocumentRevision) throws PreviewAppException;

  protected static Snapshot toSnapshot(Rendering rendering, String formRevision, String documentRevision,
      @Nullable String knownFormRevision, @Nullable String knownDocumentRevision) {
    boolean sendDocument = !documentRevision.equals(knownDocumentRevision);
    return new Snapshot(rendering.title(), formRevision, documentRevision,
        formRevision.equals(knownFormRevision) ? null : rendering.formModel(),
        sendDocument ? rendering.documentModel() : null,
        sendDocument ? rendering.validationCode() : null);
  }

  /**
   * The Form Model as the Form Engine expects it. The engine only accepts a model whose content has a {@code
   * subHeaderBox} and a {@code footerBox}, which the studio leaves out while they are empty (as does a fresh Form
   * Model); they are added to this copy only.
   */
  static String serializeFormModel(FormModel formModel) throws PreviewAppException {
    try {
      JsonNode root = JsonSettings.objectMapper.readTree(serialize(formModel));
      if (root.path("content") instanceof ObjectNode content) {
        for (String box : List.of("subHeaderBox", "footerBox")) {
          if (!content.has(box)) {
            content.putObject(box).put("id", box);
          }
        }
      }
      return JsonSettings.objectMapper.writeValueAsString(root);
    }
    catch (RuntimeException e) {
      throw new PreviewAppException("The Form Model could not be prepared for the preview: " + e.getMessage(), e);
    }
  }

  static String serialize(Object model) throws PreviewAppException {
    try {
      return JsonSettings.objectMapper.writeValueAsString(model);
    }
    catch (RuntimeException e) {
      // The models are plain unsynchronized POJOs read here while the editor may be mutating them; the page just retries.
      throw new PreviewAppException("The model is being edited, retrying: " + e.getMessage(), e);
    }
  }

  static String revision(String... parts) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      for (String part : parts) {
        digest.update(part.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
      }
      return HexFormat.of().formatHex(digest.digest(), 0, 8);
    }
    catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * The expansion input for the model {@code targetId}, which must be a Document Model or a Combination Model of
   * {@code context}'s project. Live in-memory models are used; {@code liveDocumentModel} (if given) replaces the
   * project's own instance of that id.
   */
  protected static ExpansionInput expansionInput(@NonNull String targetId, @NonNull ProjectItem context,
      @Nullable DocumentModel liveDocumentModel) throws PreviewAppException {
    Map<String, DocumentModel> documentModels = new LinkedHashMap<>();
    for (DocumentModel documentModel : ProjectDocumentModels.getOtherDocumentModels(context)) {
      documentModels.put(documentModel.getId(), documentModel);
    }
    if (liveDocumentModel != null) {
      documentModels.put(liveDocumentModel.getId(), liveDocumentModel);
    }

    if (documentModels.containsKey(targetId)) {
      return new ExpansionInput(targetId, null, serializeClosure(documentModels.get(targetId), documentModels));
    }

    List<A12Model<?>> combinations = ProjectDocumentModels.getOtherModelsOfType(context, ModelType.COMBINATION);
    A12Model<?> combination = combinations.stream().filter(candidate -> targetId.equals(candidate.getId())).findFirst().orElse(null);
    if (combination != null) {
      List<String> referenced = new ArrayList<>();
      for (DocumentModel documentModel : documentModels.values()) {
        referenced.add(serialize(documentModel));
      }
      for (A12Model<?> selection : ProjectDocumentModels.getOtherModelsOfType(context, ModelType.SELECTION)) {
        referenced.add(serialize(selection));
      }
      for (A12Model<?> other : combinations) {
        if (other != combination) {
          referenced.add(serialize(other));
        }
      }
      return new ExpansionInput(targetId, serialize(combination), referenced);
    }
    throw new PreviewAppException("The Document Model \"" + targetId + "\" was not found in the project.");
  }

  // The target plus every Document Model it (transitively) includes or imports type definitions from, following the
  // header's model references - the same set SME sends to the expansion endpoint.
  private static List<String> serializeClosure(DocumentModel target, Map<String, DocumentModel> all) throws PreviewAppException {
    List<String> result = new ArrayList<>();
    Set<String> visited = new HashSet<>();
    Deque<DocumentModel> queue = new ArrayDeque<>(List.of(target));
    while (!queue.isEmpty()) {
      DocumentModel current = queue.poll();
      if (!visited.add(current.getId())) {
        continue;
      }
      result.add(serialize(current));
      for (ModelReference reference : current.getModelReferences()) {
        DocumentModel referenced = reference.getModelType() == ModelType.DOCUMENT ? all.get(reference.getReference()) : null;
        if (referenced != null) {
          queue.add(referenced);
        }
      }
    }
    return result;
  }
}
