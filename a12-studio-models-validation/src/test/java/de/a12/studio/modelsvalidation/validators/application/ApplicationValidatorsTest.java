package de.a12.studio.modelsvalidation.validators.application;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per application model validator rule, each loading a fixture that contains exactly that error. */
class ApplicationValidatorsTest {

  private ApplicationModel load(String name) {
    return TestModels.load("/applicationmodel/" + name + ".json", ApplicationModel.class);
  }

  private DocumentModel refDm() {
    return TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
  }

  @Test
  void uniqueNamesValidatorReportsDuplicateModuleName() {
    ApplicationModel model = load("ApplicationUniqueNamesValidator_module_invalid");
    List<ModelValidationError> errors = new ApplicationUniqueNamesValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Mod1"));
  }

  @Test
  void uniqueNamesValidatorReportsDuplicateFlowName() {
    ApplicationModel model = load("ApplicationUniqueNamesValidator_flow_invalid");
    List<ModelValidationError> errors = new ApplicationUniqueNamesValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Flow1"));
  }

  @Test
  void uniqueNamesValidatorReportsDuplicateSceneName() {
    ApplicationModel model = load("ApplicationUniqueNamesValidator_scene_invalid");
    List<ModelValidationError> errors = new ApplicationUniqueNamesValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Scene1"));
  }

  @Test
  void uniqueNamesValidatorReportsDuplicateCaseName() {
    ApplicationModel model = load("ApplicationUniqueNamesValidator_case_invalid");
    List<ModelValidationError> errors = new ApplicationUniqueNamesValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Case1"));
  }

  @Test
  void uniqueNamesValidatorReportsDuplicateRegionName() {
    ApplicationModel model = load("ApplicationUniqueNamesValidator_region_invalid");
    List<ModelValidationError> errors = new ApplicationUniqueNamesValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("APP"));
  }

  @Test
  void uniqueNamesValidatorReportsDuplicateChildMenuName() {
    ApplicationModel model = load("ApplicationUniqueNamesValidator_childMenu_invalid");
    List<ModelValidationError> errors = new ApplicationUniqueNamesValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Child1"));
  }

  @Test
  void sceneGraphValidatorReportsSceneIsOwnPrior() {
    ApplicationModel model = load("ApplicationSceneGraphValidator_sceneIsOwnPrior_invalid");
    List<ModelValidationError> errors = new ApplicationSceneGraphValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("SceneA"));
  }

  @Test
  void sceneGraphValidatorReportsUnknownPriorScene() {
    ApplicationModel model = load("ApplicationSceneGraphValidator_unknownPriorScene_invalid");
    List<ModelValidationError> errors = new ApplicationSceneGraphValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Missing"));
  }

  @Test
  void sceneGraphValidatorReportsUnknownDefaultCase() {
    ApplicationModel model = load("ApplicationSceneGraphValidator_unknownDefaultCase_invalid");
    List<ModelValidationError> errors = new ApplicationSceneGraphValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Missing"));
  }

  @Test
  void sceneGraphValidatorReportsUnknownDirectiveRegion() {
    ApplicationModel model = load("ApplicationSceneGraphValidator_unknownDirectiveRegion_invalid");
    List<ModelValidationError> errors = new ApplicationSceneGraphValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("UNKNOWN"));
  }

  @Test
  void sceneGraphValidatorReportsMatchConditionNotExactlyOne() {
    ApplicationModel model = load("ApplicationSceneGraphValidator_matchConditionExactlyOne_invalid");
    List<ModelValidationError> errors = new ApplicationSceneGraphValidator().validate(model, TestModels.context(model));

    // One scene sets both mustEqual and isSet, the other sets neither.
    assertEquals(2, errors.size());
  }

  @Test
  void sceneGraphValidatorReportsMissingMatchConditions() {
    ApplicationModel model = load("ApplicationSceneGraphValidator_matchConditionsRequired_invalid");
    List<ModelValidationError> errors = new ApplicationSceneGraphValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("SceneA"));
  }

  @Test
  void sceneGraphValidatorReportsUnknownDefaultRegion() {
    ApplicationModel model = load("ApplicationSceneGraphValidator_unknownDefaultRegion_invalid");
    List<ModelValidationError> errors = new ApplicationSceneGraphValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("MISSING"));
  }

  @Test
  void viewAddValidatorReportsMissingName() {
    ApplicationModel model = load("ApplicationViewAddValidator_missingName_invalid");
    List<ModelValidationError> errors = new ApplicationViewAddValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
  }

  @Test
  void viewAddValidatorReportsMissingModel() {
    ApplicationModel model = load("ApplicationViewAddValidator_missingModel_invalid");
    List<ModelValidationError> errors = new ApplicationViewAddValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Missing_OM"));
  }

  @Test
  void viewAddValidatorReportsMissingDocumentModel() {
    ApplicationModel model = load("ApplicationViewAddValidator_missingDocumentModel_invalid");
    List<ModelValidationError> errors = new ApplicationViewAddValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm()));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Missing_DM"));
  }
}
