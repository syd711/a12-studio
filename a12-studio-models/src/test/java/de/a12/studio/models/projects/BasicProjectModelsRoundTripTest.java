package de.a12.studio.models.projects;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Copies the real {@code testing/basic} sample project into a temp directory and round-trips
 * every model file found in it (load, then save), verifying each one comes back semantically
 * identical: the same JSON content, regardless of object property order (array order is still
 * compared, since element order is meaningful in these models - e.g. scenes, annotations).
 * Files without a recognized {@code header.modelType} (settings, auth exports, ...) are loaded
 * too but skipped, since {@link ProjectItem} leaves their content untouched.
 */
class BasicProjectModelsRoundTripTest {

  @TestFactory
  Stream<DynamicTest> saveAfterLoadLeavesEveryModelFileUnchanged(@TempDir Path tempDir) throws IOException {
    Path source = resolveTestingBasicDir();
    Path projectDir = tempDir.resolve("basic");
    copyDirectory(source, projectDir);

    List<Path> jsonFiles;
    try (Stream<Path> walk = Files.walk(projectDir)) {
      jsonFiles = walk.filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".json"))
          .sorted()
          .toList();
    }

    List<DynamicTest> tests = new ArrayList<>();
    for (Path file : jsonFiles) {
      ProjectItem item = new ProjectItem(file.toFile());
      A12Model<?> model = item.getModel();
      if (model == null) {
        continue;
      }

      String relativeName = projectDir.relativize(file).toString();
      tests.add(DynamicTest.dynamicTest(relativeName, () -> {
        String before = Files.readString(file, StandardCharsets.UTF_8);
        JsonNode beforeTree = JsonSettings.objectMapper.readTree(before);
        item.save();
        String after = Files.readString(file, StandardCharsets.UTF_8);
        JsonNode afterTree = JsonSettings.objectMapper.readTree(after);
        assertEquals(beforeTree, afterTree, "Saving a freshly loaded '" + relativeName + "' must not change its content");
      }));
    }

    if (tests.isEmpty()) {
      fail("No model files found under '" + source + "' - test fixture may have moved");
    }
    return tests.stream();
  }

  private static Path resolveTestingBasicDir() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("basic");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/basic' above " + Path.of("").toAbsolutePath());
  }

  private static void copyDirectory(Path source, Path target) throws IOException {
    try (Stream<Path> walk = Files.walk(source)) {
      for (Path path : (Iterable<Path>) walk::iterator) {
        Path destination = target.resolve(source.relativize(path));
        if (Files.isDirectory(path)) {
          Files.createDirectories(destination);
        }
        else {
          Files.createDirectories(destination.getParent());
          Files.copy(path, destination, StandardCopyOption.COPY_ATTRIBUTES);
        }
      }
    }
  }
}
