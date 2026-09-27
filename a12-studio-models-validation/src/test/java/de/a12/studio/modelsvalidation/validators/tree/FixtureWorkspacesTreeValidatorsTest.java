package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeModel;
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
 * The tree models of the sample workspaces are authored in SME, so valid by construction: none of the tree validators may
 * report anything for them, with the sibling models of their workspace as context. Guards against a rule being stricter
 * than SME's.
 */
class FixtureWorkspacesTreeValidatorsTest {

  @Test
  void realTreeModelsHaveNoFindingsFromTheTreeValidators() throws IOException {
    Path workspaces = locateWorkspaces();
    List<ModelValidator> validators = List.of(new TreeNodesNotEmptyValidator(), new TreeColumnsNotEmptyValidator(),
        new TreeUniqueNodeValidator(), new TreeDocumentModelReferenceValidator(), new TreeColumnFieldValidator(),
        new TreeHierarchicalColumnRefValidator(), new TreeExpansionStrategyValidator(), new TreeWholeTreeExpansionValidator(),
        new TreeRootRefValidator(), new TreeVirtualScrollingValidator(), new TreeMultiSelectionElementValidator(),
        new TreeStylesValidator(), new TreeColumnValidator(), new TreeNodeStructureValidator(), new TreeChildRelationshipValidator(),
        new TreeActionsValidator(), new TreeVirtualRootValidator());
    List<String> problems = new ArrayList<>();
    int treeModels = 0;
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
          if (!(models.get(i) instanceof TreeModel model)) {
            continue;
          }
          treeModels++;
          for (ModelValidator validator : validators) {
            for (ModelValidationError error : validator.validate(model, TestModels.contextWithOtherModels(model, models.toArray(new A12Model<?>[0])))) {
              problems.add(workspaces.relativize(modelFiles.get(i)) + " [" + validator.getClass().getSimpleName() + " "
                  + error.severity() + "]: " + error.message());
            }
          }
        }
      }
    }
    assertTrue(treeModels > 0, "no fixture tree models found under " + workspaces);
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
