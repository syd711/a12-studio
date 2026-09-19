package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// SME's isIndexableControl: a Control needs an index when its field sits below more repeatable groups than the
// enclosing Embedded/Detached Repeat (or the model root) provides.
class ControlIndexSupportTest {

  private static final String DOCUMENT_MODEL = """
      {"header": {"id": "Company_DM", "modelType": "document", "modelVersion": "29.4.0"},
       "content": {"modelRoot": {"rootGroups": [
         {"type": "Group", "id": "g_root", "name": "Company", "Group": {"repeatability": 1, "elements": [
           {"type": "Field", "id": "field_title", "name": "Title", "Field": {"fieldType": {"type": "StringType"}}},
           {"type": "Group", "id": "g_items", "name": "Items", "Group": {"repeatability": 10, "elements": [
             {"type": "Field", "id": "field_name", "name": "Name", "Field": {"fieldType": {"type": "StringType"}}},
             {"type": "Group", "id": "g_sub", "name": "Sub", "Group": {"repeatability": 5, "elements": [
               {"type": "Field", "id": "field_sub", "name": "SubName", "Field": {"fieldType": {"type": "StringType"}}}
             ]}}
           ]}}
         ]}}
       ]}}}
      """;

  private static Control control(String id, String elementRef) {
    Control control = new Control();
    control.setId(id);
    control.setElementRef(elementRef);
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

  private final Control title = control("c_title", "field_title");
  private final Control outsideName = control("c_outside_name", "field_name");
  private final Control outsideSub = control("c_outside_sub", "field_sub");
  private final Control insideName = control("c_inside_name", "field_name");
  private final Control insideSub = control("c_inside_sub", "field_sub");
  private final Control insideTitle = control("c_inside_title", "field_title");
  private final Control detailName = control("c_detail_name", "field_name");
  private final Control nestedSub = control("c_nested_sub", "field_sub");
  private final Control dangling = control("c_dangling", "field_gone");
  private final FormModelContent content = content();
  private final ElementIndex index = new ElementIndex(JsonSettings.objectMapper.readValue(DOCUMENT_MODEL, DocumentModel.class));

  private FormModelContent content() {
    EmbeddedRepeat embedded = new EmbeddedRepeat();
    embedded.setId("embedded1");
    embedded.setGroupRef("g_items");
    embedded.setControlGrid(grid(insideName, insideSub, insideTitle));

    // A repeat inside a repeat's detail screen: the nearest one is the data context.
    EmbeddedRepeat nested = new EmbeddedRepeat();
    nested.setId("nested1");
    nested.setGroupRef("g_sub");
    nested.setControlGrid(grid(nestedSub));
    Screen detailScreen = new Screen();
    detailScreen.setId("detail1");
    Section detailSection = new Section();
    detailSection.getScreenElements().add(grid(detailName));
    detailSection.getScreenElements().add(nested);
    detailScreen.getScreenElements().add(detailSection);
    DetachedRepeat detached = new DetachedRepeat();
    detached.setId("detached1");
    detached.setGroupRef("g_items");
    detached.setDetailScreen(detailScreen);

    Section section = new Section();
    section.getScreenElements().add(grid(title, outsideName, outsideSub, dangling));
    section.getScreenElements().add(embedded);
    section.getScreenElements().add(detached);
    Screen screen = new Screen();
    screen.setId("screen1");
    screen.getScreenElements().add(section);
    FormModelContent content = new FormModelContent();
    content.getScreens().add(screen);
    return content;
  }

  private boolean indexable(Control control) {
    return ControlIndexSupport.isIndexable(control, content, index);
  }

  @Test
  void aControlOnTheRootForAFieldOutsideAnyRepeatNeedsNoIndex() {
    assertFalse(indexable(title));
  }

  @Test
  void aControlOutsideTheRepeatOfItsFieldsGroupNeedsAnIndex() {
    assertTrue(indexable(outsideName), "one repeatable group above, none provided");
    assertTrue(indexable(outsideSub), "two above, none provided");
  }

  @Test
  void aControlInItsOwnRepeatNeedsNoIndexButOneBelowItsGroupDoes() {
    assertFalse(indexable(insideName), "the repeat provides Items");
    assertTrue(indexable(insideSub), "Sub is a further repeatable group below Items");
  }

  @Test
  void aControlShowingAncestorDataNeedsNoIndex() {
    assertFalse(indexable(insideTitle), "a field above the repeat's group: distance below zero");
  }

  @Test
  void theNearestRepeatIsTheDataContextAlsoInADetailScreen() {
    assertFalse(indexable(detailName), "the detached repeat provides Items");
    assertFalse(indexable(nestedSub), "the nested embedded repeat provides Sub, the outer one is not the context");
    assertEquals(Optional.of("g_sub"), ControlIndexSupport.dataContextGroupRef(nestedSub, content));
    assertEquals(Optional.of("g_items"), ControlIndexSupport.dataContextGroupRef(detailName, content));
    assertEquals(Optional.empty(), ControlIndexSupport.dataContextGroupRef(outsideName, content));
  }

  @Test
  void anUnresolvableFieldOrMissingContextNeedsNoIndex() {
    assertFalse(indexable(dangling));
    assertFalse(ControlIndexSupport.isIndexable(outsideName, null, index));
    assertFalse(ControlIndexSupport.isIndexable(outsideName, content, null));
  }
}
