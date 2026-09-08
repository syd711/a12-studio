package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per pre-existing document model validator, each loading a fixture that contains exactly that error. */
class DocumentModelValidatorsTest {

  private DocumentModel load(String name) {
    return TestModels.load("/documentmodel/" + name + ".json", DocumentModel.class);
  }

  @Test
  void missingReferenceValidatorReportsInvalidIndexField() {
    DocumentModel model = load("MissingReferenceValidator_invalid");
    List<ModelValidationError> errors = new MissingReferenceValidator().validate(model, TestModels.context(model));

    assertFalse(errors.isEmpty(), "An index field that doesn't resolve must be reported");
  }

  /**
   * Regression test: a {@code TypeDefType} field pointing at a type definition owned by an Included model
   * (not the field's own model) must resolve via {@link ElementIndex#effectiveFieldType}'s
   * {@link TransitiveTypeDefinitions} fallback, not get wrongly flagged "Missing Type Definition" just
   * because it isn't in the field's own model's local {@code typeDefinitions} list.
   */
  @Test
  void missingReferenceValidatorAcceptsATypeDefinitionInheritedThroughAnInclude() {
    TypeDefinition productType = new TypeDefinition();
    productType.setId("typedef_1");
    productType.setName("ProductType");
    productType.setFieldType(new StringFieldType());

    DocumentModel owner = new DocumentModel();
    owner.setId("Owner_DM");
    DocumentModelContent ownerContent = new DocumentModelContent();
    ownerContent.setModelRoot(new ModelRoot());
    ownerContent.setTypeDefinitions(new ArrayList<>(List.of(productType)));
    owner.setContent(ownerContent);

    GroupElement includeGroup = new GroupElement();
    includeGroup.setId("include_owner");
    includeGroup.setName("Included");
    GroupConfig includeConfig = new GroupConfig();
    IncludeConfig includeReference = new IncludeConfig();
    includeReference.setReference("Owner_DM");
    includeConfig.setIncludeConfig(includeReference);
    includeGroup.setGroup(includeConfig);

    FieldElement field = new FieldElement();
    field.setId("field_product_type");
    field.setName("ProductType");
    FieldConfig fieldConfig = new FieldConfig();
    TypeDefFieldType typeDefFieldType = new TypeDefFieldType();
    typeDefFieldType.getTypeDefType().setTypeDefinitionId("typedef_1");
    fieldConfig.setFieldType(typeDefFieldType);
    field.setField(fieldConfig);

    GroupElement businessGroup = new GroupElement();
    businessGroup.setId("business_group");
    businessGroup.setName("Business");
    GroupConfig businessConfig = new GroupConfig();
    businessConfig.setElements(new ArrayList<>(List.of(field)));
    businessGroup.setGroup(businessConfig);

    DocumentModel company = new DocumentModel();
    company.setId("Company_DM");
    DocumentModelContent companyContent = new DocumentModelContent();
    ModelRoot companyModelRoot = new ModelRoot();
    companyModelRoot.setRootGroups(List.of(businessGroup, includeGroup));
    companyContent.setModelRoot(companyModelRoot);
    company.setContent(companyContent);

    List<ModelValidationError> errors = new MissingReferenceValidator()
        .validate(company, TestModels.contextWithDocumentModels(company, owner));

    assertTrue(errors.stream().noneMatch(error -> "field_product_type".equals(error.elementId())),
        "A TypeDefType field referencing a type definition inherited via Include must not be reported as missing");
  }

