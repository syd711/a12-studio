package de.a12.studio.modelsvalidation.validators.transformer;

import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.transformermodel.CodeLists;
import de.a12.studio.models.transformermodel.DeletePath;
import de.a12.studio.models.transformermodel.EnumLabel;
import de.a12.studio.models.transformermodel.PatternError;
import de.a12.studio.models.transformermodel.RenamePath;
import de.a12.studio.models.transformermodel.TransformerCmd;
import de.a12.studio.models.transformermodel.TransformerConfiguration;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.models.transformermodel.TransformerModelContent;
import de.a12.studio.models.transformermodel.TypeMappingEntry;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.services.TransformerModelValidationService;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One group of tests per Transformer Model validator, each against a minimal in-memory model. The rules are SME's
 * ({@code TransformerConfigModel} 29.4.0 of the installed SME 13.0.2); the test names use SME's rule names.
 */
class TransformerValidatorsTest {

  // ---- the whole model ------------------------------------------------------------------------------------------

  @Test
  void aModelThatUsesEveryTabAndIsCorrectHasNoProblem() {
    TransformerModel model = TestModels.load("/transformer/Everything_TfM.json", TransformerModel.class);

    List<ModelValidationError> errors = new TransformerModelValidationService().validate(model, TestModels.context(model));

    assertTrue(errors.isEmpty(), errors.toString());
  }

  @Test
  void aFreshModelOnlyAsksForTheMainXsdAndTheRootElement() {
    TransformerModel model = model();
    model.getContent().getOrCreateCmd();

    List<ModelValidationError> errors = new TransformerModelValidationService().validate(model, TestModels.context(model));

    assertEquals(List.of(TransformerElementIds.MAIN_XSD, TransformerElementIds.ROOT_ELEMENT), ids(errors));
  }

  @Test
  void theHeaderRulesApplyToATransformerModel() {
    TransformerModel model = model();
    model.getContent().getOrCreateCmd().setMainXsd("a.xsd");
    model.getContent().getOrCreateCmd().setRootElement("A");
    model.setLocales(new ArrayList<>());

    List<ModelValidationError> errors = new TransformerModelValidationService().validate(model, TestModels.context(model));

    assertEquals(List.of("header/locales"), ids(errors), "mustHaveAtLeastOneLocale");
  }

  // ---- Transformation: main XSD, root element -------------------------------------------------------------------

  @Test
  void theMainXsdMustBeAnXsdFileOfTheProject(@TempDir Path folder) throws Exception {
    Files.createDirectories(folder.resolve("resources"));
    Files.writeString(folder.resolve("resources").resolve("Persons.xsd"), "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>");
    Project project = new Project();
    project.load(folder.toFile());
    TransformerModel model = model();
    model.getContent().getOrCreateCmd().setRootElement("Person");

    model.getContent().getOrCreateCmd().setMainXsd("Persons.xsd");
    assertTrue(new TransformerSourceValidator().validate(model, context(model, project)).isEmpty());

    model.getContent().getOrCreateCmd().setMainXsd("Missing.xsd");
    List<ModelValidationError> errors = new TransformerSourceValidator().validate(model, context(model, project));
    assertEquals(List.of(TransformerElementIds.MAIN_XSD), ids(errors));
    assertTrue(errors.get(0).message().contains("Missing.xsd"), errors.get(0).message());
  }

  @Test
  void aBlankMainXsdAndRootElementAreMissing() {
    TransformerModel model = model();
    model.getContent().getOrCreateCmd().setMainXsd(" ");
    model.getContent().getOrCreateCmd().setRootElement("");

    assertEquals(List.of(TransformerElementIds.MAIN_XSD, TransformerElementIds.ROOT_ELEMENT),
        ids(new TransformerSourceValidator().validate(model, TestModels.context(model))));
  }

  // ---- Type mappings --------------------------------------------------------------------------------------------

