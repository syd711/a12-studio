package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentModule;
import de.a12.studio.models.contentmodel.ContentNodes;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.models.contentmodel.LexicalText;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.contentmodel.fields.CollectedRow;
import de.a12.studio.ui.editors.contentmodel.fields.ListRow;
import de.a12.studio.ui.editors.contentmodel.fields.NumberRow;
import de.a12.studio.ui.editors.contentmodel.fields.ReferenceRow;
import de.a12.studio.ui.editors.contentmodel.fields.SettingRow;
import de.a12.studio.ui.editors.contentmodel.fields.SwitchRow;
import de.a12.studio.ui.editors.contentmodel.fields.LexicalTextRow;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preview.PreviewServer;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
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
 * The editors that were the leftovers of the property column: the Image's attachment group picker, the Date Picker
 * Config, the lists and automatic collection of the Message Group Container, and the references inside a text. On a
 * Content Model bound to the real Product Document Model of the e-commerce fixtures.
 */
class ContentModelEditorLeftoversTest {

  private static final String P = "include_common_";
  private static final String VARIANTS = P + "group_082cd";
  private static final String GENERAL = P + "group_1094d";
  private static final String MAIN_IMAGE = P + "group_8837c";
  private static final String NAME = P + "field_4217e";
  private static final String VARIANT_NAME = P + "field_df210";
  private static final String ADVERTISING_DATE = P + "field_7e6fe";

  @TempDir
  Path workspace;

