package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DependentCase;
import de.a12.studio.models.formmodel.DependentConfig;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FieldConfiguration;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// The Dependencies tab shared by the Boolean, Confirm and Enumeration control editors, against real FXML on the
// JavaFX thread: which sections it shows, what its trees offer, and the SME wire shape it writes.
class DependentControlsPanelTest {

  private static final String DIR = "/de/a12/studio/ui/editors/formmodel/formtree/nodeeditors/";

  private static final String DOCUMENT_MODEL = """
      {"header": {"id": "Company_DM", "modelType": "document", "modelVersion": "29.4.0"},
       "content": {"modelInfo": {"name": "Company_DM", "immutable": false},
         "modelConfig": {"timeZone": "UTC", "decimalSeparator": ".", "conditionLanguage": {"code": "en_US"}},
         "modelRoot": {"rootGroups": [
         {"type": "Group", "id": "g_root", "name": "Company", "Group": {"repeatability": 1, "elements": [
           {"type": "Field", "id": "field_title", "name": "Title", "Field": {"fieldType": {"type": "StringType"}}},
           {"type": "Field", "id": "field_flag", "name": "Flag", "Field": {"fieldType": {"type": "BooleanType"}}},
           {"type": "Field", "id": "field_ok", "name": "Ok", "Field": {"fieldType": {"type": "ConfirmType"}}},
           {"type": "Field", "id": "field_status", "name": "Status", "Field": {"fieldType": {"type": "EnumerationType",
             "EnumerationType": {"values": [{"value": "a"}, {"value": "b"}]}}}}
         ]}}
       ]}}}
      """;

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  private static ElementIndex index() {
    return new ElementIndex(JsonSettings.objectMapper.readValue(DOCUMENT_MODEL, DocumentModel.class));
  }

  private static Control control(String id, String elementRef) {
    Control control = new Control();
    control.setId(id);
    control.setElementRef(elementRef);
    return control;
  }

  private static ControlGrid grid(String id, Control... controls) {
    ControlGrid grid = new ControlGrid();
    grid.setId(id);
    grid.setName(id);
    Row row = new Row();
    for (Control control : controls) {
      row.getCell().add((Cell) control);
    }
    grid.getRow().add(row);
    return grid;
  }

  /** Screen "s1": section_top { grid_master { master } } and two more grids beside it. */
  private static FormModelContent content(Control master) {
    Section top = new Section();
    top.setId("section_top");
    top.setName("section_top");
    top.getScreenElements().add(grid("grid_master", master));
    Screen screen = new Screen();
    screen.setId("s1");
    screen.setName("Screen1");
    screen.getScreenElements().add(top);
    screen.getScreenElements().add(grid("grid_a", control("c_a", "field_title")));
    screen.getScreenElements().add(grid("grid_b", control("c_b", "field_title")));
    FormModelContent content = new FormModelContent();
    content.getScreens().add(screen);
    return content;
  }

  private static DependentControlsPanelController load() throws Exception {
    return FxTestSupport.<DependentControlsPanelController>load(DIR + "dependent-controls-panel.fxml").controller();
  }

  private static List<TitledPane> sections(DependentControlsPanelController panel) throws Exception {
    VBox sections = FxTestSupport.field(panel, "sections");
    return sections.getChildren().stream().map(TitledPane.class::cast).toList();
  }

  @SuppressWarnings("unchecked")
  private static TreeView<DependentControlsPanelController.TreeNode> tree(TitledPane section) {
    return (TreeView<DependentControlsPanelController.TreeNode>) section.getContent();
  }

  private static List<CheckBoxTreeItem<DependentControlsPanelController.TreeNode>> items(TitledPane section) {
    List<CheckBoxTreeItem<DependentControlsPanelController.TreeNode>> items = new ArrayList<>();
    collect(tree(section).getRoot(), items);
    return items;
  }

  @SuppressWarnings("unchecked")
  private static void collect(TreeItem<DependentControlsPanelController.TreeNode> item,
      List<CheckBoxTreeItem<DependentControlsPanelController.TreeNode>> items) {
    if (item instanceof CheckBoxTreeItem<DependentControlsPanelController.TreeNode> checkBoxItem && item.getValue() != null
        && item.getValue().id() != null) {
      items.add(checkBoxItem);
    }
    item.getChildren().forEach(child -> collect(child, items));
  }

  private static CheckBoxTreeItem<DependentControlsPanelController.TreeNode> item(TitledPane section, String id) {
    return items(section).stream().filter(i -> id.equals(i.getValue().id())).findFirst().orElseThrow();
  }

  private static List<String> titles(List<TitledPane> sections) {
    return sections.stream().map(TitledPane::getText).toList();
  }

  @Test
  void anEnumerationMasterGetsNoValueFirstThenOneSectionPerDeclaredValue() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Control master = control("c_master", "field_status");
    DependentControlsPanelController panel = load();

    FxTestSupport.onFx(() -> panel.setControl(master, content(master), index()));