  /** Same setup as above, but without the Owner_DM sibling - the reference genuinely can't resolve then. */
  @Test
  void missingReferenceValidatorStillReportsATypeDefinitionThatTrulyDoesNotResolve() {
    GroupElement includeGroup = new GroupElement();
    includeGroup.setId("include_owner");
    includeGroup.setName("Included");
    GroupConfig includeConfig = new GroupConfig();
    IncludeConfig includeReference = new IncludeConfig();
    includeReference.setReference("Owner_DM");
    includeConfig.setIncludeConfig(includeReference);
    includeGroup.setGroup(includeConfig);

    FieldElement field = new FieldElement();
    field.setId("field_product_type");
    field.setName("ProductType");
    FieldConfig fieldConfig = new FieldConfig();
    TypeDefFieldType typeDefFieldType = new TypeDefFieldType();
    typeDefFieldType.getTypeDefType().setTypeDefinitionId("typedef_1");
    fieldConfig.setFieldType(typeDefFieldType);
    field.setField(fieldConfig);

    GroupElement businessGroup = new GroupElement();
    businessGroup.setId("business_group");
    businessGroup.setName("Business");
    GroupConfig businessConfig = new GroupConfig();
    businessConfig.setElements(new ArrayList<>(List.of(field)));
    businessGroup.setGroup(businessConfig);

    DocumentModel company = new DocumentModel();
    company.setId("Company_DM");
    DocumentModelContent companyContent = new DocumentModelContent();
    ModelRoot companyModelRoot = new ModelRoot();
    companyModelRoot.setRootGroups(List.of(businessGroup, includeGroup));
    companyContent.setModelRoot(companyModelRoot);
    company.setContent(companyContent);

    List<ModelValidationError> errors = new MissingReferenceValidator().validate(company, TestModels.context(company));

    assertTrue(errors.stream().anyMatch(error -> "field_product_type".equals(error.elementId())),
        "A TypeDefType field whose type definition can't be resolved anywhere must still be reported");
  }

  /**
   * A model with its own local type definitions cannot Include a model whose type definitions are all
   * imported (mirrors SME's {@code IncludeDifferentTypeDefinitionMode}).
   */
  @Test
  void includeTypeDefinitionModeValidatorReportsMismatchedMode() {
    TypeDefinition ownedByImportSource = new TypeDefinition();
    ownedByImportSource.setId("typedef_source");
    ownedByImportSource.setName("SourceType");
    ownedByImportSource.setFieldType(new StringFieldType());

    DocumentModel importSource = new DocumentModel();
    importSource.setId("Import_Source_TdM");
    DocumentModelContent importSourceContent = new DocumentModelContent();
    importSourceContent.setModelRoot(new ModelRoot());
    importSourceContent.setTypeDefinitions(new ArrayList<>(List.of(ownedByImportSource)));
    importSource.setContent(importSourceContent);

    ModelReference importReference = new ModelReference();
    importReference.setPurpose(ModelReference.PURPOSE_TYPE_DEFINITIONS);
    importReference.setReference("Import_Source_TdM");

    DocumentModel includedModel = new DocumentModel();
    includedModel.setId("Included_DM");
    DocumentModelContent includedContent = new DocumentModelContent();
    includedContent.setModelRoot(new ModelRoot());
    includedModel.setContent(includedContent);
    includedModel.setModelReferences(new ArrayList<>(List.of(importReference)));

    TypeDefinition ownType = new TypeDefinition();
    ownType.setId("typedef_own");
    ownType.setName("OwnType");
    ownType.setFieldType(new StringFieldType());

    GroupElement includeGroup = new GroupElement();
    includeGroup.setId("include_group");
    includeGroup.setName("Included");
    GroupConfig includeConfig = new GroupConfig();
    IncludeConfig includeReference = new IncludeConfig();
    includeReference.setReference("Included_DM");
    includeConfig.setIncludeConfig(includeReference);
    includeGroup.setGroup(includeConfig);

    DocumentModel model = new DocumentModel();
    model.setId("Model_DM");
    DocumentModelContent modelContent = new DocumentModelContent();
    ModelRoot modelRoot = new ModelRoot();
    modelRoot.setRootGroups(List.of(includeGroup));
    modelContent.setModelRoot(modelRoot);
    modelContent.setTypeDefinitions(new ArrayList<>(List.of(ownType)));
    model.setContent(modelContent);

    List<ModelValidationError> errors = new IncludeTypeDefinitionModeValidator()
        .validate(model, TestModels.contextWithDocumentModels(model, includedModel, importSource));

    assertEquals(1, errors.size());
    assertEquals("include_group", errors.get(0).elementId());
  }

