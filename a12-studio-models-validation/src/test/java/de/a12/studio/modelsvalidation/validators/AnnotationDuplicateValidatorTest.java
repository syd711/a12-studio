package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.typesettingmodel.TypesettingModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ports the kernel's shared {@code annotationNamesNotUnique} rule (SME's {@code core/ModelHeader.json}):
 * no two header annotations may share the same name. */
class AnnotationDuplicateValidatorTest {

  private final AnnotationDuplicateValidator validator = new AnnotationDuplicateValidator();

  @Test
  void aModelWithNoAnnotationsHasNoProblem() {
    TypesettingModel model = model();

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  @Test
  void distinctAnnotationNamesHaveNoProblem() {
    TypesettingModel model = model(annotation("owner", "a"), annotation("reviewer", "b"));

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  @Test
  void reportsADuplicateAnnotationNameOnce() {
    TypesettingModel model = model(annotation("owner", "a"), annotation("reviewer", "b"), annotation("owner", "c"), annotation("owner", "d"));

    List<ModelValidationError> errors = validator.validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("ERROR", errors.get(0).severity());
    assertEquals(AnnotationDuplicateValidator.ELEMENT_ID, errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("owner"), errors.get(0).message());
  }

  @Test
  void reportsEachDistinctDuplicateNameSeparately() {
    TypesettingModel model = model(annotation("owner", "a"), annotation("owner", "b"), annotation("reviewer", "c"), annotation("reviewer", "d"));

    List<ModelValidationError> errors = validator.validate(model, TestModels.context(model));

    assertEquals(2, errors.size());
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("owner")));
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("reviewer")));
  }

  @Test
  void aBlankOrMissingAnnotationNameIsIgnored() {
    Annotation blank = new Annotation();
    blank.setName("");
    Annotation noName = new Annotation();
    TypesettingModel model = model(blank, noName);

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  private static Annotation annotation(String name, String value) {
    Annotation annotation = new Annotation();
    annotation.setName(name);
    annotation.setValue(value);
    return annotation;
  }

  private static TypesettingModel model(Annotation... annotations) {
    TypesettingModel model = new TypesettingModel();
    model.setId("Test_TSM");
    for (Annotation annotation : annotations) {
      model.getAnnotations().add(annotation);
    }
    return model;
  }
}
