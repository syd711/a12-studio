package de.a12.studio.models.contentmodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.ModelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentModelLoadTest {

  @Test
  void loadsWelcomePageContentModel() throws Exception {
    ContentModel model = ModelRoundTrip.load(getClass(), "/contentmodel/WelcomePage_CM.json", ContentModel.class);

    assertEquals("WelcomePage_CM", model.getId());
    assertEquals(ModelType.CONTENT, model.getModelType());
    assertEquals("0.8.0", model.getModelVersion());

    ContentModelContent content = model.getContent();
    assertNotNull(content);
    assertEquals("0.9.0", content.getConfiguration().getNamespaceVersions().get("com.mgmtp.a12.contentengine"));

    ContentElement root = content.getRoot();
    assertEquals("7083ee19", root.getId());
    assertEquals("Box", root.getType());
    assertEquals("com.mgmtp.a12.contentengine", root.getNamespace());
    assertEquals("flex", ((java.util.Map<?, ?>) root.getProps().get("style")).get("display"));
    assertEquals(1, root.getChildren().size());

    // The Lexical payload of a paragraph must be captured verbatim inside the opaque props map.
    ContentElement grid = root.getChildren().get(0).getChildren().get(0);
    assertEquals("Grid", grid.getType());
    ContentElement paragraph = grid.getChildren().get(0).getChildren().get(1).getChildren().get(0);
    assertEquals("Paragraph", paragraph.getType());
    assertTrue(paragraph.getProps().containsKey("tree"));
    assertTrue(paragraph.getProps().containsKey("html"));

    // Elements without a "children" key must keep it absent (null), not gain an empty list.
    ContentElement emptyColumn = grid.getChildren().get(0).getChildren().get(0);
    assertEquals("GridColumn", emptyColumn.getType());
    assertNull(emptyColumn.getChildren());
  }

  @Test
  void roundTripsWelcomePage() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/contentmodel/WelcomePage_CM.json", ContentModel.class);
  }
}
