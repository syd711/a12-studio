package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.editors.transformermodel.TransformationOutcome.Discovery;
import de.a12.studio.ui.editors.transformermodel.TransformationOutcome.Issue;
import de.a12.studio.ui.previewapp.SmeBackend.TransformerResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What {@link TransformerRun} sends to the Simple Model Editor backend and how it reads the answer. The two responses
 * in {@code /transformer} are the real answers of the installed backend (SME 13.0.2, {@code transform} and
 * {@code discover}) to SME's own example, {@code University_Certificates_TfM.json} with
 * {@code EnrollmentCertificate_XSD.xsd}.
 */
class TransformerRunTest {

  private static String resource(String name) throws Exception {
    try (InputStream in = TransformerRunTest.class.getResourceAsStream("/transformer/" + name)) {
      assertNotNull(in, "Missing test resource " + name);
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static JsonNode json(String name) throws Exception {
    return JsonSettings.objectMapper.readTree(resource(name));
  }

  // ---- the answer to discover ---------------------------------------------------------------------------------

  @Test
  void theDiscoveryOfTheExampleXsdIsRead() throws Exception {
    Discovery discovery = TransformerRun.parseDiscovery(json("discover-response.json"));

    assertEquals(List.of("EnrollmentCertificate"), discovery.rootElements());
    assertTrue(discovery.elementPaths().contains("/EnrollmentCertificate/IdNr"), discovery.elementPaths().toString());
    assertTrue(discovery.elementPaths().contains("/EnrollmentCertificate/University/Name"));
    assertEquals(List.of("\\d{11}"), discovery.patterns());
    assertEquals(List.of("/EnrollmentCertificate/IdNr"), discovery.patternFields().get("\\d{11}"));

    var taxId = discovery.simpleTypes().stream().filter(type -> type.name().equals("custom_TaxIDNr")).findFirst().orElseThrow();
    assertEquals("string", taxId.xsType());
    assertEquals("StringType", taxId.defaultMapping());
    var amount = discovery.simpleTypes().stream().filter(type -> type.name().equals("custom_AmountWithTwoDigits")).findFirst().orElseThrow();
    assertEquals("NumberType", amount.defaultMapping());

    assertEquals(16, discovery.enumValues().size());
    assertEquals(List.of("/EnrollmentCertificate/University/State"), discovery.enumValues().get("BW").fieldPaths());
    assertTrue(discovery.enumValues().get("BW").typeDefinitionIds().isEmpty());
  }

  @Test
  void aFailedDiscoveryIsEmptyNotAnError() throws Exception {
    JsonNode failed = JsonSettings.objectMapper.readTree("{\"success\": false}");

    assertSame(Discovery.EMPTY, TransformerRun.parseDiscovery(failed));
    assertSame(Discovery.EMPTY, TransformerRun.parseDiscovery(JsonSettings.objectMapper.readTree("{}")));
  }

  private static void assertSame(Object expected, Object actual) {
    org.junit.jupiter.api.Assertions.assertSame(expected, actual);
  }

  // ---- the answer to transform --------------------------------------------------------------------------------

  @Test
  void theGeneratedDocumentModelIsReadAndNamedAfterTheTransformerModel() throws Exception {
    TransformationOutcome outcome = TransformerRun.parseTransformation(json("transform-response.json"), Discovery.EMPTY);

    assertEquals(TransformationOutcome.State.DONE, outcome.state());
    assertTrue(outcome.success());
    assertNotNull(outcome.documentModel());
    assertEquals("University_Certificates_TfM", outcome.documentModel().getId());
    var rootGroups = outcome.documentModel().getContent().getModelRoot().getRootGroups();
    assertEquals(1, rootGroups.size());
    assertEquals("EnrollmentCertificate", rootGroups.get(0).getName());
  }

  @Test
  void theConsistencyWarningOfTheForcedSkipIsHiddenLikeSmeDoes() throws Exception {
    TransformationOutcome outcome = TransformerRun.parseTransformation(json("transform-response.json"), Discovery.EMPTY);

    assertTrue(outcome.issues().stream().noneMatch(issue -> issue.message().equals(TransformerRun.CONSISTENCY_CHECK_SKIPPED)),
        "the warning that only exists because skipConsistencyCheck is forced on must not be shown");
    assertEquals(0, outcome.count(TransformationOutcome.SEVERITY_WARNING));
    assertTrue(outcome.count(TransformationOutcome.SEVERITY_INFO) > 0, "the information messages stay");
    assertTrue(outcome.issues().stream().anyMatch(issue -> issue.message().startsWith("Fallback to default type mappings")
        && "/content/TypeMapping".equals(issue.source())), outcome.issues().toString());
  }

  @Test
  void aFailedTransformationKeepsItsErrorsAndHasNoDocumentModel() throws Exception {
    JsonNode failed = JsonSettings.objectMapper.readTree("""
        {"generatedResources": [], "success": false,
         "issues": [{"severity": "ERROR", "message": "No root element with name 'Nope' has been found in the schema!", "source": null, "errorCode": null}]}""");

    TransformationOutcome outcome = TransformerRun.parseTransformation(failed, Discovery.EMPTY);

    assertEquals(TransformationOutcome.State.DONE, outcome.state(), "the transformer was reached");
    assertFalse(outcome.success());
    assertNull(outcome.documentModel());
    assertEquals(List.of(new Issue("ERROR", "No root element with name 'Nope' has been found in the schema!", null)), outcome.issues());
  }

  @Test
  void aSuccessfulFlagWithoutADocumentModelIsNotASuccess() throws Exception {
    JsonNode odd = JsonSettings.objectMapper.readTree("{\"generatedResources\": [], \"success\": true, \"issues\": []}");

    assertFalse(TransformerRun.parseTransformation(odd, Discovery.EMPTY).success());
  }

  @Test
  void aGeneratedDocumentModelThatCannotBeReadIsReportedAsAnError() throws Exception {
    JsonNode broken = JsonSettings.objectMapper.readTree("""
        {"generatedResources": [{"type": "A12_DOCUMENT_MODEL", "name": "X", "content": "this is not json"}], "success": true, "issues": []}""");

    TransformationOutcome outcome = TransformerRun.parseTransformation(broken, Discovery.EMPTY);

    assertFalse(outcome.success());
    assertNull(outcome.documentModel());
    assertEquals(1, outcome.count(TransformationOutcome.SEVERITY_ERROR), outcome.issues().toString());
  }

  // ---- the request ----------------------------------------------------------------------------------------------

  @Test
  void theRequestedModelHasNoLegacyNameAndSkipsTheConsistencyCheck() throws Exception {
    TransformerModel model = JsonSettings.objectMapper.readValue(resource("University_Certificates_TfM.json"), TransformerModel.class);
    assertEquals("Univercity_Certificates_TfM", model.getContent().getCmd().getGenDocModelName(), "the fixture has the legacy parameter");

    JsonNode config = TransformerRun.requestConfig(model);

    JsonNode cmd = config.get("content").get("Cmd");
    assertFalse(cmd.has("genDocModelName"), "the 2026.06 transformer no longer supports it");
    assertTrue(cmd.get("skipConsistencyCheck").asBoolean());
    assertEquals("EnrollmentCertificate_XSD.xsd", cmd.get("mainXsd").asString());
    assertEquals("EnrollmentCertificate", cmd.get("rootElement").asString());
    assertEquals("transformer", config.get("header").get("modelType").asString());
    assertEquals("University_Certificates_TfM", config.get("header").get("id").asString());
    assertEquals(1, config.get("content").get("PatternErrors").size());
    assertEquals("Univercity_Certificates_TfM", model.getContent().getCmd().getGenDocModelName(), "the edited model itself is left alone");
    assertNull(model.getContent().getCmd().getSkipConsistencyCheck());
  }

  @Test
  void theRequestAddsACmdWhenTheModelHasNone() {
    TransformerModel model = new TransformerModel();
    model.setId("X_TfM");
    model.setContent(new de.a12.studio.models.transformermodel.TransformerModelContent());

    JsonNode cmd = TransformerRun.requestConfig(model).get("content").get("Cmd");

    assertTrue(cmd.get("skipConsistencyCheck").asBoolean());
  }

  @Test
  void everyXsdFileIsSentUnderItsFileName(@TempDir Path folder) throws Exception {
    Files.writeString(folder.resolve("A.xsd"), "<a/>");
    Files.writeString(folder.resolve("B.xsd"), "<b/>");

    List<TransformerResource> resources = TransformerRun.readResources(List.of(new File(folder.toFile(), "A.xsd"), new File(folder.toFile(), "B.xsd")));

    assertEquals(List.of(TransformerResource.xsd("A.xsd", "<a/>"), TransformerResource.xsd("B.xsd", "<b/>")), resources);
    assertEquals("XSD", resources.get(0).type());
  }

  // ---- when nothing can be run -------------------------------------------------------------------------------------

  @Test
  void withoutAMainXsdNothingIsRunAndThatIsSaid() {
    TransformerModel model = new TransformerModel();
    model.setId("X_TfM");
    model.setContent(new de.a12.studio.models.transformermodel.TransformerModelContent());

    TransformationOutcome outcome = TransformerRun.run(model, List.of());

    assertEquals(TransformationOutcome.State.INCOMPLETE, outcome.state());
    assertNotNull(outcome.message());
    assertFalse(outcome.message().isBlank());
    assertSame(Discovery.EMPTY, outcome.discovery());
  }

  @Test
  void aMainXsdThatIsNotAProjectFileIsReportedByName(@TempDir Path folder) throws Exception {
    Files.writeString(folder.resolve("Other.xsd"), "<a/>");
    TransformerModel model = new TransformerModel();
    model.setId("X_TfM");
    model.setContent(new de.a12.studio.models.transformermodel.TransformerModelContent());
    model.getContent().getOrCreateCmd().setMainXsd("Missing.xsd");

    TransformationOutcome outcome = TransformerRun.run(model, List.of(new File(folder.toFile(), "Other.xsd")));

    assertEquals(TransformationOutcome.State.INCOMPLETE, outcome.state());
    assertTrue(outcome.message().contains("Missing.xsd"), outcome.message());
  }
}
