package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentStructure;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.Severity;
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
 * The Content Models of the fixture workspaces were authored in SME, so they must satisfy every rule of the Content Model
 * validators - the rules were derived from SME and this is the check that they were not made stricter than SME. Two of
 * the fixtures are bound to a Document Model (with an Include) and use groups, fields and conditions inside repeatable
 * groups, so the data context rules are exercised on real data.
 */
class FixtureWorkspacesContentValidatorsTest {

  @Test
  void realContentModelsHaveNoErrorsAndFollowTheStructureRules() throws IOException {
    Path workspaces = locateWorkspaces();
    List<String> problems = new ArrayList<>();
    int contentModels = 0;
    int bound = 0;
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
          if (!(models.get(i) instanceof ContentModel model)) {
            continue;
          }
          contentModels++;
          if (model.getDocumentModelId() != null) {
            bound++;
          }
          String name = workspaces.relativize(modelFiles.get(i)).toString();
          // Called directly: the runner of the validation service swallows a validator that throws.
          for (ModelValidator validator : validators()) {
            validator.validate(model, TestModels.contextWithOtherModels(model, models.toArray(new A12Model<?>[0]))).stream()
                .filter(error -> !Severity.WARNING.name().equals(error.severity()))
                .forEach(error -> problems.add(name + " [" + error.elementId() + "]: " + error.message()));
          }
          ContentStructure.violations(model.getContent().getRoot())
              .forEach(violation -> problems.add(name + " structure: " + violation.kind() + " " + violation.element().getId()));
        }
      }
    }
    assertTrue(contentModels > 0 && bound > 0, "no fixture content models (bound ones) found under " + workspaces);
    assertEquals(List.of(), problems);
  }

  private static Path locateWorkspaces() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate testing/workspaces");
  }

  private static List<ModelValidator> validators() {
    return List.of(new ContentDocumentModelTypeValidator(), new ContentRootElementValidator(), new ContentElementIdUniqueValidator(),
        new ContentNodeShapeValidator(), new ContentStructureValidator(), new ContentBaseGroupValidator(),
        new ContentGroupReferenceValidator(), new ContentFieldReferenceValidator(), new ContentFormElementValidator(),
        new ContentEventNodeValidator(), new ContentSettingsValidator(), new ContentWarningsValidator());
  }
}
