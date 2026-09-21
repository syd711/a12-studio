package de.a12.studio.ui.tabs;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.ModelOpenedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// The tab strip with a real JavaFX toolkit: a model rewritten by a refactoring in another model is redrawn.
class TabPaneControllerTest {

  private static boolean toolkitAvailable;

  private TabPaneController controller;

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

  // The editors of the opened tabs listen on the application-wide event manager for as long as they are not closed,
  // so every one of them is closed again - a later test must not find them still reacting to its events.
  @AfterEach
  void unregister() throws Exception {
    if (controller != null) {
      TabPane tabPane = FxTestSupport.field(controller, "tabPane");
      for (Tab tab : List.copyOf(tabPane.getTabs())) {
        StudioEventManager.getInstance().fireModelClosedEvent((ProjectItem) tab.getUserData());
      }
      StudioEventManager.getInstance().removeListener(controller);
    }
  }

  @Test
  void aModelRewrittenByARefactoringElsewhereIsRedrawnInItsOpenTab() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    ProjectItem item = queryItem("Query_A");
    ProjectItem other = queryItem("Query_B");
    controller = FxTestSupport.<TabPaneController>load("/de/a12/studio/ui/tabs/scene-tab-pane.fxml").controller();
    TabPane tabPane = FxTestSupport.field(controller, "tabPane");
    FxTestSupport.onFx(() -> controller.modelOpened(new ModelOpenedEvent(item)));
    FxTestSupport.onFx(() -> controller.modelOpened(new ModelOpenedEvent(other)));
    assertEquals(2, tabPane.getTabs().size());
    Tab tab = tabPane.getTabs().get(0);
    Node before = tab.getContent();
    assertNotNull(before);

    FxTestSupport.onFx(() -> StudioEventManager.getInstance().fireModelRefactoredEvent(item));

    assertEquals(2, tabPane.getTabs().size(), "the tab stays");
    assertSame(item, tab.getUserData());
    assertNotNull(tab.getContent());
    assertNotSame(before, tab.getContent(), "its editor was rebuilt from the model");
    assertSame(other, tabPane.getTabs().get(1).getUserData(), "the other tab is left alone");
  }

  @Test
  void aRefactoredModelWithoutAnOpenTabChangesNothing() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    ProjectItem item = queryItem("Query_A");
    controller = FxTestSupport.<TabPaneController>load("/de/a12/studio/ui/tabs/scene-tab-pane.fxml").controller();
    TabPane tabPane = FxTestSupport.field(controller, "tabPane");

    FxTestSupport.onFx(() -> StudioEventManager.getInstance().fireModelRefactoredEvent(item));

    assertEquals(0, tabPane.getTabs().size());
  }

  // The editors read the project's settings, so the models live in a real (temporary) project.
  private ProjectItem queryItem(String id) throws Exception {
    Path models = Files.createDirectories(workspace.resolve("models"));
    Path file = models.resolve(id + ".json");
    Files.writeString(file, """
        {
          "header": {"id": "%s", "modelType": "query", "modelVersion": "0.1.0", "locales": [{"code": "en"}]},
          "content": {"projectionName": "document", "targetDocumentModel": "Person_DM", "fields": ["/Person/Name"]}
        }
        """.formatted(id));
    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);
    // The editors ask Studio for the validation service of the project that is open.
    Field validationService = Studio.class.getDeclaredField("validationService");
    validationService.setAccessible(true);
    validationService.set(null, new ValidationService(project));
    ProjectItem item = project.getRoot().findByPath(file.toString());
    return item != null ? item : new ProjectItem(file.toFile());
  }

  private static void setCurrentProject(Project project) throws Exception {
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, project);
  }
}
