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
  void fieldReferenceValidatorAcceptsAKernelMetaFieldColumn() {
    // Gap 15 of the "Overview Model: gap review" - a __meta field is a legitimate reference even though
    // ElementIndex never contains it (see OverviewElementResolution.isMetaFieldId's own callers).
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFieldReferenceValidator_meta_valid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFieldReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    assertEquals(0, errors.size());
  }

  @Test
  void sortableMultiSelectValidatorReportsSortableMultiSelectColumn() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewSortableMultiSelectValidator_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/RefMultiSelect_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewSortableMultiSelectValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("multi-select"));
  }

  @Test
  void columnHeaderLabelOrIconValidatorReportsEmptyHeader() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewColumnHeaderLabelOrIconValidator_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewColumnHeaderLabelOrIconValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("no icon and no visible label"));
    assertTrue(errors.get(0).message().contains("NoLabel"));
  }

  @Test
  void columnHeaderLabelOrIconValidatorAcceptsColumnInheritingFieldLabel() {
    // field_1 ("Name") has its own label in Ref_DM - even though the column itself sets no label, SME
    // auto-fills the column header from the referenced field's label, so this must not warn.
    OverviewModel model = TestModels.load("/overviewmodel/OverviewColumnHeaderLabelOrIconValidator_invalid.json", OverviewModel.class);
    model.getContent().getColumns().get(0).setElementRef("field_1");
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewColumnHeaderLabelOrIconValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    assertEquals(0, errors.size());
  }

  @Test
  void columnHeaderLabelOrIconValidatorSkipsWithoutReferencedDocumentModel() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewColumnHeaderLabelOrIconValidator_invalid.json", OverviewModel.class);
    // Without the referenced Document Model in context, the column's elementRef can't be resolved - since a
    // genuinely dangling reference is already reported separately (as an ERROR) by
    // OverviewFieldReferenceValidator, this validator must not also fire.
    List<ModelValidationError> errors = new OverviewColumnHeaderLabelOrIconValidator().validate(model, TestModels.context(model));

    assertEquals(0, errors.size());
  }

  @Test
  void columnHeaderLabelOrIconValidatorReportsMissingHeaderOnExpressionColumn() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewColumnHeaderLabelOrIconValidator_expression_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewColumnHeaderLabelOrIconValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("expression column"));
    assertTrue(errors.get(0).message().contains("column_expr"));
  }

  @Test
  void expressionColumnValidatorReportsMissingFieldsAndBadSyntax() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewExpressionColumnValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewExpressionColumnValidator().validate(model, TestModels.context(model));

    assertEquals(3, errors.size());
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("needs a Name")));
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("needs an Expression")));
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("invalid Expression")));
  }

  @Test
  void expressionColumnValidatorAcceptsAWellFormedColumn() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewExpressionColumnValidator_invalid.json", OverviewModel.class);
    model.getContent().getColumns().removeIf(column -> !"column_bad_syntax".equals(column.getId()));
    model.getContent().getColumns().get(0).setExpression("[FirstName] \" \" [LastName]");
    List<ModelValidationError> errors = new OverviewExpressionColumnValidator().validate(model, TestModels.context(model));

    assertEquals(0, errors.size());
  }

  @Test
  void filterModeIndexedAnnotationValidatorReportsUnindexedFieldForAllMode() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterModeIndexedAnnotationValidator_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFilterModeIndexedAnnotationValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("indexed"));
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
  void filterModeRequiredValidatorAcceptsCustomFilterWithoutFilterMode() {
    // SME has no filterMode-equivalent value for the "Custom Filter" mode - a populated newFilterConfiguration
    // satisfies the requirement on its own, with no sibling filterConfiguration.filterMode needed.
    OverviewModel model =
        TestModels.load("/overviewmodel/OverviewFilterModeRequiredValidator_customFilter_valid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewFilterModeRequiredValidator().validate(model, TestModels.context(model));

    assertEquals(0, errors.size());
  }

  @Test
  void filterCustomFieldsValidatorReportsEmptySelection() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterCustomFieldsValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewFilterCustomFieldsValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("At least one field"));
  }

  @Test
  void enumeratedStringFilterValidatorReportsEmptySelectionAndMissingPagingSize() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewEnumeratedStringFilterValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewEnumeratedStringFilterValidator().validate(model, TestModels.context(model));

    assertEquals(2, errors.size());
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("At least one field")));
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("Paging Size is required")));
  }

  @Test
  void enumeratedStringFilterValidatorReportsDuplicateAndUnresolvedField() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewEnumeratedStringFilterValidator_duplicate_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewEnumeratedStringFilterValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    // field_1 selected twice, plus field_missing not resolving.
    assertEquals(2, errors.size());
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("is selected more than once")));
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("field_missing")));
  }

  @Test
  void filterCustomFieldsValidatorReportsAnInvalidSubModelReference() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterCustomFieldsValidator_subModel_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFilterCustomFieldsValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Does_Not_Exist_DM"));
  }

  @Test
  void filterCustomFieldsValidatorResolvesAFieldThroughASubModel() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterCustomFieldsValidator_subModel_valid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    DocumentModel refSubDm = TestModels.load("/documentmodel/RefSub_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFilterCustomFieldsValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm, refSubDm));

    assertEquals(0, errors.size());
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
  void filterSectionsValidatorReportsMissingLabelWhileFilterButtonShown() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterSectionsValidator_label_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFilterSectionsValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("needs a label text"));
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
  void filterGroupsValidatorReportsMissingFilterDefinitionOnQueryItem() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterGroupsValidator_query_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewFilterGroupsValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("must define a filter definition"));
  }

  @Test
  void filterGroupsValidatorAcceptsStructuredOperatorOnQueryItem() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterGroupsValidator_query_operator_valid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewFilterGroupsValidator().validate(model, TestModels.context(model));

    assertEquals(0, errors.size());
  }

  @Test
  void filterDefinitionSyntaxValidatorReportsInvalidQueryItemSyntax() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterDefinitionSyntaxValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewFilterDefinitionSyntaxValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Invalid filter expression"));
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
  void searchElementValidatorReportsMissingElement() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewSearchElementValidator_missing_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewSearchElementValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("No search element is added"));
  }

  @Test
  void searchElementValidatorReportsDuplicateElement() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewSearchElementValidator_duplicate_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewSearchElementValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Only one search is allowed"));
  }

  @Test
  void pagingSizeValidatorReportsInvalidValue() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewPagingSizeValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewPagingSizeValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("at least 1"));
  }

  @Test
  void infiniteScrollingValidatorReportsMissingPagingSizeForPagination() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewInfiniteScrollingValidator_pagination_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewInfiniteScrollingValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Paging Size is required"));
  }

  @Test
  void infiniteScrollingValidatorReportsMissingRowHeightAndActionColumnWidth() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewInfiniteScrollingValidator_infiniteScroll_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewInfiniteScrollingValidator().validate(model, TestModels.context(model));

    assertEquals(2, errors.size());
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("Row Height is required")));
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("Action Column Width is required")));
  }

  @Test
  void infiniteScrollingValidatorAcceptsPaginationWithPagingSize() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewInfiniteScrollingValidator_pagination_invalid.json", OverviewModel.class);
    model.getContent().getConfiguration().setPagingSize(10);
    List<ModelValidationError> errors = new OverviewInfiniteScrollingValidator().validate(model, TestModels.context(model));

    assertEquals(0, errors.size());
  }

  @Test
  void initialSortingReferenceValidatorReportsDeletedColumn() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewInitialSortingReferenceValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewInitialSortingReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("no longer exists"));
  }

  @Test
  void initialSortingReferenceValidatorReportsDuplicateColumn() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewInitialSortingReferenceValidator_duplicate_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewInitialSortingReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("used more than once"));
  }

  @Test
  void stylesValidatorReportsBlankEntry() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewStylesValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewStylesValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("required"));
  }

  @Test
  void contextMenuValidatorReportsGroupWithoutAction() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewContextMenuValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewContextMenuValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Empty Group"));
    assertTrue(errors.get(0).message().contains("at least one action"));
  }

  @Test
  void footerExportExcelValidatorWarnsWhenNotBoundToAComposedDocumentModel() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFooterExportExcelValidator_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFooterExportExcelValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    assertEquals(1, errors.size());
    assertEquals("WARNING", errors.get(0).severity());
    assertTrue(errors.get(0).message().contains("Composed Document Models only"));
  }

  @Test
  void columnValidatorReportsWidthSortingSuffixAndStyleProblems() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewColumnValidator_invalid.json", OverviewModel.class);
    DocumentModel aggregationDm = TestModels.load("/documentmodel/Aggregation_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewColumnValidator().validate(model,
        TestModels.contextWithDocumentModels(model, aggregationDm));

    assertEquals(6, errors.size());
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("width must be at least")));
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("Preferred Sorting is required")));
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("field_does_not_exist")));
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("indexed")));
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("not defined in Styles")));
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("used more than once")));
  }

  @Test
  void columnValidatorAcceptsAStaticSuffixOnAnUnindexedField() {
    // useDynamicSuffix is false/absent - SME's indexedAnnotationShouldBeNotFalseForDynamicSuffix rule needs
    // FieldFilled(useDynamicSuffix) too, so a static suffix must not trip the indexed check.
    OverviewModel model = TestModels.load("/overviewmodel/OverviewColumnValidator_invalid.json", OverviewModel.class);
    model.getContent().getColumns().removeIf(column -> !"column_suffix_indexed".equals(column.getId()));
    model.getContent().getColumns().get(0).setUseDynamicSuffix(null);
    DocumentModel aggregationDm = TestModels.load("/documentmodel/Aggregation_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewColumnValidator().validate(model,
        TestModels.contextWithDocumentModels(model, aggregationDm));

    assertEquals(0, errors.size());
  }

  @Test
  void footerExportExcelValidatorAcceptsAComposedDocumentModel() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFooterExportExcelValidator_cdm_valid.json", OverviewModel.class);
    de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel cdm =
        TestModels.load("/documentmodel/CdmRef_CdM.json", de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel.class);
    List<ModelValidationError> errors = new OverviewFooterExportExcelValidator().validate(model,
        TestModels.contextWithDocumentModels(model, cdm));

    assertEquals(0, errors.size());
  }
}
