package de.a12.studio.modelsvalidation.validators.print;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
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
 * The print models of the sample workspaces are authored in SME, so valid by construction: none of the
 * print-specific validators may report an ERROR for them, with the rest of their workspace as context (a real
 * {@link Project#load}, matching {@link
 * de.a12.studio.modelsvalidation.validators.overview.FixtureWorkspacesOverviewValidatorsTest}'s reasoning).
 * Guards against a rule being stricter than SME's. Only ERROR severity is asserted to zero, not WARNING.
 */
class FixtureWorkspacesPrintValidatorsTest {

  @Test
  void realPrintModelsHaveNoErrorsFromThePrintValidators() throws IOException {
    Path workspaces = locateWorkspaces();
    List<ModelValidator> validators = List.of(new PrintDocumentModelReferenceValidator(), new PrintFieldReferenceValidator(),
        new PrintElementReferenceIntegrityValidator(), new PrintCalculationValidator(), new PrintTableColumnWidthValidator(),
        new PrintImageValidator(), new PrintHeadlineOrderValidator());
    List<String> problems = new ArrayList<>();
    int printModels = 0;
    try (Stream<Path> workspaceDirs = Files.list(workspaces)) {
      for (Path workspace : workspaceDirs.filter(Files::isDirectory).sorted().toList()) {
        Project project = new Project();
        project.load(workspace.toFile());
        List<ProjectItem> allItems = new ArrayList<>();
        collect(project.getRoot(), allItems);
        List<A12Model<?>> allModels = allItems.stream().map(ProjectItem::getModel).filter(m -> m != null).toList();

        for (ProjectItem item : allItems) {
          if (!(item.getModel() instanceof PrintModel model)) {
            continue;
          }
          printModels++;
          List<DocumentModel> otherDocumentModels = new ArrayList<>();
          List<A12Model<?>> otherModels = new ArrayList<>();
          for (A12Model<?> other : allModels) {
            if (other == model) {
              continue;
            }
            otherModels.add(other);
            if (other instanceof DocumentModel documentModel) {
              otherDocumentModels.add(documentModel);
            }
          }
          ValidationContext context = new ValidationContext(project, item, otherDocumentModels, otherModels, model);
          for (ModelValidator validator : validators) {
            for (ModelValidationError error : validator.validate(model, context)) {
              if (!"ERROR".equals(error.severity())) {
                continue;
              }
              problems.add(workspace.getFileName() + "/" + item.getFile().getName() + " [" + validator.getClass().getSimpleName()
                  + " " + error.severity() + "]: " + error.message());
            }
          }
        }
      }
    }
    assertTrue(printModels > 0, "no fixture print models found under " + workspaces);
    assertEquals(List.of(), problems);
  }

  private static void collect(ProjectItem item, List<ProjectItem> out) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collect(child, out);
      }
    }
    else {
      out.add(item);
    }
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
