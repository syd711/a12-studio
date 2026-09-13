package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.TableStyle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepeatConverterTest {

  @Test
  void inlineToEmbeddedBuildsControlGridFromColumnsAndPreservesColumns() {
    InlineRepeat inline = newInlineRepeatWithOneFieldColumn();

    AbstractRepeat converted = RepeatConverter.convert(inline, RepeatConverter.RepeatKind.EMBEDDED);

    EmbeddedRepeat embedded = assertInstanceOf(EmbeddedRepeat.class, converted);
    assertEquals("group_field", embedded.getGroupRef());
    assertEquals(1, embedded.getRepeatOverviewColumn().size());
    assertNotNull(embedded.getControlGrid());
    assertEquals(1, embedded.getControlGrid().getRow().size());
    Control control = (Control) embedded.getControlGrid().getRow().get(0).getCell().get(0);
    assertEquals("field_0f9c1", control.getElementRef());
  }

  @Test
  void inlineToDetachedWrapsDerivedControlGridInNewDetailScreen() {
    InlineRepeat inline = newInlineRepeatWithOneFieldColumn();

    AbstractRepeat converted = RepeatConverter.convert(inline, RepeatConverter.RepeatKind.DETACHED);

    DetachedRepeat detached = assertInstanceOf(DetachedRepeat.class, converted);
    assertNotNull(detached.getDetailScreen());
    assertEquals(1, detached.getDetailScreen().getScreenElements().size());
    ControlGrid grid = (ControlGrid) detached.getDetailScreen().getScreenElements().get(0);
    assertEquals(1, grid.getRow().size());
  }

  @Test
  void embeddedToDetachedMovesTheSameControlGridInstance() {
    EmbeddedRepeat embedded = new EmbeddedRepeat();
    embedded.setId("embeddedrepeat-1");
    embedded.setGroupRef("group_field");
    ControlGrid grid = new ControlGrid();
    grid.setId("controlgrid-1");
    embedded.setControlGrid(grid);

    AbstractRepeat converted = RepeatConverter.convert(embedded, RepeatConverter.RepeatKind.DETACHED);

    DetachedRepeat detached = assertInstanceOf(DetachedRepeat.class, converted);
    assertEquals(1, detached.getDetailScreen().getScreenElements().size());
    assertSame(grid, detached.getDetailScreen().getScreenElements().get(0));
  }

  @Test
  void detachedToEmbeddedCollectsCellsFromDetailScreenSkippingNestedRepeats() {
    DetachedRepeat detached = new DetachedRepeat();
    detached.setId("detachedrepeat-1");
    detached.setGroupRef("group_field");
    Screen screen = new Screen();
    screen.setId("screen-1");
    ControlGrid grid = new ControlGrid();
    grid.setId("controlgrid-1");
    Row row = new Row();
    row.setId("row-1");
    Control control = new Control();
    control.setId("control-1");
    control.setElementRef("field_x");
    row.getCell().add(control);
    grid.getRow().add(row);
    screen.getScreenElements().add(grid);
    // A nested repeat's cells must not be lifted out.
    InlineRepeat nested = new InlineRepeat();
    nested.setId("inlinerepeat-nested");
    screen.getScreenElements().add(nested);
    detached.setDetailScreen(screen);

    AbstractRepeat converted = RepeatConverter.convert(detached, RepeatConverter.RepeatKind.EMBEDDED);

    EmbeddedRepeat embedded = assertInstanceOf(EmbeddedRepeat.class, converted);
    assertEquals(1, embedded.getControlGrid().getRow().size());
    Control convertedControl = (Control) embedded.getControlGrid().getRow().get(0).getCell().get(0);
    assertEquals("field_x", convertedControl.getElementRef());
  }

  @Test
  void detachedToInlineDropsDetailScreen() {
    DetachedRepeat detached = new DetachedRepeat();
    detached.setId("detachedrepeat-1");
    detached.setGroupRef("group_field");
    Screen screen = new Screen();
    screen.setId("screen-1");
    detached.setDetailScreen(screen);

    AbstractRepeat converted = RepeatConverter.convert(detached, RepeatConverter.RepeatKind.INLINE);

    assertInstanceOf(InlineRepeat.class, converted);
  }

  @Test
  void embeddedToInlineDropsControlGrid() {
    EmbeddedRepeat embedded = new EmbeddedRepeat();
    embedded.setId("embeddedrepeat-1");
    embedded.setGroupRef("group_field");
    embedded.setControlGrid(new ControlGrid());

    AbstractRepeat converted = RepeatConverter.convert(embedded, RepeatConverter.RepeatKind.INLINE);

    assertInstanceOf(InlineRepeat.class, converted);
  }

  @Test
  void conversionAssignsAFreshIdAndNeverMutatesTheSourceRepeat() {
    InlineRepeat inline = newInlineRepeatWithOneFieldColumn();
    TableStyle style = new TableStyle();
    style.setTableHeight(500);
    style.setRowHeight(40);
    inline.setTableStyle(style);

    AbstractRepeat converted = RepeatConverter.convert(inline, RepeatConverter.RepeatKind.EMBEDDED);

    assertNotEquals(inline.getId(), converted.getId());
    // Embedded repeats drop tableHeight - must not affect the original inline repeat's own TableStyle.
    assertNull(((EmbeddedRepeat) converted).getTableStyle().getTableHeight());
    assertEquals(500, inline.getTableStyle().getTableHeight());
    assertEquals(40, inline.getTableStyle().getRowHeight());
    assertNotSame(inline.getRepeatOverviewColumn(), converted.getRepeatOverviewColumn());
  }

  @Test
  void kindOfIdentifiesEachConcreteRepeatType() {
    assertEquals(RepeatConverter.RepeatKind.INLINE, RepeatConverter.kindOf(new InlineRepeat()));
    assertEquals(RepeatConverter.RepeatKind.EMBEDDED, RepeatConverter.kindOf(new EmbeddedRepeat()));
    assertEquals(RepeatConverter.RepeatKind.DETACHED, RepeatConverter.kindOf(new DetachedRepeat()));
  }

  private static InlineRepeat newInlineRepeatWithOneFieldColumn() {
    InlineRepeat inline = new InlineRepeat();
    inline.setId("inlinerepeat-1");
    inline.setGroupRef("group_field");
    FieldBasedRepeatOverviewColumn column = new FieldBasedRepeatOverviewColumn();
    column.setId("repeatoverviewcolumn-1");
    column.setElementRef("field_0f9c1");
    inline.getRepeatOverviewColumn().add(column);
    return inline;
  }
}
