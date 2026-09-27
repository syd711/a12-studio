package de.a12.studio.models.overviewmodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A new Overview Model must be seeded like SME's {@code OverviewModelModule.initializeNewOverviewModel} does it
 * (checked directly against the SME source): a Multi-Selection element on the left of the Subheader, Search and
 * Filter on the right, and an empty row action group - none of it tied yet to the Multi-Selection/Search/Filter
 * feature actually being switched on, matching SME. Also pins gap 3 of the "Overview Model: gap review" (the
 * three fields must never round-trip as an explicit JSON {@code null}).
 */
class NewOverviewModelTest {

  @Test
  void aNewOverviewModelIsSeededLikeSmeDoesIt(@TempDir Path dir) throws Exception {
    ProjectItem folder = new ProjectItem(dir.toFile());
    folder.setRoot(true);

    ProjectItem item = NewModelFactory.createModel(folder, ModelType.OVERVIEW, "Fresh_OM");

    JsonNode json = JsonSettings.objectMapper.readTree(Files.readString(dir.resolve("Fresh_OM.json")));
    assertEquals("Fresh_OM", json.at("/header/id").asString());
    assertEquals("overview", json.at("/header/modelType").asString());

    // Never an explicit null (gap 3).
    assertFalse(json.at("/content").has("footerBox"));
    assertFalse(json.at("/content/configuration").has("enableFilter"));
    assertFalse(json.at("/content/configuration").has("showFullTextSearch"));

    JsonNode subHeaderBox = json.at("/content/subHeaderBox");
    assertTrue(subHeaderBox.at("/leftSlot/0").isObject());
    assertEquals("multi_selection", subHeaderBox.at("/leftSlot/0/type").asString());
    assertEquals("search", subHeaderBox.at("/rightSlot/0/type").asString());
    assertEquals("filter", subHeaderBox.at("/rightSlot/1/type").asString());
    // No confirmation/priority - see the class javadoc.
    assertFalse(subHeaderBox.at("/leftSlot/0").has("confirmation"));
    assertFalse(subHeaderBox.at("/leftSlot/0").has("priority"));

    assertTrue(json.at("/content/rowActionGroup").isObject());
    assertFalse(json.at("/content/rowActionGroup").has("actions"));

    OverviewModel reloaded = assertInstanceOf(OverviewModel.class, new ProjectItem(item.getFile()).getModel());
    assertEquals(1, reloaded.getContent().getSubHeaderBox().getLeftSlot().size());
    assertEquals(2, reloaded.getContent().getSubHeaderBox().getRightSlot().size());
    assertTrue(reloaded.getContent().getRowActionGroup().getActions().isEmpty());
  }
}
