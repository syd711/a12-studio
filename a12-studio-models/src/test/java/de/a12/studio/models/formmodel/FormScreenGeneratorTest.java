package de.a12.studio.models.formmodel;

import de.a12.studio.models.Locale;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pins the rules ported from A12's form-model-generator (see {@link FormScreenGenerator}). */
class FormScreenGeneratorTest {

  private static final String DM = """
      {"header":{"id":"Dm","modelType":"document","modelVersion":"29.4.0","locales":[{"code":"en"},{"code":"de"}]},
       "content":{"modelRoot":{"rootGroups":[
        {"type":"Group","id":"g_main","name":"Main","Group":{"repeatability":1,"elements":[
          {"type":"Field","id":"f_name","name":"Name","Field":{"fieldType":{"type":"StringType"},
             "label":[{"locale":"en","text":"Full name"},{"locale":"fr","text":"Nom"}]}},
          {"type":"Field","id":"f_rate","name":"Rate","Field":{"fieldType":{"type":"NumberType","NumberType":{"trait":"percent"}}}},
          {"type":"Group","id":"g_addr","name":"Address","Group":{"repeatability":1,"elements":[
            {"type":"Field","id":"f_city","name":"City","Field":{"fieldType":{"type":"StringType"}}}]}},
          {"type":"Field","id":"f_after","name":"After","Field":{"fieldType":{"type":"StringType"}}},
          {"type":"Group","id":"g_items","name":"Items","Group":{"repeatability":5,"elements":[
            {"type":"Field","id":"f_item","name":"Item","Field":{"fieldType":{"type":"StringType"}}},
            {"type":"Group","id":"g_sub","name":"Sub","Group":{"repeatability":3,"elements":[
              {"type":"Field","id":"f_deep","name":"Deep","Field":{"fieldType":{"type":"StringType"}}}]}}]}}]}},
        {"type":"Group","id":"g_meta","name":"Meta","Group":{"repeatability":1,"usageType":"metadata","elements":[
          {"type":"Field","id":"f_created","name":"Created","Field":{"fieldType":{"type":"StringType"}}}]}}
       ]}}}
      """;

  private static FormModelContent generate() throws Exception {
    DocumentModel dm = JsonSettings.objectMapper.readValue(DM, DocumentModel.class);
    Locale en = new Locale();
    en.setCode("en");
    Locale de = new Locale();
    de.setCode("de");
    FormModelContent content = new FormModelContent();
    FormScreenGenerator.generate(content, dm, List.of(en, de));
    return content;
  }

  @Test
  void idsFollowTheDocumentModelElements() throws Exception {
    FormModelContent content = generate();
    assertEquals(List.of("Screen_for_g_main", "Screen_for_g_meta"),
        content.getScreens().stream().map(Screen::getId).toList());
    List<ScreenElement> elements = content.getScreens().get(0).getScreenElements();
    // fields before the Section share one grid, the field after it starts a numbered one
    ControlGrid first = assertInstanceOf(ControlGrid.class, elements.get(0));
    assertEquals("ControlGrid_for_g_main", first.getId());
    assertEquals("Main_Controls", first.getName());
    assertEquals(List.of("Row_for_f_name", "Row_for_f_rate"), first.getRow().stream().map(Row::getId).toList());
    Section section = assertInstanceOf(Section.class, elements.get(1));
    assertEquals("Section_for_g_addr", section.getId());
    ControlGrid second = assertInstanceOf(ControlGrid.class, elements.get(2));
    assertEquals("ControlGrid_for_g_main_1", second.getId());
    assertEquals("Main_Controls_1", second.getName());
  }

  @Test
  void labelOnlyForControlsWithoutOwnFieldLabel() throws Exception {
    ControlGrid grid = (ControlGrid) generate().getScreens().get(0).getScreenElements().get(0);
    assertNull(((Control) grid.getRow().get(0).getCell().get(0)).getLabel());
    Control rate = (Control) grid.getRow().get(1).getCell().get(0);
    MultilingualText label = assertInstanceOf(MultilingualText.class, rate.getLabel());
    assertEquals(List.of("en", "de"), label.getMultilingualText().getText().stream().map(l -> l.getLocale()).toList());
    assertEquals("Rate", label.getMultilingualText().getText().get(0).getText());
  }

  @Test
  void repeatGetsColumnsWithoutNestedRepeatsAndDetailScreen() throws Exception {
    DetachedRepeat repeat = (DetachedRepeat) generate().getScreens().get(0).getScreenElements().get(3);
    assertEquals("DetachedRepeat_for_g_items", repeat.getId());
    assertEquals("Items_Repeat", repeat.getName());
    assertEquals("g_items", repeat.getGroupRef());
    assertEquals(List.of("Column_for_f_item"), repeat.getRepeatOverviewColumn().stream().map(RepeatOverviewColumn::getId).toList());
    assertEquals("Details", repeat.getDetailScreen().getName());
    assertEquals("Screen_for_g_items", repeat.getDetailScreen().getId());
    assertInstanceOf(DetachedRepeat.class, repeat.getDetailScreen().getScreenElements().get(1));
  }

  @Test
  void metadataGroupIsReadOnlyAndNavigationAndSuffixAreGenerated() throws Exception {
    FormModelContent content = generate();
    ControlGrid grid = (ControlGrid) content.getScreens().get(1).getScreenElements().get(0);
    assertEquals(Boolean.TRUE, ((Control) grid.getRow().get(0).getCell().get(0)).getReadonly());

    List<Button> buttons = content.getSubHeaderBox().getMajorButtons().getButton();
    assertEquals("Button_for_Screen_for_g_main", buttons.get(0).getId());
    assertEquals("Screen_for_g_meta", ((NavigationButton) buttons.get(1)).getTarget());
    assertEquals("ALWAYS", buttons.get(1).getScope());
    assertEquals("footerBox", content.getFooterBox().getId());

    FieldConfigEntry entry = content.getFieldConfiguration().getField().get(0);
    assertEquals("f_rate", entry.getElementRef());
    assertEquals("%", entry.getSuffix().getText().get(0).getText());
    assertTrue(content.getFieldConfiguration().getField().size() == 1);
  }
}
