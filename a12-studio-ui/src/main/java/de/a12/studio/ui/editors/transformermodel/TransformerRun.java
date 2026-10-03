package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.transformermodel.TransformerCmd;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.editors.transformermodel.TransformationOutcome.Discovery;
import de.a12.studio.ui.editors.transformermodel.TransformationOutcome.EnumValue;
import de.a12.studio.ui.editors.transformermodel.TransformationOutcome.Issue;
import de.a12.studio.ui.editors.transformermodel.TransformationOutcome.SimpleType;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeBackend;
import de.a12.studio.ui.previewapp.SmeBackend.TransformerResource;
import de.a12.studio.ui.util.StudioBundle;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs a {@link TransformerModel}'s transformation the way SME's editor does ({@code getNextEditorTransformerModel},
 * {@code doTransform}): the Transformer Model plus every XSD file of the project go to the installed Simple Model
 * Editor backend ({@link SmeBackend}, which hosts the A12 {@code transformer} library), which answers with the
 * generated Document Model and the transformer's issues; a second call, the XSD discovery, supplies the suggestions
 * for the editor's fields. Nothing of the transformer is implemented or depended on here.
 *
 * <p>What is sent matches SME's request: the model with {@code skipConsistencyCheck} forced on (SME validates the
 * result itself afterwards, and filters the warning that goes with it) and every {@code .xsd} resource under its file
 * name. The legacy {@code Cmd.genDocModelName} is left out - the 2026.06 transformer names the Document Model after
 * the Transformer Model's id.
 */
@Slf4j
public final class TransformerRun {

  /** The warning the transformer adds because {@code skipConsistencyCheck} was forced on; SME hides it as well. */
  static final String CONSISTENCY_CHECK_SKIPPED = "Consistency check skipped as per configuration. Model may contain consistency issues.";

  private static final String GENERATED_DOCUMENT_MODEL = "A12_DOCUMENT_MODEL";

  private TransformerRun() {
  }

  /**
   * Transforms {@code model} against the XSD files {@code xsdFiles}. Blocking (starts the backend on first use, a few
   * seconds): call it off the FX thread. Never throws; a backend that cannot be reached is reported as {@link
   * TransformationOutcome.State#UNAVAILABLE}.
   */
  @NonNull
  public static TransformationOutcome run(@NonNull TransformerModel model, @NonNull List<File> xsdFiles) {
    TransformerCmd cmd = model.getContent() == null ? null : model.getContent().getCmd();
    String mainXsd = cmd == null ? null : cmd.getMainXsd();
    if (mainXsd == null || mainXsd.isBlank()) {
      return TransformationOutcome.incomplete(StudioBundle.get("transformer_model.run.no_main_xsd"));
    }
    if (xsdFiles.stream().noneMatch(file -> file.getName().equals(mainXsd))) {
      return TransformationOutcome.incomplete(StudioBundle.get("transformer_model.run.xsd_not_in_project", mainXsd));
    }

    List<TransformerResource> resources;
    try {
      resources = readResources(xsdFiles);
    }
    catch (IOException e) {
      return TransformationOutcome.incomplete(StudioBundle.get("transformer_model.run.xsd_unreadable", e.getMessage()));
    }

    JsonNode config = requestConfig(model);
    try {
      SmeBackend backend = SmeBackend.getInstance();
      Discovery discovery = parseDiscovery(backend.discover(config, resources));
      String rootElement = cmd.getRootElement();
      if (rootElement == null || rootElement.isBlank()) {
        return new TransformationOutcome(TransformationOutcome.State.INCOMPLETE, StudioBundle.get("transformer_model.run.no_root_element"),
            false, null, List.of(), discovery);
      }
      return parseTransformation(backend.transform(config, resources), discovery);
    }
    catch (PreviewAppException e) {
      return TransformationOutcome.unavailable(e.getMessage());
    }
    catch (RuntimeException e) {
      log.warn("The transformation of '{}' failed unexpectedly: {}", model.getId(), e.getMessage(), e);
      return TransformationOutcome.unavailable(String.valueOf(e.getMessage()));
    }
  }

