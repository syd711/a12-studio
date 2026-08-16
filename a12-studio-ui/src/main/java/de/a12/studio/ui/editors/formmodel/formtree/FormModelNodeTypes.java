package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.Cell;
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
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.formmodel.TextCell;
import de.a12.studio.ui.util.Icons;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * The Java analogue of the SME reference's per-type {@code getRelationship()}/meta-file registry: a static
 * table of which node types may be added as a child of which other node type. Single source of truth for the
 * tree's context-menu "Add" submenu ({@link FormModelActions}), Cut/Copy/Paste target validity, and
 * drag-and-drop reparenting validity ({@link FormModelTreeController}).
 */
final class FormModelNodeTypes {

  record ChildTypeDescriptor(String label, String icon, Class<?> resultClass, Supplier<Object> factory) {
  }

  private static final List<ChildTypeDescriptor> SCREEN_ELEMENT_CHILD_TYPES = List.of(
      new ChildTypeDescriptor("Section", Icons.FORM_SECTION, Section.class, FormModelElementFactory::newSection),
      new ChildTypeDescriptor("Multi-Column Section", Icons.FORM_MULTI_COLUMN_SECTION, MultiColumnSection.class,
          FormModelElementFactory::newMultiColumnSection),
      new ChildTypeDescriptor("Control Grid", Icons.FORM_CONTROL_GRID, ControlGrid.class, FormModelElementFactory::newControlGrid),
      new ChildTypeDescriptor("Inline Repeat", Icons.FORM_INLINE_REPEAT, InlineRepeat.class, FormModelElementFactory::newInlineRepeat),
      new ChildTypeDescriptor("Embedded Repeat", Icons.FORM_EMBEDDED_REPEAT, EmbeddedRepeat.class, FormModelElementFactory::newEmbeddedRepeat),
      new ChildTypeDescriptor("Detached Repeat", Icons.FORM_DETACHED_REPEAT, DetachedRepeat.class, FormModelElementFactory::newDetachedRepeat),
      new ChildTypeDescriptor("Custom Screen Element", Icons.FORM_CUSTOM_SCREEN_ELEMENT, CustomScreenElement.class,
          FormModelElementFactory::newCustomScreenElement));

  private static final List<ChildTypeDescriptor> ROW_CHILD_TYPES = List.of(
      new ChildTypeDescriptor("Control", Icons.FORM_CONTROL, Control.class, FormModelElementFactory::newControl),
      new ChildTypeDescriptor("Text", Icons.FORM_TEXT_CELL, TextCell.class, FormModelElementFactory::newTextCell),
      new ChildTypeDescriptor("Expression", Icons.FORM_EXPRESSION_CELL, ExpressionCell.class, FormModelElementFactory::newExpressionCell));

  private static final ChildTypeDescriptor ROW_TYPE =
      new ChildTypeDescriptor("Row", Icons.FORM_ROW, Row.class, FormModelElementFactory::newRow);

  private static final ChildTypeDescriptor CONTROL_GRID_TYPE =
      new ChildTypeDescriptor("Control Grid", Icons.FORM_CONTROL_GRID, ControlGrid.class, FormModelElementFactory::newControlGrid);

  private static final ChildTypeDescriptor DETAIL_SCREEN_TYPE =
      new ChildTypeDescriptor("Detail Screen", Icons.FORM_SCREEN, Screen.class, FormModelElementFactory::newScreen);

  private FormModelNodeTypes() {
  }

  /**
   * The child types that may be added under {@code node} - drives the context menu's "Add" submenu, and
   * (via {@link #canContain}) Cut/Copy/Paste and drag-and-drop reparenting validity.
   */
  static List<ChildTypeDescriptor> allowedChildTypes(@NonNull Object node) {
    if (node instanceof Screen || node instanceof Section || node instanceof MultiColumnSection) {
      return SCREEN_ELEMENT_CHILD_TYPES;
    }
    if (node instanceof ControlGrid) {
      return List.of(ROW_TYPE);
    }
    if (node instanceof Row) {
      return ROW_CHILD_TYPES;
    }
    if (node instanceof EmbeddedRepeat repeat) {
      return repeat.getControlGrid() == null ? List.of(CONTROL_GRID_TYPE) : List.of();
    }
    if (node instanceof DetachedRepeat repeat) {
      return repeat.getDetailScreen() == null ? List.of(DETAIL_SCREEN_TYPE) : List.of();
    }
    // InlineRepeat, CustomScreenElement, Control, TextCell, ExpressionCell: no children can be added.
    return List.of();
  }

  /** Whether an object of {@code childClass} may be added as a child of {@code node}. */
  static boolean canContain(@NonNull Object node, @NonNull Class<?> childClass) {
    for (ChildTypeDescriptor descriptor : allowedChildTypes(node)) {
      if (descriptor.resultClass().isAssignableFrom(childClass)) {
        return true;
      }
    }
    return false;
  }

  /** Whether {@code node} is (or could be, once a child is added) itself a valid child under a screen-like parent. */
  static boolean isScreenElement(Object node) {
    return node instanceof ScreenElement;
  }

  static boolean isRow(Object node) {
    return node instanceof Row;
  }

  static boolean isCell(Object node) {
    return node instanceof Cell;
  }
}
