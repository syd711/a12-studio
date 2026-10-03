package de.a12.studio.ui.editors.mappingmodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.scene.control.ComboBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** The Structural Mapping Model panel of the Mapping Model editor: choosing the model stores its id and references it from the header. */
class StructuralMappingModelPanelTest {

  private static final String EDITOR_FXML = "/de/a12/studio/ui/editors/mappingmodel/mapping-model-editor.fxml";

  private static final String MAPPING_MODEL = """
      {"header":{"id":"Test_Ma","modelType":"mapping","modelVersion":"1.0.0","locales":[{"code":"en"}],"modelReferences":[]},
       "content":{"Source":[],"Target":{}}}""";

  private static final String SMM = """
      {"header":{"id":"Test_SMM","modelType":"structuralmapping","modelVersion":"1.0.0","locales":[{"code":"en"}],"modelReferences":[]},
       "content":{"GroupsToClearOnFirstFill":[],"MappingBlocks":[]}}""";

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  private ProjectItem item;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void restoreStudio() throws Exception {
    if (toolkitAvailable) {
      setStatic("currentProject", new Project());
    }
  }

  @BeforeEach
  void openProject() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Files.writeString(workspace.resolve("Test_Ma.json"), MAPPING_MODEL);
    Files.writeString(workspace.resolve("Test_SMM.json"), SMM);
    Project project = new Project();
    project.load(workspace.toFile());
    setStatic("currentProject", project);
    FxTestSupport.setValidationServiceForProject(project);
    item = new ProjectItem(workspace.resolve("Test_Ma.json").toFile());
    FxTestSupport.selectProjectItem(item);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (!toolkitAvailable) {
      return;
    }
    if (item != null) {
      StudioEventManager.getInstance().fireModelClosedEvent(item);
    }
    FxTestSupport.onFx(() -> {
    });
    FxTestSupport.clearValidationService();
    FxTestSupport.selectProjectItem(null);
  }

  @Test
  void offersTheProjectsStructuralMappingModelsAndRequiresOne() throws Exception {
    FxTestSupport.Loaded<MappingModelEditorController> loaded = FxTestSupport.load(EDITOR_FXML);
    FxTestSupport.onFx(() -> loaded.controller().load(item));

    StructuralMappingModelPanelController panel = FxTestSupport.field(loaded.controller(), "structuralMappingModelPanelController");
    assertNotNull(panel, "the panel must be injected");
    ComboBox<String> combo = FxTestSupport.field(panel, "structuralMappingModelField");
    assertEquals(List.of("Test_SMM"), FxTestSupport.onFx(() -> List.copyOf(combo.getItems())));
    ErrorContainerController error = FxTestSupport.field(panel, "errorContainerController");
    assertTrue(FxTestSupport.onFx(() -> error.errorProperty().get()), "nothing selected is an error");
  }

  @Test
  void choosingOneStoresItsIdAndReferencesItFromTheHeader() throws Exception {
    FxTestSupport.Loaded<MappingModelEditorController> loaded = FxTestSupport.load(EDITOR_FXML);
    FxTestSupport.onFx(() -> loaded.controller().load(item));
    StructuralMappingModelPanelController panel = FxTestSupport.field(loaded.controller(), "structuralMappingModelPanelController");
    ComboBox<String> combo = FxTestSupport.field(panel, "structuralMappingModelField");

    FxTestSupport.onFx(() -> combo.setValue("Test_SMM"));

    MappingModel model = (MappingModel) item.getModel();
    assertEquals("Test_SMM", model.getContent().getStructuralMappingModel().getId());
    assertTrue(model.getModelReferences().stream()
        .anyMatch(reference -> reference.getModelType() == ModelType.STRUCTURALMAPPING && "Test_SMM".equals(reference.getReference())));
    ErrorContainerController error = FxTestSupport.field(panel, "errorContainerController");
    assertFalse(FxTestSupport.onFx(() -> error.errorProperty().get()), "a selected model is no error");
    String saved = Files.readString(workspace.resolve("Test_Ma.json"));
    assertTrue(saved.contains("\"StructuralMappingModel\"") && saved.contains("Test_SMM"), saved);

    // and clearing it removes both again
    FxTestSupport.onFx(() -> combo.setValue(null));
    assertNull(model.getContent().getStructuralMappingModel());
    assertTrue(model.getModelReferences().stream().map(ModelReference::getModelType).noneMatch(ModelType.STRUCTURALMAPPING::equals));
  }

  private static void setStatic(String name, Object value) throws Exception {
    Field field = Studio.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }
}
