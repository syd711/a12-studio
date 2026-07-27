package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per model-type-agnostic validator, each loading a fixture that contains exactly that error. */
class CrossCuttingValidatorsTest {

  @Test
  void missingLocaleValidatorReportsEmptyLocales() {
    RelationshipModel model = TestModels.load("/crosscutting/MissingLocaleValidator_invalid.json", RelationshipModel.class);
    List<ModelValidationError> errors = new MissingLocaleValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals(MissingLocaleValidator.ELEMENT_ID, errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("at least one locale"));
  }

  @Test
  void localeCodeValidatorReportsInvalidCode() {
    RelationshipModel model = TestModels.load("/crosscutting/LocaleCodeValidator_invalid.json", RelationshipModel.class);
    List<ModelValidationError> errors = new LocaleCodeValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals(LocaleCodeValidator.ELEMENT_ID, errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("english"));
  }

  @Test
  void modelIdFilenameValidatorReportsMismatch() {
    RelationshipModel model = TestModels.load("/crosscutting/ModelIdFilenameValidator_invalid.json", RelationshipModel.class);
    // The fixture's header id is "SomeOtherName" while the file is named after the validator.
    List<ModelValidationError> errors = new ModelIdFilenameValidator().validate(model,
        TestModels.contextWithFileName("ModelIdFilenameValidator_invalid.json"));

    assertEquals(1, errors.size());
    assertEquals(ModelIdFilenameValidator.ELEMENT_ID, errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("SomeOtherName"));

    assertTrue(new ModelIdFilenameValidator().validate(model, TestModels.contextWithFileName("SomeOtherName.json")).isEmpty(),
        "A matching file name must produce no error");
  }

  @Test
  void uniqueModelIdValidatorReportsDuplicate() {
    RelationshipModel model = TestModels.load("/crosscutting/UniqueModelIdValidator_invalid.json", RelationshipModel.class);
    RelationshipModel duplicate = TestModels.load("/crosscutting/UniqueModelIdValidator_invalid.json", RelationshipModel.class);
    List<ModelValidationError> errors = new UniqueModelIdValidator().validate(model,
        TestModels.contextWithOtherModels(model, duplicate));

    assertEquals(1, errors.size());
    assertNotNull(errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("already exists"));

    assertTrue(new UniqueModelIdValidator().validate(model, TestModels.context(model)).isEmpty(),
        "Without a sibling of the same id there must be no error");
  }

  @Test
  void nameConventionValidatorReportsInvalidAndXmlPrefixedName() {
    RelationshipModel model = TestModels.load("/crosscutting/NameConventionValidator_invalid.json", RelationshipModel.class);
    List<ModelValidationError> errors = new NameConventionValidator().validate(model, TestModels.context(model));

    // "xml Invalid Name!" violates both the character/pattern rule and the xml-prefix rule.
    assertEquals(2, errors.size());
  }

  @Test
  void headerModelReferenceValidatorReportsMissingReference() {
    RelationshipModel model = TestModels.load("/crosscutting/HeaderModelReferenceValidator_invalid.json", RelationshipModel.class);
    List<ModelValidationError> errors = new HeaderModelReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals(HeaderModelReferenceValidator.ELEMENT_ID, errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("Missing_DM"));
  }
}
