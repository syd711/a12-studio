package de.a12.studio.modelsvalidation.validators.selection;

import de.a12.studio.models.selectionmodel.PathSpecification;
import de.a12.studio.models.selectionmodel.SelectionCategory;
import de.a12.studio.models.selectionmodel.SelectionDefault;
import de.a12.studio.models.selectionmodel.SelectionModel;
import de.a12.studio.models.selectionmodel.SelectionModelContent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One test per Selection Model structural validator, each against a minimal in-memory model - these rules
 * only look at a handful of {@link SelectionCategory} fields, same reasoning as {@code
 * CombinationValidatorsTest}.
 */
class SelectionValidatorsTest {

  @Test
  void defaultMissingValidatorReportsMissingDefault() {
    SelectionModel model = modelWithData(category(null));
    List<ModelValidationError> errors = new SelectionDefaultMissingValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("content/Data/Default", errors.get(0).elementId());
  }

  @Test
  void defaultMissingValidatorAllowsFilledDefault() {
    SelectionModel model = modelWithData(category(SelectionDefault.SELECTED));
    List<ModelValidationError> errors = new SelectionDefaultMissingValidator().validate(model, TestModels.context(model));

    assertTrue(errors.isEmpty());
  }

  @Test
  void pathPatternValidatorReportsOnlyWildcard() {
    SelectionCategory category = category(SelectionDefault.SELECTED);
    category.setUnselected(List.of(path("/*/")));
    SelectionModel model = modelWithData(category);
    List<ModelValidationError> errors = new SelectionPathPatternValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("content/Data/Unselected/0", errors.get(0).elementId());
  }

  @Test
  void pathPatternValidatorReportsInvalidPattern() {
    SelectionCategory category = category(SelectionDefault.SELECTED);
    category.setSelected(List.of(path("not a valid path")));
    SelectionModel model = modelWithData(category);
    List<ModelValidationError> errors = new SelectionPathPatternValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("content/Data/Selected/0", errors.get(0).elementId());
  }

  @Test
  void pathPatternValidatorAllowsRealPaths() {
    SelectionCategory category = category(SelectionDefault.SELECTED);
    category.setUnselected(List.of(path("/Status/AcquiredAt"), path("/HR/Acknowledged"), path("/Group/"), path("/Group/field*")));
    SelectionModel model = modelWithData(category);
    List<ModelValidationError> errors = new SelectionPathPatternValidator().validate(model, TestModels.context(model));

    assertTrue(errors.isEmpty());
  }

  @Test
  void duplicatePathValidatorReportsSecondOccurrence() {
    SelectionCategory category = category(SelectionDefault.UNSELECTED);
    category.setSelected(List.of(path("/A/B"), path("/A/B")));
    SelectionModel model = modelWithData(category);
    List<ModelValidationError> errors = new SelectionDuplicatePathValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("content/Data/Selected/1", errors.get(0).elementId());
  }

  @Test
  void duplicatePathValidatorAllowsDistinctPaths() {
    SelectionCategory category = category(SelectionDefault.UNSELECTED);
    category.setSelected(List.of(path("/A/B"), path("/A/C")));
    SelectionModel model = modelWithData(category);
    List<ModelValidationError> errors = new SelectionDuplicatePathValidator().validate(model, TestModels.context(model));

    assertTrue(errors.isEmpty());
  }

  @Test
  void pathInBothListsValidatorReportsOverlap() {
    SelectionCategory category = category(SelectionDefault.SELECTED);
    category.setSelected(List.of(path("/A/B")));
    category.setUnselected(List.of(path("/A/B")));
    SelectionModel model = modelWithData(category);
    List<ModelValidationError> errors = new SelectionPathInBothListsValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("content/Data/Selected/0", errors.get(0).elementId());
  }

  @Test
  void redundantDefaultValidatorReportsSelectedButNoUnselected() {
    SelectionCategory category = category(SelectionDefault.SELECTED);
    category.setSelected(List.of(path("/A/B")));
    SelectionModel model = modelWithData(category);
    List<ModelValidationError> errors = new SelectionRedundantDefaultValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("content/Data/Default", errors.get(0).elementId());
  }

  @Test
  void redundantDefaultValidatorReportsUnselectedButNoSelected() {
    SelectionCategory category = category(SelectionDefault.UNSELECTED);
    category.setUnselected(List.of(path("/A/B")));
    SelectionModel model = modelWithData(category);
    List<ModelValidationError> errors = new SelectionRedundantDefaultValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("content/Data/Default", errors.get(0).elementId());
  }

  @Test
  void redundantDefaultValidatorAllowsBothListsFilled() {
    SelectionCategory category = category(SelectionDefault.SELECTED);
    category.setSelected(List.of(path("/A/B")));
    category.setUnselected(List.of(path("/A/C")));
    SelectionModel model = modelWithData(category);
    List<ModelValidationError> errors = new SelectionRedundantDefaultValidator().validate(model, TestModels.context(model));

    assertTrue(errors.isEmpty());
  }

  private static SelectionModel modelWithData(SelectionCategory data) {
    SelectionModel model = new SelectionModel();
    model.setId("Test_SeM");
    SelectionModelContent content = new SelectionModelContent();
    content.setData(data);
    content.setComputation(category(SelectionDefault.SELECTED));
    content.setValidation(category(SelectionDefault.SELECTED));
    model.setContent(content);
    return model;
  }

  private static SelectionCategory category(SelectionDefault defaultValue) {
    SelectionCategory category = new SelectionCategory();
    category.setDefaultValue(defaultValue);
    return category;
  }

  private static PathSpecification path(String path) {
    PathSpecification spec = new PathSpecification();
    spec.setPath(path);
    return spec;
  }
}
