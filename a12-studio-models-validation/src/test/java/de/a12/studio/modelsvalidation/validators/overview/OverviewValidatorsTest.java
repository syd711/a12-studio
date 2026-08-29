package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per overview model validator, each loading a fixture that contains exactly that error. */
class OverviewValidatorsTest {

  @Test
  void columnsNotEmptyValidatorReportsMissingColumns() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewColumnsNotEmptyValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewColumnsNotEmptyValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Columns must not be empty"));
  }

  @Test
  void fieldReferenceValidatorReportsMissingAndUnindexedFields() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFieldReferenceValidator_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFieldReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    // One column references a field that doesn't exist, the other a field annotated indexed=false.
    assertEquals(2, errors.size());
  }

  @Test
  void fieldReferenceValidatorResolvesFieldInsideIncludedDocumentModel() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFieldReferenceValidator_include_valid.json", OverviewModel.class);
    DocumentModel hostDm = TestModels.load("/documentmodel/RefWithInclude_DM.json", DocumentModel.class);
    DocumentModel includedDm = TestModels.load("/documentmodel/RefIncluded_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFieldReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, hostDm, includedDm));

    // The column's elementRef points through the Include group into RefIncluded_DM's own field, not
    // a direct child of RefWithInclude_DM - must resolve rather than being reported as missing.
    assertEquals(0, errors.size());
  }

  @Test
  void fieldReferenceValidatorReportsRepeatableGroupInsideIncludedDocumentModel() {
    OverviewModel model = TestModels.load(
        "/overviewmodel/OverviewFieldReferenceValidator_includeInternalRepeatable_invalid.json", OverviewModel.class);
    DocumentModel hostDm = TestModels.load("/documentmodel/RefWithIncludeOfRepeatableGroup_DM.json", DocumentModel.class);
    DocumentModel includedDm = TestModels.load("/documentmodel/RefIncludedWithRepeatableGroup_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFieldReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, hostDm, includedDm));

    // The field is repeatable because of a group *inside* the included model, not the Include itself.
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("is repeatable"));
  }

  @Test
  void fieldReferenceValidatorReportsRepeatableInclude() {
    OverviewModel model = TestModels.load(
        "/overviewmodel/OverviewFieldReferenceValidator_repeatableInclude_invalid.json", OverviewModel.class);
    DocumentModel hostDm = TestModels.load("/documentmodel/RefWithRepeatableInclude_DM.json", DocumentModel.class);
    DocumentModel includedDm = TestModels.load("/documentmodel/RefIncluded_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFieldReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, hostDm, includedDm));

    // The included field itself sits under no repeatable group, but the Include group wrapping it in the
    // host model is itself repeatable (repeatability=3) - that must propagate to fields reached through it.
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("is repeatable"));
  }

  @Test
  void documentModelRequiredValidatorReportsMissingReference() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewDocumentModelRequiredValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewDocumentModelRequiredValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Document Model or Query Model reference is required"));
  }

  @Test
  void filterModeRequiredValidatorReportsMissingFilterMode() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterModeRequiredValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewFilterModeRequiredValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Filter Mode"));
  }

  @Test
  void filterCustomFieldsValidatorReportsEmptySelection() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterCustomFieldsValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewFilterCustomFieldsValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("At least one field"));
  }

  @Test
  void filterSectionsValidatorReportsMissingIdAndDuplicateField() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterSectionsValidator_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFilterSectionsValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    // Missing section id, plus the same field selected twice within the section.
    assertEquals(2, errors.size());
  }

  @Test
  void filterGroupsValidatorReportsMissingIdMissingFieldAndUnindexedField() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterGroupsValidator_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFilterGroupsValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    // Missing group id, a filter item with no field reference, plus a field reference annotated indexed=false.
    assertEquals(3, errors.size());
  }

  @Test
  void multiSelectionElementValidatorReportsMissingElement() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewMultiSelectionElementValidator_missing_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewMultiSelectionElementValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("No Multi-Selection element is added"));
  }

  @Test
  void multiSelectionElementValidatorReportsDuplicateElement() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewMultiSelectionElementValidator_duplicate_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewMultiSelectionElementValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Only one Multi-Selection is allowed"));
  }

  @Test
  void pagingSizeValidatorReportsInvalidValue() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewPagingSizeValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewPagingSizeValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("at least 1"));
  }

  @Test
  void initialSortingReferenceValidatorReportsDeletedColumn() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewInitialSortingReferenceValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewInitialSortingReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("no longer exists"));
  }

  @Test
  void stylesValidatorReportsBlankEntry() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewStylesValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewStylesValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("required"));
  }
}
