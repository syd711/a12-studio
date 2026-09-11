package de.a12.studio.modelsvalidation.validators.relationshipui;

import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
import de.a12.studio.models.relationshipuimodel.DualPaneSelectionComponent;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.ValidationContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelationshipUiValidatorsTest {

  private RelationshipUiModel load(String name) {
    return TestModels.load("/relationshipuimodel/" + name + ".json", RelationshipUiModel.class);
  }

  private RelationshipModel relationshipModel(String id, String... roles) {
    RelationshipModel model = new RelationshipModel();
    model.setId(id);
    RelationshipModelContent content = new RelationshipModelContent();
    for (String role : roles) {
      EntityCharacteristic entity = new EntityCharacteristic();
      entity.setRole(role);
      content.getEntityCharacteristics().add(entity);
    }
    model.setContent(content);
    return model;
  }

  @Test
  void relationshipReferenceValidatorAcceptsExistingRelationship() {
    RelationshipUiModel model = load("DualPaneMinimal_Ru");
    RelationshipModel relationship = relationshipModel(model.getContent().getRelationshipName(), model.getContent().getTargetRole());
    ValidationContext context = TestModels.contextWithOtherModels(model, relationship);

    assertEquals(List.of(), new RelationshipUiRelationshipReferenceValidator().validate(model, context));
    assertEquals(List.of(), new RelationshipUiTargetRoleValidator().validate(model, context));
  }

  @Test
  void relationshipReferenceValidatorReportsMissingRelationship() {
    RelationshipUiModel model = load("DualPaneMinimal_Ru");
    List<ModelValidationError> errors = new RelationshipUiRelationshipReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains(model.getContent().getRelationshipName()));
  }

  @Test
  void relationshipReferenceValidatorReportsBlankRelationshipName() {
    RelationshipUiModel model = load("DualPaneMinimal_Ru");
    model.getContent().setRelationshipName(null);
    List<ModelValidationError> errors = new RelationshipUiRelationshipReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
  }

  @Test
  void targetRoleValidatorReportsRoleNotOnRelationship() {
    RelationshipUiModel model = load("DualPaneMinimal_Ru");
    RelationshipModel relationship = relationshipModel(model.getContent().getRelationshipName(), "SomeOtherRole");
    ValidationContext context = TestModels.contextWithOtherModels(model, relationship);

    List<ModelValidationError> errors = new RelationshipUiTargetRoleValidator().validate(model, context);
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains(model.getContent().getTargetRole()));
  }

  @Test
  void componentValidatorReportsMissingComponent() {
    RelationshipUiModel model = load("DualPaneMinimal_Ru");
    model.getContent().setComponent(null);

    List<ModelValidationError> errors = new RelationshipUiComponentValidator().validate(model, TestModels.context(model));
    assertEquals(1, errors.size());
  }

  @Test
  void componentValidatorReportsMissingDualPaneReferences() {
    RelationshipUiModel model = load("DualPaneMinimal_Ru");
    DualPaneSelectionComponent component = (DualPaneSelectionComponent) model.getContent().getComponent();
    component.setAvailableItemsOverviewModel(null);
    component.setSelectedItemsOverviewModel("");

    List<ModelValidationError> errors = new RelationshipUiComponentValidator().validate(model, TestModels.context(model));
    assertEquals(2, errors.size());
  }

  @Test
  void componentValidatorAcceptsEveryRealFixture() {
    for (String fixture : new String[] {"DualPaneMinimal_Ru", "DualPaneWithLinkForm_Ru", "TableListReadOnly_Ru",
        "TableListWithEditModal_Ru", "TableListEditModalNoDestructive_Ru", "DropDownSelection_Ru"}) {
      RelationshipUiModel model = load(fixture);
      assertEquals(List.of(), new RelationshipUiComponentValidator().validate(model, TestModels.context(model)), fixture);
    }
  }
}
