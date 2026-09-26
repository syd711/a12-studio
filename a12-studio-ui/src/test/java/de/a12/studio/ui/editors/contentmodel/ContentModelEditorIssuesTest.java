package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentModule;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preview.PreviewServer;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.shape.Circle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * What the Content Model editor shows of the validators' findings: the row of the element they are about (color and
 * tooltip), the count under the tree, the message box above the selected element's settings, and the badge on the
 * settings button.
 */
class ContentModelEditorIssuesTest {

  @TempDir
  Path workspace;

  private ProjectItem item;
  private FxTestSupport.Loaded<ContentModelEditorController> loaded;
  private ContentModel model;
  private TreeView<ContentElement> tree;

  @AfterAll
  static void tearDown() throws Exception {
    PreviewServer.stopIfRunning();
    setStatic("currentProject", new Project());
    setStatic("validationService", null);
    FxTestSupport.selectProjectItem(null);
  }

  @BeforeEach
  void openEditor() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    Files.createDirectories(workspace.resolve("models"));
    Path file = workspace.resolve("models").resolve("WelcomePage_CM.json");
    Files.copy(locateFixture(), file);
    Project project = new Project();
    project.load(workspace.toFile());
    setStatic("currentProject", project);
    setStatic("validationService", new ValidationService(project));
    item = project.getRoot().findByPath(file.toString());
    FxTestSupport.selectProjectItem(item);

