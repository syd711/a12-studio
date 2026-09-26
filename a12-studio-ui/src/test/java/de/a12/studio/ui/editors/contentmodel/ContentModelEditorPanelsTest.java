package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.models.contentmodel.ContentTableColumns;
import de.a12.studio.models.contentmodel.LexicalText;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.contentmodel.fields.LexicalTextRow;
import de.a12.studio.ui.editors.contentmodel.fields.SettingRow;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preview.PreviewServer;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The property column of the Content Model editor, driven through the real FXML and a real fixture: the panels shown
 * for a type, that merely selecting elements never changes the model, and that edits reach the props and the file.
 */
class ContentModelEditorPanelsTest {

  @TempDir
  Path workspace;

  private ProjectItem item;
  private FxTestSupport.Loaded<ContentModelEditorController> loaded;
  private ContentModel model;
  private VBox settingsBox;
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
    settingsBox = FxTestSupport.field(loaded.controller(), "settingsBox");
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
  void thePanelsFollowTheTypeOfTheSelectedElement() throws Exception {
    select("Box");
    List<String> box = visibleTitles();
    for (String key : List.of("element", "layout", "dimensions", "color", "background_image", "border", "shadow", "raw_props")) {
      assertTrue(box.contains(title(key)), "Box shows " + key + " but shows " + box);
    }
    assertFalse(box.contains(title("source")));
    assertFalse(box.contains(title("events")));

    select("Paragraph");
    List<String> paragraph = visibleTitles();
    assertTrue(paragraph.contains(title("dimensions")));
    assertFalse(paragraph.contains(title("layout")));
    assertFalse(paragraph.contains(title("color")));

    select("Table");
    List<String> table = visibleTitles();
    assertTrue(table.contains(title("row")));
    assertTrue(table.contains(title("accessibility")));
    assertTrue(table.contains(title("event")));

    select("MessageBox");
    List<String> messageBox = visibleTitles();
    assertTrue(messageBox.contains(title("content")));
    assertTrue(messageBox.contains(title("color")));
    assertTrue(messageBox.contains(title("accessibility")));
  }

  @Test
  void selectingElementsNeverChangesTheModel() throws Exception {
    String before = JsonSettings.objectMapper.writeValueAsString(model.getContent());
    for (ContentElement element : allElements(model.getContent().getRoot())) {
      FxTestSupport.onFx(() -> tree.getSelectionModel().select(find(tree.getRoot(), element)));
    }
    assertEquals(before, JsonSettings.objectMapper.writeValueAsString(model.getContent()));
  }

  @Test
  void aLengthEditWritesTheCssValueAndSavesTheFile() throws Exception {
    select("Box");
    SettingRow width = row("style.width");
    ComboBox<Object> unit = FxTestSupport.field(FxTestSupport.field(width, "editor"), "choices");
    TextField number = FxTestSupport.field(FxTestSupport.field(width, "editor"), "number");
    ContentProps props = new ContentProps(selected());

    assertEquals("1000px", props.getString("style.width"));
    FxTestSupport.onFx(() -> unit.setValue(choice(unit, "%")));
    assertEquals("100%", props.getString("style.width"));
    FxTestSupport.onFx(() -> number.setText("50"));
    assertEquals("50%", props.getString("style.width"));
    FxTestSupport.onFx(() -> unit.setValue(choice(unit, "px")));
    assertEquals("400px", props.getString("style.width"));
    FxTestSupport.onFx(() -> number.setText("250"));
    assertEquals("250px", props.getString("style.width"));
    FxTestSupport.onFx(() -> unit.setValue(choice(unit, "Auto")));
    assertEquals("auto", props.getString("style.width"));
    FxTestSupport.onFx(() -> unit.setValue(choice(unit, StudioBundle.get("content_settings.unspecified"))));
    assertNull(props.getString("style.width"));

    // The file is written once the edits pause.
    FxTestSupport.onFx(() -> unit.setValue(choice(unit, "%")));
    assertEquals("100%", props.getString("style.width"));
    FxTestSupport.onFx(() -> number.setText("77"));
    String saved = waitForFile("77%");
    assertTrue(saved.contains("77%"), saved);
  }

