package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentModule;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.contentmodel.fields.ClickEventRow;
import de.a12.studio.ui.editors.contentmodel.fields.ConditionsRow;
import de.a12.studio.ui.editors.contentmodel.fields.PairListRow;
import de.a12.studio.ui.editors.contentmodel.fields.ReferenceRow;
import de.a12.studio.ui.editors.contentmodel.fields.SettingRow;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preview.PreviewServer;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
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
 * The pickers and list editors of the property column on a Content Model that is bound to a real Document Model (the
 * Product model of the e-commerce fixtures): what they offer, what they store, and that what they store is what the
 * validators accept.
 */
class ContentModelEditorReferencesTest {

  private static final String P = "include_common_";
  private static final String VARIANTS = P + "group_082cd";
  private static final String NAME = P + "field_4217e";
  private static final String IS_ACTIVE = P + "field_64375";
  private static final String NUMBER_OF_VARIANTS = P + "field_12ee1";
  private static final String ADDITIONAL_IMAGES = P + "group_57fbb";
  private static final String GENERAL = P + "group_1094d";

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

  @Test
  void theFixtureIsBoundAndItsGroupReferencesAreOfferedByPath() throws Exception {
    assertEquals("Product_DM", model.getDocumentModelId());
    select("Group");
    ReferenceRow row = row("groupId", ReferenceRow.class);
    ComboBox<String> combo = FxTestSupport.field(row, "combo");

    assertFalse(combo.isDisabled());
    assertTrue(combo.getItems().contains(VARIANTS));
    assertEquals(new ContentProps(selected()).getString("groupId"), combo.getValue());
    assertTrue(combo.getConverter().toString(VARIANTS).endsWith("/Variants"), combo.getConverter().toString(VARIANTS));
  }

  @Test
  void pickingAGroupStoresItsId() throws Exception {
    select("Group");
    ContentElement group = selected();
    ComboBox<String> combo = FxTestSupport.field(row("groupId", ReferenceRow.class), "combo");

    FxTestSupport.onFx(() -> combo.setValue(ADDITIONAL_IMAGES));

    assertEquals(ADDITIONAL_IMAGES, new ContentProps(group).getString("groupId"));
  }

  @Test
  void aStoredIdThatIsNotAvailableStaysSelectedAndMarked() throws Exception {
    select("Group");
    ContentElement group = selected();
    new ContentProps(group).set("groupId", "gone_group");
    select("Paragraph");
    select("Group");
    ComboBox<String> combo = FxTestSupport.field(row("groupId", ReferenceRow.class), "combo");

    assertEquals("gone_group", combo.getValue(), "not silently dropped");
    assertTrue(combo.getConverter().toString("gone_group").contains("gone_group"));
    assertTrue(combo.getConverter().toString("gone_group").contains("("), "marked");
  }

  // ---- form elements ----

