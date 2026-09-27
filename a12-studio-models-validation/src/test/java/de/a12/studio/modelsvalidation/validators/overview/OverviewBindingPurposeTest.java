package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.BindingComponent;
import de.a12.studio.models.formmodel.BindingComponentModelsSme;
import de.a12.studio.models.formmodel.BindingContent;
import de.a12.studio.models.formmodel.BindingDetails;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.relationshipuimodel.DualPaneSelectionComponent;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModelContent;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.ValidationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OverviewBindingPurposeTest {

  @Test
  void resolvesAvailableAndSelectedItemsFromAFormModelBinding() {
    FormModel formModel = new FormModel();
    formModel.setId("Team_Fm");
    FormModelContent content = new FormModelContent();
    Screen screen = new Screen();
    Binding binding = new Binding();
    binding.setId("binding1");
    BindingContent bindingContent = new BindingContent();
    BindingDetails details = new BindingDetails();
    BindingComponent mainComponent = new BindingComponent();
    BindingComponentModelsSme modelsSme = new BindingComponentModelsSme();
    modelsSme.setAvailableItemsOverview("Person_AvailableItems_OM");
    modelsSme.setSelectedItemsOverview("Person_SelectedItems_OM");
    mainComponent.setModelsSME(modelsSme);
    details.setMainComponent(mainComponent);
    bindingContent.setDetails(details);
    binding.setBinding(bindingContent);
    screen.getScreenElements().add(binding);
    content.getScreens().add(screen);
    formModel.setContent(content);

    OverviewModel available = overviewModel("Person_AvailableItems_OM");
    ValidationContext context = TestModels.contextWithOtherModels(available, formModel);
    assertEquals(OverviewBindingPurpose.AVAILABLE_ITEM, OverviewBindingPurpose.resolve("Person_AvailableItems_OM", context));
    assertEquals(OverviewBindingPurpose.SELECTED_ITEM, OverviewBindingPurpose.resolve("Person_SelectedItems_OM", context));
    assertNull(OverviewBindingPurpose.resolve("SomeOther_OM", context));
  }

  @Test
  void resolvesFromARelationshipUiModelDualPaneComponent() {
    RelationshipUiModel relationshipUiModel = new RelationshipUiModel();
    relationshipUiModel.setId("Teammembers_Ru");
    RelationshipUiModelContent content = new RelationshipUiModelContent();
    DualPaneSelectionComponent dualPane = new DualPaneSelectionComponent();
    dualPane.setAvailableItemsOverviewModel("Teammembers_AvailableItems_Ov");
    dualPane.setSelectedItemsOverviewModel("Teammembers_SelectedItems_Ov");
    content.setComponent(dualPane);
    relationshipUiModel.setContent(content);

    OverviewModel available = overviewModel("Teammembers_AvailableItems_Ov");
    ValidationContext context = TestModels.contextWithOtherModels(available, relationshipUiModel);
    assertEquals(OverviewBindingPurpose.AVAILABLE_ITEM, OverviewBindingPurpose.resolve("Teammembers_AvailableItems_Ov", context));
    assertEquals(OverviewBindingPurpose.SELECTED_ITEM, OverviewBindingPurpose.resolve("Teammembers_SelectedItems_Ov", context));
  }

  @Test
  void returnsNullForAPlainNonBindingOverview() {
    OverviewModel model = overviewModel("Plain_OM");
    ValidationContext context = TestModels.context(model);
    assertNull(OverviewBindingPurpose.resolve("Plain_OM", context));
  }

  private static OverviewModel overviewModel(String id) {
    OverviewModel model = new OverviewModel();
    model.setId(id);
    model.setContent(new de.a12.studio.models.overviewmodel.OverviewModelContent());
    return model;
  }
}
