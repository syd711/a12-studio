package de.a12.studio.modelsvalidation.validators.combination;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinationStepType;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelContent;
import de.a12.studio.models.combineddocumentmodel.DocumentModelIdRef;
import de.a12.studio.models.combineddocumentmodel.SelectionModelIdRef;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base/additive-model reference-loop checks (port of SME's {@code CombModelReferenceHelper}), against minimal
 * in-memory project graphs: a self-reference, a two-model cycle, a cycle the validated model is not part of,
 * and the purpose rules (only {@code include} references count, except below an Additive Model).
 */
class CombinationLoopValidatorsTest {

  private static final String INCLUDE = ModelReference.PURPOSE_INCLUDE;

  // ---- base model ----

  @Test
  void baseModelThatIsTheModelItselfIsALoop() {
    CombinedDocumentModel model = combined("Self_Cm", "Self_Cm");

    List<ModelValidationError> errors = validateBase(model);

    assertEquals(1, errors.size());
    assertEquals("content/baseModelId", errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("Self_Cm -> Self_Cm"), errors.get(0).message());
  }

  @Test
  void twoCombinedModelsUsingEachOtherAsBaseAreALoop() {
    CombinedDocumentModel first = combined("First_Cm", "Second_Cm");
    CombinedDocumentModel second = combined("Second_Cm", "First_Cm");

    List<ModelValidationError> errors = validateBase(first, second);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("First_Cm -> Second_Cm -> First_Cm"), errors.get(0).message());
    // The other half of the cycle is reported when that model is validated.
    assertEquals(1, validateBase(second, first).size());
  }

  @Test
  void baseModelThatIncludesTheCombinedModelIsALoop() {
    CombinedDocumentModel model = combined("Loop_Cm", "Base_DM");
    DocumentModel base = documentModel("Base_DM", ref("Middle_DM", INCLUDE));
    DocumentModel middle = documentModel("Middle_DM", ref("Loop_Cm", INCLUDE));

    List<ModelValidationError> errors = validateBase(model, base, middle);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Loop_Cm -> Base_DM -> Middle_DM -> Loop_Cm"), errors.get(0).message());
  }

  @Test
  void baseModelInsideAnIncludeCycleOfItsOwnIsALoopEvenIfTheCombinedModelIsNotPartOfIt() {
    CombinedDocumentModel model = combined("Some_Cm", "A_DM");
    DocumentModel a = documentModel("A_DM", ref("B_DM", INCLUDE));
    DocumentModel b = documentModel("B_DM", ref("A_DM", INCLUDE));

    List<ModelValidationError> errors = validateBase(model, a, b);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("A_DM -> B_DM -> A_DM"), errors.get(0).message());
  }

  @Test
  void baseModelLoopThroughACombinedModelsBaseIsFound() {
    CombinedDocumentModel model = combined("Top_Cm", "Mid_Cm");
    CombinedDocumentModel mid = combined("Mid_Cm", "Leaf_DM");
    DocumentModel leaf = documentModel("Leaf_DM", ref("Top_Cm", INCLUDE));

    assertEquals(1, validateBase(model, mid, leaf).size());
  }

  @Test
  void onlyIncludeReferencesCountBelowTheBaseModel() {
    CombinedDocumentModel model = combined("Loop_Cm", "Base_DM");
    // A "data binding" reference back to the combined model is not an include, SME ignores it.
    DocumentModel base = documentModel("Base_DM", ref("Loop_Cm", ModelReference.PURPOSE_DATA_BINDING),
        ref("Loop_Cm", null));

    assertTrue(validateBase(model, base).isEmpty());
  }

  @Test
  void aLongChainWithoutALoopIsFine() {
    CombinedDocumentModel model = combined("Top_Cm", "A_DM");
    DocumentModel a = documentModel("A_DM", ref("B_DM", INCLUDE));
    DocumentModel b = documentModel("B_DM", ref("C_DM", INCLUDE), ref("D_DM", INCLUDE));
    DocumentModel c = documentModel("C_DM", ref("D_DM", INCLUDE));
    DocumentModel d = documentModel("D_DM");

    assertTrue(validateBase(model, a, b, c, d).isEmpty());
  }

  @Test
  void missingOrUnsetBaseModelIsNotALoop() {
    assertTrue(validateBase(combined("Top_Cm", "Gone_DM")).isEmpty());
    assertTrue(validateBase(combined("Top_Cm", null)).isEmpty());
  }

  // ---- additive model ----

  @Test
  void additiveModelThatIsTheModelItselfIsALoop() {
    CombinedDocumentModel model = combined("Self_Cm", "Base_DM", step(CombinationStepType.ADDITION, "Self_Cm", null, null));

    List<ModelValidationError> errors = validateAdditive(model, documentModel("Base_DM"));

    assertEquals(1, errors.size());
    assertEquals("content/combinationSteps/0", errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("Self_Cm -> Self_Cm"), errors.get(0).message());
  }

  @Test
  void additiveModelIncludingTheCombinedModelIsALoop() {
    CombinedDocumentModel model = combined("Loop_Cm", "Base_DM",
        step(CombinationStepType.ADDITION, "Other_Ad", null, null),
        step(CombinationStepType.ADDITION, "Loop_Ad", null, null));
    DocumentModel other = documentModel("Other_Ad");
    DocumentModel looping = documentModel("Loop_Ad", ref("Loop_Cm", INCLUDE));

    List<ModelValidationError> errors = validateAdditive(model, documentModel("Base_DM"), other, looping);

    assertEquals(1, errors.size());
    assertEquals("content/combinationSteps/1", errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("Loop_Cm -> Loop_Ad -> Loop_Cm"), errors.get(0).message());
  }

  @Test
  void untaggedReferencesAreIgnoredBelowADirectlySelectedModel() {
    // SME starts the walk from the selected model in include-only mode, for the base and the additive model alike.
    CombinedDocumentModel model = combined("Loop_Cm", "Base_DM", step(CombinationStepType.ADDITION, "Loop_Ad", null, null));
    DocumentModel base = documentModel("Base_DM", ref("Loop_Cm", null));
    DocumentModel additive = documentModel("Loop_Ad", ref("Loop_Cm", null));

    assertTrue(validateBase(model, base, additive).isEmpty());
    assertTrue(validateAdditive(model, base, additive).isEmpty());
  }

  @Test
  void everyPurposeCountsBelowAnAdditiveStepOfAReachedCombinedModel() {
    // The one place SME follows all purposes: an Addition step of a Combined Document Model on the walk.
    CombinedDocumentModel model = combined("Loop_Cm", "Base_DM", step(CombinationStepType.ADDITION, "Inner_Cm", null, null));
    CombinedDocumentModel inner = combined("Inner_Cm", "Base_DM", step(CombinationStepType.ADDITION, "Deep_Ad", null, null));
    DocumentModel deep = documentModel("Deep_Ad", ref("Loop_Cm", null));

    List<ModelValidationError> errors = validateAdditive(model, documentModel("Base_DM"), inner, deep);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Loop_Cm -> Inner_Cm -> Deep_Ad -> Loop_Cm"), errors.get(0).message());
  }

  @Test
  void additiveModelReferencingItselfThroughACycleIsALoop() {
    CombinedDocumentModel model = combined("Some_Cm", "Base_DM", step(CombinationStepType.ADDITION, "A_Ad", null, null));
    DocumentModel a = documentModel("A_Ad", ref("B_DM", INCLUDE));
    DocumentModel b = documentModel("B_DM", ref("A_Ad", INCLUDE));

    List<ModelValidationError> errors = validateAdditive(model, documentModel("Base_DM"), a, b);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("A_Ad -> B_DM -> A_Ad"), errors.get(0).message());
  }

  @Test
  void selectionAndDecorationModelsAreLeavesLikeInSme() {
    CombinedDocumentModel model = combined("Loop_Cm", "Base_DM", step(CombinationStepType.ADDITION, "Inner_Cm", null, null));
    CombinedDocumentModel inner = combined("Inner_Cm", "Base_DM",
        step(CombinationStepType.SELECTION, null, "Some_SeM", null),
        step(CombinationStepType.DECORATION_FOR_FIELDS, null, "Some_SeM", "Deco_DM"));
    // SME records the decoration model but does not follow its references.
    DocumentModel deco = documentModel("Deco_DM", ref("Loop_Cm", INCLUDE));

    assertTrue(validateAdditive(model, documentModel("Base_DM"), inner, deco).isEmpty());

    // ...but a decoration model that *is* the validated model is in the reachable set, so SME reports it.
    CombinedDocumentModel decoratedByParent = combined("Inner_Cm", "Base_DM",
        step(CombinationStepType.DECORATION_FOR_GROUPS, null, "Some_SeM", "Loop_Cm"));
    assertEquals(1, validateAdditive(model, documentModel("Base_DM"), decoratedByParent).size());
  }

  @Test
  void additiveModelThatIsACombinedModelReachingBackThroughItsStepsIsALoop() {
    CombinedDocumentModel model = combined("Top_Cm", "Base_DM", step(CombinationStepType.ADDITION, "Inner_Cm", null, null));
    CombinedDocumentModel inner = combined("Inner_Cm", "Base_DM", step(CombinationStepType.ADDITION, "Top_Cm", null, null));

    assertEquals(1, validateAdditive(model, documentModel("Base_DM"), inner).size());
  }

  @Test
  void stepsThatAreNotAdditionsAreLeftToTheNotAllowedValidator() {
    // A Selection step wrongly carrying an AdditiveModel is reported by CombinationAdditiveModelNotAllowedValidator.
    CombinedDocumentModel model = combined("Self_Cm", "Base_DM", step(CombinationStepType.SELECTION, "Self_Cm", "Some_SeM", null));

    assertTrue(validateAdditive(model, documentModel("Base_DM")).isEmpty());
  }

  // ---- helpers ----

  private static List<ModelValidationError> validateBase(CombinedDocumentModel model, A12Model<?>... others) {
    return new CombinationBaseModelLoopValidator().validate(model, TestModels.contextWithOtherModels(model, others));
  }

  private static List<ModelValidationError> validateAdditive(CombinedDocumentModel model, A12Model<?>... others) {
    return new CombinationAdditiveModelLoopValidator().validate(model, TestModels.contextWithOtherModels(model, others));
  }

  private static CombinedDocumentModel combined(String id, String baseModelId, CombinationStep... steps) {
    CombinedDocumentModel model = new CombinedDocumentModel();
    model.setId(id);
    CombinedDocumentModelContent content = new CombinedDocumentModelContent();
    content.setBaseModelId(baseModelId);
    content.getCombinationSteps().addAll(List.of(steps));
    model.setContent(content);
    return model;
  }

  private static DocumentModel documentModel(String id, ModelReference... references) {
    DocumentModel model = new DocumentModel();
    model.setId(id);
    model.getModelReferences().addAll(List.of(references));
    return model;
  }

  private static ModelReference ref(String reference, String purpose) {
    ModelReference modelReference = new ModelReference();
    modelReference.setModelType(ModelType.DOCUMENT);
    modelReference.setReference(reference);
    modelReference.setPurpose(purpose);
    return modelReference;
  }

  private static CombinationStep step(CombinationStepType type, String additiveDmId, String selectionSmId, String decorationDmId) {
    CombinationStep step = new CombinationStep();
    step.setType(type);
    if (additiveDmId != null) {
      DocumentModelIdRef additive = new DocumentModelIdRef();
      additive.setDmId(additiveDmId);
      step.setAdditiveModel(additive);
    }
    if (selectionSmId != null) {
      SelectionModelIdRef selection = new SelectionModelIdRef();
      selection.setSmId(selectionSmId);
      step.setSelectionModel(selection);
    }
    if (decorationDmId != null) {
      DocumentModelIdRef decoration = new DocumentModelIdRef();
      decoration.setDmId(decorationDmId);
      step.setDecorationModel(decoration);
    }
    return step;
  }
}
