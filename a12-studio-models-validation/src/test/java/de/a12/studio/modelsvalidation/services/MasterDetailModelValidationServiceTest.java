package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.HeaderRolesValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The validators a Master Detail Model gets from its validation service, as far as they are not the Master
 * Detail Model's own. */
class MasterDetailModelValidationServiceTest {

  @Test
  void theRolesOfAMasterDetailModelAreChecked() {
    MasterDetailModel model =
        TestModels.load("/masterdetailmodel/MasterDetailTypeConsistencyValidator_invalid.json", MasterDetailModel.class);
    Annotation roles = new Annotation();
    roles.setName("roles");
    roles.setValue("admin,9bad");
    model.getAnnotations().add(roles);

    List<ModelValidationError> errors =
        new MasterDetailModelValidationService().validate(model, TestModels.context(model)).stream()
            .filter(error -> HeaderRolesValidator.ELEMENT_ID.equals(error.elementId())).toList();

    assertEquals(1, errors.size(), errors.toString());
    assertTrue(errors.get(0).message().contains("9bad"), errors.get(0).message());
  }
}
