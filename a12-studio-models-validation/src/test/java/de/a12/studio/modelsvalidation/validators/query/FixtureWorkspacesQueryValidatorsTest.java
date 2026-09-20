package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.querymodel.QueryModel;
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
 * The target-resolves rule is a strict check; the real Query Models of the sample workspaces (authored in SME, so
 * valid by construction) must not trip it. Guards against it being stricter than SME's - e.g. by not knowing a
 * kind of model a query can legitimately target.
 */
class FixtureWorkspacesQueryValidatorsTest {

  @Test
  void everyRealQueryModelsTargetResolvesWithinItsWorkspace() throws IOException {
    Path workspaces = locateWorkspaces();
    List<String> problems = new ArrayList<>();
    int queries = 0;
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
          if (!(models.get(i) instanceof QueryModel query)) {
            continue;
          }
          queries++;
          for (ModelValidationError error : new QueryTargetDocumentModelRequiredValidator()
              .validate(query, TestModels.contextWithOtherModels(query, models.toArray(new A12Model<?>[0])))) {
            problems.add(workspaces.relativize(modelFiles.get(i)) + ": " + error.message());
          }
        }
      }
    }
    assertTrue(queries > 0, "no fixture query models found under " + workspaces);
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
