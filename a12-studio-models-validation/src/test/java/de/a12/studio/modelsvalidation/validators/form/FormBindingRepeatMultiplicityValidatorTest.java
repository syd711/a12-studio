package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.formmodel.BindingContent;
import de.a12.studio.models.formmodel.BindingDetails;
import de.a12.studio.models.formmodel.BindingRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.LinkConstraints;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormBindingRepeatMultiplicityValidatorTest {

  private static FormModel formModel(String relationshipName) {
    FormModel model = new FormModel();
    model.setId("Order_FM");
    FormModelContent content = new FormModelContent();
    Screen screen = new Screen();
    BindingRepeat bindingRepeat = new BindingRepeat();
    bindingRepeat.setId("bindingrepeat1");
    BindingContent bindingContent = new BindingContent();
    BindingDetails details = new BindingDetails();
    details.setRelationshipName(relationshipName);
    bindingContent.setDetails(details);
    bindingRepeat.setBinding(bindingContent);
    screen.getScreenElements().add(bindingRepeat);
    content.getScreens().add(screen);
    model.setContent(content);
    ModelReference reference = new ModelReference();
    reference.setModelType(ModelType.DOCUMENT);
    reference.setPurpose(ModelReference.PURPOSE_DATA_BINDING);
    reference.setReference("Order_CdM");
    model.getModelReferences().add(reference);
    return model;
  }

  private static ComposedDocumentModel cdm(Integer repeatability) {
    ComposedDocumentModel cdm = new ComposedDocumentModel();
    cdm.setId("Order_CdM");
    cdm.setContent(new DocumentModelContent());
    cdm.getContent().setModelRoot(new ModelRoot());
    GroupElement group = new GroupElement();
    group.setId("group1");
    group.getAnnotations().add(annotation("cdm.relationship", "OrderPosition_ReM"));
    group.getAnnotations().add(annotation("cdm.targetRole", "position"));
    GroupConfig config = new GroupConfig();
    config.setRepeatability(repeatability);
    group.setGroup(config);
    cdm.getContent().getModelRoot().getRootGroups().add(group);
    return cdm;
  }

  private static Annotation annotation(String name, String value) {
    Annotation annotation = new Annotation();
    annotation.setName(name);
    annotation.setValue(value);
    return annotation;
  }

  private static RelationshipModel relationship(Integer upperLimit, boolean unbounded) {
    RelationshipModel model = new RelationshipModel();
    model.setId("OrderPosition_ReM");
    RelationshipModelContent content = new RelationshipModelContent();
    EntityCharacteristic characteristic = new EntityCharacteristic();
    characteristic.setRole("position");
    Multiplicity multiplicity = new Multiplicity();
    multiplicity.setUnbounded(unbounded);
    multiplicity.setUpperLimit(upperLimit);
    LinkConstraints constraints = new LinkConstraints();
    constraints.setMultiplicity(multiplicity);
    characteristic.setLinkConstraints(constraints);
    content.getEntityCharacteristics().add(characteristic);
    model.setContent(content);
    return model;
  }

  private static List<ModelValidationError> validate(FormModel model, ComposedDocumentModel cdm, RelationshipModel relationship) {
    return new FormBindingRepeatMultiplicityValidator().validate(model, TestModels.contextWithOtherModels(model, cdm, relationship));
  }

  @Test
  void reportsRepeatabilityOneAgainstAToManyTargetRole() {
    List<ModelValidationError> errors = validate(formModel("OrderPosition_ReM"), cdm(1), relationship(3, false));
    assertEquals(1, errors.size());
    assertEquals("bindingrepeat1", errors.get(0).elementId());
  }

  @Test
  void reportsRepeatabilityOneAgainstAnUnboundedTargetRole() {
    assertEquals(1, validate(formModel("OrderPosition_ReM"), cdm(1), relationship(null, true)).size());
  }

  @Test
  void acceptsARepeatabilityGreaterThanOne() {
    assertEquals(List.of(), validate(formModel("OrderPosition_ReM"), cdm(2), relationship(3, false)));
  }

  @Test
  void acceptsAToOneTargetRole() {
    assertEquals(List.of(), validate(formModel("OrderPosition_ReM"), cdm(1), relationship(1, false)));
  }

  @Test
  void ignoresABindingWithoutARelationshipOrWithoutAMatchingGroup() {
    assertEquals(List.of(), validate(formModel(null), cdm(1), relationship(3, false)));
    assertEquals(List.of(), validate(formModel("Other_ReM"), cdm(1), relationship(3, false)));
  }
}
