package de.a12.studio.models.projects;

import de.a12.studio.models.documentmodel.DocumentModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectItemRoundTripTest {

  @Test
  void saveAfterLoadLeavesDocumentModelFileUnchanged(@TempDir Path tempDir) throws Exception {
    String original;
    try (InputStream in = getClass().getResourceAsStream("/documentmodel/Company_DM.json")) {
      original = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    Path modelFile = tempDir.resolve("Company_DM.json");
    Files.writeString(modelFile, original, StandardCharsets.UTF_8);

    ProjectItem item = new ProjectItem(modelFile.toFile());
    item.save();

    String resaved = Files.readString(modelFile, StandardCharsets.UTF_8);
    assertEquals(original, resaved, "Saving an unmodified, freshly loaded model must not change the file's content or formatting");
  }

  /**
   * {@code content.modelInfo.name} mirrors {@code header.id} in every real fixture (the test resource's own
   * {@code modelInfo.name} is "Company_DM", matching its filename) - renaming must keep both in sync, not just
   * {@code header.id}, or the two silently drift apart the first time a Document Model is renamed.
   */
  @Test
  void renameSyncsHeaderIdAndModelInfoName(@TempDir Path tempDir) throws Exception {
    String original;
    try (InputStream in = getClass().getResourceAsStream("/documentmodel/Company_DM.json")) {
      original = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    Path modelFile = tempDir.resolve("Company_DM.json");
    Files.writeString(modelFile, original, StandardCharsets.UTF_8);

    ProjectItem item = new ProjectItem(modelFile.toFile());
    item.renameTo("Renamed_DM.json");

    assertEquals("Renamed_DM", item.getModel().getId());
    DocumentModel documentModel = (DocumentModel) item.getModel();
    assertEquals("Renamed_DM", documentModel.getContent().getModelInfo().getName());
  }
}