  @Test
  void typeMappingRequiresBothTypes() {
    TransformerModel model = model();
    model.getContent().getOrCreateTypeMapping().add(mapping(null, null));
    model.getContent().getOrCreateTypeMapping().add(mapping("string", "Nonsense"));

    List<ModelValidationError> errors = validate(new TransformerTypeMappingValidator(), model);

    assertEquals(List.of(TransformerElementIds.typeMapping(0, "xsdType"), TransformerElementIds.typeMapping(0, "a12Type"),
        TransformerElementIds.typeMapping(1, "a12Type")), ids(errors));
    assertTrue(errors.get(0).message().startsWith("XSD Type Name"), errors.get(0).message());
    assertTrue(errors.get(2).message().contains("Nonsense"), errors.get(2).message());
  }

  @Test
  void xsdTypeNotUniqueReportsTheSecondUse() {
    TransformerModel model = model();
    model.getContent().getOrCreateTypeMapping().add(mapping("string", "StringType"));
    model.getContent().getOrCreateTypeMapping().add(mapping("date", "DateType"));
    model.getContent().getOrCreateTypeMapping().add(mapping("string", "StringType"));

    List<ModelValidationError> errors = validate(new TransformerTypeMappingValidator(), model);

    assertEquals(List.of(TransformerElementIds.typeMapping(2, "xsdType")), ids(errors));
  }

  @Test
  void aConfigurationGroupNeedsAMatchingSuperType() {
    TransformerModel model = model();
    TypeMappingEntry onDate = mapping("a", "DateType");
    onDate.getOrCreateStringType().setMinLength(2);
    TypeMappingEntry numberOnString = mapping("b", "StringType");
    numberOnString.getOrCreateNumberType().setPositivesOnly(true);
    TypeMappingEntry enumOnNumber = mapping("c", "NumberType");
    enumOnNumber.getOrCreateEnumerationType().setAlphabeticalSorting(true);
    TypeMappingEntry fine = mapping("d", "StringWithXsPatternType");
    fine.getOrCreateStringType().setMaxLength(4);
    TypeMappingEntry fineEnum = mapping("e", "EnumForBooleanType");
    fineEnum.getOrCreateEnumerationType().setAlphabeticalSorting(true);
    TypeMappingEntry emptyGroup = mapping("f", "DateType");
    emptyGroup.getOrCreateStringType();
    model.getContent().getOrCreateTypeMapping().addAll(List.of(onDate, numberOnString, enumOnNumber, fine, fineEnum, emptyGroup));

    List<ModelValidationError> errors = validate(new TransformerTypeMappingValidator(), model);

    assertEquals(List.of(TransformerElementIds.typeMapping(0, "a12Type"), TransformerElementIds.typeMapping(1, "a12Type"),
        TransformerElementIds.typeMapping(2, "a12Type")), ids(errors));
    assertTrue(errors.get(0).message().contains("StringType") && errors.get(0).message().contains("DateType"), errors.get(0).message());
  }

  @Test
  void stringRulesOfTheMeta() {
    TransformerModel model = model();
    TypeMappingEntry lineBreakAndLength1 = mapping("a", "StringType");
    lineBreakAndLength1.getOrCreateStringType().setLineBreaksPermitted(true);
    lineBreakAndLength1.getOrCreateStringType().setMaxLength(1);
    TypeMappingEntry lineBreakAndSorting = mapping("b", "StringType");
    lineBreakAndSorting.getOrCreateStringType().setLineBreaksPermitted(true);
    lineBreakAndSorting.getOrCreateStringType().setAlphabeticalSorting(true);
    TypeMappingEntry minAboveMax = mapping("c", "StringType");
    minAboveMax.getOrCreateStringType().setMinLength(5);
    minAboveMax.getOrCreateStringType().setMaxLength(4);
    TypeMappingEntry okay = mapping("d", "StringType");
    okay.getOrCreateStringType().setLineBreaksPermitted(true);
    okay.getOrCreateStringType().setMinLength(4);
    okay.getOrCreateStringType().setMaxLength(4);
    model.getContent().getOrCreateTypeMapping().addAll(List.of(lineBreakAndLength1, lineBreakAndSorting, minAboveMax, okay));

    List<ModelValidationError> errors = validate(new TransformerTypeMappingConfigValidator(), model);

    assertEquals(List.of(TransformerElementIds.typeMapping(0, "StringType/lineBreaksPermitted"),
        TransformerElementIds.typeMapping(1, "StringType/lineBreaksPermitted"),
        TransformerElementIds.typeMapping(2, "StringType/minLength")), ids(errors));
  }