  @Test
  void theTextOfHeadingsAndParagraphsIsEditedInAPlainTextField() throws Exception {
    select("Heading");
    ContentElement heading = selected();
    TextInputControl headingField = FxTestSupport.field(textRow(), "input");
    assertTrue(headingField instanceof TextField, "a heading is a single line");
    String before = FxTestSupport.onFx(() -> headingField.getText());
    assertFalse(before.isBlank());
    assertTrue(visibleTitles().contains(title("content")));

    FxTestSupport.onFx(() -> headingField.setText("Brand new headline"));
    assertEquals("Brand new headline", LexicalText.getText(heading));
    assertTrue(String.valueOf(heading.getProps().get("html")).contains("Brand new headline"));
    assertFalse(String.valueOf(heading.getProps().get("html")).contains(before));
    String saved = waitForFile("Brand new headline");
    assertTrue(saved.contains("Brand new headline"), saved);

    select("Paragraph");
    ContentElement paragraph = selected();
    TextInputControl paragraphField = FxTestSupport.field(textRow(), "input");
    assertTrue(paragraphField instanceof TextArea, "a paragraph can span several lines");
    assertEquals(LexicalText.getText(paragraph), FxTestSupport.onFx(() -> paragraphField.getText()));
    FxTestSupport.onFx(() -> paragraphField.setText("First" + (char) 10 + "Second"));
    assertEquals("First" + (char) 10 + "Second", LexicalText.getText(paragraph));
  }

  @Test
  void selectingTextElementsAndTypingNothingLeavesTheirTreeUntouched() throws Exception {
    select("Paragraph");
    ContentElement paragraph = selected();
    String before = JsonSettings.objectMapper.writeValueAsString(paragraph.getProps());
    select("Heading");
    select("Paragraph");
    assertEquals(before, JsonSettings.objectMapper.writeValueAsString(paragraph.getProps()));
  }

  /** The visible text row of the currently selected element. */
  private LexicalTextRow textRow() throws Exception {
    return FxTestSupport.onFx(() -> {
      List<LexicalTextRow> found = new ArrayList<>();
      collectTextRows(settingsBox, found);
      if (found.isEmpty()) {
        throw new AssertionError("No visible text row");
      }
      return found.get(0);
    });
  }

  private static void collectTextRows(Node node, List<LexicalTextRow> found) {
    if (!node.isVisible() || !node.isManaged()) {
      return;
    }
    if (node instanceof LexicalTextRow row) {
      found.add(row);
    }
    else if (node instanceof TitledPane pane && pane.getContent() != null) {
      collectTextRows(pane.getContent(), found);
    }
    else if (node instanceof Parent parent) {
      parent.getChildrenUnmodifiable().forEach(child -> collectTextRows(child, found));
    }
  }

  @Test
  void aSwitchAndAToggleWriteTheirValues() throws Exception {
    select("Grid");
    ContentElement grid = selected();
    assertNull(grid.getProps().get("cellBorder"));

    javafx.scene.control.CheckBox cellBorder = FxTestSupport.field(row("cellBorder"), "checkBox");
    FxTestSupport.onFx(() -> cellBorder.setSelected(true));
    assertEquals(true, grid.getProps().get("cellBorder"));
    FxTestSupport.onFx(() -> cellBorder.setSelected(false));
    assertFalse(grid.getProps().containsKey("cellBorder"));

    select("Box");
    SettingRow direction = row("style.flexDirection");
    javafx.scene.control.ToggleGroup group = FxTestSupport.field(direction, "group");
    FxTestSupport.onFx(() -> group.selectToggle(group.getToggles().stream()
        .filter(toggle -> "row".equals(toggle.getUserData())).findFirst().orElseThrow()));
    assertEquals("row", new ContentProps(selected()).getString("style.flexDirection"));
  }

  @Test
  void mixedPaddingIsWrittenAsFourValueShorthand() throws Exception {
    select("Box");
    SettingRow padding = row("style.padding");
    ComboBox<Object> unit = FxTestSupport.field(FxTestSupport.field(padding, "simple"), "choices");

    FxTestSupport.onFx(() -> unit.setValue(choice(unit, "Mixed")));

    assertEquals("8px 8px 8px 8px", new ContentProps(selected()).getString("style.padding"));
    List<Object> sides = FxTestSupport.field(padding, "sides");
    TextField top = FxTestSupport.field(sides.get(0), "number");
    FxTestSupport.onFx(() -> top.setText("12"));
    assertEquals("12px 8px 8px 8px", new ContentProps(selected()).getString("style.padding"));
  }

