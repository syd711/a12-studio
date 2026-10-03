package de.a12.studio.models.transformermodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelFactory;
import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransformerModelTest {

  private static final String SME_EXAMPLE = "/transformermodel/University_Certificates_TfM.json";
  private static final String EVERYTHING = "/transformermodel/Everything_TfM.json";

  @Test
  void theExampleModelOfSmeRoundTripsUnchanged() throws Exception {
    // Real file of SME's example workspace, with the legacy Cmd.genDocModelName and an explicit "TypeMapping": [].
    ModelRoundTrip.assertRoundTrip(getClass(), SME_EXAMPLE, TransformerModel.class);
  }

  @Test
  void aModelUsingEveryContentSectionRoundTripsUnchanged() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), EVERYTHING, TransformerModel.class);
  }

  @Test
  void theSectionsAreReadIntoTypedClasses() throws Exception {
    TransformerModel model = ModelRoundTrip.load(getClass(), EVERYTHING, TransformerModel.class);
    TransformerModelContent content = model.getContent();

    assertEquals("EnrollmentCertificate_XSD.xsd", content.getCmd().getMainXsd());
    assertEquals("EnrollmentCertificate", content.getCmd().getRootElement());
    assertEquals(Boolean.TRUE, content.getCmd().getMinimal());
    assertEquals("Europe/Berlin", content.getConfiguration().getTimeZone());
    assertEquals("datatypeC", content.getConfiguration().getSupportedCharactersTypeInXsd());

    assertEquals(4, content.getTypeMapping().size());
    TypeMappingEntry string = content.getTypeMapping().get(0);
    assertEquals(A12DataType.STRING, string.getDataType());
    assertEquals(11, string.getStringType().getMinLength());
    assertEquals("[0-9]{11}", string.getStringType().getPattern());
    assertEquals(Boolean.FALSE, string.getStringType().getLineBreaksPermitted());
    NumberTypeConfig number = content.getTypeMapping().get(1).getNumberType();
    assertEquals(new BigDecimal("999999.99"), number.getMaxValue());
    assertEquals("Amount", number.getTrait());
    assertEquals(6, number.getMaxIntegerDigits());
    assertEquals(Boolean.TRUE, content.getTypeMapping().get(2).getEnumerationType().getAlphabeticalSorting());
    assertNull(content.getTypeMapping().get(3).getStringType());

    assertEquals("TaxId", content.getRenamePaths().get(0).getNewElementName());
    assertEquals("/EnrollmentCertificate/CreationDate", content.getDeletePaths().get(0).getPath());
    assertEquals(PatternErrorAction.REPLACE, content.getPatternErrors().get(0).getEffectiveAction());
    assertEquals("Please enter 11 digits.", content.getPatternErrors().get(0).findError("en").getText());
    assertEquals(PatternErrorAction.UPDATE_MESSAGE, content.getPatternErrors().get(1).getEffectiveAction());
    assertEquals(PatternErrorAction.SUPPRESS, content.getPatternErrors().get(2).getEffectiveAction());
    assertEquals("custom_FederalStateAbbreviation", content.getEnumLabels().get(0).getTypeDefinitionId());
    assertEquals("Saxony", content.getEnumLabels().get(1).findReplacement("en").getText());
    assertEquals("2023-12", content.getCodeLists().getUriVersionList().get(0).getVersion());
  }

  @Test
  void unknownKeysAreKeptNotDropped() throws Exception {
    TransformerModel model = ModelRoundTrip.load(getClass(), EVERYTHING, TransformerModel.class);

    JsonNode resaved = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model));

    assertTrue(resaved.get("content").get("SomethingNew").get("kept").asBoolean());
  }

  @Test
  void anAbsentListStaysAbsentAndAnEmptyOneStaysExplicit() throws Exception {
    TransformerModel everything = ModelRoundTrip.load(getClass(), SME_EXAMPLE, TransformerModel.class);
    JsonNode resaved = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(everything)).get("content");

    assertTrue(resaved.get("TypeMapping").isArray() && resaved.get("TypeMapping").isEmpty(), "explicit [] must stay");
    assertFalse(resaved.has("RenamePaths"), "an absent list must not appear");
    assertFalse(resaved.has("DeletePaths"));
    assertFalse(resaved.has("CodeLists"));
    assertEquals("Univercity_Certificates_TfM", resaved.get("Cmd").get("genDocModelName").asString(),
        "the legacy parameter is preserved");
  }

  @Test
  void readingTheListsThroughTheOrEmptyAccessorsDoesNotCreateThem() throws Exception {
    TransformerModel model = ModelRoundTrip.load(getClass(), SME_EXAMPLE, TransformerModel.class);

    assertTrue(model.getContent().getRenamePathsOrEmpty().isEmpty());
    assertTrue(model.getContent().getDeletePathsOrEmpty().isEmpty());

    JsonNode resaved = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model)).get("content");
    assertFalse(resaved.has("RenamePaths"));
    assertFalse(resaved.has("DeletePaths"));
  }

  @Test
  void anEmptiedConfigurationGroupIsPrunedSoItIsNotWrittenAsAnEmptyObject() throws Exception {
    TypeMappingEntry entry = new TypeMappingEntry();
    entry.setXsdType("string");
    entry.setA12Type("StringType");
    entry.getOrCreateStringType().setMinLength(3);
    entry.getOrCreateStringType().setMinLength(null);
    entry.getOrCreateNumberType();

    entry.pruneEmptyConfigs();

    assertNull(entry.getStringType());
    assertNull(entry.getNumberType());
    JsonNode json = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(entry));
    assertEquals(2, json.size(), json.toString());
  }

  @Test
  void theDataTypesCarryTheSuperTypesOfSmesRules() {
    assertEquals("String", A12DataType.STRING_WITH_XS_PATTERN.getSuperType());
    assertEquals("Date", A12DataType.TIME.getSuperType());
    assertEquals("Boolean", A12DataType.CONFIRM.getSuperType());
    assertEquals("Enumeration", A12DataType.ENUM_FOR_BOOLEAN.getSuperType());
    assertTrue(A12DataType.ENUM_FOR_STRING.acceptsEnumerationConfig());
    assertFalse(A12DataType.ENUM_FOR_STRING.acceptsStringConfig());
    // The editor only offers the String fields for plain StringType, the rule accepts any String super type.
    assertTrue(A12DataType.STRING_WITH_XS_PATTERN.acceptsStringConfig());
    assertFalse(A12DataType.STRING_WITH_XS_PATTERN.showsStringConfig());
    assertEquals(A12DataType.NUMBER, A12DataType.fromValue("NumberType"));
    assertNull(A12DataType.fromValue("Nonsense"));
    assertNull(A12DataType.fromValue(null));
  }

  @Test
  void modelFactoryLoadsATransformerModelByItsModelType() {
    File file = new File(getClass().getResource(SME_EXAMPLE).getFile());

    A12Model<?> model = ModelFactory.load(new ProjectItem(file));

    TransformerModel transformerModel = assertInstanceOf(TransformerModel.class, model);
    assertEquals(ModelType.TRANSFORMER, transformerModel.getModelType());
    assertEquals("University_Certificates_TfM", transformerModel.getId());
  }

  @Test
  void aNewTransformerModelStartsWithTheDefaultTimeZoneAndAnEmptyCommand(@TempDir Path folder) throws Exception {
    ProjectItem parent = new ProjectItem(folder.toFile());

    ProjectItem item = NewModelFactory.createModel(parent, ModelType.TRANSFORMER, "Persons_TfM");

    TransformerModel model = assertInstanceOf(TransformerModel.class, item.getModel());
    assertEquals("Persons_TfM", model.getId());
    assertEquals("transformer", model.getModelType().getValue());
    assertEquals("UTC", model.getContent().getConfiguration().getTimeZone());
    assertNull(model.getContent().getCmd().getMainXsd());
    // What reaches the file: Cmd is an empty object, no list the user has not touched.
    JsonNode written = JsonSettings.objectMapper.readTree(Files.readString(folder.resolve("Persons_TfM.json"), StandardCharsets.UTF_8));
    assertEquals("transformer", written.get("header").get("modelType").asString());
    assertTrue(written.get("content").get("Cmd").isObject());
    assertFalse(written.get("content").has("TypeMapping"));
    assertEquals("TfM", ModelType.TRANSFORMER.getSuffix());
  }
}
