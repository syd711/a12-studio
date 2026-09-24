package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code PersonSkills_Person_Ru_SelectedItems_Ov.json} (see {@code testing/workspaces/advanced_new}) is bound
 * only through a Query Model header reference (no {@code document-model-for-overview}), and half its columns
 * carry {@code linkReferences} projecting fields from the relationship's own link document model (itself a
 * Combination Model, {@code PersonSkills_LinkFields_Cm}) rather than the query's target Document Model. Both
 * used to make {@link OverviewElementResolution#referencedDocumentModel} return {@code null}, so {@link
 * OverviewFieldReferenceValidator} and {@link OverviewColumnHeaderLabelOrIconValidator} silently produced no
 * errors/warnings at all for every column, dangling or not.
 */
class OverviewFieldReferenceValidatorQueryModelLinkColumnsTest {

  @Test
  void resolvesPlainAndLinkedColumnsWithNoFalsePositives(@TempDir Path tempDir) throws IOException {
    Path source = locateWorkspace();
    copy(source, tempDir, "10_People/Person_Dc.json");
    copy(source, tempDir, "30_Skills/Skill_Dc.json");
    copy(source, tempDir, "30_Skills/PersonSkills_LinkFields_Cm.json");
    copy(source, tempDir, "30_Skills/PersonSkills_LinkFields_Base_Dc.json");
    copy(source, tempDir, "30_Skills/PersonSkills/PersonSkills_Re.json");
    copy(source, tempDir, "30_Skills/PersonSkills/PersonSkills_Person_Ru_SelectedItems_Qe.json");
    copy(source, tempDir, "30_Skills/PersonSkills/PersonSkills_Person_Ru_SelectedItems_Ov.json");

    ProjectItem root = new ProjectItem(tempDir.toFile());
    ProjectItem overviewItem = root.getChildren().stream()
        .filter(item -> "PersonSkills_Person_Ru_SelectedItems_Ov.json".equals(item.getName()))
        .findFirst()
        .orElseThrow();
    OverviewModel overviewModel = (OverviewModel) overviewItem.getModel();
    ValidationContext context = contextFor(root, overviewItem, overviewModel);

    List<ModelValidationError> fieldReferenceErrors = new OverviewFieldReferenceValidator().validate(overviewModel, context);
    assertEquals(List.of(), fieldReferenceErrors, "no column should be flagged as an unresolved/invalid reference");

    List<ModelValidationError> labelWarnings = new OverviewColumnHeaderLabelOrIconValidator().validate(overviewModel, context);
    // Column 0 references the "Photo" attachment group, which - unlike every other referenced field in this
    // fixture - genuinely has no label of its own, so it alone is correctly flagged.
    assertEquals(1, labelWarnings.size());
    assertTrue(labelWarnings.get(0).message().contains("Photo"), labelWarnings.get(0).message());
  }

  private static ValidationContext contextFor(ProjectItem root, ProjectItem projectItem, A12Model<?> model) {
    List<DocumentModel> otherDocumentModels = new ArrayList<>();
    List<A12Model<?>> otherModels = new ArrayList<>();
    collect(root, model, otherDocumentModels, otherModels);
    return new ValidationContext(null, projectItem, otherDocumentModels, otherModels, model);
  }

  private static void collect(ProjectItem item, A12Model<?> exclude, List<DocumentModel> otherDocumentModels, List<A12Model<?>> otherModels) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collect(child, exclude, otherDocumentModels, otherModels);
      }
      return;
    }
    A12Model<?> candidate = item.getModel();
    if (candidate == null || candidate == exclude) {
      return;
    }
    otherModels.add(candidate);
    if (candidate instanceof DocumentModel documentModel) {
      otherDocumentModels.add(documentModel);
    }
  }

  private static void copy(Path source, Path tempDir, String relativePath) throws IOException {
    Path from = source.resolve(relativePath.replace('/', java.io.File.separatorChar));
    Files.copy(from, tempDir.resolve(from.getFileName()));
  }

  private static Path locateWorkspace() throws IOException {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("advanced_new").resolve("models");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces/advanced_new/models' above " + Path.of("").toAbsolutePath());
  }
}