  @Test
  void stringRulesOnlyApplyToStringType() {
    TransformerModel model = model();
    TypeMappingEntry other = mapping("a", "StringWithXsPatternType");
    other.getOrCreateStringType().setMinLength(5);
    other.getOrCreateStringType().setMaxLength(4);
    model.getContent().getOrCreateTypeMapping().add(other);

    assertTrue(validate(new TransformerTypeMappingConfigValidator(), model).isEmpty(),
        "SME's String rules test [a12Type] == \"StringType\"");
  }

  @Test
  void numberRulesOfTheMeta() {
    TransformerModel model = model();
    TypeMappingEntry digits = mapping("a", "NumberType");
    digits.getOrCreateNumberType().setMinFractionalDigits(3);
    digits.getOrCreateNumberType().setMaxFractionalDigits(2);
    TypeMappingEntry values = mapping("b", "NumberType");
    values.getOrCreateNumberType().setMinValue(new BigDecimal("10.5"));
    values.getOrCreateNumberType().setMaxValue(new BigDecimal("10.4"));
    TypeMappingEntry lengths = mapping("c", "NumberType");
    lengths.getOrCreateNumberType().setMinLength(9);
    lengths.getOrCreateNumberType().setMaxLength(8);
    TypeMappingEntry tooShort = mapping("d", "NumberType");
    tooShort.getOrCreateNumberType().setMinFractionalDigits(3);
    tooShort.getOrCreateNumberType().setMaxLength(4);
    TypeMappingEntry shortButNoDecimals = mapping("e", "NumberType");
    shortButNoDecimals.getOrCreateNumberType().setMinFractionalDigits(0);
    shortButNoDecimals.getOrCreateNumberType().setMaxLength(1);
    model.getContent().getOrCreateTypeMapping().addAll(List.of(digits, values, lengths, tooShort, shortButNoDecimals));

    List<ModelValidationError> errors = validate(new TransformerTypeMappingConfigValidator(), model);

    assertEquals(List.of(TransformerElementIds.typeMapping(0, "NumberType/maxFractionalDigits"),
        TransformerElementIds.typeMapping(1, "NumberType/minValue"),
        TransformerElementIds.typeMapping(2, "NumberType/minLength"),
        TransformerElementIds.typeMapping(3, "NumberType/maxLength")), ids(errors));
  }

  @Test
  void anAmountHasZeroOrTwoFixedDecimalPlaces() {
    TransformerModel model = model();
    for (int[] places : new int[][] {{2, 2}, {0, 0}, {1, 1}, {0, 2}, {2, 3}}) {
      TypeMappingEntry amount = mapping("t" + places[0] + places[1], "NumberType");
      amount.getOrCreateNumberType().setTrait("Amount");
      amount.getOrCreateNumberType().setMinFractionalDigits(places[0]);
      amount.getOrCreateNumberType().setMaxFractionalDigits(places[1]);
      model.getContent().getOrCreateTypeMapping().add(amount);
    }

    List<ModelValidationError> errors = validate(new TransformerTypeMappingConfigValidator(), model);

    // {2,2} and {0,0} are the allowed fixed places; {1,1} is fixed but not 0/2, {0,2} and {2,3} are not fixed.
    assertEquals(List.of(TransformerElementIds.typeMapping(2, "NumberType/minFractionalDigits"),
        TransformerElementIds.typeMapping(3, "NumberType/minFractionalDigits"),
        TransformerElementIds.typeMapping(4, "NumberType/minFractionalDigits")), ids(errors));
  }

