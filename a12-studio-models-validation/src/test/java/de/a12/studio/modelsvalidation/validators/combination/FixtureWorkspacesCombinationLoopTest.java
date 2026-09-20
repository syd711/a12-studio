package de.a12.studio.modelsvalidation.validators.combination;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The loop checks are strict; the real Combined Document Models of the sample workspaces (authored in SME, so
 * loop-free by construction) must not trip them - e.g. because an Additive Model's header references, which are
 * all followed, happen to point back at its own combination.
 */
class FixtureWorkspacesCombinationLoopTest {

  @Test
  void realCombinedDocumentModelsHaveNoReferenceLoop() throws IOException {
    Path workspaces = locateWorkspaces();
    List<String> problems = new ArrayList<>();
    int combinations = 0;
    try (Stream<Path> workspaceDirs = Files.list(workspaces)) {
      for (Path workspace : workspaceDirs.filter(Files::isDirectory).sorted().toList()) {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(workspace)) {
          files = walk.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList();
        }
        List<Path> modelFiles = new ArrayList<>();
        List<A12Model<?>> models = new ArrayList<>();
        for (Path file : files) {
          A12Model<?> loaded = new ProjectItem(file.toFile()).getModel();
          if (loaded != null) {
            modelFiles.add(file);
            models.add(loaded);
          }
        }
        for (int i = 0; i < models.size(); i++) {
          if (!(models.get(i) instanceof CombinedDocumentModel combination)) {
            continue;
          }
          combinations++;
          var context = TestModels.contextWithOtherModels(combination, models.toArray(new A12Model<?>[0]));
          List<ModelValidationError> errors = new ArrayList<>();
          errors.addAll(new CombinationBaseModelLoopValidator().validate(combination, context));
          errors.addAll(new CombinationAdditiveModelLoopValidator().validate(combination, context));
          for (ModelValidationError error : errors) {
            problems.add(workspaces.relativize(modelFiles.get(i)) + ": " + error.message());
          }
        }
      }
    }
    assertTrue(combinations > 0, "no fixture combined document models found under " + workspaces);
    assertEquals(List.of(), problems);
  }

  private static Path locateWorkspaces() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces' above " + Path.of("").toAbsolutePath());
  }
}
