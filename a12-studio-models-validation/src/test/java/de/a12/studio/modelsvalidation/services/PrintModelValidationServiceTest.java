package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.printmodel.PrintModelContent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.HeaderRolesValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The validators a Print Model gets from its validation service, as far as they are not the Print Model's
 * own. */
class PrintModelValidationServiceTest {

  @Test
  void theRolesOfAPrintModelAreChecked() {
    PrintModel model = new PrintModel();
    model.setId("Test_PM");
    model.setContent(new PrintModelContent());
    Annotation roles = new Annotation();
    roles.setName("roles");
    roles.setValue("admin,9bad");
    model.getAnnotations().add(roles);

    List<ModelValidationError> errors =
        new PrintModelValidationService().validate(model, TestModels.context(model)).stream()
            .filter(error -> HeaderRolesValidator.ELEMENT_ID.equals(error.elementId())).toList();

    assertEquals(1, errors.size(), errors.toString());
    assertTrue(errors.get(0).message().contains("9bad"), errors.get(0).message());
  }
}
