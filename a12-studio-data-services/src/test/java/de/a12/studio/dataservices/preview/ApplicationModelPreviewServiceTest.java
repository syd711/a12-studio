package de.a12.studio.dataservices.preview;

import de.a12.studio.models.projects.ProjectItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationModelPreviewServiceTest {

  private final ApplicationModelPreviewService service = new ApplicationModelPreviewService();

  @Test
  void resolvesOverviewModelColumnsAgainstBoundDocumentModel(@TempDir Path tempDir) throws IOException {
    copyFixture(tempDir, "PreviewApp_AM.json");
    copyFixture(tempDir, "Company_OM.json");
    copyFixture(tempDir, "Company_DM.json");

    ProjectItem projectRoot = new ProjectItem(tempDir.toFile());
    ProjectItem applicationItem = projectRoot.getChildren().stream()
        .filter(item -> "PreviewApp_AM.json".equals(item.getName()))
        .findFirst()
        .orElseThrow();

    PreviewSceneDto scene = service.resolveScene(applicationItem, "CompanyModule", "CompanyModuleOverview");
    PreviewViewDto view = scene.regionTree().views().get(0);

    assertEquals("OverviewEngine", view.name());
    assertEquals("Company_OM", view.modelName());
    assertEquals("overview", view.modelType());
    assertEquals(3, view.fields().size());
    assertEquals("Logo", view.fields().get(0).label());
    assertEquals("Company Name", view.fields().get(1).label());
    assertEquals("NumberType", view.fields().get(1).fieldType());
    assertEquals("Company ID", view.fields().get(2).label());
    assertEquals("StringType", view.fields().get(2).fieldType());
  }

  private void copyFixture(Path tempDir, String name) throws IOException {
    try (InputStream in = getClass().getResourceAsStream("/preview/" + name)) {
      Files.write(tempDir.resolve(name), in.readAllBytes());
    }
  }
}