  @Test
  void theValueRangesOfTheFieldTypeDefinitionsAreEnforced() {
    TransformerModel model = model();
    TypeMappingEntry string = mapping("a", "StringType");
    string.getOrCreateStringType().setMinLength(0);
    string.getOrCreateStringType().setMaxLength(100000);
    string.getOrCreateStringType().setPattern("x".repeat(1001));
    TypeMappingEntry number = mapping("b", "NumberType");
    number.getOrCreateNumberType().setMinFractionalDigits(-1);
    number.getOrCreateNumberType().setMaxFractionalDigits(15);
    number.getOrCreateNumberType().setMaxIntegerDigits(0);
    number.getOrCreateNumberType().setMinValue(new BigDecimal("0.123456789012345"));
    number.getOrCreateNumberType().setMaxValue(new BigDecimal("1.50000000000000000"));
    model.getContent().getOrCreateTypeMapping().addAll(List.of(string, number));

    List<ModelValidationError> errors = validate(new TransformerTypeMappingConfigValidator(), model);

    assertEquals(List.of(TransformerElementIds.typeMapping(0, "StringType/minLength"),
        TransformerElementIds.typeMapping(0, "StringType/maxLength"),
        TransformerElementIds.typeMapping(0, "StringType/pattern"),
        TransformerElementIds.typeMapping(1, "NumberType/minFractionalDigits"),
        TransformerElementIds.typeMapping(1, "NumberType/maxFractionalDigits"),
        TransformerElementIds.typeMapping(1, "NumberType/maxIntegerDigits"),
        TransformerElementIds.typeMapping(1, "NumberType/minValue")), ids(errors));
    assertTrue(errors.get(0).message().contains("Min. Length") && errors.get(0).message().contains("99999"), errors.get(0).message());
  }

  // ---- Element selection ----------------------------------------------------------------------------------------

  @Test
  void renamePathsNeedBothValuesAValidNameAndAreUnique() {
    TransformerModel model = model();
    model.getContent().getOrCreateRenamePaths().add(rename(null, null));
    model.getContent().getOrCreateRenamePaths().add(rename("/A/B", "9bad"));
    model.getContent().getOrCreateRenamePaths().add(rename("/A/C", "Good-name.1"));
    model.getContent().getOrCreateRenamePaths().add(rename("/A/B", "Other"));

    List<ModelValidationError> errors = validate(new TransformerElementSelectionValidator(), model);

    assertEquals(List.of(TransformerElementIds.renamePath(0, "OriginalPath"), TransformerElementIds.renamePath(0, "NewElementName"),
        TransformerElementIds.renamePath(1, "NewElementName"), TransformerElementIds.renamePath(3, "OriginalPath")), ids(errors));
    assertTrue(errors.get(2).message().contains("9bad"), errors.get(2).message());
  }

  @Test
  void deletePathsNeedAValueAndAreUnique() {
    TransformerModel model = model();
    model.getContent().getOrCreateDeletePaths().addAll(List.of(delete(null), delete("/A/B"), delete("/A/B"), delete("/A/C")));

    assertEquals(List.of(TransformerElementIds.deletePath(0), TransformerElementIds.deletePath(2)),
        ids(validate(new TransformerElementSelectionValidator(), model)));
  }

  // ---- Custom texts ---------------------------------------------------------------------------------------------

  @Test
  void patternErrorsNeedAPatternThatIsUnique() {
    TransformerModel model = model();
    model.getContent().getOrCreatePatternErrors().addAll(List.of(pattern(null, null), pattern("\\d+", null), pattern("\\d+", null)));

    assertEquals(List.of(TransformerElementIds.patternError(0, "pattern"), TransformerElementIds.patternError(2, "pattern")),
        ids(validate(new TransformerCustomTextsValidator(), model)));
  }