  @Test
  void retypingAnElementAddsTheDefaultsOfTheNewTypeAndKeepsWhatIsThere() throws Exception {
    select("Box");
    ContentElement box = selected();
    ElementPanelController element = FxTestSupport.field(loaded.controller(), "elementPanelController");
    ComboBox<String> type = FxTestSupport.field(element, "elementTypeField");

    FxTestSupport.onFx(() -> type.setValue("Image"));

    assertEquals("Image", box.getType());
    assertEquals(Map.of("static", ""), box.getProps().get("src"));
    assertEquals("1000px", new ContentProps(box).getString("style.width"), "an existing value must survive retyping");
  }

  @Test
  void tableColumnOperationsKeepCellsAlignedAndRebuildTheTree() throws Exception {
    select("Table");
    ContentElement table = selected();
    TableColumnsPanelController columns = panel(TableColumnsPanelController.class);
    int before = ContentTableColumns.columns(table).size();

    FxTestSupport.onFx(() -> columns.deleteColumn(1));

    assertEquals(before - 1, ContentTableColumns.columns(table).size());
    for (ContentElement section : table.getChildren()) {
      for (ContentElement row : section.getChildren()) {
        assertEquals(before - 1, row.getChildren().size());
      }
    }
    // The tree below the table shows the new cells, not the deleted one.
    int treeCells = FxTestSupport.onFx(() -> find(tree.getRoot(), "TableHeadRow").getChildren().size());
    assertEquals(before - 1, treeCells);

    Object movedId = ContentTableColumns.columns(table).get(0).get("id");
    FxTestSupport.onFx(() -> columns.moveColumn(0, 1));
    assertEquals(movedId, ContentTableColumns.columns(table).get(1).get("id"));
  }

  @Test
  void gridRowResponsiveSettingsFollowSmesRules() throws Exception {
    select("GridRow");
    ContentElement row = selected();
    ResponsivePanelController responsive = panel(ResponsivePanelController.class);
    Map<String, TextField> fields = FxTestSupport.field(responsive, "rowFields");
    ContentProps props = new ContentProps(row);

    FxTestSupport.onFx(() -> {
      fields.get("offsets.lg").setText("1 0 1");
      fields.get("offsets.lg").getOnAction().handle(new javafx.event.ActionEvent());
    });
    assertEquals(List.of(1, 0, 1), props.get("layoutConfig.offsets.lg"));

    // 13 is not a column count: refused, nothing written.
    FxTestSupport.onFx(() -> {
      fields.get("spans.lg").setText("13");
      fields.get("spans.lg").getOnAction().handle(new javafx.event.ActionEvent());
    });
    assertNull(props.get("layoutConfig.spans"));

    // The large value is required while a smaller one is set.
    Object layoutBefore = props.get("layoutConfig.layout");
    FxTestSupport.onFx(() -> {
      fields.get("layout.lg").setText("");
      fields.get("layout.lg").getOnAction().handle(new javafx.event.ActionEvent());
    });
    assertEquals(layoutBefore, props.get("layoutConfig.layout"));
  }

  @Test
  void aColumnsWidthIsDisabledWhileItsRowHasAResponsiveLayout() throws Exception {
    select("GridColumn");
    ResponsivePanelController responsive = panel(ResponsivePanelController.class);
    Map<String, TextField> sizeFields = FxTestSupport.field(responsive, "sizeFields");

    assertTrue(FxTestSupport.onFx(() -> sizeFields.get("lg").isDisabled()));
  }

  @Test
  void mediaQueriesCanBeAddedAndDeleted() throws Exception {
    select("Box");
    ContentElement element = selected();
    ElementPanelController elementPanel = FxTestSupport.field(loaded.controller(), "elementPanelController");
    ComboBox<String> type = FxTestSupport.field(elementPanel, "elementTypeField");
    FxTestSupport.onFx(() -> type.setValue("MediaQuery"));
    MediaQueryPanelController mediaQuery = panel(MediaQueryPanelController.class);

    assertEquals(1, ((List<?>) element.getProps().get("queries")).size());
    FxTestSupport.onFx(mediaQuery::addQuery);
    assertEquals(2, ((List<?>) element.getProps().get("queries")).size());
    FxTestSupport.onFx(() -> mediaQuery.deleteQuery(0));
    assertEquals(1, ((List<?>) element.getProps().get("queries")).size());
    assertEquals("or", element.getProps().get("operator"));
  }

