package de.a12.studio.models.projects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectResourcesTest {

  @TempDir
  Path project;

  @TempDir
  Path elsewhere;

  private File write(Path root, String relativePath) throws Exception {
    Path file = root.resolve(relativePath);
    Files.createDirectories(file.getParent());
    Files.writeString(file, "<xs:schema/>");
    return file.toFile();
  }

  @Test
  void findsTheFilesOfAnExtensionBelowTheProjectSortedByName() throws Exception {
    write(project, "resources/Zeta.xsd");
    write(project, "schemas/nested/alpha.XSD");
    write(project, "models/Person_DM.json");
    write(project, "Beta.xsd");

    List<String> names = ProjectResources.findFiles(project.toFile(), "xsd").stream().map(File::getName).toList();

    assertEquals(List.of("alpha.XSD", "Beta.xsd", "Zeta.xsd"), names, "any casing, any depth, sorted by name, no JSON model");
    assertEquals(ProjectResources.findFiles(project.toFile(), ".xsd"), ProjectResources.findFiles(project.toFile(), "xsd"),
        "the extension may come with its dot");
  }

  @Test
  void skipsHiddenAndBuildFolders() throws Exception {
    write(project, ".studio/Hidden.xsd");
    write(project, "build/Generated.xsd");
    write(project, "node_modules/Dependency.xsd");
    write(project, "resources/Kept.xsd");

    assertEquals(List.of("Kept.xsd"), ProjectResources.findFiles(project.toFile(), "xsd").stream().map(File::getName).toList());
  }

  @Test
  void aMissingFolderHasNoFiles() {
    assertTrue(ProjectResources.findFiles(null, "xsd").isEmpty());
    assertTrue(ProjectResources.findFiles(project.resolve("nope").toFile(), "xsd").isEmpty());
  }

  @Test
  void findByNameIsCaseSensitiveAndNeedsTheExtension() throws Exception {
    List<File> files = List.of(write(project, "Persons.xsd"));

    assertEquals("Persons.xsd", ProjectResources.findByName(files, "Persons.xsd").getName());
    assertNull(ProjectResources.findByName(files, "persons.xsd"));
    assertNull(ProjectResources.findByName(files, "Persons"));
    assertNull(ProjectResources.findByName(files, null));
    assertNull(ProjectResources.findByName(files, " "));
  }

  @Test
  void aNewResourceGoesIntoTheResourcesFolderWhenTheProjectHasNone() throws Exception {
    File source = write(elsewhere, "Persons.xsd");

    File added = ProjectResources.addResource(project.toFile(), source, "xsd");

    assertEquals(project.resolve("resources").resolve("Persons.xsd").toFile(), added);
    assertTrue(added.isFile());
    assertTrue(source.isFile(), "the original is copied, not moved");
  }

  @Test
  void aNewResourceGoesNextToTheExistingOnes() throws Exception {
    write(project, "schemas/Existing.xsd");
    File source = write(elsewhere, "Included.xsd");

    File added = ProjectResources.addResource(project.toFile(), source, "xsd");

    assertEquals(project.resolve("schemas").resolve("Included.xsd").toFile(), added);
  }

  @Test
  void aResourceOfThatNameIsNotOverwrittenOrCopiedAgain() throws Exception {
    File existing = write(project, "schemas/Persons.xsd");
    Files.writeString(existing.toPath(), "the project's own version");
    File source = write(elsewhere, "Persons.xsd");

    File added = ProjectResources.addResource(project.toFile(), source, "xsd");

    assertEquals(existing, added);
    assertEquals("the project's own version", Files.readString(existing.toPath()));
    assertEquals(1, ProjectResources.findFiles(project.toFile(), "xsd").size());
  }
}
