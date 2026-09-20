package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * A Query Model whose target Document Model is unset or no longer in the project used to render an empty tree and
 * nothing else; the tree now says why (see {@code QueryModelTreeController#showTargetProblem}).
 */
class QueryModelTreeTargetProblemTest {

  private static final String QUERY = "PersonsWithFulltimeContract_UnassignedToTeam_Qe";

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
    }
  }

  @Test
  void aTargetThatResolvesShowsItsTreeAndNoBanner() throws Exception {
    Fixture fixture = fixture();

    assertFalse(bannerShown(fixture.controller));
    assertEquals(1, fixture.tree.getRoot().getChildren().size());
  }

  @Test
  void aTargetThatIsNoLongerInTheProjectIsReportedByNameInsteadOfAnEmptyTree() throws Exception {
    Fixture fixture = fixture();
    fixture.query.getContent().setTargetDocumentModel("Gone_DM");

    FxTestSupport.onFx(() -> fixture.controller.load(fixture.item, fixture.query));

    assertTrue(bannerShown(fixture.controller));
    assertEquals(StudioBundle.get("query_model_tree.target_not_found", "Gone_DM"), bannerText(fixture.controller));
    assertTrue(bannerText(fixture.controller).contains("Gone_DM"));
    assertTrue(fixture.tree.getRoot().getChildren().isEmpty());
  }

  @Test
  void aQueryWithoutATargetSaysSo() throws Exception {
    Fixture fixture = fixture();
    fixture.query.getContent().setTargetDocumentModel(null);

    FxTestSupport.onFx(() -> fixture.controller.load(fixture.item, fixture.query));

    assertTrue(bannerShown(fixture.controller));
    assertEquals(StudioBundle.get("query_model_tree.target_missing"), bannerText(fixture.controller));
  }

  @Test
  void theBannerGoesAwayOnceTheTargetResolvesAgain() throws Exception {
    Fixture fixture = fixture();
    String target = fixture.query.getContent().getTargetDocumentModel();
    fixture.query.getContent().setTargetDocumentModel("Gone_DM");
    FxTestSupport.onFx(() -> fixture.controller.load(fixture.item, fixture.query));
    assertTrue(bannerShown(fixture.controller));

    fixture.query.getContent().setTargetDocumentModel(target);
    FxTestSupport.onFx(() -> fixture.controller.load(fixture.item, fixture.query));

    assertFalse(bannerShown(fixture.controller));
  }

  // ---- helpers ----------------------------------------------------------------------------------------------

  private record Fixture(QueryModelTreeController controller, TreeTableView<QueryTreeRow> tree, ProjectItem item,
      QueryModel query) {
  }

  /** The advanced_new workspace's query and the Document Model it targets, loaded as the current project. */
  private Fixture fixture() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path source = locateWorkspace().resolve("models").resolve("10_People");
    Path models = Files.createDirectories(workspace.resolve("models"));
    Files.copy(source.resolve("Person_Dc.json"), models.resolve("Person_Dc.json"));
    Files.copy(source.resolve(QUERY + ".json"), models.resolve(QUERY + ".json"));

    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);
    ProjectItem item = project.getRoot().findByPath(models.resolve(QUERY + ".json").toString());
    if (item == null) {
      item = new ProjectItem(models.resolve(QUERY + ".json").toFile());
    }
    QueryModel query = (QueryModel) item.getModel();
    FxTestSupport.Loaded<QueryModelTreeController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/querymodel/query-model-tree.fxml");
    ProjectItem queryItem = item;
    FxTestSupport.onFx(() -> loaded.controller().load(queryItem, query));
    TreeTableView<QueryTreeRow> tree = FxTestSupport.field(loaded.controller(), "elementsTreeTable");
    return new Fixture(loaded.controller(), tree, item, query);
  }

  private static boolean bannerShown(QueryModelTreeController controller) throws Exception {
    ErrorContainerController banner = FxTestSupport.field(controller, "errorContainerController");
    return banner.errorProperty().get();
  }

  private static String bannerText(QueryModelTreeController controller) throws Exception {
    ErrorContainerController banner = FxTestSupport.field(controller, "errorContainerController");
    Label label = FxTestSupport.field(banner, "errorMessage");
    return label.getText();
  }

  private static void setCurrentProject(Project project) throws Exception {
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, project);
  }

  private static Path locateWorkspace() throws IOException {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("advanced_new");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces/advanced_new' above " + Path.of("").toAbsolutePath());
  }
}