  @Test
  void aClickEventNameIsStoredAndRemovedWithNone() throws Exception {
    select("Box");
    ElementPanelController elementPanel = FxTestSupport.field(loaded.controller(), "elementPanelController");
    ComboBox<String> type = FxTestSupport.field(elementPanel, "elementTypeField");
    FxTestSupport.onFx(() -> type.setValue("Button"));
    ContentElement button = selected();
    SettingRow click = row("onClick");
    ToggleGroup kinds = FxTestSupport.field(click, "kinds");
    ToggleButton eventName = FxTestSupport.field(click, "eventName");
    ToggleButton none = FxTestSupport.field(click, "none");
    TextField name = FxTestSupport.field(click, "nameField");

    FxTestSupport.onFx(() -> kinds.selectToggle(eventName));
    FxTestSupport.onFx(() -> name.setText("save"));
    assertEquals("save", button.getProps().get("onClick"));

    FxTestSupport.onFx(() -> kinds.selectToggle(none));
    assertFalse(button.getProps().containsKey("onClick"));
  }

  @Test
  void duplicateCopyPasteAndCutKeepTreeAndModelInSyncWithFreshIds() throws Exception {
    select("Paragraph");
    ContentElement original = selected();
    ContentElement parent = FxTestSupport.onFx(() -> tree.getSelectionModel().getSelectedItem().getParent().getValue());
    int before = parent.getChildren().size();
    int index = parent.getChildren().indexOf(original);

    FxTestSupport.onFx(() -> loaded.controller().onDuplicate(null));
    ContentElement copy = selected();
    assertEquals(before + 1, parent.getChildren().size());
    assertEquals(index + 1, parent.getChildren().indexOf(copy), "the duplicate follows the original");
    assertEquals(original.getType(), copy.getType());
    assertFalse(original.getId().equals(copy.getId()), "the duplicate gets its own id");
    assertEquals(before + 1, FxTestSupport.onFx(() -> tree.getSelectionModel().getSelectedItem().getParent().getChildren().size()));

    // Copy the original, paste it into the selected element (as its last child) - the source stays put.
    select("Paragraph");
    FxTestSupport.onFx(() -> loaded.controller().onCopy(null));
    ContentElement target = parent;
    FxTestSupport.onFx(() -> tree.getSelectionModel().select(find(tree.getRoot(), target)));
    int targetChildren = target.getChildren().size();
    FxTestSupport.onFx(() -> loaded.controller().onPaste(null));
    assertEquals(targetChildren + 1, target.getChildren().size());
    ContentElement pasted = target.getChildren().get(target.getChildren().size() - 1);
    assertEquals(original.getType(), pasted.getType());
    assertFalse(original.getId().equals(pasted.getId()));
    assertTrue(parent.getChildren().contains(original));

    // Cut removes it from the model and the tree, and a paste brings it back with another id.
    FxTestSupport.onFx(() -> tree.getSelectionModel().select(find(tree.getRoot(), pasted)));
    FxTestSupport.onFx(() -> loaded.controller().onCut(null));
    assertFalse(target.getChildren().contains(pasted));
    assertNull(FxTestSupport.onFx(() -> find(tree.getRoot(), pasted)));
    FxTestSupport.onFx(() -> tree.getSelectionModel().select(find(tree.getRoot(), target)));
    FxTestSupport.onFx(() -> loaded.controller().onPaste(null));
    assertEquals(targetChildren + 1, target.getChildren().size());
    assertFalse(pasted.getId().equals(target.getChildren().get(target.getChildren().size() - 1).getId()));
  }

  @Test
  void theRootCanBeCopiedButNeitherCutDuplicatedNorDeleted() throws Exception {
    FxTestSupport.onFx(() -> tree.getSelectionModel().select(tree.getRoot()));
    ContentElement root = model.getContent().getRoot();
    int children = root.getChildren().size();
    FxTestSupport.onFx(() -> {
      loaded.controller().onCut(null);
      loaded.controller().onDuplicate(null);
      loaded.controller().onRemoveElement(null);
    });
    assertEquals(children, root.getChildren().size());
    assertTrue(FxTestSupport.onFx(() -> ((javafx.scene.control.Button) FxTestSupport.field(loaded.controller(), "deleteButton")).isDisabled()));
  }

