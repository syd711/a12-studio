package de.a12.studio.modelsvalidation.validators.composeddocument;

import de.a12.studio.models.composeddocumentmodel.CdmRelationshipStep;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModelResolver;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.ValidationContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CdmValidatorsTest {

  private static ComposedDocumentModel composedDocumentModel(String id) {
    ComposedDocumentModel model = new ComposedDocumentModel();
    model.setId(id);
    model.setContent(new DocumentModelContent());
    return model;
  }

  private static DocumentModel documentModel(String id) {
    DocumentModel model = new DocumentModel();
    model.setId(id);
    model.setContent(new DocumentModelContent());
    return model;
  }

  private static RelationshipModel relationshipModel(String id, String role1, String documentModel1, String role2, String documentModel2) {
    RelationshipModel model = new RelationshipModel();
    model.setId(id);
    RelationshipModelContent content = new RelationshipModelContent();
    EntityCharacteristic first = new EntityCharacteristic();
    first.setRole(role1);
    first.setDocumentModel(documentModel1);
    EntityCharacteristic second = new EntityCharacteristic();
    second.setRole(role2);
    second.setDocumentModel(documentModel2);
    content.getEntityCharacteristics().add(first);
    content.getEntityCharacteristics().add(second);
    model.setContent(content);
    return model;
  }

  @Test
  void queryRootValidatorReportsMissingQueryRoot() {
    ComposedDocumentModel model = composedDocumentModel("Order_CdM");
    List<ModelValidationError> errors = new CdmQueryRootReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals(CdmQueryRootReferenceValidator.ELEMENT_ID, errors.get(0).elementId());
  }

  @Test
  void queryRootValidatorReportsUnresolvedQueryRoot() {
    ComposedDocumentModel model = composedDocumentModel("Order_CdM");
    ComposedDocumentModelResolver.setQueryRootId(model, "MissingModel");

    List<ModelValidationError> errors = new CdmQueryRootReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("MissingModel"));
  }

  @Test
  void queryRootValidatorAcceptsResolvedQueryRoot() {
    ComposedDocumentModel model = composedDocumentModel("Order_CdM");
    DocumentModel root = documentModel("Order_DM");
    ComposedDocumentModelResolver.setQueryRootId(model, root.getId());

    ValidationContext context = TestModels.contextWithDocumentModels(model, root);
    assertEquals(List.of(), new CdmQueryRootReferenceValidator().validate(model, context));
  }

  @Test
  void relationshipStepValidatorAcceptsAConsistentStep() {
    ComposedDocumentModel model = composedDocumentModel("Order_CdM");
    RelationshipModel relationship = relationshipModel("OrderPosition_ReM", "order", "Order_DM", "position", "Position_DM");
    ComposedDocumentModelResolver.setRelationshipSteps(model,
        List.of(new CdmRelationshipStep(relationship.getId(), "order", "position", "Position_DM")));

    ValidationContext context = TestModels.contextWithOtherModels(model, relationship);
    assertEquals(List.of(), new CdmRelationshipStepValidator().validate(model, context));
  }

  @Test
  void relationshipStepValidatorReportsUnresolvedRelationship() {
    ComposedDocumentModel model = composedDocumentModel("Order_CdM");
    ComposedDocumentModelResolver.setRelationshipSteps(model,
        List.of(new CdmRelationshipStep("MissingRelationship", "order", "position", "Position_DM")));

    List<ModelValidationError> errors = new CdmRelationshipStepValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("MissingRelationship"));
  }

  @Test
  void relationshipStepValidatorReportsRoleNotOnRelationship() {
    ComposedDocumentModel model = composedDocumentModel("Order_CdM");
    RelationshipModel relationship = relationshipModel("OrderPosition_ReM", "order", "Order_DM", "position", "Position_DM");
    ComposedDocumentModelResolver.setRelationshipSteps(model,
        List.of(new CdmRelationshipStep(relationship.getId(), "wrongRole", "position", "Position_DM")));

    ValidationContext context = TestModels.contextWithOtherModels(model, relationship);
    List<ModelValidationError> errors = new CdmRelationshipStepValidator().validate(model, context);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("wrongRole"));
  }

  @Test
  void relationshipStepValidatorReportsTargetDocumentModelMismatch() {
    ComposedDocumentModel model = composedDocumentModel("Order_CdM");
    RelationshipModel relationship = relationshipModel("OrderPosition_ReM", "order", "Order_DM", "position", "Position_DM");
    ComposedDocumentModelResolver.setRelationshipSteps(model,
        List.of(new CdmRelationshipStep(relationship.getId(), "order", "position", "WrongTarget_DM")));

    ValidationContext context = TestModels.contextWithOtherModels(model, relationship);
    List<ModelValidationError> errors = new CdmRelationshipStepValidator().validate(model, context);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("WrongTarget_DM") && errors.get(0).message().contains("Position_DM"));
  }

  @Test
  void relationshipStepValidatorHandlesMultipleSteps() {
    ComposedDocumentModel model = composedDocumentModel("Order_CdM");
    RelationshipModel first = relationshipModel("OrderPosition_ReM", "order", "Order_DM", "position", "Position_DM");
    RelationshipModel second = relationshipModel("PositionProduct_ReM", "position", "Position_DM", "product", "Product_DM");
    ComposedDocumentModelResolver.setRelationshipSteps(model, List.of(
        new CdmRelationshipStep(first.getId(), "order", "position", "Position_DM"),
        new CdmRelationshipStep(second.getId(), "position", "product", "Product_DM")));

    ValidationContext context = TestModels.contextWithOtherModels(model, first, second);
    assertEquals(List.of(), new CdmRelationshipStepValidator().validate(model, context));
    assertEquals(2, ComposedDocumentModelResolver.getRelationshipSteps(model).size());
  }
}
