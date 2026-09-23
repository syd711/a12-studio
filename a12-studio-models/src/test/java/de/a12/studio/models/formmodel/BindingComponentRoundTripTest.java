package de.a12.studio.models.formmodel;

import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the Binding UI-component configuration fields added to {@link BindingDetails}
 * ({@code mainComponent}/{@code editModalComponent}, {@code isFixedRelationship}, {@code
 * cdmChildActivitiesEnabled}, {@code modificationConfiguration}) through a load-reserialize-reload cycle, since
 * {@code BasicProjectModelsRoundTripTest}'s fixtures don't exercise a real {@link Binding}.
 */
class BindingComponentRoundTripTest {

  @Test
  void loadsAndRoundTripsBindingComponentConfiguration() throws Exception {
    String json;
    try (InputStream in = getClass().getResourceAsStream("/formmodel/BindingComponent_FM.json")) {
      json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    FormModel model = JsonSettings.objectMapper.readValue(json, FormModel.class);
    assertBindingComponentConfiguration(model);

    String reserialized = JsonSettings.objectMapper.writeValueAsString(model);
    FormModel reloaded = JsonSettings.objectMapper.readValue(reserialized, FormModel.class);
    assertBindingComponentConfiguration(reloaded);
  }

  private void assertBindingComponentConfiguration(FormModel model) {
    Binding binding = (Binding) model.getContent().getScreens().get(0).getScreenElements().get(0);
    BindingDetails details = binding.getBinding().getDetails();

    assertEquals("OrderPosition_ReM", details.getRelationshipName());
    assertEquals(Boolean.TRUE, details.getIsFixedRelationship());
    assertEquals(Boolean.TRUE, details.getCdmChildActivitiesEnabled());

    BindingComponent mainComponent = details.getMainComponent();
    assertEquals(BindingComponentType.DUAL_PANE_SELECTION, mainComponent.getName());
    assertEquals("Position_Available_Ov", mainComponent.getModelsSME().getAvailableItemsOverview());
    assertEquals(10, mainComponent.getCandidatePageSize());
    assertEquals(5, mainComponent.getLinkPageSize());
    assertEquals(400, mainComponent.getPropsExtensions().getDualPaneProps().getHeight());
    assertEquals("Available Positions",
        mainComponent.getPropsExtensions().getDualPaneProps().getAvailableItemsTable().getLabel().get(0).getText());

    BindingComponent editModalComponent = details.getEditModalComponent();
    assertEquals(BindingComponentType.TABLE_LIST, editModalComponent.getName());
    assertEquals("PositionEdit_FM", editModalComponent.getPropsExtensions().getTableListProps().getEditComponent());
    assertEquals(600, editModalComponent.getPropsExtensions().getTableListProps().getEditDialogWidth());

    BindingModificationConfiguration modificationConfiguration = details.getModificationConfiguration();
    assertEquals("extendParent", modificationConfiguration.getScdmAddEditDescriptor());
    assertEquals(Boolean.TRUE, modificationConfiguration.getExtendParentActivityDescriptor());
    assertTrue(modificationConfiguration.getAddButtonLabel().get(0).getText().contains("Add Position"));
  }
}