  @Test
  void theTreeContextMenuHasAllToolbarActionsAndMirrorsTheirEnabledState() throws Exception {
    javafx.scene.control.ContextMenu menu = tree.getContextMenu();
    assertTrue(menu != null && menu.getItems().stream().filter(i -> !(i instanceof javafx.scene.control.SeparatorMenuItem)).count() == 10,
        "undo, redo, add, delete, up, down, cut, copy, paste and duplicate must all be offered");

    // The menu is only shown when it has items, so they must exist before it is first shown.
    FxTestSupport.onFx(() -> tree.getSelectionModel().select(tree.getRoot()));
    FxTestSupport.onFx(() -> javafx.event.Event.fireEvent(menu, new javafx.stage.WindowEvent(menu, javafx.stage.WindowEvent.WINDOW_SHOWING)));
    assertTrue(menuItem(menu, "content_model_tree.delete").isDisable(), "the root cannot be deleted");
    assertTrue(menuItem(menu, "content_model_tree.cut").isDisable());
    assertFalse(menuItem(menu, "content_model_tree.copy").isDisable());
    assertTrue(menuItem(menu, "undo").isDisable(), "nothing to undo yet");

    select("Paragraph");
    FxTestSupport.onFx(() -> javafx.event.Event.fireEvent(menu, new javafx.stage.WindowEvent(menu, javafx.stage.WindowEvent.WINDOW_SHOWING)));
    assertFalse(menuItem(menu, "content_model_tree.delete").isDisable());
    assertFalse(menuItem(menu, "content_model_tree.duplicate").isDisable());
  }