    loaded = FxTestSupport.load("/de/a12/studio/ui/editors/contentmodel/content-model-editor.fxml");
    FxTestSupport.onFx(() -> loaded.controller().load(item));
    model = (ContentModel) item.getModel();
    tree = FxTestSupport.field(loaded.controller(), "elementsTree");
  }

  @AfterEach
  void closeEditor() throws Exception {
    if (loaded != null) {
      FxTestSupport.onFx(() -> loaded.controller().modelClosed(new ModelClosedEvent(item)));
      StudioEventManager.getInstance().removeListener(loaded.controller());
    }
  }

  @Test
  void aRealModelShowsNoErrorAndNoMessage() throws Exception {
    for (ContentElement element : allElements(model.getContent().getRoot())) {
      assertFalse(issues(element).stream().anyMatch(issue -> Severity.ERROR.name().equals(issue.severity())),
          element.getType() + " " + element.getId() + ": " + issues(element));
    }
    assertFalse(errorBox().errorProperty().get());
  }

  @Test
  void anElementWithAnErrorIsMarkedAndItsMessageIsTheTooltip() throws Exception {
    ContentElement group = addToRoot("Group");

    List<ModelValidationError> issues = issues(group);
    assertEquals(1, issues.size(), issues.toString());
    assertEquals(Severity.ERROR.name(), issues.get(0).severity());
    assertEquals(ValidationMessages.get("validation.contentGroupReference.missing", "Repeatable Group", group.getId()),
        issues.get(0).message());

    TreeCell<ContentElement> cell = renderedCell(group);
    assertTrue(cell.getStyleClass().contains("validation-error"));
    assertFalse(cell.getStyleClass().contains("validation-warning"));
    Tooltip tooltip = cell.getTooltip();
    assertTrue(tooltip != null && tooltip.getText().contains(issues.get(0).message()), "the tooltip lists the message");

    ContentElement clean = root().getChildren().get(0);
    TreeCell<ContentElement> cleanCell = renderedCell(clean);
    assertFalse(cleanCell.getStyleClass().contains("validation-error"));
    assertNull(cleanCell.getTooltip());
  }

  @Test
  void aHintIsMarkedAsAWarning() throws Exception {
    ContentElement box = addToRoot("Box");

    List<ModelValidationError> issues = issues(box);
    assertEquals(1, issues.size(), issues.toString());
    assertEquals(Severity.WARNING.name(), issues.get(0).severity());
    TreeCell<ContentElement> cell = renderedCell(box);
    assertTrue(cell.getStyleClass().contains("validation-warning"));
    assertFalse(cell.getStyleClass().contains("validation-error"));
  }

  @Test
  void theSummaryCountsErrorsAndWarningsAndDisappearsWithThem() throws Exception {
    Label summary = FxTestSupport.field(loaded.controller(), "issueSummaryLabel");
    int errorsBefore = count(Severity.ERROR);
    int warningsBefore = count(Severity.WARNING);

    addToRoot("Group");
    addToRoot("Box");

    assertTrue(summary.isVisible() && summary.isManaged());
    assertEquals(errorsBefore + 1, count(Severity.ERROR));
    assertEquals(warningsBefore + 1, count(Severity.WARNING));
    assertTrue(summary.getText().contains(String.valueOf(errorsBefore + 1)), summary.getText());
    assertTrue(summary.getStyleClass().contains("validation-error"), "an error makes the summary an error");

    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    assertEquals(errorsBefore, count(Severity.ERROR));
    assertEquals(warningsBefore, count(Severity.WARNING));
  }

  @Test
  void theSelectedElementsMessagesShowAboveItsSettings() throws Exception {
    ContentElement group = addToRoot("Group");

    assertEquals(group, selected());
    assertTrue(errorBox().errorProperty().get());
    assertEquals(Severity.ERROR.name(), errorBox().severityProperty().get());

    FxTestSupport.onFx(() -> tree.getSelectionModel().select(tree.getRoot()));
    assertEquals(!issues(root()).isEmpty(), errorBox().errorProperty().get(),
        "the box shows the findings of the selected element only, and nothing for one without any");
  }

  @Test
  void clickingTheSummaryGoesToTheNextElementWithAProblemAndWraps() throws Exception {
    ContentElement first = addToRoot("Group");
    ContentElement second = addToRoot("Group");
    FxTestSupport.onFx(() -> tree.getSelectionModel().select(tree.getRoot()));

    List<ContentElement> flagged = allElements(root()).stream().filter(element -> !issues(element).isEmpty()).toList();
    assertTrue(flagged.containsAll(List.of(first, second)));

    FxTestSupport.onFx(() -> loaded.controller().onIssueSummaryClicked(null));
    assertEquals(flagged.get(0), selected());
    for (int i = 1; i < flagged.size(); i++) {
      FxTestSupport.onFx(() -> loaded.controller().onIssueSummaryClicked(null));
      assertEquals(flagged.get(i), selected());
    }
    FxTestSupport.onFx(() -> loaded.controller().onIssueSummaryClicked(null));
    assertEquals(flagged.get(0), selected(), "after the last one it starts over");
  }

  @Test
  void aSaveOfTheModelElsewhereRefreshesWhatIsShown() throws Exception {
    ContentElement group = addToRoot("Group");
    assertFalse(issues(group).isEmpty());

    // Changed behind the editor's back (like the settings dialog does) and announced by the save event.
    root().getChildren().remove(group);
    FxTestSupport.onFx(() -> StudioEventManager.getInstance().fireModelSavedEvent(item));

    assertTrue(issues(group).isEmpty(), "the element is not part of the model any more");
  }

  @Test
  void aProblemWithTheModelsSettingsShowsOnTheSettingsButton() throws Exception {
    Object controller = FxTestSupport.field(loaded.controller(), "settingsToolbarButtonController");
    Circle badge = FxTestSupport.field(controller, "settingsErrorBadge");
    Tooltip tooltip = FxTestSupport.field(controller, "settingsButtonTooltip");
    // The fixture may have settings findings of its own (its roles, with no roles file in the workspace).
    assertFalse(tooltip.getText().contains("group_1"));

    model.getContent().getConfiguration().setBaseGroupId("group_1");
    FxTestSupport.onFx(() -> loaded.controller().refreshIssues());

    assertTrue(badge.isVisible());
    assertTrue(tooltip.getText().contains("group_1"), tooltip.getText());

    model.getContent().getConfiguration().setBaseGroupId(null);
    FxTestSupport.onFx(() -> loaded.controller().refreshIssues());
    assertFalse(tooltip.getText().contains("group_1"), tooltip.getText());
  }

  // ---- helpers ----

  private ContentElement root() {
    return model.getContent().getRoot();
  }

  private List<ModelValidationError> issues(ContentElement element) {
    return loaded.controller().issuesOf(element);
  }

  private int count(Severity severity) {
    return (int) allElements(root()).stream().flatMap(element -> issues(element).stream())
        .filter(issue -> severity.name().equals(issue.severity())).count();
  }

  private ErrorContainerController errorBox() throws Exception {
    return FxTestSupport.field(loaded.controller(), "elementIssuesController");
  }

  private ContentElement addToRoot(String type) throws Exception {
    ContentModule module = ContentElementLibrary.find(ContentElementLibrary.NAMESPACE, type).orElseThrow();
    FxTestSupport.onFx(() -> loaded.controller().addChild(root(), module));
    return root().getChildren().get(root().getChildren().size() - 1);
  }

  /** The cell the tree would use for {@code element}, updated for its row. */
  private TreeCell<ContentElement> renderedCell(ContentElement element) throws Exception {
    return FxTestSupport.onFx(() -> {
      TreeCell<ContentElement> cell = tree.getCellFactory().call(tree);
      cell.updateTreeView(tree);
      TreeItem<ContentElement> found = find(tree.getRoot(), element);
      cell.updateIndex(tree.getRow(found));
      assertNotEquals(-1, tree.getRow(found), "the element's row is visible");
      assertEquals(element, cell.getItem());
      return cell;
    });
  }

  private ContentElement selected() throws Exception {
    return FxTestSupport.onFx(() -> tree.getSelectionModel().getSelectedItem().getValue());
  }

  private static TreeItem<ContentElement> find(TreeItem<ContentElement> from, ContentElement element) {
    if (from.getValue() == element) {
      return from;
    }
    for (TreeItem<ContentElement> child : from.getChildren()) {
      TreeItem<ContentElement> found = find(child, element);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private static List<ContentElement> allElements(ContentElement root) {
    List<ContentElement> all = new java.util.ArrayList<>();
    all.add(root);
    if (root.getChildren() != null) {
      root.getChildren().forEach(child -> all.addAll(allElements(child)));
    }
    return all;
  }

  private static void setStatic(String name, Object value) throws Exception {
    Field field = Studio.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }

  private static Path locateFixture() throws IOException {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("basic").resolve("models").resolve("WelcomePage_CM.json");
      if (Files.isRegularFile(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate the WelcomePage_CM.json fixture");
  }
}
