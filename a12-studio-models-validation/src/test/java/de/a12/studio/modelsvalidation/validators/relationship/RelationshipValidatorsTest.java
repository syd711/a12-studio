package de.a12.studio.modelsvalidation.validators.relationship;

import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per relationship model validator, each loading a fixture that contains exactly that error. */
class RelationshipValidatorsTest {

  private RelationshipModel load(String name) {
    return TestModels.load("/relationshipmodel/" + name + ".json", RelationshipModel.class);
  }

  @Test
  void entityCountValidatorReportsSingleEntity() {
    RelationshipModel model = load("RelationshipEntityCountValidator_invalid");
    List<ModelValidationError> errors = new RelationshipEntityCountValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("exactly two related entities"));
  }

  @Test
  void uniqueRolesValidatorReportsDuplicateRole() {
    RelationshipModel model = load("RelationshipUniqueRolesValidator_invalid");
    List<ModelValidationError> errors = new RelationshipUniqueRolesValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("SameRole"));
  }

  @Test
  void upperLimitValidatorReportsMissingLimit() {
    RelationshipModel model = load("RelationshipUpperLimitValidator_invalid");
    List<ModelValidationError> errors = new RelationshipUpperLimitValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("upper limit"));
  }

  @Test
  void documentModelReferenceValidatorReportsMissingAndUnsetModels() {
    RelationshipModel model = load("RelationshipDocumentModelReferenceValidator_invalid");
    List<ModelValidationError> errors = new RelationshipDocumentModelReferenceValidator().validate(model, TestModels.context(model));

    // First entity references a document model that doesn't exist; the second has none selected.
    assertEquals(2, errors.size());
    assertNotNull(errors.get(0).elementId());
  }

  @Test
  void linkDocumentModelValidatorWarnsForNonManyToMany() {
    RelationshipModel model = load("RelationshipLinkDocumentModelValidator_invalid");
    List<ModelValidationError> errors = new RelationshipLinkDocumentModelValidator().validate(model, TestModels.context(model));

    // Link document model set AND duplicates allowed on a 1:n relationship -> two warnings.
    assertEquals(2, errors.size());
    assertEquals(Severity.WARNING.name(), errors.get(0).severity());
  }

  @Test
  void linkDocumentModelValidatorWarnsWhenOnlyOneEntityConfigured() {
    // A single to-many entity isn't enough: SME's commonPrecondition requires both related entities'
    // multiplicity groups to be filled before it considers the relationship many-to-many, so a lone entity
    // (even one that is itself to-many) still triggers the warning.
    RelationshipModel model = load("RelationshipLinkDocumentModelValidator_singleEntity");
    List<ModelValidationError> errors = new RelationshipLinkDocumentModelValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals(Severity.WARNING.name(), errors.get(0).severity());
  }

  @Test
  void generatedDmNameLengthValidatorReportsTooLongName() {
    RelationshipModel model = load("RelationshipGeneratedDmNameLengthValidator_invalid");
    List<ModelValidationError> errors = new RelationshipGeneratedDmNameLengthValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("exceed 100 characters"));
  }
}