  /**
   * Regression test: a blank {@code typeDefinitionId} used to be reported by both
   * {@code MissingReferenceValidator} ("Missing Type Definition") and {@code BasicConsistencyValidator}
   * ("id ... not set properly") - two errors for the same problem. Now only {@code MissingReferenceValidator}
   * reports it, with a message distinguishing "must be specified" from "does not exist" (mirrors SME's
   * {@code A12_TYPE_DEFINITION_MISSING} vs. {@code A12_TYPE_DEFINITION_INVALID}).
   */
  @Test
  void missingReferenceValidatorDistinguishesUnspecifiedFromUnresolvableTypeDefinition() {
    FieldElement unspecified = new FieldElement();
    unspecified.setId("field_unspecified");
    unspecified.setName("Unspecified");
    FieldConfig unspecifiedConfig = new FieldConfig();
    unspecifiedConfig.setFieldType(new TypeDefFieldType());
    unspecified.setField(unspecifiedConfig);

    FieldElement unresolvable = new FieldElement();
    unresolvable.setId("field_unresolvable");
    unresolvable.setName("Unresolvable");
    FieldConfig unresolvableConfig = new FieldConfig();
    TypeDefFieldType unresolvableType = new TypeDefFieldType();
    unresolvableType.getTypeDefType().setTypeDefinitionId("does_not_exist");
    unresolvableConfig.setFieldType(unresolvableType);
    unresolvable.setField(unresolvableConfig);

    GroupElement group = new GroupElement();
    group.setId("group_root");
    group.setName("Root");
    GroupConfig groupConfig = new GroupConfig();
    groupConfig.setElements(new ArrayList<>(List.of(unspecified, unresolvable)));
    group.setGroup(groupConfig);

    DocumentModel model = new DocumentModel();
    model.setId("Model_DM");
    DocumentModelContent content = new DocumentModelContent();
    ModelRoot modelRoot = new ModelRoot();
    modelRoot.setRootGroups(List.of(group));
    content.setModelRoot(modelRoot);
    model.setContent(content);

    List<ModelValidationError> missingReferenceErrors = new MissingReferenceValidator().validate(model, TestModels.context(model));
    assertEquals(2, missingReferenceErrors.size());
    assertTrue(missingReferenceErrors.stream().anyMatch(e -> "field_unspecified".equals(e.elementId()) && e.message().contains("must be specified")));
    assertTrue(missingReferenceErrors.stream().anyMatch(e -> "field_unresolvable".equals(e.elementId()) && e.message().contains("does not exist")));

    List<ModelValidationError> basicConsistencyErrors = new BasicConsistencyValidator().validate(model, TestModels.context(model));
    assertTrue(basicConsistencyErrors.isEmpty(), "BasicConsistencyValidator must no longer duplicate the TypeDefType check");
  }

  @Test
  void schemaVersionValidatorReportsUnparsableVersion() {
    DocumentModel model = load("SchemaVersionValidator_invalid");
    List<ModelValidationError> errors = new SchemaVersionValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    // Schema version problems are model-level: they carry no element id (and are dropped by the
    // ValidatorRunner for the UI), but the validator itself must still find them.
    assertNull(errors.get(0).elementId());
  }

  @Test
  void duplicateIdValidatorReportsDuplicateElementId() {
    DocumentModel model = load("DuplicateIdValidator_invalid");
    List<ModelValidationError> errors = new DuplicateIdValidator().validate(model, TestModels.context(model));

    assertFalse(errors.isEmpty());
    assertEquals("field_same", errors.get(0).elementId());
  }

