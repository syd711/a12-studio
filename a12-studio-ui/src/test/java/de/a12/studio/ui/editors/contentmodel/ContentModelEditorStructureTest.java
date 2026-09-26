package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentInsertion.Position;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentStructure;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preview.PreviewServer;
import de.a12.studio.ui.util.StudioBundle;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The element tree of the Content Model editor keeps to the structure rules of the element types: the actions the rules
 * forbid are disabled (and do nothing when triggered anyway), add and paste work above and below an element, and
 * elements can be dragged to other places the rules allow, as one undoable step.
 */
class ContentModelEditorStructureTest {

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

  // ---- what the rules forbid ----

  @Test
  void theRequiredPartsOfATableCannotBeDeletedCutMovedOrCopied() throws Exception {
    select("TableHead");
    ContentElement head = selected();
    ContentElement table = parentOf(head);
    List<ContentElement> before = new ArrayList<>(table.getChildren());

    for (String button : List.of("deleteButton", "cutButton", "moveDownButton", "duplicateButton")) {
      assertTrue(disabled(button), button + " must be disabled for a table's head");
    }
    // The shortcuts call the handlers without looking at the buttons.
    FxTestSupport.onFx(() -> {
      loaded.controller().onRemoveElement(null);
      loaded.controller().onCut(null);
      loaded.controller().onMoveDown(null);
      loaded.controller().onDuplicate(null);
    });

    assertEquals(before, table.getChildren());
    assertFalse(disabled("copyButton"), "copying is always possible");
  }

  @Test
  void aRowOfTheTableBodyCanStillBeDuplicatedMovedAndDeleted() throws Exception {
    select("TableBodyRow");

    assertFalse(disabled("deleteButton"));
    assertFalse(disabled("cutButton"));
    assertFalse(disabled("duplicateButton"));
    assertFalse(disabled("moveDownButton"));
    assertTrue(disabled("moveUpButton"), "it is the first row");
  }

  @Test
  void pasteOffersTheRulesOfTheTargetAndAboveOrBelowItsParent() throws Exception {
    ContextMenu menu = tree.getContextMenu();
    select("Paragraph");
    FxTestSupport.onFx(() -> loaded.controller().onCopy(null));

    select("Paragraph");
    showMenu(menu);
    assertTrue(menuItem(menu, "content_model_tree.paste").isDisable(), "a paragraph takes no children");
    assertFalse(menuItem(menu, "content_model_tree.paste_above").isDisable());
    assertFalse(menuItem(menu, "content_model_tree.paste_below").isDisable());

    select("TableHead");
    showMenu(menu);
    assertTrue(menuItem(menu, "content_model_tree.paste_above").isDisable(), "a paragraph does not belong between a table's parts");
    assertTrue(menuItem(menu, "content_model_tree.paste_below").isDisable());

    FxTestSupport.onFx(() -> tree.getSelectionModel().select(tree.getRoot()));
    showMenu(menu);
    assertFalse(menuItem(menu, "content_model_tree.paste").isDisable());
    assertTrue(menuItem(menu, "content_model_tree.paste_above").isDisable(), "nothing goes above the root");
    assertTrue(menuItem(menu, "content_model_tree.add_above").isDisable());
    assertTrue(menuItem(menu, "content_model_tree.add_below").isDisable());
  }

  @Test
  void addIsOfferedNextToAnElementToo() throws Exception {
    ContextMenu menu = tree.getContextMenu();

    select("Paragraph");
    showMenu(menu);

    assertFalse(menuItem(menu, "content_model_tree.add_above").isDisable());
    assertFalse(menuItem(menu, "content_model_tree.add_below").isDisable());
    assertTrue(menuItem(menu, "content_model_tree.add_child").isDisable(), "a paragraph takes no children");
  }

