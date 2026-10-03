package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.transformermodel.NumberTypeConfig;
import de.a12.studio.models.transformermodel.PatternError;
import de.a12.studio.models.transformermodel.RenamePath;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.models.transformermodel.TransformerModelContent;
import de.a12.studio.models.transformermodel.TypeMappingEntry;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeBackend;
import de.a12.studio.ui.previewapp.SmeInstallation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@link TransformerRun} against the real transformer: the Simple Model Editor backend of the A12 installation
 * configured on the machine, started as a process (a few seconds). Skipped when there is none, like the other tests
 * that need the installed SME.
 */
class TransformerRunBackendTest {

  @TempDir
  static Path folder;

  private static File xsd;

  @BeforeAll
  static void installationAndXsd() throws Exception {
    boolean installed;
    try {
      SmeInstallation.resolve();
      installed = true;
    }
    catch (PreviewAppException e) {
      installed = false;
    }
    assumeTrue(installed, "No A12 installation with the Simple Model Editor is configured");
    try (InputStream in = TransformerRunBackendTest.class.getResourceAsStream("/transformer/EnrollmentCertificate_XSD.xsd")) {
      assertNotNull(in);
      xsd = folder.resolve("EnrollmentCertificate_XSD.xsd").toFile();
      Files.write(xsd.toPath(), in.readAllBytes());
    }
  }

  @AfterAll
  static void stopBackend() {
    SmeBackend.getInstance().stop();
  }

  private static TransformerModel model(String id) {
    TransformerModel model = new TransformerModel();
    model.setId(id);
    model.setContent(new TransformerModelContent());
    model.getContent().getOrCreateCmd().setMainXsd("EnrollmentCertificate_XSD.xsd");
    model.getContent().getOrCreateConfiguration().setTimeZone("UTC");
    de.a12.studio.models.Locale en = new de.a12.studio.models.Locale();
    en.setCode("en");
    model.getLocales().add(en);
    return model;
  }

  @Test
  void theXsdIsTransformedIntoADocumentModelNamedAfterTheTransformerModel() {
    TransformerModel model = model("Certificates_TfM");
    model.getContent().getOrCreateCmd().setRootElement("EnrollmentCertificate");

    TransformationOutcome outcome = TransformerRun.run(model, List.of(xsd));

    assertEquals(TransformationOutcome.State.DONE, outcome.state(), outcome.message());
    assertTrue(outcome.success(), outcome.issues().toString());
    assertEquals("Certificates_TfM", outcome.documentModel().getId());
    assertEquals("EnrollmentCertificate", outcome.documentModel().getContent().getModelRoot().getRootGroups().get(0).getName());
    assertEquals(List.of("EnrollmentCertificate"), outcome.discovery().rootElements());
    assertTrue(outcome.discovery().elementPaths().contains("/EnrollmentCertificate/IdNr"));
    assertEquals(0, outcome.count(TransformationOutcome.SEVERITY_ERROR), outcome.issues().toString());
  }

  @Test
  void theConfigurationChangesTheGeneratedDocumentModel() {
    TransformerModel model = model("Configured_TfM");
    model.getContent().getOrCreateCmd().setRootElement("EnrollmentCertificate");
    model.getContent().getOrCreateRenamePaths().add(rename("/EnrollmentCertificate/IdNr", "TaxId"));
    TypeMappingEntry amount = new TypeMappingEntry();
    amount.setXsdType("custom_AmountWithTwoDigits");
    amount.setA12Type("NumberType");
    amount.getOrCreateNumberType().setMaxIntegerDigits(6);
    amount.getOrCreateNumberType().setTrait(NumberTypeConfig.TRAIT_AMOUNT);
    model.getContent().getOrCreateTypeMapping().add(amount);
    PatternError patternError = new PatternError();
    patternError.setPattern("\\d{11}");
    de.a12.studio.models.Label message = new de.a12.studio.models.Label();
    message.setLocale("en");
    message.setText("Eleven digits, please.");
    patternError.getOrCreateErrors().add(message);
    model.getContent().getOrCreatePatternErrors().add(patternError);

    TransformationOutcome outcome = TransformerRun.run(model, List.of(xsd));

    assertTrue(outcome.success(), outcome.issues().toString());
    String generated = JsonSettings.objectMapper.writeValueAsString(outcome.documentModel());
    assertTrue(generated.contains("\"TaxId\""), "the renamed element must be in the generated model");
    assertFalse(generated.contains("\"name\": \"IdNr\""), "the original name must be gone");
    assertTrue(generated.contains("Eleven digits, please."), "the custom pattern error text must be in the generated model");
  }

  @Test
  void anUnknownRootElementIsReportedByTheTransformerWithTheSuggestionsStillThere() {
    TransformerModel model = model("Wrong_TfM");
    model.getContent().getOrCreateCmd().setRootElement("Nope");

    TransformationOutcome outcome = TransformerRun.run(model, List.of(xsd));

    assertEquals(TransformationOutcome.State.DONE, outcome.state());
    assertFalse(outcome.success());
    assertTrue(outcome.issues().stream().anyMatch(issue -> issue.severity().equals("ERROR") && issue.message().contains("Nope")),
        outcome.issues().toString());
    assertEquals(List.of("EnrollmentCertificate"), outcome.discovery().rootElements(), "so the user can pick the right one");
  }

  @Test
  void withoutARootElementOnlyTheDiscoveryRuns() {
    TransformerModel model = model("NoRoot_TfM");

    TransformationOutcome outcome = TransformerRun.run(model, List.of(xsd));

    assertEquals(TransformationOutcome.State.INCOMPLETE, outcome.state());
    assertEquals(List.of("EnrollmentCertificate"), outcome.discovery().rootElements());
    assertTrue(outcome.issues().isEmpty(), "the backend's own 'This field is required.' is not shown");
  }

  private static RenamePath rename(String original, String name) {
    RenamePath rename = new RenamePath();
    rename.setOriginalPath(original);
    rename.setNewElementName(name);
    return rename;
  }
}
