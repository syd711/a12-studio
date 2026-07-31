package de.a12.studio.modelsvalidation.validators;

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
}