  @Test
  void pasteAboveAndBelowPutTheCopyNextToTheSelectionAsOneUndoableStep() throws Exception {
    select("Paragraph");
    ContentElement paragraph = selected();
    ContentElement parent = parentOf(paragraph);
    List<ContentElement> before = new ArrayList<>(parent.getChildren());
    int index = before.indexOf(paragraph);

    FxTestSupport.onFx(() -> loaded.controller().onCopy(null));
    FxTestSupport.onFx(() -> loaded.controller().onPasteAbove(null));
    assertEquals(before.size() + 1, parent.getChildren().size());
    ContentElement above = parent.getChildren().get(index);
    assertEquals("Paragraph", above.getType());
    assertFalse(before.contains(above), "a copy, not the same element");
    assertEquals(paragraph, parent.getChildren().get(index + 1));
    assertEquals(above, selected());

    select("Paragraph");
    FxTestSupport.onFx(() -> tree.getSelectionModel().select(find(tree.getRoot(), paragraph)));
    FxTestSupport.onFx(() -> loaded.controller().onPasteBelow(null));
    assertEquals(before.size() + 2, parent.getChildren().size());
    assertEquals("Paragraph", parent.getChildren().get(parent.getChildren().indexOf(paragraph) + 1).getType());

    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    assertEquals(before, parent.getChildren());
    assertEquals(List.of(), ContentStructure.violations(model.getContent().getRoot()));
  }

  @Test
  void aPasteTheRulesRefuseDoesNothing() throws Exception {
    select("Paragraph");
    FxTestSupport.onFx(() -> loaded.controller().onCopy(null));
    select("TableHead");
    ContentElement table = parentOf(selected());
    List<ContentElement> before = new ArrayList<>(table.getChildren());

    FxTestSupport.onFx(() -> loaded.controller().onPasteAbove(null));
    FxTestSupport.onFx(() -> loaded.controller().onPasteBelow(null));
    FxTestSupport.onFx(() -> loaded.controller().onPaste(null));

    assertEquals(before, table.getChildren());
    assertEquals(0, ContentStructure.violations(model.getContent().getRoot()).size());
  }

  // ---- drag and drop ----

  @Test
  void draggingIntoTheMiddleOfARowMakesTheElementItsLastChildAndUndoBringsItBack() throws Exception {
    ContentElement root = model.getContent().getRoot();
    ContentElement paragraph = addToRoot("Paragraph");
    ContentElement target = addToRoot("Box");
    List<ContentElement> before = new ArrayList<>(root.getChildren());

    drag(paragraph);
    assertEquals(Position.AS_CHILD, dropPosition(target, 10, 20));
    FxTestSupport.onFx(() -> relocate(paragraph, target, Position.AS_CHILD));

    assertEquals(List.of(paragraph), target.getChildren());
    assertFalse(root.getChildren().contains(paragraph));
    assertEquals(paragraph, selected());
    assertEquals(List.of(), ContentStructure.violations(root));
    assertEquals(paragraph, find(tree.getRoot(), target).getChildren().get(0).getValue(), "the tree shows it");

    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    assertEquals(before, root.getChildren());
    assertNull(target.getChildren() == null || target.getChildren().isEmpty() ? null : target.getChildren());
    FxTestSupport.onFx(() -> loaded.controller().onRedo(null));
    assertEquals(List.of(paragraph), target.getChildren());
  }

  @Test
  void draggingOntoTheTopOrBottomOfASiblingReordersWithinTheParent() throws Exception {
    ContentElement root = model.getContent().getRoot();
    ContentElement first = root.getChildren().get(0);
    ContentElement second = addToRoot("Paragraph");
    ContentElement last = addToRoot("Heading");
    List<ContentElement> before = new ArrayList<>(root.getChildren());
    assertEquals(List.of(first, second, last), before);

    drag(first);
    assertEquals(Position.BELOW, dropPosition(last, 19, 20));
    FxTestSupport.onFx(() -> relocate(first, last, Position.BELOW));
    assertEquals(List.of(second, last, first), root.getChildren());
    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    assertEquals(before, root.getChildren());

    drag(last);
    assertEquals(Position.ABOVE, dropPosition(first, 1, 20));
    FxTestSupport.onFx(() -> relocate(last, first, Position.ABOVE));
    assertEquals(List.of(last, first, second), root.getChildren());
    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    assertEquals(before, root.getChildren());

    // Right below the element that is just above it: the moving element's own old place must not skew the index.
    drag(first);
    FxTestSupport.onFx(() -> relocate(first, second, Position.BELOW));
    assertEquals(List.of(second, first, last), root.getChildren());
    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    drag(last);
    FxTestSupport.onFx(() -> relocate(last, second, Position.ABOVE));
    assertEquals(List.of(first, last, second), root.getChildren());
  }

