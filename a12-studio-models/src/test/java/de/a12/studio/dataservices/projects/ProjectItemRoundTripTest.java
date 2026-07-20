package de.a12.studio.dataservices.projects;

import de.a12.studio.models.projects.ProjectItem;
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
}