    List<TitledPane> sections = sections(panel);
    assertEquals(3, sections.size());
    assertEquals(List.of("a", "b"), titles(sections).subList(1, 3));
    // Only whole blocks are offered, never the master's own grid or the section around it.
    List<String> offered = items(sections.get(0)).stream()
        .filter(i -> i.getValue().candidate()).map(i -> i.getValue().id()).toList();
    assertEquals(List.of("grid_a", "grid_b"), offered);
    // The section around the master is not a candidate but stays out of the way: it has no candidate below it either.
    assertTrue(items(sections.get(0)).stream().noneMatch(i -> "section_top".equals(i.getValue().id())));
  }

  @Test
  void aBooleanMasterHasFalseAndTrueAndAConfirmMasterOnlyTrue() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Control flag = control("c_flag", "field_flag");
    Control ok = control("c_ok", "field_ok");
    DependentControlsPanelController panel = load();

    FxTestSupport.onFx(() -> panel.setControl(flag, content(flag), index()));
    assertEquals(List.of("false", "true"), titles(sections(panel)).subList(1, 3));

    FxTestSupport.onFx(() -> panel.setControl(ok, content(ok), index()));
    assertEquals(List.of("true"), titles(sections(panel)).subList(1, 2));
    assertEquals(2, sections(panel).size());
  }

  @Test
  void aFieldThatCannotBeAMasterShowsNothing() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Control title = control("c_title", "field_title");
    DependentControlsPanelController panel = load();

    FxTestSupport.onFx(() -> panel.setControl(title, content(title), index()));

    assertEquals(0, sections(panel).size());
  }

  @Test
  void checkingBlocksWritesTheSmeWireShapeAndUncheckingTheLastOneRemovesTheBlock() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Control master = control("c_master", "field_status");
    FormModelContent content = content(master);
    DependentControlsPanelController panel = load();
    FxTestSupport.onFx(() -> panel.setControl(master, content, index()));
    List<TitledPane> sections = sections(panel);

    FxTestSupport.onFx(() -> item(sections.get(1), "grid_a").setSelected(true));
    FxTestSupport.onFx(() -> item(sections.get(0), "grid_b").setSelected(true));

    List<Control.DependentControls.Entry> entries = master.getDependentControls().getScreenElement();
    assertEquals(2, entries.size());
    // "(no value)" is masterValue null, and comes first like SME's map order.
    assertEquals("grid_b", entries.get(0).getIdref());
    assertNull(entries.get(0).getMasterValue());
    assertEquals("grid_a", entries.get(1).getIdref());
    assertEquals("a", entries.get(1).getMasterValue());

    FxTestSupport.onFx(() -> item(sections.get(1), "grid_a").setSelected(false));
    assertEquals(1, master.getDependentControls().getScreenElement().size());
    FxTestSupport.onFx(() -> item(sections.get(0), "grid_b").setSelected(false));
    assertNull(master.getDependentControls(), "an empty dependency is dropped, the validator would report it");
  }

  @Test
  void theStoredSelectionIsRestoredAndStaleEntriesAreWarnedAboutUntilTheNextChange() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Control master = control("c_master", "field_status");
    Control.DependentControls stored = new Control.DependentControls();
    stored.setScreenElement(new ArrayList<>(List.of(entry("grid_a", "b"), entry("grid_gone", "a"), entry("grid_b", "zz"))));
    master.setDependentControls(stored);
    DependentControlsPanelController panel = load();
    Label stale = FxTestSupport.field(panel, "staleLabel");

    FxTestSupport.onFx(() -> panel.setControl(master, content(master), index()));

    List<TitledPane> sections = sections(panel);
    assertTrue(item(sections.get(2), "grid_a").isSelected(), "restored under its master value");
    assertFalse(item(sections.get(1), "grid_a").isSelected());
    assertTrue(stale.isVisible());
    assertTrue(stale.getText().contains("grid_gone"), stale.getText());
    assertEquals(3, master.getDependentControls().getScreenElement().size(), "opening the tab never rewrites anything");

    FxTestSupport.onFx(() -> item(sections.get(0), "grid_b").setSelected(true));
    // Rewritten from what is checked: the entry for the deleted element and the one for the value "zz" are gone.
    assertEquals(List.of("grid_b:null", "grid_a:b"), master.getDependentControls().getScreenElement().stream()
        .map(e -> e.getIdref() + ":" + e.getMasterValue()).toList());
  }

  @Test
  void aConfirmDependencyKeptInTheLegacyNotRelevantNodesIsShownAndMovedOnTheFirstChange() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Control master = control("c_master", "field_ok");
    FormModelContent content = content(master);
    DependentCase trueCase = new DependentCase();
    trueCase.setMasterValue("true");
    trueCase.getNotRelevantNodes().add("grid_a");
    DependentConfig config = new DependentConfig();
    config.setMasterField("field_ok");
    config.getCases().add(trueCase);
    FieldConfigEntry entry = new FieldConfigEntry();
    entry.setElementRef("field_ok");
    entry.setDependentField(config);
    FieldConfiguration fieldConfiguration = new FieldConfiguration();
    fieldConfiguration.getField().add(entry);
    content.setFieldConfiguration(fieldConfiguration);
    DependentControlsPanelController panel = load();

    FxTestSupport.onFx(() -> panel.setControl(master, content, index()));

    List<TitledPane> sections = sections(panel);
    assertEquals(List.of("true"), titles(sections).subList(1, 2));
    assertTrue(item(sections.get(1), "grid_a").isSelected(), "the old shape is still shown");
    assertNull(master.getDependentControls(), "and left alone until the user changes something");

    FxTestSupport.onFx(() -> item(sections.get(0), "grid_b").setSelected(true));

    assertNotNull(master.getDependentControls());
    assertEquals(List.of("grid_b:null", "grid_a:true"), master.getDependentControls().getScreenElement().stream()
        .map(e -> e.getIdref() + ":" + e.getMasterValue()).toList());
    assertNull(entry.getDependentField(), "the legacy block this tab created is gone");
  }

  private static Control.DependentControls.Entry entry(String idref, String masterValue) {
    Control.DependentControls.Entry entry = new Control.DependentControls.Entry();
    entry.setIdref(idref);
    entry.setMasterValue(masterValue);
    return entry;
  }
}
