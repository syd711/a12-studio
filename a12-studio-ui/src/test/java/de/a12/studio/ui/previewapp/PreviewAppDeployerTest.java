package de.a12.studio.ui.previewapp;

import de.a12.studio.models.projects.ProjectItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reproduces the empty_appgroups tutorial workspace's exact gap: an Application Model whose header
 * references a Master-Detail Module Model, but whose {@code content.modules} was never hand-extended with
 * a matching entry (because a12-studio, unlike SME, has no editor feature for that). Verifies {@link
 * PreviewAppDeployer#buildModelsZip} closes that gap by expanding the reference into a generated module in
 * the zipped Application Model, while every other model file is uploaded byte-for-byte unchanged.
 */
class PreviewAppDeployerTest {

  private static final String PREVIEW_APP_AM = """
      {
        "header": {
          "id": "PreviewApp_AM",
          "modelType": "application",
          "modelVersion": "6.0.0",
          "locales": [ { "code": "en" } ],
          "labels": [ { "locale": "en", "text": "New Workspace" } ],
          "annotations": [],
          "modelReferences": [
            { "modelType": "module-masterdetail", "reference": "Product_MDM" }
          ]
        },
        "content": {
          "modules": [ { "name": "WelcomeModule" } ],
          "region": { "name": "APP", "layout": { "name": "ApplicationFrame" } },
          "defaultRegion": [ "CONTENT" ]
        }
      }
      """;

  private static final String PRODUCT_MDM = """
      {
        "header": {
          "id": "Product_MDM",
          "modelType": "module-masterdetail",
          "modelVersion": "1.0.0",
          "locales": [ { "code": "en" } ],
          "labels": [ { "locale": "en", "text": "My Shopping List" } ],
          "annotations": [],
          "modelReferences": []
        },
        "content": {
          "type": "overview",
          "overviewModel": "Product_OM",
          "formMapping": [ { "documentModel": "Product_DM", "formModel": "Product_FM" } ]
        }
      }
      """;

  private static final String PRODUCT_OM = """
      {
        "header": {
          "id": "Product_OM",
          "modelType": "overview",
          "modelVersion": "39.0.0",
          "locales": [ { "code": "en" } ],
          "labels": [],
          "annotations": [],
          "modelReferences": []
        },
        "content": { "columns": [] }
      }
      """;

  @Test
  void expandsMasterDetailReferenceIntoGeneratedModuleWhileLeavingOtherFilesUntouched(@TempDir File dir) throws Exception {
    File amFile = writeModel(dir, "PreviewApp_AM.json", PREVIEW_APP_AM);
    File mdmFile = writeModel(dir, "Product_MDM.json", PRODUCT_MDM);
    File omFile = writeModel(dir, "Product_OM.json", PRODUCT_OM);

    List<ProjectItem> modelItems = List.of(new ProjectItem(amFile), new ProjectItem(mdmFile), new ProjectItem(omFile));

    byte[] zip = PreviewAppDeployer.buildModelsZip(modelItems);
    Map<String, byte[]> entries = readZipEntries(zip);

    // Untouched files are re-uploaded byte-for-byte, not reformatted.
    assertEquals(PRODUCT_MDM, new String(entries.get("Product_MDM.json"), StandardCharsets.UTF_8));
    assertEquals(PRODUCT_OM, new String(entries.get("Product_OM.json"), StandardCharsets.UTF_8));

    JsonNode deployedAm = JsonMapper.shared().readTree(entries.get("PreviewApp_AM.json"));
    JsonNode modules = deployedAm.get("content").get("modules");
    assertEquals(2, modules.size(), "the hand-authored WelcomeModule must be kept, and a module generated for the reference appended");
    assertEquals("WelcomeModule", modules.get(0).get("name").asString());
    assertEquals("Product_MDMModule", modules.get(1).get("name").asString());
    assertEquals("My Shopping List", modules.get(1).get("menu").get("label").get(0).get("text").asString());

    // The source file on disk (and thus any open editor tab backed by the same ProjectItem) must never be
    // mutated by building the deploy zip.
    assertFalse(Files.readString(amFile.toPath()).contains("Product_MDMModule"),
        "generating the deploy content must not write back into the on-disk Application Model");
  }

  @Test
  void throwsWhenReferencedMasterDetailModelIsMissingFromTheDeploySet(@TempDir File dir) throws Exception {
    File amFile = writeModel(dir, "PreviewApp_AM.json", PREVIEW_APP_AM);
    List<ProjectItem> modelItems = List.of(new ProjectItem(amFile));

    assertTrue(assertThrowsIOException(() -> PreviewAppDeployer.buildModelsZip(modelItems))
        .getMessage().contains("Product_MDM"));
  }

  private interface ThrowingRunnable {
    void run() throws Exception;
  }

  private static java.io.IOException assertThrowsIOException(ThrowingRunnable runnable) {
    try {
      runnable.run();
    }
    catch (java.io.IOException e) {
      return e;
    }
    catch (Exception e) {
      throw new AssertionError("Expected an IOException, got " + e, e);
    }
    throw new AssertionError("Expected an IOException, but nothing was thrown");
  }

  private static File writeModel(File dir, String fileName, String json) throws Exception {
    File file = new File(dir, fileName);
    Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
    return file;
  }

  private static Map<String, byte[]> readZipEntries(byte[] zip) throws Exception {
    Map<String, byte[]> entries = new HashMap<>();
    try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(zip))) {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        entries.put(entry.getName(), zipIn.readAllBytes());
      }
    }
    return entries;
  }
}
