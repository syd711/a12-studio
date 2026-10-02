package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.BindingContent;
import de.a12.studio.models.formmodel.BindingDetails;
import de.a12.studio.models.formmodel.BindingRepeat;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FieldConfiguration;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The Composed Document Model validators against CDMs, Document Models and Relationship Models copied unchanged from
 * SME's own integration fixtures ({@code client/resources/test/modules/documentModel/integration_cdm}). Pins the two
 * things only real files show: includes in a CDM are written as {@code modelAlias} (resolved through the header's
 * {@code modelReferences}), and a Form Model bound to a CDM references included fields by their kernel-expanded id
 * ({@code <include group id>_<field id>}).
 */
class SmeCdmFixtureValidatorsTest {

  private static final String DIR = "/smecdm/";

  private static List<A12Model<?>> projectModels(String cdmFixture) {
    List<A12Model<?>> models = new ArrayList<>();
    for (String dm : List.of("A_DM", "B_DM", "C_DM", "A_child_DM", "A_grandchild_DM", "LinkedDM")) {
      models.add(TestModels.load(DIR + dm + ".json", DocumentModel.class));
    }
    for (String rel : List.of("A_A", "A_B", "A_C", "B_C", "A_A_child")) {
      models.add(TestModels.load(DIR + rel + ".json", RelationshipModel.class));
    }
    models.add(TestModels.load(DIR + cdmFixture + ".json", ComposedDocumentModel.class));
    return models;
  }

  private static FormModel form(String cdmId) {
    FormModel model = new FormModel();
    model.setId("Test_FM");
    model.setContent(new FormModelContent());
    model.getContent().getScreens().add(new Screen());
    ModelReference reference = new ModelReference();
    reference.setModelType(ModelType.DOCUMENT);
    reference.setPurpose(ModelReference.PURPOSE_DATA_BINDING);
    reference.setReference(cdmId);
    model.getModelReferences().add(reference);
    return model;
  }

  private static FormModel bindingForm(String cdmId, String relationshipName) {
    FormModel model = form(cdmId);
    BindingRepeat bindingRepeat = new BindingRepeat();
    bindingRepeat.setId("bindingrepeat1");
    BindingContent content = new BindingContent();
    BindingDetails details = new BindingDetails();
    details.setRelationshipName(relationshipName);
    content.setDetails(details);
    bindingRepeat.setBinding(content);
    model.getContent().getScreens().get(0).getScreenElements().add(bindingRepeat);
    return model;
  }

  private static FormModel initialValueForm(String cdmId, String elementRef) {
    FormModel model = form(cdmId);
    FieldConfiguration configuration = new FieldConfiguration();
    FieldConfigEntry entry = new FieldConfigEntry();
    entry.setElementRef(elementRef);
    entry.setInitialValue("x");
    configuration.getField().add(entry);
    model.getContent().setFieldConfiguration(configuration);
    return model;
  }

  private static List<ModelValidationError> multiplicity(FormModel form, List<A12Model<?>> models) {
    return new FormBindingRepeatMultiplicityValidator()
        .validate(form, TestModels.contextWithOtherModels(form, models.toArray(new A12Model<?>[0])));
  }

  private static List<ModelValidationError> heterogeneity(FormModel form, List<A12Model<?>> models) {
    return new FormInitialValueHeterogeneousRelationshipValidator()
        .validate(form, TestModels.contextWithOtherModels(form, models.toArray(new A12Model<?>[0])));
  }

  private static GroupElement relationshipGroup(List<A12Model<?>> models, String relationship) {
    ComposedDocumentModel cdm = (ComposedDocumentModel) models.get(models.size() - 1);
    return cdm.getContent().getModelRoot().getRootGroups().stream()
        .filter(group -> group.getAnnotations().stream().anyMatch(a -> "cdm.relationship".equals(a.getName()) && relationship.equals(a.getValue())))
        .findFirst().orElseThrow();
  }

  @Test
  void acceptsTheRepeatabilitiesOfSmesOwnCdm() {
    List<A12Model<?>> models = projectModels("Cdm");
    for (String relationship : List.of("A_B", "A_C", "A_A")) {
      assertEquals(List.of(), multiplicity(bindingForm("Cdm", relationship), models), relationship);
    }
  }

  @Test
  void reportsARelationshipGroupWithRepeatabilityOneAgainstAToManyTargetRole() {
    List<A12Model<?>> models = projectModels("Cdm");
    relationshipGroup(models, "A_B").getGroup().setRepeatability(1);
    assertEquals(1, multiplicity(bindingForm("Cdm", "A_B"), models).size());
  }

  @Test
  void acceptsSmesInvalidRelationshipRepetitionsCdm() {
    // Its repeatability (2,000,000) exceeds the relationship's upper limit (1,000,000): a different check, not this one.
    assertEquals(List.of(), multiplicity(bindingForm("Cdm_invalid_RelationshipRepetitions", "A_B"),
        projectModels("Cdm_invalid_RelationshipRepetitions")));
  }

  @Test
  void reportsAnInitialValueBehindAHeterogeneousToManyRelationship() {
    List<ModelValidationError> errors = heterogeneity(
        initialValueForm("Cdm_heterogeneity_child_2nd_level", "include_cdb5c_field_a0fbd"), projectModels("Cdm_heterogeneity_child_2nd_level"));
    assertEquals(1, errors.size());
  }

  @Test
  void acceptsAnInitialValueOnTheIncludedRootOfTheCdm() {
    assertEquals(List.of(), heterogeneity(
        initialValueForm("Cdm_heterogeneity_child_2nd_level", "include_203c5_field_a0fbd"), projectModels("Cdm_heterogeneity_child_2nd_level")));
  }

  @Test
  void acceptsAnInitialValueBehindAHomogeneousRelationship() {
    assertEquals(List.of(), heterogeneity(initialValueForm("Cdm", "include_a55cc_field_8a3e3"), projectModels("Cdm")));
  }

  @Test
  void keepsTheIncludeAliasesOfACdmWhenSaving() throws Exception {
    ComposedDocumentModel cdm = TestModels.load(DIR + "Cdm.json", ComposedDocumentModel.class);
    String json = de.a12.studio.models.util.JsonSettings.objectMapper.writeValueAsString(cdm);
    assertEquals(7, json.split("\"modelAlias\"", -1).length - 1);
  }
}
