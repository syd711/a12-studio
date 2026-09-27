package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.BoxElementType;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.CheckBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Gap 17 of the "Overview Model: gap review" (structural refactoring) - {@link
 * OverviewModelEditorController#pruneSubHeader} removes a Subheader element once the user switches its feature
 * off, wiring {@link OverviewSearchAndFiltersPanelController#setOnFeatureSwitchChange}/{@link
 * OverviewMultiSelectionPanelController#setOnEnabledChange} into {@link
 * de.a12.studio.modelsvalidation.validators.overview.OverviewSubHeaderPruning}. */
class OverviewModelEditorControllerSubHeaderPruningTest {

  private static final String OVERVIEW = """
      {"header": {"id": "Team_Ov", "modelType": "overview", "modelVersion": "39.0.0"},
       "content": {
         "configuration": {
           "showFullTextSearch": true,
           "enableFilter": true,
           "filterConfiguration": {"showFilterButton": true},
           "multiSelection": {}
         },
         "columns": [],
         "subHeaderBox": {
           "leftSlot": [{"type": "multi_selection"}],
           "rightSlot": [{"type": "search"}, {"type": "filter"}]
         }
       }}
      """;

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
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

  private OverviewModelEditorController loadEditor(Path dir, ProjectItem[] itemHolder) throws Exception {
    Path models = Files.createDirectories(dir.resolve("models"));
    Path overviewFile = models.resolve("Team_Ov.json");
    Files.writeString(overviewFile, OVERVIEW);

    Project project = new Project();
    project.load(dir.toFile());
    setStatic("currentProject", project);
    setStatic("validationService", new ValidationService(project));
    ProjectItem item = project.getRoot().findByPath(overviewFile.toString());
    itemHolder[0] = item;

    FxTestSupport.Loaded<OverviewModelEditorController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/overviewmodel/overview-model-editor.fxml");
    OverviewModelEditorController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.load(item));
    return controller;
  }

  @Test
  void turningOffShowFullTextSearchRemovesOnlyTheSearchElement(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    ProjectItem[] itemHolder = new ProjectItem[1];
    OverviewModelEditorController controller = loadEditor(dir, itemHolder);
    OverviewModel model = (OverviewModel) itemHolder[0].getModel();

    OverviewSearchAndFiltersPanelController searchAndFilters = FxTestSupport.field(controller, "overviewSearchAndFiltersController");
    CheckBox showFullTextSearchField = FxTestSupport.field(searchAndFilters, "showFullTextSearchField");
    FxTestSupport.onFx(() -> showFullTextSearchField.setSelected(false));

    assertTrue(countType(model, model.getContent().getSubHeaderBox().getRightSlot(), BoxElementType.SEARCH) == 0,
        "Search element removed");
    assertEquals(1, countType(model, model.getContent().getSubHeaderBox().getRightSlot(), BoxElementType.FILTER),
        "Filter element stays");
    assertEquals(1, countType(model, model.getContent().getSubHeaderBox().getLeftSlot(), BoxElementType.MULTI_SELECTION),
        "Multi-Selection element stays");
  }

  @Test
  void turningOffMultiSelectionRemovesOnlyTheMultiSelectionElement(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    ProjectItem[] itemHolder = new ProjectItem[1];
    OverviewModelEditorController controller = loadEditor(dir, itemHolder);
    OverviewModel model = (OverviewModel) itemHolder[0].getModel();

    OverviewMultiSelectionPanelController multiSelection = FxTestSupport.field(controller, "overviewMultiSelectionController");
    CheckBox multiSelectionEnabledField = FxTestSupport.field(multiSelection, "multiSelectionEnabledField");
    FxTestSupport.onFx(() -> multiSelectionEnabledField.setSelected(false));

    assertTrue(model.getContent().getSubHeaderBox().getLeftSlot().isEmpty(), "Multi-Selection element removed");
    assertEquals(2, model.getContent().getSubHeaderBox().getRightSlot().size(), "Search and Filter stay");
  }

  private static long countType(OverviewModel model, java.util.List<de.a12.studio.models.overviewmodel.BoxElement> slot, BoxElementType type) {
    return slot.stream().filter(element -> element.getType() == type).count();
  }
}
