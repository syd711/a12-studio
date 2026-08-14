package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.ExpressionCell;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.formmodel.TextCell;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Creates new Form Model tree nodes for the "Add" context menu ({@link FormModelNodeTypes}) and for drag-and-drop
 * from the Document Model source tree ({@link FormModelTreeController}). Every {@code newXxx} generates a fresh
 * id (also reused by {@link FormModelActions#regenerateIds} when duplicating/pasting a subtree) matching the
 * {@code <lowercase-type>-<5-hex-digits>} convention already used by existing Form Model files (see
 * {@code Invoice_FM.json}, e.g. {@code "section-fc573"}, {@code "controlgrid-95f39"}).
 */
final class FormModelElementFactory {

  private static final Random ID_RANDOM = new SecureRandom();

  private FormModelElementFactory() {
  }

  static String generateId(String prefix) {
    return prefix + "-" + String.format("%05x", ID_RANDOM.nextInt(0x100000));
  }

  static Screen newScreen() {
    Screen screen = new Screen();
    screen.setId(generateId("screen"));
    screen.setName("Screen");
    return screen;
  }

  static Section newSection() {
    Section section = new Section();
    section.setId(generateId("section"));
    section.setName("Section");
    return section;
  }

  static MultiColumnSection newMultiColumnSection() {
    MultiColumnSection section = new MultiColumnSection();
    section.setId(generateId("multicolumnsection"));
    section.setName("MultiColumnSection");
    return section;
  }

  static ControlGrid newControlGrid() {
    ControlGrid grid = new ControlGrid();
    grid.setId(generateId("controlgrid"));
    grid.setName("ControlGrid");
    return grid;
  }

  static CustomScreenElement newCustomScreenElement() {
    CustomScreenElement element = new CustomScreenElement();
    element.setId(generateId("customscreenelement"));
    element.setName("CustomScreenElement");
    return element;
  }

  static InlineRepeat newInlineRepeat() {
    InlineRepeat repeat = new InlineRepeat();
    repeat.setId(generateId("inlinerepeat"));
    repeat.setName("InlineRepeat");
    return repeat;
  }

  static InlineRepeat newInlineRepeat(String groupRef) {
    InlineRepeat repeat = newInlineRepeat();
    repeat.setGroupRef(groupRef);
    return repeat;
  }

  static EmbeddedRepeat newEmbeddedRepeat() {
    EmbeddedRepeat repeat = new EmbeddedRepeat();
    repeat.setId(generateId("embeddedrepeat"));
    repeat.setName("EmbeddedRepeat");
    return repeat;
  }

  static DetachedRepeat newDetachedRepeat() {
    DetachedRepeat repeat = new DetachedRepeat();
    repeat.setId(generateId("detachedrepeat"));
    repeat.setName("DetachedRepeat");
    return repeat;
  }

  static Row newRow() {
    Row row = new Row();
    row.setId(generateId("row"));
    return row;
  }

  static Control newControl() {
    Control control = new Control();
    control.setId(generateId("control"));
    return control;
  }

  static Control newControl(String elementRef) {
    Control control = newControl();
    control.setElementRef(elementRef);
    return control;
  }

  static TextCell newTextCell() {
    TextCell cell = new TextCell();
    cell.setId(generateId("textcell"));
    cell.setName("TextCell");
    return cell;
  }

  static ExpressionCell newExpressionCell() {
    ExpressionCell cell = new ExpressionCell();
    cell.setId(generateId("expressioncell"));
    cell.setName("ExpressionCell");
    return cell;
  }
}