  @Test
  void aFormElementOffersOnlyWhatItCanShowAndBecomesValidOnceBound() throws Exception {
    ContentElement textLine = addToRoot("TextLine", ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    select("TextLine");
    ComboBox<String> combo = FxTestSupport.field(row("elementId", ReferenceRow.class), "combo");

    assertTrue(combo.getItems().contains(NAME));
    assertTrue(combo.getItems().contains(NUMBER_OF_VARIANTS), "a text line shows numbers too");
    assertFalse(combo.getItems().contains(IS_ACTIVE), "but not a boolean");
    FxTestSupport.onFx(() -> loaded.controller().refreshIssues());
    assertTrue(errors(textLine) > 0, "no element selected yet");

    FxTestSupport.onFx(() -> combo.setValue(NAME));
    FxTestSupport.onFx(() -> loaded.controller().refreshIssues());

    assertEquals(NAME, new ContentProps(textLine).getString("elementId"));
    assertEquals(0, errors(textLine));
  }

  @Test
  void theLocalizedTextsOfAFormElementAreAListPerLocale() throws Exception {
    ContentElement textLine = addToRoot("TextLine", ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    select("TextLine");
    PairListRow label = row("label", PairListRow.class);
    Button add = FxTestSupport.field(label, "add");

    FxTestSupport.onFx(add::fire);

    List<?> stored = (List<?>) new ContentProps(textLine).get("label");
    assertEquals(1, stored.size());
    Map<?, ?> entry = (Map<?, ?>) stored.get(0);
    assertEquals(model.getLocales().get(0).getCode(), entry.get("locale"));
    VBox entries = FxTestSupport.field(label, "entries");
    TextField text = (TextField) ((javafx.scene.layout.HBox) entries.getChildren().get(0)).getChildren().get(1);

    FxTestSupport.onFx(() -> text.setText("Product name"));
    assertEquals("Product name", ((Map<?, ?>) ((List<?>) new ContentProps(textLine).get("label")).get(0)).get("text"));

    Button remove = (Button) ((javafx.scene.layout.HBox) entries.getChildren().get(0)).getChildren().get(2);
    FxTestSupport.onFx(remove::fire);
    assertNull(new ContentProps(textLine).get("label"), "no entries: the key is removed, like SME");
  }

  // ---- conditions ----

  @Test
  void aConditionComparesAFieldWithAValueOfItsType() throws Exception {
    select("Conditional");
    ContentElement conditional = selected();
    ConditionsRow row = row("conditions", ConditionsRow.class);
    Button add = FxTestSupport.field(row, "add");
    int before = ((List<?>) new ContentProps(conditional).get("conditions")).size();

    FxTestSupport.onFx(add::fire);

    List<?> conditions = (List<?>) new ContentProps(conditional).get("conditions");
    assertEquals(before + 1, conditions.size());
    Map<?, ?> added = (Map<?, ?>) conditions.get(before);
    assertEquals("equal", added.get("operator"));
    assertFalse(added.containsKey("fieldId"), "no field chosen yet");

    VBox blocks = FxTestSupport.field(row, "conditions");
    VBox block = (VBox) blocks.getChildren().get(before);
    @SuppressWarnings("unchecked")
    ComboBox<String> field = (ComboBox<String>) ((javafx.scene.layout.HBox) block.getChildren().get(1)).getChildren().get(1);
    assertTrue(field.getItems().contains(NUMBER_OF_VARIANTS));
    FxTestSupport.onFx(() -> field.setValue(NUMBER_OF_VARIANTS));
    assertEquals(NUMBER_OF_VARIANTS, ((Map<?, ?>) ((List<?>) new ContentProps(conditional).get("conditions")).get(before)).get("fieldId"));

    // A number field takes a text input that is stored as a number.
    VBox rebuilt = (VBox) ((VBox) FxTestSupport.field(row, "conditions")).getChildren().get(before);
    TextField value = (TextField) ((javafx.scene.layout.HBox) rebuilt.getChildren().get(3)).getChildren().get(1);
    FxTestSupport.onFx(() -> value.setText("5"));
    assertEquals(5L, ((Map<?, ?>) ((List<?>) new ContentProps(conditional).get("conditions")).get(before)).get("value"));
    FxTestSupport.onFx(() -> value.setText("abc"));
    assertEquals(5L, ((Map<?, ?>) ((List<?>) new ContentProps(conditional).get("conditions")).get(before)).get("value"), "not a number: not stored");
  }

  // ---- add row action ----

  @Test
  void anAddRowActionIsOfferedRepeatedGroupsOnly() throws Exception {
    ContentElement button = addToRoot("Button", ContentElementLibrary.NAMESPACE);
    select("Button");
    ClickEventRow click = row("onClick", ClickEventRow.class);
    ComboBox<String> addNode = FxTestSupport.field(click, "addNode");

    FxTestSupport.onFx(() -> addNode.setValue("AddRowAction"));

    ComboBox<String> groups = FxTestSupport.field(click, "groupPicker");
    assertTrue(groups.getItems().contains(ADDITIONAL_IMAGES));
    assertTrue(groups.getItems().contains(VARIANTS));
    assertFalse(groups.getItems().contains(GENERAL), "General occurs once");

    FxTestSupport.onFx(() -> groups.setValue(ADDITIONAL_IMAGES));

    Map<?, ?> node = (Map<?, ?>) new ContentProps(button).get("onClick");
    assertEquals("AddRowAction", node.get("type"));
    assertEquals(ADDITIONAL_IMAGES, ((Map<?, ?>) node.get("props")).get("groupId"));
    FxTestSupport.onFx(() -> loaded.controller().refreshIssues());
    assertEquals(0, errors(button), "the group is valid for an add row action");
  }

  // ---- helpers ----

  private int errors(ContentElement element) {
    return (int) loaded.controller().issuesOf(element).stream().filter(issue -> Severity.ERROR.name().equals(issue.severity())).count();
  }

  private ContentElement addToRoot(String type, String namespace) throws Exception {
    ContentElement root = model.getContent().getRoot();
    ContentModule module = ContentElementLibrary.find(namespace, type).orElseThrow();
    FxTestSupport.onFx(() -> loaded.controller().addChild(root, module));
    return root.getChildren().get(root.getChildren().size() - 1);
  }

  /** The visible row of type {@code kind} bound to {@code path} in the selected element's panels. */
  private <T extends SettingRow> T row(String path, Class<T> kind) throws Exception {
    List<T> found = FxTestSupport.onFx(() -> {
      List<T> rows = new ArrayList<>();
      collect(settingsBox, path, kind, rows);
      return rows;
    });
    assertEquals(1, found.size(), "one visible " + kind.getSimpleName() + " for " + path + ": " + found);
    return found.get(0);
  }

  private static <T extends SettingRow> void collect(Node node, String path, Class<T> kind, List<T> result) {
    if (!node.isVisible() || !node.isManaged()) {
      return;
    }
    if (kind.isInstance(node) && path.equals(((SettingRow) node).getPath())) {
      result.add(kind.cast(node));
    }
    if (node instanceof javafx.scene.control.TitledPane pane && pane.getContent() != null) {
      // Without a scene the pane has no skin yet, so its content is not among its children.
      collect(pane.getContent(), path, kind, result);
    }
    else if (node instanceof Parent parent) {
      parent.getChildrenUnmodifiable().forEach(child -> collect(child, path, kind, result));
    }
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
