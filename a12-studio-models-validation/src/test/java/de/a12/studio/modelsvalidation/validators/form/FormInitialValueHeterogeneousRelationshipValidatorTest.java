package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.ControlIndex;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FieldConfiguration;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.LinkConstraints;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormInitialValueHeterogeneousRelationshipValidatorTest {

  private static Annotation annotation(String name, String value) {
    Annotation annotation = new Annotation();
    annotation.setName(name);
    annotation.setValue(value);
    return annotation;
  }

  /** A Document Model with one root group holding the single field {@code field_<id>}. */
  private static DocumentModel documentModel(String id, String superType) {
    DocumentModel model = new DocumentModel();
    model.setId(id);
    model.setContent(new DocumentModelContent());
    model.getContent().setModelRoot(new ModelRoot());
    if (superType != null) {
      model.getAnnotations().add(annotation("superTypes", superType));
    }
    GroupElement root = new GroupElement();
    root.setId(id + "_root");
    root.setGroup(new GroupConfig());
    FieldElement field = new FieldElement();
    field.setId("field_" + id);
    root.getGroup().getElements().add(field);
    model.getContent().getModelRoot().getRootGroups().add(root);
    return model;
  }

  /** CDM with one root-level field and a relationship group that includes Position_DM. */
  private static ComposedDocumentModel cdm() {
    ComposedDocumentModel cdm = new ComposedDocumentModel();
    cdm.setId("Order_CdM");
    cdm.setContent(new DocumentModelContent());
    cdm.getContent().setModelRoot(new ModelRoot());
    GroupElement root = new GroupElement();
    root.setId("cdmRoot");
    root.setGroup(new GroupConfig());
    FieldElement rootField = new FieldElement();
    rootField.setId("rootField");
    root.getGroup().getElements().add(rootField);
    GroupElement relationship = new GroupElement();
    relationship.setId("relGroup");
    relationship.getAnnotations().add(annotation("cdm.relationship", "OrderPosition_ReM"));
    relationship.getAnnotations().add(annotation("cdm.targetRole", "position"));
    relationship.getAnnotations().add(annotation("cdm.targetDocumentModel", "Position_DM"));
    relationship.setGroup(new GroupConfig());
    GroupElement include = new GroupElement();
    include.setId("include1");
    GroupConfig includeGroup = new GroupConfig();
    IncludeConfig includeConfig = new IncludeConfig();
    includeConfig.setReference("Position_DM");
    includeGroup.setIncludeConfig(includeConfig);
    include.setGroup(includeGroup);
    relationship.getGroup().getElements().add(include);
    root.getGroup().getElements().add(relationship);
    cdm.getContent().getModelRoot().getRootGroups().add(root);
    return cdm;
  }

  private static RelationshipModel relationship(int upperLimit) {
    RelationshipModel model = new RelationshipModel();
    model.setId("OrderPosition_ReM");
    RelationshipModelContent content = new RelationshipModelContent();
    EntityCharacteristic characteristic = new EntityCharacteristic();
    characteristic.setRole("position");
    Multiplicity multiplicity = new Multiplicity();
    multiplicity.setUpperLimit(upperLimit);
    LinkConstraints constraints = new LinkConstraints();
    constraints.setMultiplicity(multiplicity);
    characteristic.setLinkConstraints(constraints);
    content.getEntityCharacteristics().add(characteristic);
    model.setContent(content);
    return model;
  }

  private static FormModel formModel(String elementRef, String indexValue) {
    FormModel model = new FormModel();
    model.setId("Order_FM");
    FormModelContent content = new FormModelContent();
    FieldConfiguration configuration = new FieldConfiguration();
    FieldConfigEntry entry = new FieldConfigEntry();
    entry.setElementRef(elementRef);
    entry.setInitialValue("x");
    configuration.getField().add(entry);
    content.setFieldConfiguration(configuration);
    Screen screen = new Screen();
    Control control = new Control();
    control.setId("control1");
    control.setElementRef(elementRef);
    if (indexValue != null) {
      ControlIndex index = new ControlIndex();
      index.setValue(indexValue);
      control.setIndex(index);
    }
    ControlGrid grid = new ControlGrid();
    Row row = new Row();
    row.getCell().add(control);
    grid.getRow().add(row);
    screen.getScreenElements().add(grid);
    content.getScreens().add(screen);
    model.setContent(content);
    ModelReference reference = new ModelReference();
    reference.setModelType(ModelType.DOCUMENT);
    reference.setPurpose(ModelReference.PURPOSE_DATA_BINDING);
    reference.setReference("Order_CdM");
    model.getModelReferences().add(reference);
    return model;
  }

  private static List<ModelValidationError> validate(FormModel model, int upperLimit, DocumentModel... variants) {
    List<A12Model<?>> others = new ArrayList<>(List.of(cdm(), documentModel("Position_DM", null), relationship(upperLimit)));
    others.addAll(List.of(variants));
    return new FormInitialValueHeterogeneousRelationshipValidator()
        .validate(model, TestModels.contextWithOtherModels(model, others.toArray(new A12Model<?>[0])));
  }

  @Test
  void reportsAnInitialValueBehindAHeterogeneousRelationship() {
    DocumentModel special = documentModel("SpecialPosition_DM", "Position_DM");
    assertEquals(1, validate(formModel("field_Position_DM", null), 1, special).size());
  }

  @Test
  void acceptsAnInitialValueBehindAHomogeneousRelationship() {
    assertEquals(List.of(), validate(formModel("field_Position_DM", null), 1));
  }

  @Test
  void acceptsAnInitialValueOnAFieldOfTheCdmRoot() {
    DocumentModel special = documentModel("SpecialPosition_DM", "Position_DM");
    assertEquals(List.of(), validate(formModel("rootField", null), 3, special));
  }

  @Test
  void reportsAnIndexedControlBehindAHeterogeneousRelationshipOnlyOnce() {
    DocumentModel special = documentModel("SpecialPosition_DM", "Position_DM");
    assertEquals(1, validate(formModel("field_Position_DM", "1"), 3, special).size());
  }
}
