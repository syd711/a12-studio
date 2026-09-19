package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
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
 * The style-preset and default-row-action validators are strict reference checks; the real form models of the
 * sample workspaces (authored in SME, so valid by construction) must not trip them. Guards against the rules
 * being stricter than SME's.
 */
class FixtureWorkspacesFormValidatorsTest {

  @Test
  void realFormModelsHaveNoStyleOrDefaultRowActionErrors() throws IOException {
    Path workspaces = locateWorkspaces();
    List<ModelValidator> validators = List.of(new FormStyleReferenceValidator(), new FormDefaultRowActionValidator());
    List<String> problems = new ArrayList<>();
    int formModels = 0;
    try (Stream<Path> walk = Files.walk(workspaces)) {
      for (Path file : walk.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList()) {
        if (!(new ProjectItem(file.toFile()).getModel() instanceof FormModel model)) {
          continue;
        }
        formModels++;
        for (ModelValidator validator : validators) {
          for (ModelValidationError error : validator.validate(model, TestModels.context(model))) {
            problems.add(workspaces.relativize(file) + ": " + error.message());
          }
        }
      }
    }
    assertTrue(formModels > 0, "no fixture form models found under " + workspaces);
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
