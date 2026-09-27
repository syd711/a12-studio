package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnRef;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.OverviewModelContent;
import de.a12.studio.models.projects.Project;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Gap 17 of the "Overview Model: gap review" - {@link OverviewSortingPanelController#refresh()} prunes an
 * Initial Sorting entry that a column delete or a "Sortable" toggle-off just made invalid, mirroring what
 * {@link OverviewAccessibilityPanelController#refresh()} already does for the Screen Reader Column. */
class OverviewSortingPanelControllerTest {

  private static final String FXML = "/de/a12/studio/ui/editors/overviewmodel/overview-sorting-panel.fxml";

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @BeforeEach
  void setUp(@TempDir Path dir) throws Exception {
    if (toolkitAvailable) {
      Project project = new Project();
      project.load(dir.toFile());
      setStatic("currentProject", project);
      setStatic("validationService", new ValidationService(project));
    }
  }

  @AfterEach
  void tearDown() throws Exception {
    if (toolkitAvailable) {
      setStatic("currentProject", new Project());
      setStatic("validationService", null);
    }
  }

  private static void setStatic(String name, Object value) throws Exception {
    Field field = Studio.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }

  private static OverviewModel modelWithOneSortedColumn(boolean sortable) {
    OverviewModel model = new OverviewModel();
    model.setId("Team_Ov");
    model.setContent(new OverviewModelContent());
    Column column = new Column();
    column.setId("column_1");
    column.setWidth(1.0);
    column.setElementRef("field_1");
    column.setSortable(sortable);
    column.setPreferredSorting(sortable ? Column.PREFERRED_SORTING_ASC : null);
    model.getContent().getColumns().add(column);
    OverviewConfiguration configuration = new OverviewConfiguration();
    ColumnRef sortRef = new ColumnRef();
    sortRef.setIdref("column_1");
    configuration.getInitialSorting().add(sortRef);
    model.getContent().setConfiguration(configuration);
    return model;
  }

  @Test
  void refreshRemovesASortingEntryWhoseColumnWasDeleted() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    OverviewModel model = modelWithOneSortedColumn(true);

    FxTestSupport.Loaded<OverviewSortingPanelController> loaded = FxTestSupport.load(FXML);
    OverviewSortingPanelController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.setModel(model));
    assertEquals(1, model.getContent().getConfiguration().getInitialSorting().size());

    model.getContent().getColumns().clear();
    FxTestSupport.onFx(controller::refresh);

    assertTrue(model.getContent().getConfiguration().getInitialSorting().isEmpty());
  }

  @Test
  void refreshRemovesASortingEntryForAColumnMadeNonSortable() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    OverviewModel model = modelWithOneSortedColumn(true);

    FxTestSupport.Loaded<OverviewSortingPanelController> loaded = FxTestSupport.load(FXML);
    OverviewSortingPanelController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.setModel(model));

    model.getContent().getColumns().get(0).setSortable(false);
    FxTestSupport.onFx(controller::refresh);

    assertTrue(model.getContent().getConfiguration().getInitialSorting().isEmpty());
  }

  @Test
  void refreshKeepsAValidSortingEntry() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    OverviewModel model = modelWithOneSortedColumn(true);

    FxTestSupport.Loaded<OverviewSortingPanelController> loaded = FxTestSupport.load(FXML);
    OverviewSortingPanelController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.setModel(model));
    FxTestSupport.onFx(controller::refresh);

    assertEquals(1, model.getContent().getConfiguration().getInitialSorting().size());
  }
}