  private ProjectItem item;
  private FxTestSupport.Loaded<ContentModelEditorController> loaded;
  private ContentModel model;
  private TreeView<ContentElement> tree;
  private VBox settingsBox;

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
    Path models = Files.createDirectories(workspace.resolve("models"));
    Path fixtures = locate().resolve("e-commerce").resolve("models");
    for (String name : List.of("CommonTypes_TDM.json", "01_Products/Product_DM.json", "01_Products/Product_Common_DM.json")) {
      Files.copy(fixtures.resolve(name), models.resolve(Path.of(name).getFileName().toString()));
    }
    Path file = models.resolve("Page_CM.json");
    Files.copy(fixtures.resolve("01_Products").resolve("Product_OfBundle_CM.json"), file);
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
    settingsBox = FxTestSupport.field(loaded.controller(), "settingsBox");
  }

  @AfterEach
  void closeEditor() throws Exception {
    if (loaded != null) {
      FxTestSupport.onFx(() -> loaded.controller().modelClosed(new ModelClosedEvent(item)));
      StudioEventManager.getInstance().removeListener(loaded.controller());
    }
  }

  // ---- image ----

  @Test
  void anImagesDynamicSourceIsPickedFromTheAttachmentGroups() throws Exception {
    ContentElement image = add(model.getContent().getRoot(), "Image", ContentElementLibrary.NAMESPACE);
    selectElement(image);
    ComboBox<String> combo = FxTestSupport.field(row("src.dynamic", ReferenceRow.class), "combo");

    assertTrue(combo.getItems().contains(MAIN_IMAGE), "an attachment group");
    assertFalse(combo.getItems().contains(VARIANTS), "a repeated group that is no attachment");
    assertFalse(combo.getItems().contains(GENERAL));

    FxTestSupport.onFx(() -> combo.setValue(MAIN_IMAGE));

    assertEquals(MAIN_IMAGE, new ContentProps(image).getString("src.dynamic"));
    FxTestSupport.onFx(() -> loaded.controller().refreshIssues());
    assertEquals(0, errors(image), loaded.controller().issuesOf(image).toString());
  }

  // ---- date picker ----

  @Test
  void theDatePickerConfigShowsForADateFieldOnlyAndFollowsThePickedElement() throws Exception {
    ContentElement picker = add(model.getContent().getRoot(), "DatePicker", ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    selectElement(picker);
    assertEquals(0, visibleRows("datePickerConfig.minYear", NumberRow.class).size(), "no element picked");

    ComboBox<String> elementCombo = FxTestSupport.field(row("elementId", ReferenceRow.class), "combo");
    FxTestSupport.onFx(() -> elementCombo.setValue(NAME));
    assertEquals(0, visibleRows("datePickerConfig.minYear", NumberRow.class).size(), "a text field has no date range");

    FxTestSupport.onFx(() -> elementCombo.setValue(ADVERTISING_DATE));
    assertEquals(1, visibleRows("datePickerConfig.minYear", NumberRow.class).size(), "the panel follows the picked element");
    assertEquals(1, visibleRows("datePickerConfig.preselectionYear", NumberRow.class).size());
  }

  @Test
  void theDatePickerConfigIsStoredAsNumbersAndAFlagAndRemovedWhenEmpty() throws Exception {
    ContentElement picker = add(model.getContent().getRoot(), "DatePicker", ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    new ContentProps(picker).set("elementId", ADVERTISING_DATE);
    selectElement(picker);
    TextField min = FxTestSupport.field(row("datePickerConfig.minYear", NumberRow.class), "input");
    TextField max = FxTestSupport.field(row("datePickerConfig.maxYear", NumberRow.class), "input");
    TextField preselection = FxTestSupport.field(row("datePickerConfig.preselectionYear", NumberRow.class), "input");
    CheckBox absolute = FxTestSupport.field(row("datePickerConfig.absolute", SwitchRow.class), "checkBox");

    FxTestSupport.onFx(() -> {
      min.setText("-10");
      max.setText("5");
      preselection.setText("0");
      absolute.setSelected(true);
    });

    assertEquals(Map.of("minYear", -10L, "maxYear", 5L, "preselectionYear", 0L, "absolute", true),
        new ContentProps(picker).get("datePickerConfig"));

    FxTestSupport.onFx(() -> max.setText("abc"));
    assertEquals(5L, new ContentProps(picker).get("datePickerConfig.maxYear"), "not a number: the value stays");
    assertTrue(max.getStyleClass().contains("content-setting-invalid"));

    FxTestSupport.onFx(() -> {
      min.setText("");
      max.setText("");
      preselection.setText("");
      absolute.setSelected(false);
    });
    assertNull(new ContentProps(picker).get("datePickerConfig"), "nothing left: the whole object goes");
  }

  // ---- message group container ----

  @Test
  void aMessageGroupContainerListsFieldsAndGroupsPickedFromTheDocumentModel() throws Exception {
    ContentElement container = add(model.getContent().getRoot(), "MessageGroupContainer", ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    add(container, "MessageGroupDisplay", ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    selectElement(container);
    ListRow fields = row("fields", ListRow.class);
    Button addField = FxTestSupport.field(fields, "add");

    FxTestSupport.onFx(addField::fire);
    assertEquals(List.of(""), new ContentProps(container).get("fields"));
    FxTestSupport.onFx(() -> loaded.controller().refreshIssues());
    assertTrue(errors(container) > 0, "an empty entry is not a field");

    ComboBox<String> combo = entryCombo(fields, 0);
    assertTrue(combo.getItems().contains(NAME));
    assertTrue(combo.getItems().contains(VARIANT_NAME), "fields inside repeated groups are listed too");
    assertFalse(combo.getItems().contains(GENERAL), "a group is no field");
    FxTestSupport.onFx(() -> combo.setValue(VARIANT_NAME));
    assertEquals(List.of(VARIANT_NAME), new ContentProps(container).get("fields"));
    FxTestSupport.onFx(() -> loaded.controller().refreshIssues());
    assertEquals(0, errors(container), loaded.controller().issuesOf(container).toString());

    ListRow groups = row("groups", ListRow.class);
    Button addGroup = FxTestSupport.field(groups, "add");
    FxTestSupport.onFx(addGroup::fire);
    ComboBox<String> groupCombo = entryCombo(groups, 0);
    assertTrue(groupCombo.getItems().contains(GENERAL));
    assertFalse(groupCombo.getItems().contains(NAME));
    FxTestSupport.onFx(() -> groupCombo.setValue(GENERAL));
    assertEquals(List.of(GENERAL), new ContentProps(container).get("groups"));

    VBox entries = FxTestSupport.field(fields, "entries");
    Button removeField = (Button) ((HBox) entries.getChildren().get(0)).getChildren().get(1);
    FxTestSupport.onFx(removeField::fire);
    assertEquals(List.of(), new ContentProps(container).get("fields"), "the list stays, empty, like SME creates it");
  }

  @Test
  void theRulesOfAMessageGroupContainerAreFreeText() throws Exception {
    ContentElement container = add(model.getContent().getRoot(), "MessageGroupContainer", ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    selectElement(container);
    ListRow rules = row("rules", ListRow.class);

    Button addRule = FxTestSupport.field(rules, "add");
    FxTestSupport.onFx(addRule::fire);
    VBox ruleEntries = FxTestSupport.field(rules, "entries");
    TextField text = (TextField) ((HBox) ruleEntries.getChildren().get(0)).getChildren().get(0);
    FxTestSupport.onFx(() -> text.setText("SomeRule"));

    assertEquals(List.of("SomeRule"), new ContentProps(container).get("rules"));
  }

  @Test
  void automaticCollectionShowsTheFieldsAndGroupsOfTheFormElementsInside() throws Exception {
    ContentElement container = add(model.getContent().getRoot(), "MessageGroupContainer", ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    ContentElement textLine = add(container, "TextLine", ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    new ContentProps(textLine).set("elementId", NAME);
    ContentElement nested = add(container, "MessageGroupContainer", ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    ContentElement hidden = add(nested, "TextLine", ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    new ContentProps(hidden).set("elementId", VARIANT_NAME);
    selectElement(container);

    assertEquals(0, collectedRows().size(), "not collecting yet: the lists are hidden");
    CheckBox collect = FxTestSupport.field(row("autoCollectNodes", SwitchRow.class), "checkBox");
    FxTestSupport.onFx(() -> collect.setSelected(true));

    assertEquals(Boolean.TRUE, new ContentProps(container).get("autoCollectNodes"));
    List<CollectedRow> rows = collectedRows();
    assertEquals(2, rows.size());
    List<String> fieldLines = lines(rows.stream().filter(row -> !row.isGroups()).findFirst().orElseThrow());
    assertEquals(1, fieldLines.size(), fieldLines.toString());
    assertTrue(fieldLines.get(0).endsWith("/Name"), fieldLines.get(0));
    assertTrue(fieldLines.stream().noneMatch(line -> line.contains("VariantName")), "a nested container collects for itself");
  }

  // ---- references inside a text ----

  @Test
  void aFieldReferenceIsInsertedIntoTheTextAndItsDisplayCanBeSet() throws Exception {
    ContentElement paragraph = add(model.getContent().getRoot(), "Paragraph", ContentElementLibrary.NAMESPACE);
    selectElement(paragraph);
    LexicalTextRow row = row(null, LexicalTextRow.class);
    TextArea input = FxTestSupport.field(row, "input");
    ComboBox<ContentReferences.Choice> insertField = FxTestSupport.field(row, "insertField");
    FxTestSupport.onFx(() -> input.setText("Hello "));

    ContentReferences.Choice name = insertField.getItems().stream().filter(choice -> NAME.equals(choice.id())).findFirst().orElseThrow();
    FxTestSupport.onFx(() -> insertField.setValue(name));

    assertEquals(List.of(NAME), ContentNodes.lexicalReferences(paragraph).stream().map(ContentNodes.LexicalReference::fieldId).toList());
    assertEquals("Hello " + LexicalText.referenceLabel(name.label(), false), LexicalText.getText(paragraph));
    assertEquals(LexicalText.getText(paragraph), input.getText(), "the text area shows the label");
    assertNull(insertField.getValue(), "ready for the next one");
    assertEquals(input.getLength(), input.getCaretPosition(), "the caret is behind the inserted reference");
    FxTestSupport.onFx(() -> loaded.controller().refreshIssues());
    assertEquals(0, errors(paragraph), loaded.controller().issuesOf(paragraph).toString());

    VBox options = FxTestSupport.field(row, "referenceOptions");
    assertEquals(1, options.getChildren().size());
    HBox line = (HBox) ((VBox) options.getChildren().get(0)).getChildren().get(1);
    @SuppressWarnings("unchecked")
    ComboBox<String> display = (ComboBox<String>) line.getChildren().get(0);
    TextField missing = (TextField) line.getChildren().get(1);
    FxTestSupport.onFx(() -> {
      display.setValue(LexicalText.LABEL_VALUE);
      missing.setText("n/a");
    });
    assertEquals(new LexicalText.ReferenceOptions(LexicalText.referenceLabel(name.label(), false), "label-value", "n/a"),
        LexicalText.fieldReferenceOptions(paragraph).get(0));
    assertTrue(((String) paragraph.getProps().get("html")).contains("data-ce-field-ref-display-option=\"label-value\""));

    // Deleting the label removes the reference and its options.
    FxTestSupport.onFx(() -> input.setText("Hello "));
    assertEquals(List.of(), ContentNodes.lexicalReferences(paragraph));
    assertEquals(0, options.getChildren().size());
  }

  @Test
  void aGroupReferenceIsOfferedInsideARepeatableGroupOnly() throws Exception {
    ContentElement paragraph = add(model.getContent().getRoot(), "Paragraph", ContentElementLibrary.NAMESPACE);
    selectElement(paragraph);
    ComboBox<ContentReferences.Choice> outside = FxTestSupport.field(row(null, LexicalTextRow.class), "insertGroup");
    assertTrue(outside.isDisabled(), "no repeated group around the text");

    ContentElement group = add(model.getContent().getRoot(), "Group", ContentElementLibrary.NAMESPACE);
    new ContentProps(group).set("groupId", VARIANTS);
    ContentElement inside = add(group, "Paragraph", ContentElementLibrary.NAMESPACE);
    selectElement(inside);
    LexicalTextRow row = row(null, LexicalTextRow.class);
    ComboBox<ContentReferences.Choice> insertGroup = FxTestSupport.field(row, "insertGroup");

    assertFalse(insertGroup.isDisabled());
    assertEquals(List.of(VARIANTS), insertGroup.getItems().stream().map(ContentReferences.Choice::id).toList());
    FxTestSupport.onFx(() -> insertGroup.setValue(insertGroup.getItems().get(0)));

    List<ContentNodes.LexicalReference> references = ContentNodes.lexicalReferences(inside);
    assertEquals(1, references.size());
    assertTrue(references.get(0).group());
    assertTrue(LexicalText.getText(inside).endsWith("IndexOf(/Product/Common/Common/Variants)"), LexicalText.getText(inside));
    FxTestSupport.onFx(() -> loaded.controller().refreshIssues());
    assertEquals(0, errors(inside), loaded.controller().issuesOf(inside).toString());
    assertTrue(((TextArea) FxTestSupport.field(row, "input")).isEditable());
    assertTrue(FxTestSupport.<Label>field(row, "notEditableHint").isManaged() == false);
  }

  // ---- helpers ----

  private ComboBox<String> entryCombo(ListRow row, int index) throws Exception {
    return FxTestSupport.onFx(() -> {
      VBox entries = FxTestSupport.field(row, "entries");
      @SuppressWarnings("unchecked")
      ComboBox<String> combo = (ComboBox<String>) ((HBox) entries.getChildren().get(index)).getChildren().get(0);
      return combo;
    });
  }

  private List<CollectedRow> collectedRows() throws Exception {
    return FxTestSupport.onFx(() -> {
      List<CollectedRow> rows = new ArrayList<>();
      collect(settingsBox, null, CollectedRow.class, rows);
      return rows;
    });
  }

  private List<String> lines(CollectedRow row) throws Exception {
    return FxTestSupport.onFx(() -> {
      VBox lines = FxTestSupport.field(row, "lines");
      return lines.getChildren().stream().map(node -> ((Label) node).getText()).toList();
    });
  }

  private int errors(ContentElement element) {
    return (int) loaded.controller().issuesOf(element).stream().filter(issue -> Severity.ERROR.name().equals(issue.severity())).count();
  }

  private ContentElement add(ContentElement parent, String type, String namespace) throws Exception {
    ContentModule module = ContentElementLibrary.find(namespace, type).orElseThrow();
    FxTestSupport.onFx(() -> loaded.controller().addChild(parent, module));
    return parent.getChildren().get(parent.getChildren().size() - 1);
  }

  private <T extends SettingRow> List<T> visibleRows(String path, Class<T> kind) throws Exception {
    return FxTestSupport.onFx(() -> {
      List<T> rows = new ArrayList<>();
      collect(settingsBox, path, kind, rows);
      return rows;
    });
  }

  /** The one visible row of type {@code kind} bound to {@code path} (any path when null) in the selected element's panels. */
  private <T extends SettingRow> T row(String path, Class<T> kind) throws Exception {
    List<T> found = visibleRows(path, kind);
    assertEquals(1, found.size(), "one visible " + kind.getSimpleName() + " for " + path + ": " + found);
    return found.get(0);
  }

  private static <T extends SettingRow> void collect(Node node, String path, Class<T> kind, List<T> result) {
    if (!node.isVisible() || !node.isManaged()) {
      return;
    }
    if (kind.isInstance(node) && (path == null || path.equals(((SettingRow) node).getPath()))) {
      result.add(kind.cast(node));
    }
    if (node instanceof TitledPane pane && pane.getContent() != null) {
      // Without a scene the pane has no skin yet, so its content is not among its children.
      collect(pane.getContent(), path, kind, result);
    }
    else if (node instanceof Parent parent) {
      parent.getChildrenUnmodifiable().forEach(child -> collect(child, path, kind, result));
    }
  }

  private void selectElement(ContentElement element) throws Exception {
    FxTestSupport.onFx(() -> {
      TreeItem<ContentElement> found = find(tree.getRoot(), element);
      if (found == null) {
        throw new AssertionError("Not in the tree: " + element.getType());
      }
      tree.getSelectionModel().clearSelection();
      tree.getSelectionModel().select(found);
    });
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

  private static void setStatic(String name, Object value) throws Exception {
    Field field = Studio.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }

  private static Path locate() throws IOException {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate testing/workspaces");
  }
}
