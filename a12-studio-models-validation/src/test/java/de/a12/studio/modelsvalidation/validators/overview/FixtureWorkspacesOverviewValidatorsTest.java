package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
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
 * The overview models of the sample workspaces are authored in SME, so valid by construction: none of the
 * overview-specific validators may report an ERROR for them, with the rest of their workspace as context (a
 * real {@link Project#load}, not a flat model list, so a link column's Combination Model link document
 * resolves through {@code CombinedDocumentModelElements.resolveForFieldReferences}'s real
 * {@code ProjectItem.findByModelId} tree walk exactly as it would in the running app). Guards against a rule
 * being stricter than SME's.
 * <p>
 * Only ERROR-severity findings are asserted to zero: several real fixtures carry
 * {@code OverviewColumnHeaderLabelOrIconValidator} WARNINGs (an attachment-Group-referencing column, e.g.
 * "Photo"/"Flag"/"Picture", with neither an icon nor a label) that are a faithful port of SME's own rule -
 * checked directly against {@code elementRefHasNoLabel.ts}'s {@code getDefaultLabel}, which returns
 * {@code undefined} (⇒ "has no label" ⇒ warns) for anything that is not a Field or MultiSelect, Groups
 * included - so real SME-authored data can and does carry this same non-blocking warning unaddressed.
 * <p>
 * Re-checked 2026-09-29 against "Overview Model: gap review"'s "Re-checked with OverviewBindingPurpose in
 * hand" note (docs/sme-reference-comparison.md): neither of its two claimed false-positive sources reproduces
 * against real fixtures with a properly project-tree-connected context - the "link-column field resolution"
 * bug claim was an artifact of testing with a flat model list (no working {@code ProjectItem.findByModelId}
 * for the Combination Model link document to resolve through, exactly the mistake this test's real
 * {@code Project.load} avoids), and the `bindingConfiguration` wire shape gap, while real, causes no actual
 * validator misbehavior on the fixtures it was found on - see the doc for the correction.
 */
class FixtureWorkspacesOverviewValidatorsTest {

  @Test
  void realOverviewModelsHaveNoErrorsFromTheOverviewValidators() throws IOException {
    Path workspaces = locateWorkspaces();
    List<ModelValidator> validators = List.of(new OverviewColumnsNotEmptyValidator(), new OverviewFieldReferenceValidator(),
        new OverviewSortableMultiSelectValidator(), new OverviewColumnHeaderLabelOrIconValidator(),
        new OverviewDocumentModelRequiredValidator(), new OverviewFilterModeRequiredValidator(),
        new OverviewFilterCustomFieldsValidator(), new OverviewEnumeratedStringFilterValidator(),
        new OverviewFilterModeIndexedAnnotationValidator(), new OverviewFilterSectionsValidator(),
        new OverviewFilterGroupsValidator(), new OverviewFilterDefinitionSyntaxValidator(),
        new OverviewMultiSelectionElementValidator(), new OverviewSearchElementValidator(),
        new OverviewPagingSizeValidator(), new OverviewInfiniteScrollingValidator(),
        new OverviewInitialSortingReferenceValidator(), new OverviewStylesValidator(),
        new OverviewContextMenuValidator(), new OverviewFooterExportExcelValidator(),
        new OverviewColumnValidator(), new OverviewExpressionColumnValidator(), new OverviewSubHeaderElementValidator());
    List<String> problems = new ArrayList<>();
    int overviewModels = 0;
    try (Stream<Path> workspaceDirs = Files.list(workspaces)) {
      for (Path workspace : workspaceDirs.filter(Files::isDirectory).sorted().toList()) {
        Project project = new Project();
        project.load(workspace.toFile());
        List<ProjectItem> allItems = new ArrayList<>();
        collect(project.getRoot(), allItems);
        List<A12Model<?>> allModels = allItems.stream().map(ProjectItem::getModel).filter(m -> m != null).toList();

        for (ProjectItem item : allItems) {
          if (!(item.getModel() instanceof OverviewModel model)) {
            continue;
          }
          overviewModels++;
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
    assertTrue(overviewModels > 0, "no fixture overview models found under " + workspaces);
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
