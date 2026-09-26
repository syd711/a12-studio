package de.a12.studio.ui.tabs;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.ModelOpenedEvent;
import de.a12.studio.ui.components.StudioTabPane;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.WindowEvent;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
      SplitPane splitPane = FxTestSupport.field(controller, "splitPane");
      for (Node pane : List.copyOf(splitPane.getItems())) {
        for (Tab tab : List.copyOf(((TabPane) pane).getTabs())) {
          StudioEventManager.getInstance().fireModelClosedEvent((ProjectItem) tab.getUserData());
        }
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

  @Test
  void splitAndMoveRightOpensTheTabInANewPaneOnTheRight() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    ProjectItem first = queryItem("Query_A");
    ProjectItem second = queryItem("Query_B");
    controller = FxTestSupport.<TabPaneController>load("/de/a12/studio/ui/tabs/scene-tab-pane.fxml").controller();
    SplitPane splitPane = FxTestSupport.field(controller, "splitPane");
    TabPane tabPane = FxTestSupport.field(controller, "tabPane");
    FxTestSupport.onFx(() -> controller.modelOpened(new ModelOpenedEvent(first)));
    Tab firstTab = tabPane.getTabs().get(0);

    // a pane's only tab has nowhere to split off from
    assertTrue(splitAction(firstTab).isDisable());

    FxTestSupport.onFx(() -> controller.modelOpened(new ModelOpenedEvent(second)));
    Tab secondTab = tabPane.getTabs().get(1);
    Node secondContent = secondTab.getContent();
    assertFalse(splitAction(secondTab).isDisable());

    split(secondTab);

    assertEquals(2, splitPane.getItems().size());
    assertSame(tabPane, splitPane.getItems().get(0));
    StudioTabPane right = (StudioTabPane) splitPane.getItems().get(1);
    assertEquals(List.of(firstTab), tabPane.getTabs());
    assertEquals(List.of(secondTab), right.getTabs());
    assertSame(secondTab, right.getSelectionModel().getSelectedItem());
    assertSame(secondContent, secondTab.getContent(), "the editor moves along, it isn't rebuilt");
    assertSame(second, controller.getSelectedProjectItem(), "the pane the tab went to is the active one");
    assertTrue(right.getStyleClass().contains("colorful-studio") == tabPane.getStyleClass().contains("colorful-studio"));
    assertEquals(0.5, splitPane.getDividerPositions()[0], 0.01);
  }

  @Test
  void aPaneThatLosesItsLastTabIsRemoved() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    ProjectItem first = queryItem("Query_A");
    ProjectItem second = queryItem("Query_B");
    controller = FxTestSupport.<TabPaneController>load("/de/a12/studio/ui/tabs/scene-tab-pane.fxml").controller();
    SplitPane splitPane = FxTestSupport.field(controller, "splitPane");
    TabPane tabPane = FxTestSupport.field(controller, "tabPane");
    FxTestSupport.onFx(() -> controller.modelOpened(new ModelOpenedEvent(first)));
    FxTestSupport.onFx(() -> controller.modelOpened(new ModelOpenedEvent(second)));
    Tab firstTab = tabPane.getTabs().get(0);
    Tab secondTab = tabPane.getTabs().get(1);
    split(secondTab);
    StudioTabPane right = (StudioTabPane) splitPane.getItems().get(1);

    // dropping a tab onto another pane is what drag and drop reports to the controller
    FxTestSupport.onFx(() -> moveTab(secondTab, (StudioTabPane) tabPane));

    assertEquals(1, splitPane.getItems().size(), "the emptied pane is gone");
    assertEquals(List.of(firstTab, secondTab), tabPane.getTabs());
    assertSame(secondTab, tabPane.getSelectionModel().getSelectedItem());
    assertSame(second, controller.getSelectedProjectItem());
    assertTrue(right.getTabs().isEmpty());

    // a tab already open is selected wherever it is, not opened a second time
    split(secondTab);
    FxTestSupport.onFx(() -> controller.modelOpened(new ModelOpenedEvent(first)));
    assertEquals(2, splitPane.getItems().size());
    assertSame(first, controller.getSelectedProjectItem());
    assertEquals(2, splitPane.getItems().stream().mapToInt(pane -> ((TabPane) pane).getTabs().size()).sum());

    // closing what is left of the split's second pane takes it away, and the tab pane keeps working
    FxTestSupport.onFx(() -> controller.modelOpened(new ModelOpenedEvent(second)));
    FxTestSupport.onFx(() -> controller.closeSelectedTab());
    assertEquals(1, splitPane.getItems().size());
    assertSame(first, controller.getSelectedProjectItem());
  }

  private void split(Tab tab) throws Exception {
    MenuItem action = splitAction(tab);
    FxTestSupport.onFx(action::fire);
  }

  private MenuItem splitAction(Tab tab) throws Exception {
    return FxTestSupport.onFx(() -> {
      ContextMenu menu = tab.getContextMenu();
      menu.getOnShowing().handle(new WindowEvent(null, WindowEvent.WINDOW_SHOWING));
      return menu.getItems().stream()
          .filter(item -> StudioBundle.get("split_tab_right").equals(item.getText()))
          .findFirst().orElseThrow();
    });
  }

  private void moveTab(Tab tab, StudioTabPane target) {
    try {
      var method = TabPaneController.class.getDeclaredMethod("moveTab", Tab.class, StudioTabPane.class);
      method.setAccessible(true);
      method.invoke(controller, tab, target);
    }
    catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
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