  @Test
  void structuralChangesUndoAndRedo() throws Exception {
    ContentElement root = model.getContent().getRoot();
    select("Paragraph");
    ContentElement paragraph = selected();
    ContentElement parent = FxTestSupport.onFx(() -> tree.getSelectionModel().getSelectedItem().getParent().getValue());
    List<ContentElement> originalOrder = new ArrayList<>(parent.getChildren());

    // Add child: undo removes it and restores the absent "children" key of a leaf, redo brings it back.
    boolean leafBefore = paragraph.getChildren() == null;
    FxTestSupport.onFx(() -> loaded.controller().onAddChild(null));
    assertEquals(1, paragraph.getChildren().size());
    ContentElement added = paragraph.getChildren().get(0);
    assertEquals(added, selected());
    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    assertTrue(paragraph.getChildren() == null || paragraph.getChildren().isEmpty());
    if (leafBefore) {
      assertNull(paragraph.getChildren());
    }
    assertEquals(paragraph, selected());
    FxTestSupport.onFx(() -> loaded.controller().onRedo(null));
    assertEquals(List.of(added), paragraph.getChildren());
    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));

    // Cut, then undo: the element is back at its old position.
    select("Paragraph");
    FxTestSupport.onFx(() -> loaded.controller().onCut(null));
    assertFalse(parent.getChildren().contains(paragraph));
    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    assertEquals(originalOrder, parent.getChildren());
    assertEquals(paragraph, selected());

    // Move down and undo.
    select("Paragraph");
    if (parent.getChildren().indexOf(paragraph) < parent.getChildren().size() - 1) {
      FxTestSupport.onFx(() -> loaded.controller().onMoveDown(null));
      assertFalse(originalOrder.equals(parent.getChildren()));
      FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
      assertEquals(originalOrder, parent.getChildren());
    }

    // Duplicate, undo, redo.
    FxTestSupport.onFx(() -> loaded.controller().onDuplicate(null));
    assertEquals(originalOrder.size() + 1, parent.getChildren().size());
    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    assertEquals(originalOrder, parent.getChildren());
    FxTestSupport.onFx(() -> loaded.controller().onRedo(null));
    assertEquals(originalOrder.size() + 1, parent.getChildren().size());
    assertEquals(root, model.getContent().getRoot());

    // The tree shows what the model holds after all of that.
    assertEquals(parent.getChildren(), FxTestSupport.onFx(() ->
        find(tree.getRoot(), parent).getChildren().stream().map(TreeItem::getValue).toList()));
  }

  @Test
  void aPanelEditUndoesAndRedoesAsOneStepPerRunOfEdits() throws Exception {
    select("Box");
    SettingRow width = row("style.width");
    TextField number = FxTestSupport.field(FxTestSupport.field(width, "editor"), "number");
    ContentElement box = selected();
    ContentProps props = new ContentProps(box);
    String before = props.getString("style.width");

    FxTestSupport.onFx(() -> number.setText("31"));
    FxTestSupport.onFx(() -> number.setText("32"));
    FxTestSupport.onFx(() -> number.setText("33"));
    String edited = props.getString("style.width");
    assertFalse(before.equals(edited), "the edit must have changed the width");

    FxTestSupport.onFx(() -> loaded.controller().onUndo(null));
    assertEquals(before, new ContentProps(box).getString("style.width"), "one undo reverts the whole run of typing");
    assertEquals(box, selected());
    FxTestSupport.onFx(() -> loaded.controller().onRedo(null));
    assertEquals(edited, new ContentProps(box).getString("style.width"));
  }

  // ------------------------------------------------------------------------------------------ helpers
  private static javafx.scene.control.MenuItem menuItem(javafx.scene.control.ContextMenu menu, String bundleKey) {
    return menu.getItems().stream().filter(i -> StudioBundle.get(bundleKey).equals(i.getText())).findFirst()
        .orElseThrow(() -> new AssertionError("No menu item " + bundleKey));
  }


  /** The controller of the given class among the editor's property panels. */
  private <T> T panel(Class<T> type) throws Exception {
    return FxTestSupport.onFx(() -> settingsBox.getChildren().stream()
        .filter(TitledPane.class::isInstance)
        .map(node -> node.getProperties().get(ContentSettingsPanel.PANEL_KEY))
        .filter(type::isInstance).map(type::cast).findFirst()
        .orElseThrow(() -> new AssertionError("No panel " + type.getSimpleName())));
  }

  private static String title(String key) {
    return StudioBundle.get("content_settings." + key);
  }

  private List<String> visibleTitles() throws Exception {
    return FxTestSupport.onFx(() -> settingsBox.getChildren().stream()
        .filter(TitledPane.class::isInstance).map(TitledPane.class::cast)
        .filter(pane -> pane.isVisible() && pane.isManaged())
        .map(TitledPane::getText).toList());
  }

  private ContentElement selected() throws Exception {
    return FxTestSupport.onFx(() -> tree.getSelectionModel().getSelectedItem().getValue());
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

  /** The visible row bound to {@code path} in the currently selected element's panels. */
  private SettingRow row(String path) throws Exception {
    List<SettingRow> rows = FxTestSupport.onFx(() -> {
      List<SettingRow> found = new ArrayList<>();
      collect(settingsBox, path, found);
      return found;
    });
    if (rows.isEmpty()) {
      throw new AssertionError("No visible row for " + path);
    }
    return rows.get(0);
  }

  private static void collect(Node node, String path, List<SettingRow> found) {
    if (!node.isVisible() || !node.isManaged()) {
      return;
    }
    if (node instanceof SettingRow row && path.equals(row.getPath())) {
      found.add(row);
      return;
    }
    if (node instanceof TitledPane pane && pane.getContent() != null) {
      // Without a scene the pane has no skin yet, so its content is not among its children.
      collect(pane.getContent(), path, found);
    }
    else if (node instanceof Parent parent) {
      parent.getChildrenUnmodifiable().forEach(child -> collect(child, path, found));
    }
  }

  /** The dropdown entry of a {@code LengthEditor} whose visible label is {@code label}. */
  private static Object choice(ComboBox<Object> combo, String label) {
    return combo.getItems().stream()
        .filter(candidate -> label.equals(combo.getConverter().toString(candidate)))
        .findFirst()
        .orElseThrow(() -> new AssertionError("No choice " + label + " in " + combo.getItems()));
  }

  private String waitForFile(String... expectedFragments) throws Exception {
    String content = "";
    for (int i = 0; i < 60; i++) {
      content = Files.readString(Path.of(item.getPath()));
      for (String fragment : expectedFragments) {
        if (content.contains(fragment)) {
          return content;
        }
      }
      Thread.sleep(100);
    }
    return content;
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
