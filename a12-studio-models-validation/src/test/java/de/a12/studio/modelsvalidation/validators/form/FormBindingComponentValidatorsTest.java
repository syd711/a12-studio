package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.BindingComponent;
import de.a12.studio.models.formmodel.BindingComponentModelsSme;
import de.a12.studio.models.formmodel.BindingComponentType;
import de.a12.studio.models.formmodel.BindingContent;
import de.a12.studio.models.formmodel.BindingDetails;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.ValidationContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormBindingComponentValidatorsTest {

  private static Binding binding(BindingComponent mainComponent) {
    Binding binding = new Binding();
    binding.setId("binding1");
    BindingContent content = new BindingContent();
    BindingDetails details = new BindingDetails();
    details.setMainComponent(mainComponent);
    content.setDetails(details);
    binding.setBinding(content);
    return binding;
  }

  private static FormModel formModel(Binding binding) {
    FormModel model = new FormModel();
    model.setId("Binding_FM");
    FormModelContent content = new FormModelContent();
    Screen screen = new Screen();
    screen.getScreenElements().add(binding);
    content.getScreens().add(screen);
    model.setContent(content);
    return model;
  }

  private static OverviewModel overviewModel(String id) {
    OverviewModel model = new OverviewModel();
    model.setId(id);
    model.setModelType(de.a12.studio.models.ModelType.OVERVIEW);
    return model;
  }

  @Test
  void referenceValidatorAcceptsExistingOverviewReferences() {
    BindingComponent component = new BindingComponent();
    component.setName(BindingComponentType.DUAL_PANE_SELECTION);
    BindingComponentModelsSme modelsSme = new BindingComponentModelsSme();
    modelsSme.setAvailableItemsOverview("Available_Ov");
    modelsSme.setSelectedItemsOverview("Selected_Ov");
    component.setModelsSME(modelsSme);
    FormModel model = formModel(binding(component));

    ValidationContext context = TestModels.contextWithOtherModels(model, overviewModel("Available_Ov"), overviewModel("Selected_Ov"));
    assertEquals(List.of(), new FormBindingComponentReferenceValidator().validate(model, context));
  }

  @Test
  void referenceValidatorReportsUnresolvedOverviewReference() {
    BindingComponent component = new BindingComponent();
    component.setName(BindingComponentType.DUAL_PANE_SELECTION);
    BindingComponentModelsSme modelsSme = new BindingComponentModelsSme();
    modelsSme.setAvailableItemsOverview("Missing_Ov");
    component.setModelsSME(modelsSme);
    FormModel model = formModel(binding(component));

    List<ModelValidationError> errors = new FormBindingComponentReferenceValidator().validate(model, TestModels.context(model));
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Missing_Ov"));
  }

  @Test
  void referenceValidatorReportsAWrongModelType() {
    BindingComponent component = new BindingComponent();
    component.setName(BindingComponentType.DUAL_PANE_SELECTION);
    BindingComponentModelsSme modelsSme = new BindingComponentModelsSme();
    // Names an Order_DM (a Document Model), not an Overview Model - the wrong type for availableItemsOverview.
    modelsSme.setAvailableItemsOverview("Order_DM");
    component.setModelsSME(modelsSme);
    FormModel model = formModel(binding(component));

    de.a12.studio.models.documentmodel.DocumentModel documentModel = new de.a12.studio.models.documentmodel.DocumentModel();
    documentModel.setId("Order_DM");
    documentModel.setContent(new DocumentModelContent());

    ValidationContext context = TestModels.contextWithDocumentModels(model, documentModel);
    List<ModelValidationError> errors = new FormBindingComponentReferenceValidator().validate(model, context);
    assertEquals(1, errors.size());
  }

  @Test
  void requiredFieldsValidatorReportsMissingAvailableAndSelectedItemsForDualPane() {
    BindingComponent component = new BindingComponent();
    component.setName(BindingComponentType.DUAL_PANE_SELECTION);
    FormModel model = formModel(binding(component));

    List<ModelValidationError> errors = new FormBindingComponentRequiredFieldsValidator().validate(model, TestModels.context(model));
    assertEquals(2, errors.size());
  }

  @Test
  void requiredFieldsValidatorOnlyRequiresSelectedItemsForTableList() {
    BindingComponent component = new BindingComponent();
    component.setName(BindingComponentType.TABLE_LIST);
    FormModel model = formModel(binding(component));

    List<ModelValidationError> errors = new FormBindingComponentRequiredFieldsValidator().validate(model, TestModels.context(model));
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().toLowerCase().contains("selected"));
  }

  @Test
  void requiredFieldsValidatorAcceptsAFullyConfiguredDualPaneComponent() {
    BindingComponent component = new BindingComponent();
    component.setName(BindingComponentType.DUAL_PANE_SELECTION);
    BindingComponentModelsSme modelsSme = new BindingComponentModelsSme();
    modelsSme.setAvailableItemsOverview("Available_Ov");
    modelsSme.setSelectedItemsOverview("Selected_Ov");
    component.setModelsSME(modelsSme);
    FormModel model = formModel(binding(component));

    assertEquals(List.of(), new FormBindingComponentRequiredFieldsValidator().validate(model, TestModels.context(model)));
  }

  @Test
  void requiredFieldsValidatorIgnoresAComponentWithNoTypeSelected() {
    FormModel model = formModel(binding(new BindingComponent()));
    assertEquals(List.of(), new FormBindingComponentRequiredFieldsValidator().validate(model, TestModels.context(model)));
  }
}