  @Test
  void replaceRequiresReplacement() {
    TransformerModel model = model();
    PatternError noReplacement = pattern("a", "REPLACE");
    PatternError withReplacement = pattern("b", "REPLACE");
    withReplacement.setReplacement("[0-9]+");
    PatternError update = pattern("c", "UPDATE_MESSAGE");
    PatternError suppress = pattern("d", "SUPPRESS");
    model.getContent().getOrCreatePatternErrors().addAll(List.of(noReplacement, withReplacement, update, suppress));

    List<ModelValidationError> errors = validate(new TransformerCustomTextsValidator(), model);

    assertEquals(List.of(TransformerElementIds.patternError(0, "replacement")), ids(errors));
    assertTrue(errors.get(0).message().contains("replacement"), errors.get(0).message());
  }

  @Test
  void aTextLocaleMustBeALocaleOfTheModel() {
    TransformerModel model = model();
    PatternError error = pattern("a", null);
    error.getOrCreateErrors().addAll(List.of(label("en", "x"), label("fr", "y"), label(null, "z")));
    EnumLabel enumLabel = enumLabel("V", null, null);
    enumLabel.getOrCreateReplacements().addAll(List.of(label("de", "a"), label("it", "b")));
    model.getContent().getOrCreatePatternErrors().add(error);
    model.getContent().getOrCreateEnumLabels().add(enumLabel);

    List<ModelValidationError> errors = validate(new TransformerCustomTextsValidator(), model);

    assertEquals(List.of(TransformerElementIds.patternErrorLocale(0, 1), TransformerElementIds.patternErrorLocale(0, 2),
        TransformerElementIds.enumLabelLocale(0, 1)), ids(errors));
    assertTrue(errors.get(0).message().contains("fr"), errors.get(0).message());
  }

  @Test
  void localesAreNotCheckedWhileTheModelHasNone() {
    TransformerModel model = model();
    model.setLocales(new ArrayList<>());
    PatternError error = pattern("a", null);
    error.getOrCreateErrors().add(label("fr", "y"));
    model.getContent().getOrCreatePatternErrors().add(error);

    assertTrue(validate(new TransformerCustomTextsValidator(), model).isEmpty(),
        "SME: AtLeastOneGroupFilled(/header/locales*) is part of the rule");
  }

  @Test
  void enumLabelNeedsAValueAndTypeAndPathExcludeEachOther() {
    TransformerModel model = model();
    model.getContent().getOrCreateEnumLabels().addAll(List.of(
        enumLabel(null, null, null),
        enumLabel("A", "typedef", "/path"),
        enumLabel("B", "typedef", null),
        enumLabel("C", null, "/path")));

    assertEquals(List.of(TransformerElementIds.enumLabel(0, "value"), TransformerElementIds.enumLabel(1, "typeDefinitionId")),
        ids(validate(new TransformerCustomTextsValidator(), model)));
  }

  @Test
  void enumLabelNotUniqueComparesValueTypeAndPathTogether() {
    TransformerModel model = model();
    model.getContent().getOrCreateEnumLabels().addAll(List.of(
        enumLabel("A", null, null),
        enumLabel("A", "typedef", null),
        enumLabel("A", null, "/path"),
        enumLabel("A", null, null),
        enumLabel("A", "typedef", null)));

    assertEquals(List.of(TransformerElementIds.enumLabel(3, "value"), TransformerElementIds.enumLabel(4, "value")),
        ids(validate(new TransformerCustomTextsValidator(), model)));
  }

  // ---- Configuration and code lists -----------------------------------------------------------------------------

  @Test
  void onlyOneSupportedCharactersSourceMayBeGiven() {
    TransformerModel model = model();
    TransformerConfiguration configuration = model.getContent().getOrCreateConfiguration();
    configuration.setSupportedCharacters("[a-z]");
    assertTrue(validate(new TransformerConfigurationValidator(), model).isEmpty());

    configuration.setSupportedCharactersTypeInXsd("datatypeC");
    assertEquals(List.of(TransformerElementIds.SUPPORTED_CHARACTERS), ids(validate(new TransformerConfigurationValidator(), model)));
  }

