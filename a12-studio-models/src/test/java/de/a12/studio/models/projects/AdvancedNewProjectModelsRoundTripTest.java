package de.a12.studio.models.projects;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.TestHelper;
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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Copies the real {@code testing/workspaces/advanced_new} sample project into a temp directory
 * and round-trips every model file found in it (load, then save), verifying each one comes back
 * semantically identical: the same JSON content, regardless of object property order (array
 * order is still compared, since element order is meaningful in these models - e.g. scenes,
 * annotations). Files without a recognized {@code header.modelType} (settings, auth exports, ...)
 * are loaded too but skipped, since {@link ProjectItem} leaves their content untouched.
 */
class AdvancedNewProjectModelsRoundTripTest {

  @TestFactory
  Stream<DynamicTest> saveAfterLoadLeavesEveryModelFileUnchanged(@TempDir Path tempDir) throws IOException {
    Path source = TestHelper.resolveTestingAdvancedNewDir();
    Path projectDir = tempDir.resolve("advanced_new");
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
      // Peek at the modelType before constructing the ProjectItem so we can emit a skipped test
      // (rather than silently omitting it) for model types that are not yet supported. When a type's
      // "enabled" flag is flipped to true in model-versions.json the test will automatically run.
      String modelTypeValue = readModelTypeValue(file);
      ModelType modelType = modelTypeValue != null ? ModelType.fromValue(modelTypeValue) : null;

      ProjectItem item = new ProjectItem(file.toFile());
      A12Model<?> model = item.getModel();

      // Files with no recognised modelType (settings, auth exports, …) have no model at all —
      // skip them silently, exactly as before.
      if (model == null && modelType == null) {
        continue;
      }

      String relativeName = projectDir.relativize(file).toString();
      tests.add(DynamicTest.dynamicTest(relativeName, () -> {
        // If the model type is known but not yet enabled, abort the test as "skipped".
        // This keeps the test visible in the report and ensures it runs automatically once
        // the type is enabled, without requiring any change here.
        if (modelType != null) {
          assumeTrue(modelType.isEnabled(),
              "Model type '" + modelTypeValue + "' is not yet supported (enabled=false in model-versions.json) — skipping");
        }

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

  /** Returns the raw {@code header.modelType} string from the JSON file, or {@code null} if absent. */
  private static String readModelTypeValue(Path file) {
    try {
      JsonNode root = JsonSettings.objectMapper.readTree(file);
      String value = root.path("header").path("modelType").asString(null);
      return value != null && !value.isBlank() ? value : null;
    }
    catch (IOException e) {
      return null;
    }
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
