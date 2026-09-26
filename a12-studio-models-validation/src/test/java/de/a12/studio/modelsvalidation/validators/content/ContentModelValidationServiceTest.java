package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.services.ContentModelValidationService;
import de.a12.studio.modelsvalidation.validators.HeaderRolesValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The validators a Content Model gets from its validation service, as far as they are not the Content Model's own. */
class ContentModelValidationServiceTest {

  @Test
  void theRolesOfAContentModelAreChecked() {
    ContentModel page = ContentReferenceValidatorsTest.page(null);
    Annotation roles = new Annotation();
    roles.setName("roles");
    roles.setValue("admin,9bad");
    page.getAnnotations().add(roles);

    List<ModelValidationError> errors = new ContentModelValidationService().validate(page, TestModels.context(page)).stream()
        .filter(error -> HeaderRolesValidator.ELEMENT_ID.equals(error.elementId())).toList();

    assertEquals(1, errors.size(), errors.toString());
    assertTrue(errors.get(0).message().contains("9bad"), errors.get(0).message());
  }

  @Test
  void aBaseGroupWithoutADocumentModelComesThroughTheServiceWithItsElementId() {
    ContentModel page = ContentReferenceValidatorsTest.page(null);
    page.getContent().getConfiguration().setBaseGroupId("group_1");

    List<ModelValidationError> errors = new ContentModelValidationService().validate(page, TestModels.context(page)).stream()
        .filter(error -> ContentBaseGroupValidator.ELEMENT_ID.equals(error.elementId())).toList();

    assertEquals(1, errors.size());
  }
}
