package de.a12.studio.ui.editors.overviewmodel;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Overview Model's Multi-Selection panel, whose logic lives in {@link
 * de.a12.studio.ui.editors.propertyeditors.AbstractMultiSelectionPanelController} (shared with the Tree Model).
 */
class OverviewMultiSelectionPanelControllerTest {

  private static final String OVERVIEW = """
      {"header": {"id": "Team_Ov", "modelType": "overview", "modelVersion": "11.0.0", "modelReferences": []},
       "content": {"configuration": {}}}
      """;

  private static boolean toolkitAvailable;

  private OverviewMultiSelectionPanelController controller;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (controller != null) {
      controller.destroy();
    }
    FxTestSupport.onFx(() -> {
    });
    FxTestSupport.selectProjectItem(null);
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

  @Test
  void enablingSeedsDefaultsAndPersistsWithoutATreeOnlyFlagOrButtonIds(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path file = dir.resolve("Team_Ov.json");
    Files.writeString(file, OVERVIEW);
    ProjectItem item = new ProjectItem(file.toFile());
    assertNotNull(item.getModel(), "the fixture overview model must load");
    FxTestSupport.selectProjectItem(item);
    // The panel shows the Multi-Selection element validator's result, which comes from Studio's service.
    Project project = new Project();
    project.load(dir.toFile());
    setStatic("currentProject", project);
    setStatic("validationService", new ValidationService(project));
    FxTestSupport.Loaded<OverviewMultiSelectionPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/overviewmodel/overview-multi-selection-panel.fxml");
    controller = loaded.controller();
    OverviewModel model = (OverviewModel) item.getModel();
    FxTestSupport.onFx(() -> controller.setModel(model));
    CheckBox enabled = FxTestSupport.field(controller, "multiSelectionEnabledField");
    assertFalse(enabled.isSelected());

    FxTestSupport.onFx(() -> enabled.setSelected(true));

    var saved = ((OverviewModel) new ProjectItem(item.getFile()).getModel()).getContent().getConfiguration().getMultiSelection();
    assertNotNull(saved);
    assertEquals("collapsible_collapsed", saved.getCollapseOption());
    assertEquals("simple", saved.getCounterOption());
    assertEquals("checkbox_and_row", saved.getSelectionArea());
    assertNull(saved.getSelectParent());
    assertFalse(Files.readString(file).contains("selectParent"));
  }
}
