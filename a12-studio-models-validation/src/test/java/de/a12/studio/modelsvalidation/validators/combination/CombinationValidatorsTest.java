package de.a12.studio.modelsvalidation.validators.combination;

import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinationStepType;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelContent;
import de.a12.studio.models.combineddocumentmodel.DocumentModelIdRef;
import de.a12.studio.models.combineddocumentmodel.SelectionModelIdRef;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One test per Combined Document Model structural validator, each against a minimal in-memory model (no JSON
 * fixture needed - these rules only look at a handful of {@link CombinationStep} fields).
 */
class CombinationValidatorsTest {

  @Test
  void additiveModelMissingValidatorReportsMissingAdditiveModel() {
    CombinedDocumentModel model = modelWithSteps(step(CombinationStepType.ADDITION, null, null, null));
    List<ModelValidationError> errors = new CombinationAdditiveModelMissingValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("content/combinationSteps/0", errors.get(0).elementId());
  }

  @Test
  void additiveModelMissingValidatorAllowsFilledAdditiveModel() {
    CombinedDocumentModel model = modelWithSteps(step(CombinationStepType.ADDITION, "Other_DM", null, null));
    List<ModelValidationError> errors = new CombinationAdditiveModelMissingValidator().validate(model, TestModels.context(model));

    assertEquals(0, errors.size());
  }

  @Test
  void selectionModelMissingValidatorReportsMissingSelectionModel() {
    CombinedDocumentModel model = modelWithSteps(step(CombinationStepType.DECORATION_FOR_FIELDS, null, null, "Other_DM"));
    List<ModelValidationError> errors = new CombinationSelectionModelMissingValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
  }

  @Test
  void decorationModelMissingValidatorReportsMissingDecorationModel() {
    CombinedDocumentModel model = modelWithSteps(step(CombinationStepType.DECORATION_FOR_GROUPS, null, "Some_SeM", null));
    List<ModelValidationError> errors = new CombinationDecorationModelMissingValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
  }

  @Test
  void additiveModelNotAllowedValidatorReportsAdditiveModelOnNonAdditionStep() {
    CombinedDocumentModel model = modelWithSteps(step(CombinationStepType.SELECTION, "Other_DM", "Some_SeM", null));
    List<ModelValidationError> errors = new CombinationAdditiveModelNotAllowedValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
  }

  @Test
  void selectionModelNotAllowedValidatorReportsSelectionModelOnAdditionStep() {
    CombinedDocumentModel model = modelWithSteps(step(CombinationStepType.ADDITION, "Other_DM", "Some_SeM", null));
    List<ModelValidationError> errors = new CombinationSelectionModelNotAllowedValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
  }

  @Test
  void decorationModelNotAllowedValidatorReportsDecorationModelOnSelectionStep() {
    CombinedDocumentModel model = modelWithSteps(step(CombinationStepType.SELECTION, null, "Some_SeM", "Other_DM"));
    List<ModelValidationError> errors = new CombinationDecorationModelNotAllowedValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
  }

  @Test
  void additiveModelDuplicateValidatorReportsSecondUseOfSameModel() {
    CombinedDocumentModel model = modelWithSteps(
        step(CombinationStepType.ADDITION, "Other_DM", null, null),
        step(CombinationStepType.ADDITION, "Other_DM", null, null));
    List<ModelValidationError> errors = new CombinationAdditiveModelDuplicateValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("content/combinationSteps/1", errors.get(0).elementId());
  }

  @Test
  void additiveModelDuplicateValidatorAllowsDistinctModels() {
    CombinedDocumentModel model = modelWithSteps(
        step(CombinationStepType.ADDITION, "Other_DM", null, null),
        step(CombinationStepType.ADDITION, "Another_DM", null, null));
    List<ModelValidationError> errors = new CombinationAdditiveModelDuplicateValidator().validate(model, TestModels.context(model));

    assertTrue(errors.isEmpty());
  }

  private static CombinedDocumentModel modelWithSteps(CombinationStep... steps) {
    CombinedDocumentModel model = new CombinedDocumentModel();
    model.setId("Test_CmM");
    CombinedDocumentModelContent content = new CombinedDocumentModelContent();
    content.getCombinationSteps().addAll(List.of(steps));
    model.setContent(content);
    return model;
  }

  private static CombinationStep step(CombinationStepType type, String additiveDmId, String selectionSmId, String decorationDmId) {
    CombinationStep step = new CombinationStep();
    step.setType(type);
    if (additiveDmId != null) {
      DocumentModelIdRef ref = new DocumentModelIdRef();
      ref.setDmId(additiveDmId);
      step.setAdditiveModel(ref);
    }
    if (selectionSmId != null) {
      SelectionModelIdRef ref = new SelectionModelIdRef();
      ref.setSmId(selectionSmId);
      step.setSelectionModel(ref);
    }
    if (decorationDmId != null) {
      DocumentModelIdRef ref = new DocumentModelIdRef();
      ref.setDmId(decorationDmId);
      step.setDecorationModel(ref);
    }
    return step;
  }
}
