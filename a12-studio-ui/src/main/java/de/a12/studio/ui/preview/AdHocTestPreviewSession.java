package de.a12.studio.ui.preview;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.FormScreenGenerator;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeBackend;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ad hoc testing of a Document Model, like SME's "Ad Hoc Testing" action: the selected elements are cut out of the
 * (expanded) Document Model by the {@link SmeBackend}, a Form Model is generated for the reduced model, and the Form
 * Engine renders that - a quick way to try out fields, validation rules and computations without building a form.
 *
 * <p>The selection is fixed when the session is created; the reduced model is recomputed whenever the Document
 * Model changes, ignoring selected elements that no longer exist.
 */
public final class AdHocTestPreviewSession extends FormEnginePreviewSession {

  private static final String AD_HOC_FORM_MODEL_ID = "adhoc_test";

  // The kernel's metadata group; never part of what a user selects or sees.
  private static final String META_GROUP_NAME = "__meta";

  private final ProjectItem documentModelItem;
  private final Set<String> selectedElementIds;

  private String cachedRevision;
  private Rendering cachedRendering;

  /**
   * @param documentModelItem  the Document Model under test
   * @param selectedElementIds the ids of the elements to test (an element together with its descendants makes a
   *                           complete subtree); empty tests the whole model
   */
  public AdHocTestPreviewSession(@NonNull ProjectItem documentModelItem, @NonNull Set<String> selectedElementIds) {
    this.documentModelItem = documentModelItem;
    this.selectedElementIds = new LinkedHashSet<>(selectedElementIds);
  }

  @Override
  public String getTitle() {
    return documentModelItem.getModel().getId() + " - Document Model Ad Hoc Test";
  }

  @Override
  public synchronized Snapshot snapshot(@Nullable String knownFormRevision, @Nullable String knownDocumentRevision)
      throws PreviewAppException {
    DocumentModel documentModel = (DocumentModel) documentModelItem.getModel();
    ExpansionInput input = expansionInput(documentModel.getId(), documentModelItem, documentModel);
    String revision = input.revision();
    if (!revision.equals(cachedRevision)) {
      cachedRendering = render(input);
      cachedRevision = revision;
    }
    // Everything is derived from the same inputs, so one revision covers the form and the document model.
    return toSnapshot(cachedRendering, revision, revision, knownFormRevision, knownDocumentRevision);
  }

  private Rendering render(ExpansionInput input) throws PreviewAppException {
    SmeBackend backend = SmeBackend.getInstance();
    JsonNode expanded = input.expand(backend);

    Map<String, List<String>> ancestorsById = new LinkedHashMap<>();
    Map<String, JsonNode> nodesById = new LinkedHashMap<>();
    collectElements(expanded.path("content").path("modelRoot").path("rootGroups"), List.of(), ancestorsById, nodesById);

    Set<String> selected = new LinkedHashSet<>();
    if (selectedElementIds.isEmpty()) {
      ancestorsById.forEach((id, ancestors) -> selected.add(id));
    }
    else {
      selectedElementIds.stream().filter(ancestorsById::containsKey).forEach(selected::add);
      // The children of an Include only exist in the expanded model (under ids of their own), so selecting the
      // Include has to bring them along.
      for (String id : List.copyOf(selected)) {
        if (nodesById.get(id).path("Group").has("includeConfig")) {
          addDescendants(nodesById.get(id), selected);
        }
      }
    }
    if (selected.isEmpty()) {
      throw new PreviewAppException("None of the selected elements exist in the Document Model any more.");
    }
    Set<String> partiallySelected = new LinkedHashSet<>();
    selected.forEach(id -> partiallySelected.addAll(ancestorsById.get(id)));
    partiallySelected.removeAll(selected);

    SmeBackend.AdHocTestInput reduced = backend.generateAdHocTestInput(expanded, selected, partiallySelected);
    return new Rendering(getTitle(), generateFormModel(reduced.documentModel()), reduced.documentModel(), reduced.validationCode());
  }

  // Maps the id of every element below the (non-metadata) root groups to the ids of its ancestors, outermost first,
  // and to its JSON node.
  private static void collectElements(JsonNode elements, List<String> ancestors, Map<String, List<String>> ancestorsById,
      Map<String, JsonNode> nodesById) {
    for (JsonNode element : elements) {
      if (ancestors.isEmpty() && META_GROUP_NAME.equals(element.path("name").asString(""))) {
        continue;
      }
      String id = element.path("id").asString(null);
      if (id == null) {
        continue;
      }
      ancestorsById.put(id, ancestors);
      nodesById.put(id, element);
      List<String> childAncestors = new ArrayList<>(ancestors);
      childAncestors.add(id);
      collectElements(children(element), childAncestors, ancestorsById, nodesById);
    }
  }

  private static void addDescendants(JsonNode element, Set<String> result) {
    for (JsonNode child : children(element)) {
      String id = child.path("id").asString(null);
      if (id != null) {
        result.add(id);
      }
      addDescendants(child, result);
    }
  }

  // An element's children sit under a key named like its type ("Group" -> "elements").
  private static JsonNode children(JsonNode element) {
    return element.path(element.path("type").asString("")).path("elements");
  }

  // The Form Model SME's form-model-generator would produce for the reduced model: one screen per root group.
  private static String generateFormModel(String reducedDocumentModelJson) throws PreviewAppException {
    DocumentModel reduced;
    try {
      reduced = JsonSettings.objectMapper.readValue(reducedDocumentModelJson, DocumentModel.class);
    }
    catch (RuntimeException e) {
      throw new PreviewAppException("The reduced Document Model could not be read: " + e.getMessage(), e);
    }

    FormModel formModel = new FormModel();
    formModel.setId(AD_HOC_FORM_MODEL_ID);
    formModel.setModelType(ModelType.FORM);
    formModel.setModelVersion(ModelType.FORM.getCurrentVersion());
    formModel.setLocales(reduced.getLocales());
    ModelReference dataBinding = new ModelReference();
    dataBinding.setModelType(ModelType.DOCUMENT);
    dataBinding.setPurpose(ModelReference.PURPOSE_DATA_BINDING);
    dataBinding.setAlias(reduced.getId());
    dataBinding.setReference(reduced.getId());
    formModel.getModelReferences().add(dataBinding);

    FormModelContent content = new FormModelContent();
    formModel.setContent(content);
    FormScreenGenerator.generate(content, reduced, reduced.getLocales());
    return serialize(formModel);
  }
}
