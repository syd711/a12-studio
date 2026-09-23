package de.a12.studio.models.formmodel;

import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Pins {@link BindingRepeat}'s shape (the {@code AbstractRepeat} fields plus its own nullable {@code binding})
 * through a load-reserialize-reload cycle, since {@code BasicProjectModelsRoundTripTest}'s fixtures don't
 * exercise this new screen element type.
 */
class BindingRepeatRoundTripTest {

  @Test
  void loadsAndRoundTripsABindingRepeat() throws Exception {
    String json;
    try (InputStream in = getClass().getResourceAsStream("/formmodel/BindingRepeat_FM.json")) {
      json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    FormModel model = JsonSettings.objectMapper.readValue(json, FormModel.class);
    assertBindingRepeat(model);

    String reserialized = JsonSettings.objectMapper.writeValueAsString(model);
    FormModel reloaded = JsonSettings.objectMapper.readValue(reserialized, FormModel.class);
    assertBindingRepeat(reloaded);
  }

  private void assertBindingRepeat(FormModel model) {
    ScreenElement element = model.getContent().getScreens().get(0).getScreenElements().get(0);
    BindingRepeat bindingRepeat = assertInstanceOf(BindingRepeat.class, element);
    assertEquals(ScreenElementType.BINDING_REPEAT, bindingRepeat.getType());
    assertEquals(Boolean.TRUE, bindingRepeat.getEnableAdd());
    assertEquals(Boolean.TRUE, bindingRepeat.getEnableRemove());

    BindingDetails details = bindingRepeat.getBinding().getDetails();
    assertEquals("OrderPosition_ReM", details.getRelationshipName());
    assertEquals("position", details.getTargetRole());
    assertEquals(BindingComponentType.TABLE_LIST, details.getMainComponent().getName());
    assertEquals("Position_Selected_Ov", details.getMainComponent().getModelsSME().getSelectedItemsOverview());
    assertEquals(10, details.getMainComponent().getLinkPageSize());
  }
}