  /**
   * The request's {@code transformationConfig}: the model as JSON, without the legacy {@code genDocModelName} and with
   * {@code skipConsistencyCheck} on, like SME's {@code doTransform}.
   */
  static JsonNode requestConfig(@NonNull TransformerModel model) {
    JsonNode tree = JsonSettings.objectMapper.valueToTree(model);
    ObjectNode content = (ObjectNode) tree.path("content");
    ObjectNode cmd = content.has("Cmd") && content.get("Cmd").isObject() ? (ObjectNode) content.get("Cmd") : content.putObject("Cmd");
    cmd.remove("genDocModelName");
    cmd.put("skipConsistencyCheck", true);
    return tree;
  }

  static List<TransformerResource> readResources(@NonNull List<File> xsdFiles) throws IOException {
    List<TransformerResource> resources = new ArrayList<>();
    for (File file : xsdFiles) {
      resources.add(TransformerResource.xsd(file.getName(), Files.readString(file.toPath(), StandardCharsets.UTF_8)));
    }
    return resources;
  }

  /** The transformation's answer as an outcome, with the {@code discovery} already read. */
  static TransformationOutcome parseTransformation(@NonNull JsonNode response, @NonNull Discovery discovery) {
    List<Issue> issues = new ArrayList<>();
    for (JsonNode node : response.path("issues")) {
      String message = node.path("message").asString("");
      String severity = node.path("severity").asString(TransformationOutcome.SEVERITY_INFO);
      if (TransformationOutcome.SEVERITY_WARNING.equals(severity) && CONSISTENCY_CHECK_SKIPPED.equals(message)) {
        continue;
      }
      issues.add(new Issue(severity, message, node.path("source").isNull() ? null : node.path("source").asString(null)));
    }

    DocumentModel documentModel = null;
    for (JsonNode resource : response.path("generatedResources")) {
      if (GENERATED_DOCUMENT_MODEL.equals(resource.path("type").asString(""))) {
        try {
          documentModel = JsonSettings.objectMapper.readValue(resource.path("content").asString(""), DocumentModel.class);
        }
        catch (RuntimeException e) {
          log.warn("The generated Document Model could not be read: {}", e.getMessage(), e);
          issues.add(new Issue(TransformationOutcome.SEVERITY_ERROR, "The generated Document Model could not be read: " + e.getMessage(), null));
        }
        break;
      }
    }
    boolean success = response.path("success").asBoolean(false) && documentModel != null;
    return new TransformationOutcome(TransformationOutcome.State.DONE, null, success, documentModel, List.copyOf(issues), discovery);
  }

  /** The discovery's answer ({@code {discoveredInformation: {...}, success}}); {@link Discovery#EMPTY} when it failed. */
  static Discovery parseDiscovery(@NonNull JsonNode response) {
    JsonNode info = response.path("discoveredInformation");
    if (!response.path("success").asBoolean(false) || !info.isObject()) {
      return Discovery.EMPTY;
    }

    List<SimpleType> simpleTypes = new ArrayList<>();
    for (JsonNode type : info.path("simpleTypes")) {
      String name = type.path("name").asString("");
      if (!name.isEmpty()) {
        simpleTypes.add(new SimpleType(name, textOrNull(type, "xsType"), textOrNull(type, "defaultMapping")));
      }
    }

    Map<String, List<String>> patternFields = new LinkedHashMap<>();
    for (Map.Entry<String, JsonNode> entry : info.path("patternFields").properties()) {
      patternFields.put(entry.getKey(), strings(entry.getValue()));
    }

    Map<String, EnumValue> enumValues = new LinkedHashMap<>();
    for (Map.Entry<String, JsonNode> entry : info.path("enumValues").properties()) {
      enumValues.put(entry.getKey(), new EnumValue(entry.getKey(), strings(entry.getValue().path("fieldPaths")),
          strings(entry.getValue().path("typeDefinitionIds"))));
    }

    return new Discovery(strings(info.path("rootElements")), List.copyOf(simpleTypes), strings(info.path("elementPaths")),
        patternFields, enumValues);
  }

  private static List<String> strings(JsonNode array) {
    List<String> result = new ArrayList<>();
    for (JsonNode element : array) {
      String value = element.asString("");
      if (!value.isEmpty()) {
        result.add(value);
      }
    }
    return List.copyOf(result);
  }

  @Nullable
  private static String textOrNull(JsonNode node, String field) {
    JsonNode value = node.path(field);
    return value.isNull() || value.isMissingNode() ? null : value.asString(null);
  }
}
