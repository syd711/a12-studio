package de.a12.studio.models.projects;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.TestHelper;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
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

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Recursively searches a root folder for every directory named {@code models} and round-trips
 * each model file inside it (load, then save), verifying each one comes back semantically
 * identical - same checks as {@link BasicProjectModelsRoundTripTest}, one dynamic container per
 * {@code models} folder.
 * <p>
 * The root defaults to {@code testing/workspaces}; override with
 * {@code -Da12.roundtrip.root=/path/to/projects}. A {@code models} folder nested inside another
 * {@code models} folder is covered by its ancestor and not tested twice.
 * <p>
 * Every mismatch is also written to a JSON report (expected vs. actual per JSON pointer, the owning
 * Java class/member and a fix hint, grouped into patterns), default
 * {@code build/reports/model-roundtrip/model-roundtrip-report.json}; override with
 * {@code -Da12.roundtrip.report=/path/to/report.json}. See {@link ModelRoundTripReport}.
 */
class ProjectsFolderModelsRoundTripTest {

  private static final String ROOT_PROPERTY = "a12.roundtrip.root";
  private static final String REPORT_PROPERTY = "a12.roundtrip.report";
  private static final String MODELS_FOLDER_NAME = "models";
  private static final Path DEFAULT_REPORT = Path.of("build", "reports", "model-roundtrip", "model-roundtrip-report.json");

  private static ModelRoundTripReport report;

  @TestFactory
  Stream<DynamicNode> saveAfterLoadLeavesEveryModelFileUnchanged(@TempDir Path tempDir) throws IOException {
    Path root = resolveRoot();
    report = new ModelRoundTripReport(root);
    List<Path> modelsFolders = findModelsFolders(root);
    if (modelsFolders.isEmpty()) {
      fail("No '" + MODELS_FOLDER_NAME + "' folders found under '" + root + "'");
    }

    List<DynamicNode> containers = new ArrayList<>();
    int index = 0;
    for (Path source : modelsFolders) {
      String folderName = root.relativize(source).toString();
      Path modelsDir = tempDir.resolve(String.valueOf(index++)).resolve(MODELS_FOLDER_NAME);
      copyDirectory(source, modelsDir);

      List<DynamicTest> tests = createRoundTripTests(folderName, source, modelsDir);
      if (tests.isEmpty()) {
        continue;
      }
      containers.add(DynamicContainer.dynamicContainer(folderName, tests.stream()));
    }

    if (containers.isEmpty()) {
      fail("No model files found in any '" + MODELS_FOLDER_NAME + "' folder under '" + root + "'");
    }
    return containers.stream();
  }

  @AfterAll
  static void writeReport() throws IOException {
    if (report == null) {
      return;
    }
    String override = System.getProperty(REPORT_PROPERTY);
    Path reportFile = override != null && !override.isBlank() ? Path.of(override) : DEFAULT_REPORT;
    Path written = report.write(reportFile);
    System.out.println("Model round-trip report: " + written.toAbsolutePath());
  }

  private static Path resolveRoot() {
    String override = System.getProperty(ROOT_PROPERTY);
    if (override != null && !override.isBlank()) {
      Path root = Path.of(override).toAbsolutePath().normalize();
      if (!Files.isDirectory(root)) {
        throw new IllegalStateException("'" + ROOT_PROPERTY + "' is not a directory: " + root);
      }
      return root;
    }
    return TestHelper.resolveTestingWorkspacesDir();
  }

  private static List<Path> findModelsFolders(Path root) throws IOException {
    List<Path> candidates;
    try (Stream<Path> walk = Files.walk(root)) {
      candidates = walk.filter(Files::isDirectory)
          .filter(path -> path.getFileName() != null && MODELS_FOLDER_NAME.equals(path.getFileName().toString()))
          .sorted()
          .toList();
    }

    List<Path> result = new ArrayList<>();
    for (Path candidate : candidates) {
      boolean nested = result.stream().anyMatch(outer -> candidate.startsWith(outer));
      if (!nested) {
        result.add(candidate);
      }
    }
    return result;
  }

  private static List<DynamicTest> createRoundTripTests(String folderName, Path sourceDir, Path modelsDir) throws IOException {
    List<Path> jsonFiles;
    try (Stream<Path> walk = Files.walk(modelsDir)) {
      jsonFiles = walk.filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".json"))
          .sorted()
          .toList();
    }

    List<DynamicTest> tests = new ArrayList<>();
    for (Path file : jsonFiles) {
      String relativeName = modelsDir.relativize(file).toString();
      Path sourceFile = sourceDir.resolve(relativeName);

      ProjectItem item;
      A12Model<?> model;
      try {
        item = new ProjectItem(file.toFile());
        model = item.getModel();
      }
      catch (RuntimeException e) {
        tests.add(DynamicTest.dynamicTest(relativeName, () -> {
          report.error(folderName, relativeName, sourceFile, null, readTreeOrNull(file), e);
          throw e;
        }));
        continue;
      }
      if (model == null) {
        continue;
      }

      tests.add(DynamicTest.dynamicTest(relativeName, () -> {
        JsonNode beforeTree = JsonSettings.objectMapper.readTree(Files.readString(file, StandardCharsets.UTF_8));
        JsonNode afterTree;
        try {
          item.save();
          afterTree = JsonSettings.objectMapper.readTree(Files.readString(file, StandardCharsets.UTF_8));
        }
        catch (RuntimeException | IOException e) {
          report.error(folderName, relativeName, sourceFile, model, beforeTree, e);
          throw e;
        }

        ModelRoundTripReport.FileResult result = report.compare(folderName, relativeName, sourceFile, item.getModel(), beforeTree, afterTree);
        if (result.failed()) {
          StringBuilder message = new StringBuilder("Saving a freshly loaded '" + relativeName + "' changed "
              + result.differences().size() + (result.truncated() ? "+" : "") + " value(s):");
          result.differences().stream().limit(10).forEach(d ->
              message.append("\n  ").append(d.kind()).append(' ').append(d.path())
                  .append(d.memberDeclaringClass() != null ? "  [" + d.member() + "]" : ""));
          fail(message.append("\nSee the model round-trip report for expected/actual values.").toString());
        }
      }));
    }
    return tests;
  }

  private static JsonNode readTreeOrNull(Path file) {
    try {
      return JsonSettings.objectMapper.readTree(Files.readString(file, StandardCharsets.UTF_8));
    }
    catch (RuntimeException | IOException e) {
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