  @Test
  void codeListsNeedAUriVersionListAndTheElementNames() {
    TransformerModel model = model();
    CodeLists codeLists = new CodeLists();
    model.getContent().setCodeLists(codeLists);
    assertTrue(validate(new TransformerConfigurationValidator(), model).isEmpty(), "an empty CodeLists is not filled");

    CodeLists.CodeListValue identifier = new CodeLists.CodeListValue();
    identifier.setValue("enumValues");
    codeLists.setCodeIdentifiers(List.of(identifier));
    assertEquals(List.of(TransformerElementIds.CODE_LISTS_URI_VERSION_LIST, TransformerElementIds.CODE_LISTS_ELEMENT_NAMES),
        ids(validate(new TransformerConfigurationValidator(), model)));

    CodeLists.UriVersion uri = new CodeLists.UriVersion();
    uri.setUri("urn:x");
    codeLists.setUriVersionList(List.of(uri));
    CodeLists.CodeListValue elementName = new CodeLists.CodeListValue();
    elementName.setValue("codeElementName");
    codeLists.setElementNamesInXsd(List.of(elementName));
    assertTrue(validate(new TransformerConfigurationValidator(), model).isEmpty());
  }

  @Test
  void everyValidatorIgnoresOtherModelTypesAndAModelWithoutContent() {
    TransformerModel noContent = new TransformerModel();
    noContent.setId("X_TfM");
    List<ModelValidator> validators = List.of(new TransformerSourceValidator(), new TransformerTypeMappingValidator(),
        new TransformerTypeMappingConfigValidator(), new TransformerElementSelectionValidator(), new TransformerCustomTextsValidator(),
        new TransformerConfigurationValidator());
    for (ModelValidator validator : validators) {
      assertTrue(validator.validate(noContent, TestModels.context(noContent)).isEmpty(), validator.getClass().getSimpleName());
      de.a12.studio.models.selectionmodel.SelectionModel other = new de.a12.studio.models.selectionmodel.SelectionModel();
      other.setId("Y_SeM");
      assertTrue(validator.validate(other, TestModels.context(other)).isEmpty(), validator.getClass().getSimpleName());
    }
  }

  // ---- helpers --------------------------------------------------------------------------------------------------

  private static TransformerModel model() {
    TransformerModel model = new TransformerModel();
    model.setId("Test_TfM");
    model.setContent(new TransformerModelContent());
    Locale de = new Locale();
    de.setCode("de");
    Locale en = new Locale();
    en.setCode("en");
    model.setLocales(new ArrayList<>(List.of(de, en)));
    return model;
  }

  private static List<ModelValidationError> validate(ModelValidator validator, TransformerModel model) {
    return validator.validate(model, TestModels.context(model));
  }

  private static ValidationContext context(TransformerModel model, Project project) {
    return new ValidationContext(project, new de.a12.studio.models.projects.ProjectItem(new File(model.getId() + ".json")),
        List.of(), List.of(), model);
  }

  private static List<String> ids(List<ModelValidationError> errors) {
    return errors.stream().map(ModelValidationError::elementId).toList();
  }

  private static TypeMappingEntry mapping(String xsdType, String a12Type) {
    TypeMappingEntry entry = new TypeMappingEntry();
    entry.setXsdType(xsdType);
    entry.setA12Type(a12Type);
    return entry;
  }

  private static RenamePath rename(String original, String newName) {
    RenamePath rename = new RenamePath();
    rename.setOriginalPath(original);
    rename.setNewElementName(newName);
    return rename;
  }

  private static DeletePath delete(String path) {
    DeletePath delete = new DeletePath();
    delete.setPath(path);
    return delete;
  }

  private static PatternError pattern(String pattern, String action) {
    PatternError error = new PatternError();
    error.setPattern(pattern);
    error.setAction(action);
    return error;
  }

  private static EnumLabel enumLabel(String value, String typeDefinitionId, String enumFieldPath) {
    EnumLabel label = new EnumLabel();
    label.setValue(value);
    label.setTypeDefinitionId(typeDefinitionId);
    label.setEnumFieldPath(enumFieldPath);
    return label;
  }

  private static Label label(String locale, String text) {
    Label label = new Label();
    label.setLocale(locale);
    label.setText(text);
    return label;
  }
}
