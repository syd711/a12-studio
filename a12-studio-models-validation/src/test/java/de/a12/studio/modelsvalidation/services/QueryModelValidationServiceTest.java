package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.HeaderRolesValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The validators a Query Model gets from its validation service, as far as they are not the Query Model's own. */
class QueryModelValidationServiceTest {

  @Test
  void theRolesOfAQueryModelAreChecked() {
    QueryModel model = TestModels.load("/querymodel/QueryPagingBoundsValidator_invalid.json", QueryModel.class);
    Annotation roles = new Annotation();
    roles.setName("roles");
    roles.setValue("admin,9bad");
    model.getAnnotations().add(roles);

    List<ModelValidationError> errors = new QueryModelValidationService().validate(model, TestModels.context(model)).stream()
        .filter(error -> HeaderRolesValidator.ELEMENT_ID.equals(error.elementId())).toList();

    assertEquals(1, errors.size(), errors.toString());
    assertTrue(errors.get(0).message().contains("9bad"), errors.get(0).message());
  }
}
