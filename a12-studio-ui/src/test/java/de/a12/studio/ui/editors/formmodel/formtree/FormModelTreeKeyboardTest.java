package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Form Model tree's keyboard shortcuts: the same actions as the toolbar buttons, fired by key events on the
 * tree, and each button's tooltip naming its shortcut. Uses SME's include example as the model (see {@code
 * FormIncludeExpanderTest}), copied to a temp folder since every tree action saves the model.
 */
class FormModelTreeKeyboardTest {

  private static boolean toolkitAvailable;

  @TempDir
  Path tempDir;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  private record Fixture(FormModelTreeController controller, TreeView<FormElementViewModel> tree, Screen screen) {
  }

  private Fixture load() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    File file = tempDir.resolve("HostModel_expanded.json").toFile();
    try (var in = FormModelTreeKeyboardTest.class.getResourceAsStream("/formincludes/HostModel_expanded.json")) {
      Files.copy(in, file.toPath());
    }
    ProjectItem item = new ProjectItem(file);
    FormModel model = (FormModel) item.getModel();

    FormModelTreeController controller = FxTestSupport.<FormModelTreeController>load(
        "/de/a12/studio/ui/editors/formmodel/formtree/form-model-tree-panel.fxml").controller();
    FxTestSupport.onFx(() -> controller.setModel(model, null, item));
    TreeView<FormElementViewModel> tree = FxTestSupport.field(controller, "tree");
    return new Fixture(controller, tree, model.getContent().getScreens().get(0));
  }

  private static void select(Fixture fixture, Object node) throws Exception {
    FxTestSupport.onFx(() -> {
      TreeItem<FormElementViewModel> item = find(fixture.tree().getRoot(), node);
      fixture.tree().getSelectionModel().select(item);
    });
  }

  private static TreeItem<FormElementViewModel> find(TreeItem<FormElementViewModel> item, Object node) {
    if (item.getValue() != null && item.getValue().getNode() == node) {
      return item;
    }
    for (TreeItem<FormElementViewModel> child : item.getChildren()) {
      TreeItem<FormElementViewModel> found = find(child, node);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private static void press(Fixture fixture, KeyCode code, boolean ctrl, boolean alt) throws Exception {
    FxTestSupport.onFx(() -> fixture.tree().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, ctrl, alt, false)));
  }

  private static List<ScreenElement> elementsOf(Fixture fixture) {
    return fixture.screen().getScreenElements();
  }

  @Test
  void everyToolbarTooltipNamesItsShortcut() throws Exception {
    Fixture fixture = load();

    assertTooltipEnds(fixture, "undoButton", "(Ctrl+Z)");
    assertTooltipEnds(fixture, "redoButton", "(Ctrl+Y)");
    assertTooltipEnds(fixture, "addButton", "(Insert)");
    assertTooltipEnds(fixture, "cutButton", "(Ctrl+X)");
    assertTooltipEnds(fixture, "copyButton", "(Ctrl+C)");
    assertTooltipEnds(fixture, "pasteButton", "(Ctrl+V)");
    assertTooltipEnds(fixture, "duplicateButton", "(Ctrl+Alt+D)");
    assertTooltipEnds(fixture, "moveUpButton", "(Alt+↑)");
    assertTooltipEnds(fixture, "moveDownButton", "(Alt+↓)");
    assertTooltipEnds(fixture, "deleteButton", "(Delete)");
    assertTooltipEnds(fixture, "expandAllButton", "(Ctrl++)");
    assertTooltipEnds(fixture, "collapseAllButton", "(Ctrl+-)");
  }

  private static void assertTooltipEnds(Fixture fixture, String buttonField, String shortcut) throws Exception {
    Object button = FxTestSupport.field(fixture.controller(), buttonField);
    String text = button instanceof MenuButton menuButton ? menuButton.getTooltip().getText() : ((Button) button).getTooltip().getText();
    assertTrue(text.endsWith(" " + shortcut), buttonField + " tooltip: " + text);
  }

  @Test
  void duplicateShortcutInsertsACopyRightAfterTheSelectedNode() throws Exception {
    Fixture fixture = load();
    ScreenElement selected = elementsOf(fixture).get(1);
    int before = elementsOf(fixture).size();
    select(fixture, selected);

    press(fixture, KeyCode.D, true, true);

    assertEquals(before + 1, elementsOf(fixture).size());
    assertSame(selected, elementsOf(fixture).get(1));
    assertEquals(selected.getClass(), elementsOf(fixture).get(2).getClass());
  }

  @Test
  void copyThenPasteShortcutPastesIntoTheSelectedContainer() throws Exception {
    Fixture fixture = load();
    ScreenElement copied = elementsOf(fixture).get(1);
    int before = elementsOf(fixture).size();

    select(fixture, copied);
    press(fixture, KeyCode.C, true, false);
    select(fixture, fixture.screen());
    press(fixture, KeyCode.V, true, false);

    assertEquals(before + 1, elementsOf(fixture).size());
  }

  @Test
  void cutShortcutRemovesTheNodeAndUndoRedoShortcutsRestoreAndRepeatIt() throws Exception {
    Fixture fixture = load();
    ScreenElement selected = elementsOf(fixture).get(1);
    int before = elementsOf(fixture).size();
    select(fixture, selected);

    press(fixture, KeyCode.X, true, false);
    assertEquals(before - 1, elementsOf(fixture).size());

    press(fixture, KeyCode.Z, true, false);
    assertEquals(before, elementsOf(fixture).size());
    assertSame(selected, elementsOf(fixture).get(1));

    press(fixture, KeyCode.Y, true, false);
    assertEquals(before - 1, elementsOf(fixture).size());
  }

  @Test
  void altArrowShortcutsMoveTheSelectedNode() throws Exception {
    Fixture fixture = load();
    ScreenElement selected = elementsOf(fixture).get(1);
    select(fixture, selected);

    press(fixture, KeyCode.UP, false, true);
    assertSame(selected, elementsOf(fixture).get(0));

    press(fixture, KeyCode.DOWN, false, true);
    assertSame(selected, elementsOf(fixture).get(1));
  }

  // Whether a key press survives the tree's own shortcut handling, i.e. would reach the global StudioKeyEventHandler
  // (fireEvent dispatches a copy, so the original event's consumed flag can't be used for that).
  private static boolean bubblesPast(Fixture fixture, KeyCode code, boolean shift, boolean ctrl) throws Exception {
    boolean[] reached = {false};
    FxTestSupport.onFx(() -> {
      fixture.tree().addEventHandler(KeyEvent.KEY_PRESSED, event -> reached[0] = true);
      fixture.tree().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, ctrl, false, false));
    });
    return reached[0];
  }

  @Test
  void aShortcutIsConsumedSoTheGlobalOnesNeverSeeIt() throws Exception {
    Fixture fixture = load();
    select(fixture, elementsOf(fixture).get(1));

    assertFalse(bubblesPast(fixture, KeyCode.Z, false, true));
  }

  @Test
  void shortcutsOfTheGlobalHandlerAreLeftAlone() throws Exception {
    Fixture fixture = load();

    // Ctrl+D (deploy model) and Ctrl+Shift+Z (revert model) belong to StudioKeyEventHandler.
    assertTrue(bubblesPast(fixture, KeyCode.D, false, true));
    assertTrue(bubblesPast(fixture, KeyCode.Z, true, true));
  }
}
