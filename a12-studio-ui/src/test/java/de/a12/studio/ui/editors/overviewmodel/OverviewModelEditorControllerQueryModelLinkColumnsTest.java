package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@code PersonSkills_Person_Ru_SelectedItems_Ov.json} (a Relationship UI Model's Selected Items overview,
 * see {@code testing/workspaces/advanced_new}) is bound only through a Query Model header reference
 * (no {@code document-model-for-overview}), and half its columns carry {@code linkReferences} projecting
 * fields from the relationship's own link document model rather than the query's target Document Model.
 * Both used to leave every column's "Field" summary showing the raw {@code elementRef} id and flagged as an
 * unresolved reference; {@link OverviewModelEditorController#refreshDocumentModelIndex} now falls back to the
 * Query Model's target Document Model, and resolves a linked column against the relationship's link document
 * model (see {@link OverviewColumnOptions#indexFor}).
 */
class OverviewModelEditorControllerQueryModelLinkColumnsTest {

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void restoreEmptyProject() throws Exception {
    if (toolkitAvailable) {
      setCurrentProject(new Project());
      Field validationService = Studio.class.getDeclaredField("validationService");
      validationService.setAccessible(true);
      validationService.set(null, null);
    }
  }

  @Test
  void resolvesPlainAndLinkedColumnsAgainstTheQueryModelAndTheRelationshipsLinkDocumentModel() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    Path source = locateWorkspace();
    Path models = Files.createDirectories(workspace.resolve("models"));
    copy(source, models, "10_People/Person_Dc.json");
    copy(source, models, "30_Skills/Skill_Dc.json");
    copy(source, models, "30_Skills/PersonSkills_LinkFields_Cm.json");
    copy(source, models, "30_Skills/PersonSkills_LinkFields_Base_Dc.json");
    copy(source, models, "30_Skills/PersonSkills/PersonSkills_Re.json");
    copy(source, models, "30_Skills/PersonSkills/PersonSkills_Person_Ru_SelectedItems_Qe.json");
    copy(source, models, "30_Skills/PersonSkills/PersonSkills_Person_Ru_SelectedItems_Ov.json");

    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);

    Path ovPath = models.resolve("PersonSkills_Person_Ru_SelectedItems_Ov.json");
    ProjectItem item = project.getRoot().findByPath(ovPath.toString());

    FxTestSupport.Loaded<OverviewModelEditorController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/overviewmodel/overview-model-editor.fxml");
    FxTestSupport.onFx(() -> loaded.controller().load(item));

    OverviewColumnsPanelController columnsController = FxTestSupport.field(loaded.controller(), "overviewColumnsController");
    VBox columnRows = FxTestSupport.field(columnsController, "columnRows");

    // Order matches content.columns in PersonSkills_Person_Ru_SelectedItems_Ov.json: the first four resolve
    // against the Query Model's target Document Model (Person_Dc), the last four - each carrying a
    // linkReferences entry for the PersonSkills_Re relationship - against its link document model
    // (PersonSkills_LinkFields_Cm, whose base is PersonSkills_LinkFields_Base_Dc).
    assertColumnResolvesTo(columnRows, 0, "/Person/Photo");
    assertColumnResolvesTo(columnRows, 1, "/Person/Type");
    assertColumnResolvesTo(columnRows, 2, "/Person/FirstName");
    assertColumnResolvesTo(columnRows, 3, "/Person/LastName");
    // Linked columns are prefixed with the id of the Document Model their path lives in.
    assertColumnResolvesTo(columnRows, 4, "PersonSkills_LinkFields_Cm:/Status/Proficiency");
    assertColumnResolvesTo(columnRows, 5, "PersonSkills_LinkFields_Cm:/Status/Expierience");
    assertColumnResolvesTo(columnRows, 6, "PersonSkills_LinkFields_Cm:/HR/Acknowledged");
    assertColumnResolvesTo(columnRows, 7, "PersonSkills_LinkFields_Cm:/Status/AcquiredAt");
  }

  /**
   * {@code PersonTeamAssignment_Ru_SelectedItems_Ov.json} mixes both link reference types: its first two
   * columns are {@code CHILD} references into the {@code Team} role's own Document Model ({@code Team_Dc},
   * via {@code TeamPerson_Re}'s {@code entityCharacteristics}), the last two {@code LINK} references into the
   * relationship's link document model ({@code TeamPerson_LinkFields_Dc}). Both kinds used to be resolved
   * against the link document model only, leaving the {@code CHILD} columns unresolved.
   */
  @Test
  void resolvesChildLinkColumnsAgainstTheTargetRolesDocumentModel() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    Path source = locateWorkspace();
    Path models = Files.createDirectories(workspace.resolve("models"));
    copy(source, models, "10_People/Person_Dc.json");
    copy(source, models, "20_Teams/Team_Dc.json");
    copy(source, models, "20_Teams/TeamPerson_LinkFields_Dc.json");
    copy(source, models, "20_Teams/TeamPerson_Re.json");
    copy(source, models, "10_People/PersonTeamAssignment/PersonTeamAssignment_Ru_SelectedItems_Ov_Qe.json");
    copy(source, models, "10_People/PersonTeamAssignment/PersonTeamAssignment_Ru_SelectedItems_Ov.json");

    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);

    Path ovPath = models.resolve("PersonTeamAssignment_Ru_SelectedItems_Ov.json");
    ProjectItem item = project.getRoot().findByPath(ovPath.toString());

    FxTestSupport.Loaded<OverviewModelEditorController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/overviewmodel/overview-model-editor.fxml");
    FxTestSupport.onFx(() -> loaded.controller().load(item));

    OverviewColumnsPanelController columnsController = FxTestSupport.field(loaded.controller(), "overviewColumnsController");
    VBox columnRows = FxTestSupport.field(columnsController, "columnRows");

    assertColumnResolvesTo(columnRows, 0, "Team_Dc:/Team/TeamName");
    assertColumnResolvesTo(columnRows, 1, "Team_Dc:/Team/Location");
    assertColumnResolvesTo(columnRows, 2, "TeamPerson_LinkFields_Dc:/LinkFields/Position");
    assertColumnResolvesTo(columnRows, 3, "TeamPerson_LinkFields_Dc:/LinkFields/TimeShare");
  }

  /** Asserts the row's "Field" summary shows {@code expectedPath} rather than the column's raw {@code
   * elementRef} id - i.e. that it actually resolved. Column 0 ("Photo", an attachment group with no icon or
   * label of its own) is separately, correctly flagged by {@link
   * de.a12.studio.modelsvalidation.validators.overview.OverviewColumnHeaderLabelOrIconValidator} regardless of
   * this fix, so the "validation-error" style class isn't asserted here - only that the reference itself
   * resolved. */
  private static void assertColumnResolvesTo(VBox columnRows, int index, String expectedPath) {
    Node row = columnRows.getChildren().get(index);
    Label fieldLabel = (Label) row.lookup("#overviewColumnField-" + index);
    assertEquals(expectedPath, fieldLabel.getText());
  }

  private static void copy(Path source, Path modelsDir, String relativePath) throws IOException {
    Path from = source.resolve(relativePath.replace('/', java.io.File.separatorChar));
    Files.copy(from, modelsDir.resolve(from.getFileName()));
  }

  private static void setCurrentProject(Project project) throws Exception {
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, project);
    // OverviewColumnsPanelController.refreshValidationError() calls Studio.getValidationService(), which is
    // otherwise only populated by the real app's ProjectOpenedEvent handler.
    Field validationService = Studio.class.getDeclaredField("validationService");
    validationService.setAccessible(true);
    validationService.set(null, new ValidationService(project));
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
