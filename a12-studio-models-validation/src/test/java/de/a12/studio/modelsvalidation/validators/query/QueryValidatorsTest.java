package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per query model validator, each loading a fixture that contains exactly that error. */
class QueryValidatorsTest {

  @Test
  void targetDocumentModelRequiredValidatorReportsMissingTarget() {
    QueryModel model = TestModels.load("/querymodel/QueryTargetDocumentModelRequiredValidator_invalid.json", QueryModel.class);
    List<ModelValidationError> errors = new QueryTargetDocumentModelRequiredValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Target Document Model is required"));
  }

  @Test
  void fieldReferenceValidatorReportsMissingField() {
    QueryModel model = TestModels.load("/querymodel/QueryFieldReferenceValidator_invalid.json", QueryModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new QueryFieldReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    // "/Root/Name" resolves, "/Root/DoesNotExist" doesn't.
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("/Root/DoesNotExist"));
  }

  @Test
  void sortFieldReferenceValidatorReportsMissingField() {
    QueryModel model = TestModels.load("/querymodel/QuerySortFieldReferenceValidator_invalid.json", QueryModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new QuerySortFieldReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("/Root/DoesNotExist"));
  }

  @Test
  void relationshipTraversalValidatorReportsUnknownRelationshipAndUnknownRole() {
    QueryModel model = TestModels.load("/querymodel/QueryRelationshipTraversalValidator_invalid.json", QueryModel.class);
    RelationshipModel personCompany = TestModels.load("/relationshipmodel/PersonCompany.json", RelationshipModel.class);
    List<ModelValidationError> errors = new QueryRelationshipTraversalValidator().validate(model,
        TestModels.contextWithOtherModels(model, personCompany));

    // One sort entry references a relationship model that doesn't exist, the other a role that doesn't
    // exist on the (real) PersonCompany relationship model.
    assertEquals(2, errors.size());
    assertTrue(errors.get(0).message().contains("NoSuchRelationship"));
    assertTrue(errors.get(1).message().contains("WrongRole"));
  }

  @Test
  void pagingBoundsValidatorReportsTooLowPageSizeAndNegativePageNumber() {
    QueryModel model = TestModels.load("/querymodel/QueryPagingBoundsValidator_invalid.json", QueryModel.class);
    List<ModelValidationError> errors = new QueryPagingBoundsValidator().validate(model, TestModels.context(model));

    assertEquals(2, errors.size());
    assertTrue(errors.get(0).message().contains("at least 1"));
    assertTrue(errors.get(1).message().contains("not be negative"));
  }

  @Test
  void filterDefinitionSyntaxValidatorReportsInvalidExpression() {
    QueryModel model = TestModels.load("/querymodel/QueryFilterDefinitionSyntaxValidator_invalid.json", QueryModel.class);
    List<ModelValidationError> errors = new QueryFilterDefinitionSyntaxValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Invalid filter expression"));
  }

  @Test
  void linkValidatorReportsUnknownRelationshipUnknownRoleAndMissingFieldRecursively() {
    QueryModel model = TestModels.load("/querymodel/QueryLinkValidator_invalid.json", QueryModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    RelationshipModel personRef = TestModels.load("/relationshipmodel/PersonRef.json", RelationshipModel.class);
    List<ModelValidationError> errors = new QueryLinkValidator().validate(model,
        TestModels.contextWithOtherModels(model, refDm, personRef));

    // (1) unknown relationship on the first link, (2) unknown role on the second, (3) a missing field on the
    // third (valid) link, and (4) an unknown relationship on a link nested *under* that valid third link -
    // proving a broken hop doesn't stop validation of hops nested under a resolved sibling.
    assertEquals(4, errors.size());
    assertTrue(errors.get(0).message().contains("NoSuchRelationship"));
    assertTrue(errors.get(1).message().contains("WrongRole"));
    assertTrue(errors.get(2).message().contains("/Root/DoesNotExist"));
    assertTrue(errors.get(3).message().contains("AnotherNoSuchRelationship"));
  }
}
