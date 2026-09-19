package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.ControlIndex;
import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.editors.formmodel.MultiColumnSectionEditorPanelController;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// The panels for the small Form Model element gaps (initially focused element, Control index, custom element
// height, md/sm layout of a Multi-Column Section), against real FXML on the JavaFX thread.
class SmallElementGapPanelsTest {

  private static final String DIR = "/de/a12/studio/ui/editors/formmodel/formtree/nodeeditors/";

  private static final String DOCUMENT_MODEL = """
      {"header": {"id": "Company_DM", "modelType": "document", "modelVersion": "29.4.0"},
       "content": {"modelInfo": {"name": "Company_DM", "immutable": false},
         "modelConfig": {"timeZone": "UTC", "decimalSeparator": ".", "conditionLanguage": {"code": "en_US"}},
         "modelRoot": {"rootGroups": [
         {"type": "Group", "id": "g_root", "name": "Company", "Group": {"repeatability": 1, "elements": [
           {"type": "Field", "id": "field_title", "name": "Title", "Field": {"fieldType": {"type": "StringType"}}},
           {"type": "Group", "id": "g_items", "name": "Items", "Group": {"repeatability": 10, "elements": [
             {"type": "Field", "id": "field_name", "name": "Name", "Field": {"fieldType": {"type": "StringType"}}}
           ]}}
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

  private static Control control(String id, String elementRef, Boolean readonly) {
    Control control = new Control();
    control.setId(id);
    control.setElementRef(elementRef);
    control.setReadonly(readonly);
    return control;
  }

  private static ControlGrid grid(Control... controls) {
    ControlGrid grid = new ControlGrid();
    Row row = new Row();
    for (Control control : controls) {
      row.getCell().add((Cell) control);
    }
    grid.getRow().add(row);
    return grid;
  }

  private static Screen screen(String id, String focusedId, de.a12.studio.models.formmodel.ScreenElement... elements) {
    Screen screen = new Screen();
    screen.setId(id);
    screen.setName("Name_" + id);
    screen.setInitiallyFocusedElementId(focusedId);
    Section section = new Section();
    section.getScreenElements().addAll(List.of(elements));
    screen.getScreenElements().add(section);
    return screen;
  }

  // ---- initially focused element ----

  @Test
  void theFirstScreenOffersItsEditableControlsAndWritesTheChosenId() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Control title = control("c_title", "field_title", null);
    Control readonly = control("c_readonly", "field_name", true);
    EmbeddedRepeat repeat = new EmbeddedRepeat();
    repeat.setGroupRef("g_items");
    repeat.setControlGrid(grid(control("c_in_repeat", "field_name", null)));
    Screen first = screen("s1", null, grid(title, readonly), repeat);
    FormModelContent content = new FormModelContent();
    content.getScreens().add(first);
    InitiallyFocusedElementPanelController panel =
        FxTestSupport.<InitiallyFocusedElementPanelController>load(DIR + "initially-focused-element-panel.fxml").controller();
    ComboBox<String> combo = FxTestSupport.field(panel, "focusedElementField");
    TitledPane root = FxTestSupport.field(panel, "root");

    FxTestSupport.onFx(() -> panel.setScreen(first, content, index()));

    assertTrue(root.isVisible());
    assertEquals(List.of("c_title"), combo.getItems(), "no readonly Control, none from inside a repeat");
    assertEquals("Company/Title (c_title)", combo.getConverter().toString("c_title").replaceFirst("^/", ""));
    assertNull(combo.getValue());
    assertFalse(panel.errorProperty().get());

    FxTestSupport.onFx(() -> combo.setValue("c_title"));
    assertEquals("c_title", first.getInitiallyFocusedElementId());

    Button clear = FxTestSupport.field(panel, "clearButton");
    assertFalse(clear.isDisabled());
    FxTestSupport.onFx(clear::fire);
    assertNull(first.getInitiallyFocusedElementId(), "clearing writes no empty value");
    assertTrue(clear.isDisabled());
  }

  @Test
  void aLaterScreenHidesThePanelUnlessAStaleValueNeedsClearing() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Screen first = screen("s1", null, grid(control("c1", "field_title", null)));
    Screen second = screen("s2", null, grid(control("c2", "field_title", null)));
    Screen secondWithValue = screen("s3", "c3", grid(control("c3", "field_title", null)));
    FormModelContent content = new FormModelContent();
    content.getScreens().addAll(List.of(first, second, secondWithValue));
    InitiallyFocusedElementPanelController panel =
        FxTestSupport.<InitiallyFocusedElementPanelController>load(DIR + "initially-focused-element-panel.fxml").controller();
    TitledPane root = FxTestSupport.field(panel, "root");
    ComboBox<String> combo = FxTestSupport.field(panel, "focusedElementField");

    FxTestSupport.onFx(() -> panel.setScreen(second, content, index()));
    assertFalse(root.isVisible(), "SME hides the dropdown on every screen but the first");
    assertFalse(panel.errorProperty().get());

    FxTestSupport.onFx(() -> panel.setScreen(secondWithValue, content, index()));
    assertTrue(root.isVisible(), "a value that is there must stay visible so it can be cleared");
    assertEquals("c3", combo.getValue());
    assertTrue(panel.errorProperty().get(), "and it is reported: only the first screen may set it");
    assertEquals(List.of(), combo.getItems());

    FxTestSupport.onFx(() -> combo.setValue(null));
    assertNull(secondWithValue.getInitiallyFocusedElementId());
    assertFalse(panel.errorProperty().get());
  }

  @Test
  void aStaleFocusedIdOnTheFirstScreenIsReported() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Screen first = screen("s1", "gone", grid(control("c1", "field_title", null)));
    FormModelContent content = new FormModelContent();
    content.getScreens().add(first);
    InitiallyFocusedElementPanelController panel =
        FxTestSupport.<InitiallyFocusedElementPanelController>load(DIR + "initially-focused-element-panel.fxml").controller();

    FxTestSupport.onFx(() -> panel.setScreen(first, content, null));

    assertTrue(panel.errorProperty().get());
    assertEquals("gone", first.getInitiallyFocusedElementId(), "opening the panel never rewrites the value");
  }

  // ---- control index ----

  private static ControlIndexPanelController loadIndexPanel(ControlIndex[] holder) throws Exception {
    ControlIndexPanelController panel = FxTestSupport.<ControlIndexPanelController>load(DIR + "control-index-panel.fxml").controller();
    FxTestSupport.onFx(() -> panel.setIndex(() -> holder[0], index -> holder[0] = index));
    return panel;
  }

  @Test
  void theIndexIsCreatedWithTheTypeTheValueNeedsATypeAndChangingTheTypeDropsIt() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    ControlIndex[] holder = new ControlIndex[1];
    ControlIndexPanelController panel = loadIndexPanel(holder);
    ComboBox<String> type = FxTestSupport.field(panel, "typeField");
    TextField value = FxTestSupport.field(panel, "valueField");
    Button clear = FxTestSupport.field(panel, "clearButton");

    assertNull(holder[0], "opening the panel creates nothing");
    assertTrue(value.isDisabled(), "no value without a type");
    assertTrue(clear.isDisabled());

    FxTestSupport.onFx(() -> type.setValue(ControlIndex.TYPE_SEMANTIC));
    assertEquals(ControlIndex.TYPE_SEMANTIC, holder[0].getType());
    assertNull(holder[0].getValue());
    assertFalse(value.isDisabled());

    FxTestSupport.onFx(() -> value.setText("primary"));
    assertEquals("primary", holder[0].getValue());

    FxTestSupport.onFx(() -> type.setValue(ControlIndex.TYPE_NUMERIC));
    assertEquals(ControlIndex.TYPE_NUMERIC, holder[0].getType());
    assertNull(holder[0].getValue(), "a value means something else for the other type");
    assertEquals("", value.getText());

    FxTestSupport.onFx(() -> value.setText("2"));
    assertEquals("2", holder[0].getValue());
    FxTestSupport.onFx(() -> value.setText(" "));
    assertNull(holder[0].getValue(), "a blank value is no value");
  }

  @Test
  void clearingTheTypeRemovesTheWholeIndex() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    ControlIndex existing = new ControlIndex();
    existing.setType(ControlIndex.TYPE_NUMERIC);
    existing.setValue("3");
    ControlIndex[] holder = {existing};
    ControlIndexPanelController panel = loadIndexPanel(holder);
    ComboBox<String> type = FxTestSupport.field(panel, "typeField");
    TextField value = FxTestSupport.field(panel, "valueField");
    Button clear = FxTestSupport.field(panel, "clearButton");

    assertEquals(ControlIndex.TYPE_NUMERIC, type.getValue());
    assertEquals("3", value.getText());
    assertEquals(ControlIndex.TYPE_NUMERIC, existing.getType(), "opening the panel rewrites nothing");

    FxTestSupport.onFx(clear::fire);

    assertNull(holder[0], "no empty index object is left behind");
    assertTrue(value.isDisabled());
    assertEquals("", value.getText());
  }

  @Test
  void theControlEditorOffersTheIndexOnlyOutsideTheRepeatOfTheFieldsGroup() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormNodeEditorControlPanelController editor =
        FxTestSupport.<FormNodeEditorControlPanelController>load(DIR + "formnode-editor-control-panel.fxml").controller();
    ControlIndexPanelController panel = FxTestSupport.field(editor, "controlIndexController");
    TitledPane root = FxTestSupport.field(panel, "root");
    Control title = control("c_title", "field_title", null);
    Control outside = control("c_outside", "field_name", null);
    Control inside = control("c_inside", "field_name", null);
    EmbeddedRepeat repeat = new EmbeddedRepeat();
    repeat.setGroupRef("g_items");
    repeat.setControlGrid(grid(inside));
    Screen screen = screen("s1", null, grid(title, outside), repeat);
    FormModelContent content = new FormModelContent();
    content.getScreens().add(screen);

    FxTestSupport.onFx(() -> editor.setControl(title, null, index(), content));
    assertFalse(root.isVisible(), "a root field needs no index");
    FxTestSupport.onFx(() -> editor.setControl(inside, null, index(), content));
    assertFalse(root.isVisible(), "the control is in the repeat of its group");
    FxTestSupport.onFx(() -> editor.setControl(outside, null, index(), content));
    assertTrue(root.isVisible(), "placed outside the repeat of Items");

    TextField value = FxTestSupport.field(panel, "valueField");
    ComboBox<String> type = FxTestSupport.field(panel, "typeField");
    FxTestSupport.onFx(() -> type.setValue(ControlIndex.TYPE_NUMERIC));
    FxTestSupport.onFx(() -> value.setText("1"));
    assertNotNull(outside.getIndex());
    assertEquals("1", outside.getIndex().getValue());
    assertNull(inside.getIndex());

    // a stale index stays visible even where none is needed, so it can be removed
    inside.setIndex(new ControlIndex());
    FxTestSupport.onFx(() -> editor.setControl(inside, null, index(), content));
    assertTrue(root.isVisible());
  }

  // ---- custom screen element height ----

  @Test
  void theHeightTakesWholeNumbersFlagsZeroAndReportsEverythingElse() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    CustomScreenElement element = new CustomScreenElement();
    element.setId("custom1");
    element.setName("Relationships");
    element.setHeight(500);
    CustomScreenElementHeightPanelController panel =
        FxTestSupport.<CustomScreenElementHeightPanelController>load(DIR + "custom-screen-element-height-panel.fxml").controller();
    FxTestSupport.onFx(() -> panel.setCustomScreenElement(element));
    TextField height = FxTestSupport.field(panel, "heightField");

    assertEquals("500", height.getText());
    assertFalse(panel.errorProperty().get());

    FxTestSupport.onFx(() -> height.setText("320"));
    assertEquals(320, element.getHeight());
    assertFalse(panel.errorProperty().get());

    FxTestSupport.onFx(() -> height.setText("tall"));
    assertEquals(320, element.getHeight(), "not a number: not written");
    assertTrue(panel.errorProperty().get());

    FxTestSupport.onFx(() -> height.setText("-5"));
    assertEquals(320, element.getHeight(), "a negative height makes no sense");
    assertTrue(panel.errorProperty().get());

    FxTestSupport.onFx(() -> height.setText("0"));
    assertEquals(0, element.getHeight(), "zero is written and flagged, like the validator does");
    assertTrue(panel.errorProperty().get());

    FxTestSupport.onFx(() -> height.setText(""));
    assertNull(element.getHeight(), "blank clears it");
    assertFalse(panel.errorProperty().get());
  }

  @Test
  void theCustomElementEditorHostsTheHeightPanel() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormNodeEditorCustomScreenElementPanelController editor = FxTestSupport.<FormNodeEditorCustomScreenElementPanelController>load(
        DIR + "formnode-editor-custom-screen-element-panel.fxml").controller();

    assertNotNull(FxTestSupport.<Object>field(editor, "heightController"));
  }

  // ---- md/sm layout of a Multi-Column Section ----

  @Test
  void theMultiColumnSectionEditorEditsMdAndSmNextToLg() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    MultiColumnSectionEditorPanelController editor = FxTestSupport.<MultiColumnSectionEditorPanelController>load(
        "/de/a12/studio/ui/editors/formmodel/multi-column-section-editor-panel.fxml").controller();
    MultiColumnSection section = new MultiColumnSection();
    section.setId("mcs1");
    FxTestSupport.onFx(() -> editor.setSection(section));
    Object responsive = FxTestSupport.field(editor, "responsiveLayoutController");
    Object flex = FxTestSupport.field(editor, "flexLayoutController");
    TextField lg = FxTestSupport.field(flex, "layoutLgField");
    TextField md = FxTestSupport.field(responsive, "layoutMdField");
    TextField sm = FxTestSupport.field(responsive, "layoutSmField");

    assertNull(section.getLayout(), "opening the editor creates no layout");

    FxTestSupport.onFx(() -> lg.setText("2-10"));
    FxTestSupport.onFx(() -> md.setText("3-9"));
    FxTestSupport.onFx(() -> sm.setText("12-12"));

    assertEquals("2-10", section.getLayout().getLg());
    assertEquals("3-9", section.getLayout().getMd());
    assertEquals("12-12", section.getLayout().getSm());

    FxTestSupport.onFx(() -> md.setText(""));
    assertNull(section.getLayout().getMd(), "an emptied breakpoint is removed, not written as empty");
    assertEquals("2-10", section.getLayout().getLg());

    // rebinding to a section that already has md/sm shows them
    MultiColumnSection other = new MultiColumnSection();
    other.setId("mcs2");
    other.setLayout(section.getLayout());
    FxTestSupport.onFx(() -> editor.setSection(other));
    assertEquals("12-12", sm.getText());
    assertTrue(md.getText() == null || md.getText().isEmpty(), "no md set: the field is empty");
  }
}