  @Test
  void numberFieldValueLimitValidatorReportsExcessiveMaxValue() {
    DocumentModel model = load("NumberFieldValueLimitValidator_invalid");
    List<ModelValidationError> errors = new NumberFieldValueLimitValidator().validate(model, TestModels.context(model));

    assertFalse(errors.isEmpty(), "A max value beyond the kernel's number limit must be reported");
  }

  @Test
  void enumerationValuesValidatorReportsEmptyEnumeration() {
    DocumentModel model = load("EnumerationValuesValidator_invalid");
    List<ModelValidationError> errors = new EnumerationValuesValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("field_choice", errors.get(0).elementId());
  }

  @Test
  void enumerationValuesValidatorReportsTooFewValuesInMultiSelect() {
    DocumentModel model = load("EnumerationValuesValidator_multiSelectInvalid");
    List<ModelValidationError> errors = new EnumerationValuesValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size(), "A multi-select value field with only one value must be reported");
    assertEquals("field_choice", errors.get(0).elementId());
  }

  @Test
  void multiSelectGroupValidatorReportsInvalidMultiSelectGroup() {
    DocumentModel model = load("MultiSelectGroupValidator_invalid");
    List<ModelValidationError> errors = new MultiSelectGroupValidator().validate(model, TestModels.context(model));

    assertFalse(errors.isEmpty(), "A multi-select group with repeatability 1 must be reported");
  }

  @Test
  void attachmentGroupValidatorReportsEmptyAttachmentGroup() {
    DocumentModel model = load("AttachmentGroupValidator_invalid");
    List<ModelValidationError> errors = new AttachmentGroupValidator().validate(model, TestModels.context(model));

    assertFalse(errors.isEmpty(), "An attachment group without the mandatory fields must be reported");
  }

  @Test
  void basicConsistencyValidatorReportsRuleWithEmptyCodeAndCondition() {
    DocumentModel model = load("BasicConsistencyValidator_invalid");
    List<ModelValidationError> errors = new BasicConsistencyValidator().validate(model, TestModels.context(model));

    assertFalse(errors.isEmpty(), "A rule with an empty error code/condition must be reported");
    assertTrue(errors.stream().allMatch(error -> "rule_broken".equals(error.elementId())));
  }

  @Test
  void timeZoneValidatorReportsUtcModelInBerlinWorkspace() {
    DocumentModel model = load("TimeZoneValidator_invalid");
    DocumentModel berlinModel = load("TimeZoneValidator_other");
    List<ModelValidationError> errors = new TimeZoneValidator().validate(model,
        TestModels.contextWithDocumentModels(model, berlinModel));

    assertEquals(1, errors.size());
    assertEquals(TimeZoneValidator.ELEMENT_ID, errors.get(0).elementId());

    assertTrue(new TimeZoneValidator().validate(model, TestModels.context(model)).isEmpty(),
        "Without a Europe/Berlin sibling there must be no error");
  }

  @Test
  void stringPatternErrorMessageValidatorReportsPatternWithoutErrorMessage() {
    DocumentModel model = load("StringPatternErrorMessageValidator_invalid");
    List<ModelValidationError> errors = new StringPatternErrorMessageValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("field_pattern", errors.get(0).elementId());
  }

  @Test
  void stringPatternErrorMessageValidatorReportsMissingErrorMessageForOneLocale() {
    DocumentModel model = load("StringPatternErrorMessageValidator_missingOneLocale");
    List<ModelValidationError> errors = new StringPatternErrorMessageValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size(), "Only the missing 'de' locale must be reported");
    assertEquals("field_pattern", errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("de"), "Error message must name the missing locale");
  }

  @Test
  void stringPatternErrorMessageValidatorAcceptsPatternWithErrorMessagesForAllLocales() {
    DocumentModel model = load("StringPatternErrorMessageValidator_allLocalesCovered");
    List<ModelValidationError> errors = new StringPatternErrorMessageValidator().validate(model, TestModels.context(model));

    assertTrue(errors.isEmpty(), "A field that provides error messages for every declared locale must not be reported");
  }

  /**
   * Regression test for the Type Definition Model coverage gap: a standalone TdM has no {@code FieldElement}
   * anywhere (its {@code modelRoot} is always empty), so before this fixture's validator started walking
   * {@code content.typeDefinitions} directly, a pattern with no error message on the type definition itself
   * was never reported at all.
   */
  @Test
  void stringPatternErrorMessageValidatorReportsPatternOnATypeDefinitionWithNoFields() {
    DocumentModel model = load("StringPatternErrorMessageValidator_typeDefinitionInvalid");
    List<ModelValidationError> errors = new StringPatternErrorMessageValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("typedef_code", errors.get(0).elementId());
  }

  @Test
  void numberFieldValueLimitValidatorReportsExcessiveMaxValueOnATypeDefinitionWithNoFields() {
    DocumentModel model = load("NumberFieldValueLimitValidator_typeDefinitionInvalid");
    List<ModelValidationError> errors = new NumberFieldValueLimitValidator().validate(model, TestModels.context(model));

    assertFalse(errors.isEmpty(), "A max value beyond the kernel's number limit must be reported even with no fields");
    assertEquals("typedef_amount", errors.get(0).elementId());
  }

  @Test
  void basicConsistencyValidatorReportsDuplicateEnumValueOnATypeDefinitionWithNoFields() {
    DocumentModel model = load("BasicConsistencyValidator_typeDefinitionDuplicateEnum");
    List<ModelValidationError> errors = new BasicConsistencyValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("typedef_country", errors.get(0).elementId());
  }

  @Test
  void stringTypeConfigValidatorReportsInvalidRegexOnATypeDefinition() {
    DocumentModel model = load("StringTypeConfigValidator_typeDefinitionInvalid");
    List<ModelValidationError> errors = new StringTypeConfigValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("typedef_email", errors.get(0).elementId());
  }

  @Test
  void numberTypeConfigValidatorReportsMinValueBiggerThanMaxValueOnATypeDefinition() {
    DocumentModel model = load("NumberTypeConfigValidator_typeDefinitionInvalid");
    List<ModelValidationError> errors = new NumberTypeConfigValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("typedef_quantity", errors.get(0).elementId());
  }

  @Test
  void enumerationTypeConfigValidatorReportsEmptyValueOnATypeDefinition() {
    DocumentModel model = load("EnumerationTypeConfigValidator_typeDefinitionInvalid");
    List<ModelValidationError> errors = new EnumerationTypeConfigValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("typedef_country", errors.get(0).elementId());
  }

  @Test
  void customFieldTypeConfigValidatorReportsMissingCodenameOnATypeDefinition() {
    DocumentModel model = load("CustomFieldTypeConfigValidator_typeDefinitionInvalid");
    List<ModelValidationError> errors = new CustomFieldTypeConfigValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("typedef_custom", errors.get(0).elementId());
  }

  @Test
  void enumerationTypeConfigValidatorReportsMissingLocaleForCustomErrorMessage() {
    DocumentModel model = load("EnumerationTypeConfigValidator_errorMessageMissingLocale");
    List<ModelValidationError> errors = new EnumerationTypeConfigValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size(), "Only the missing 'de' locale must be reported");
    assertEquals("typedef_country", errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("de"), "Error message must name the missing locale");
  }

  @Test
  void dateFormatConfigValidatorReportsMissingFormatOnATypeDefinition() {
    DocumentModel model = load("DateFormatConfigValidator_typeDefinitionInvalid");
    List<ModelValidationError> errors = new DateFormatConfigValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("typedef_timestamp", errors.get(0).elementId());
  }
}
