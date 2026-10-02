package de.a12.studio.modelsvalidation.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;

class ProjectKernelModelsTest {

  private static ProjectItem workspace(String name) {
    Path dir = Path.of("").toAbsolutePath();
    while (dir != null && !Files.isDirectory(dir.resolve("testing/workspaces").resolve(name))) {
      dir = dir.getParent();
    }
    if (dir == null) {
      throw new IllegalStateException("testing/workspaces/" + name + " not found");
    }
    return new ProjectItem(dir.resolve("testing/workspaces").resolve(name).toFile());
  }

  @Test
  void aCombinationModelExpandsToADocumentModelWithTheBaseAndAdditiveElements() {
    ProjectItem project = workspace("advanced_new");

    DocumentModel merged = ProjectKernelModels.expand(project, "PersonEmployee_Cm").orElseThrow();

    assertTrue(merged.getContent().getModelRoot().getRootGroups().size() > 0);
    assertEquals("PersonEmployee_Cm", merged.getId());
  }

  @Test
  void aDocumentModelWithIncludesIsFlattened() {
    ProjectItem project = workspace("basic");

    DocumentModel flat = ProjectKernelModels.expand(project, "Invoice_DM").orElseThrow();

    assertTrue(flat.getContent().getModelRoot().getRootGroups().size() > 0);
  }

  @Test
  void anUnknownModelYieldsEmptyInsteadOfThrowing() {
    assertTrue(ProjectKernelModels.expand(workspace("basic"), "Nope_DM").isEmpty());
    assertTrue(ProjectKernelModels.expand(workspace("basic"), null).isEmpty());
  }
}
