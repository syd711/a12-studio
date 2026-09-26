package de.a12.studio.models.contentmodel;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A new Content Model must be what SME creates: the current Content Engine version in {@code namespaceVersions} (a
 * model without it counts as needing migration and makes the engine warn about a version mismatch) and nodes that
 * satisfy SME's JSON schema, which requires {@code id}, {@code namespace}, {@code props} and {@code type} on every node.
 */
class NewContentModelTest {

  @Test
  void aNewContentModelIsSeededLikeSmeDoesIt(@TempDir Path dir) throws Exception {
    ProjectItem folder = new ProjectItem(dir.toFile());
    folder.setRoot(true);

    ProjectItem item = NewModelFactory.createModel(folder, ModelType.CONTENT, "Fresh_CM");

    JsonNode json = JsonSettings.objectMapper.readTree(Files.readString(dir.resolve("Fresh_CM.json")));
    assertEquals("Fresh_CM", json.at("/header/id").asString());
    assertEquals("content", json.at("/header/modelType").asString());
    assertEquals("0.9.0", json.at("/content/configuration/namespaceVersions/com.mgmtp.a12.contentengine").asString());
    assertFalse(json.at("/content/configuration").has("baseGroupId"));

    JsonNode root = json.at("/content/root");
    assertEquals("Box", root.get("type").asString());
    assertEquals("com.mgmtp.a12.contentengine", root.get("namespace").asString());
    assertTrue(root.get("id").asString().startsWith("Box-"), "ids have SME's Type-uid form");
    assertTrue(root.has("props"), "the schema requires props on every node");
    assertEquals("flex", root.at("/props/style/display").asString());
    assertTrue(root.get("children").isArray());

    ContentModel reloaded = assertInstanceOf(ContentModel.class, new ProjectItem(item.getFile()).getModel());
    assertNull(reloaded.getDocumentModelId());
    assertEquals(ContentElementLibrary.NAMESPACE_VERSION,
        reloaded.getContent().getConfiguration().getNamespaceVersions().get(ContentElementLibrary.NAMESPACE));
  }
}
