package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per content model validator, each loading a fixture that contains exactly that error. */
class ContentValidatorsTest {

  @Test
  void rootElementValidatorReportsMissingRoot() {
    ContentModel model = TestModels.load("/contentmodel/ContentRootElementValidator_invalid.json", ContentModel.class);
    List<ModelValidationError> errors = new ContentRootElementValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("no root element"));
  }

  @Test
  void elementIdUniqueValidatorReportsDuplicateId() {
    ContentModel model = TestModels.load("/contentmodel/ContentElementIdUniqueValidator_invalid.json", ContentModel.class);
    List<ModelValidationError> errors = new ContentElementIdUniqueValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("child_1"));
  }
}