  @Test
  void anElementIsNeverDroppedWhereTheRulesForbidOrOnItself() throws Exception {
    ContentElement root = model.getContent().getRoot();
    select("TableBodyRow");
    ContentElement row = selected();
    select("Paragraph");
    ContentElement paragraph = selected();

    drag(row);
    assertNull(dropPosition(root, 10, 20), "a body row does not belong into a Box");
    assertNull(dropPosition(paragraph, 1, 20), "nor next to a paragraph");
    assertNull(dropPosition(row, 10, 20), "not onto itself");

    ContentElement box = root.getChildren().get(0);
    drag(box);
    ContentElement inside = allElements(box).get(allElements(box).size() - 1);
    assertNull(dropPosition(inside, 10, 20), "not into its own subtree");
    assertNull(dropPosition(box, 10, 20));
    assertNull(dropPosition(root, 1, 20), "nothing above the root, and it already is the parent");
  }

  @Test
  void theRootCannotBeDragged() throws Exception {
    ContentElement root = model.getContent().getRoot();

    drag(root);

    assertNull(dropPosition(root.getChildren().get(0), 10, 20), "no drop is accepted while nothing legal is dragged");
  }

  // ---- helpers ----

  /** Adds a new element of {@code type} as the last child of the root through the editor and returns it. */
  private ContentElement addToRoot(String type) throws Exception {
    ContentElement root = model.getContent().getRoot();
    de.a12.studio.models.contentmodel.ContentModule module =
        de.a12.studio.models.contentmodel.ContentElementLibrary.find(de.a12.studio.models.contentmodel.ContentElementLibrary.NAMESPACE, type).orElseThrow();
    FxTestSupport.onFx(() -> loaded.controller().addChild(root, module));
    return root.getChildren().get(root.getChildren().size() - 1);
  }

  private void drag(ContentElement element) throws Exception {
    Field field = ContentModelEditorController.class.getDeclaredField("draggedElement");
    field.setAccessible(true);
    field.set(loaded.controller(), element);
  }

  private Position dropPosition(ContentElement target, double y, double height) throws Exception {
    Method method = ContentModelEditorController.class.getDeclaredMethod("dropPosition", ContentElement.class, double.class, double.class);
    method.setAccessible(true);
    return (Position) method.invoke(loaded.controller(), target, y, height);
  }

  private void relocate(ContentElement element, ContentElement target, Position position) {
    try {
      Method method = ContentModelEditorController.class.getDeclaredMethod("relocate", ContentElement.class, ContentElement.class, Position.class);
      method.setAccessible(true);
      method.invoke(loaded.controller(), element, target, position);
    }
    catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private boolean disabled(String buttonField) throws Exception {
    Button button = FxTestSupport.field(loaded.controller(), buttonField);
    return FxTestSupport.onFx(button::isDisabled);
  }

  private void showMenu(ContextMenu menu) throws Exception {
    FxTestSupport.onFx(() -> Event.fireEvent(menu, new WindowEvent(menu, WindowEvent.WINDOW_SHOWING)));
  }

  private static MenuItem menuItem(ContextMenu menu, String bundleKey) {
    return menu.getItems().stream().filter(i -> StudioBundle.get(bundleKey).equals(i.getText())).findFirst()
        .orElseThrow(() -> new AssertionError("No menu item " + bundleKey));
  }

  private ContentElement selected() throws Exception {
    return FxTestSupport.onFx(() -> tree.getSelectionModel().getSelectedItem().getValue());
  }

  private ContentElement parentOf(ContentElement element) throws Exception {
    return FxTestSupport.onFx(() -> find(tree.getRoot(), element).getParent().getValue());
  }

  private void select(String type) throws Exception {
    FxTestSupport.onFx(() -> {
      TreeItem<ContentElement> found = find(tree.getRoot(), type);
      if (found == null) {
        throw new AssertionError("The fixture has no " + type);
      }
      tree.getSelectionModel().select(found);
    });
  }

  private static TreeItem<ContentElement> find(TreeItem<ContentElement> from, String type) {
    if (type.equals(from.getValue().getType())) {
      return from;
    }
    for (TreeItem<ContentElement> child : from.getChildren()) {
      TreeItem<ContentElement> found = find(child, type);
      if (found != null) {
        return found;
      }
    }
    return null;
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
    List<ContentElement> all = new ArrayList<>();
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
