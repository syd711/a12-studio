package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.composeddocument.CdmQueryRootReferenceValidator;
import de.a12.studio.modelsvalidation.validators.composeddocument.CdmRelationshipStepValidator;
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
 * The document models of the sample workspaces are authored in SME, so valid by construction: none of the
 * document-specific validators may report an ERROR for them, with the rest of their workspace as context (a
 * real {@link Project#load}, matching {@link
 * de.a12.studio.modelsvalidation.validators.overview.FixtureWorkspacesOverviewValidatorsTest}'s reasoning for
 * why a flat model list is not enough for any check that needs real {@code ProjectItem} tree navigation, e.g.
 * a Composed Document Model's query root or an Include's referenced model). Guards against a rule being
 * stricter than SME's. Only ERROR severity is asserted to zero, not WARNING - see the Overview test for why.
 */
class FixtureWorkspacesDocumentValidatorsTest {

  @Test
  void realDocumentModelsHaveNoErrorsFromTheDocumentValidators() throws IOException {
    Path workspaces = locateWorkspaces();
    List<ModelValidator> validators = List.of(new MissingReferenceValidator(), new DuplicateIdValidator(),
        new NumberFieldValueLimitValidator(), new EnumerationValuesValidator(), new MultiSelectGroupValidator(),
        new AttachmentGroupValidator(), new BasicConsistencyValidator(), new StringPatternErrorMessageValidator(),
        new StringTypeConfigValidator(), new NumberTypeConfigValidator(), new EnumerationTypeConfigValidator(),
        new CustomFieldTypeConfigValidator(), new DateFormatConfigValidator(), new IncludeTypeDefinitionModeValidator(),
        new IncludeStructureValidator(), new SupportedCharactersValidator(), new RuleConditionSyntaxValidator(),
        new CdmQueryRootReferenceValidator(), new CdmRelationshipStepValidator(), new ContentUniquenessCriteriaValidator());
    List<String> problems = new ArrayList<>();
    int documentModels = 0;
    try (Stream<Path> workspaceDirs = Files.list(workspaces)) {
      for (Path workspace : workspaceDirs.filter(Files::isDirectory).sorted().toList()) {
        Project project = new Project();
        project.load(workspace.toFile());
        List<ProjectItem> allItems = new ArrayList<>();
        collect(project.getRoot(), allItems);
        List<A12Model<?>> allModels = allItems.stream().map(ProjectItem::getModel).filter(m -> m != null).toList();

        for (ProjectItem item : allItems) {
          if (!(item.getModel() instanceof DocumentModel model)) {
            continue;
          }
          documentModels++;
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
    assertTrue(documentModels > 0, "no fixture document models found under " + workspaces);
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
