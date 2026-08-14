package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.AbstractRepeat;
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
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps one node of the Form Model structural tree (a {@link Screen}, {@link ScreenElement}, {@link Row} or
 * {@link Cell} - there's no single common Java supertype across all four, so this wraps {@code Object} and
 * dispatches on concrete type). {@code parentNode} is whatever object holds {@code node} in a list/slot ({@code
 * null} for a top-level {@link Screen}, which lives directly in {@link
 * de.a12.studio.models.formmodel.FormModelContent#getScreens()}) - carried alongside {@code node} so {@link
 * FormModelActions}/{@link FormModelTreeController} can mutate the right list without a second tree lookup.
 */
public class FormElementViewModel {

  private final Object node;

  private final Object parentNode;

  public FormElementViewModel(@NonNull Object node, @Nullable Object parentNode) {
    this.node = node;
    this.parentNode = parentNode;
  }

  public Object getNode() {
    return node;
  }

  public Object getParentNode() {
    return parentNode;
  }

  public String getId() {
    if (node instanceof Screen screen) {
      return screen.getId();
    }
    if (node instanceof ScreenElement screenElement) {
      return screenElement.getId();
    }
    if (node instanceof Row row) {
      return row.getId();
    }
    if (node instanceof Cell cell) {
      return cell.getId();
    }
    return null;
  }

  private String rawName() {
    if (node instanceof Screen screen) {
      return screen.getName();
    }
    if (node instanceof ScreenElement screenElement) {
      return screenElement.getName();
    }
    if (node instanceof Row row) {
      return row.getName();
    }
    if (node instanceof Cell cell) {
      return cell.getName();
    }
    return null;
  }

  /**
   * The tree label: the node's own {@code name} if set, else - for a {@link Control} or {@link AbstractRepeat},
   * which are commonly left unnamed since their real identity is the Document Model element they bind to - the
   * {@code elementRef}/{@code groupRef} they point at, else the node's own {@code id} as a last resort.
   */
  public String getName() {
    String name = rawName();
    if (name != null && !name.isBlank()) {
      return name;
    }
    if (node instanceof Control control && control.getElementRef() != null && !control.getElementRef().isBlank()) {
      return control.getElementRef();
    }
    if (node instanceof AbstractRepeat repeat && repeat.getGroupRef() != null && !repeat.getGroupRef().isBlank()) {
      return repeat.getGroupRef();
    }
    String id = getId();
    return id != null ? id : "<" + getTypeLabel() + ">";
  }

  public String getTypeLabel() {
    if (node instanceof Screen) {
      return "Screen";
    }
    if (node instanceof Section) {
      return "Section";
    }
    if (node instanceof MultiColumnSection) {
      return "Multi-Column Section";
    }
    if (node instanceof ControlGrid) {
      return "Control Grid";
    }
    if (node instanceof InlineRepeat) {
      return "Inline Repeat";
    }
    if (node instanceof EmbeddedRepeat) {
      return "Embedded Repeat";
    }
    if (node instanceof DetachedRepeat) {
      return "Detached Repeat";
    }
    if (node instanceof CustomScreenElement) {
      return "Custom Screen Element";
    }
    if (node instanceof Row) {
      return "Row";
    }
    if (node instanceof Control) {
      return "Control";
    }
    if (node instanceof TextCell) {
      return "Text";
    }
    if (node instanceof ExpressionCell) {
      return "Expression";
    }
    return "Element";
  }

  public String getIcon() {
    if (node instanceof Screen) {
      return Icons.FORM_SCREEN;
    }
    if (node instanceof Section) {
      return Icons.FORM_SECTION;
    }
    if (node instanceof MultiColumnSection) {
      return Icons.FORM_MULTI_COLUMN_SECTION;
    }
    if (node instanceof ControlGrid) {
      return Icons.FORM_CONTROL_GRID;
    }
    if (node instanceof InlineRepeat) {
      return Icons.FORM_INLINE_REPEAT;
    }
    if (node instanceof EmbeddedRepeat) {
      return Icons.FORM_EMBEDDED_REPEAT;
    }
    if (node instanceof DetachedRepeat) {
      return Icons.FORM_DETACHED_REPEAT;
    }
    if (node instanceof CustomScreenElement) {
      return Icons.FORM_CUSTOM_SCREEN_ELEMENT;
    }
    if (node instanceof Row) {
      return Icons.FORM_ROW;
    }
    if (node instanceof Control) {
      return Icons.FORM_CONTROL;
    }
    if (node instanceof TextCell) {
      return Icons.FORM_TEXT_CELL;
    }
    if (node instanceof ExpressionCell) {
      return Icons.FORM_EXPRESSION_CELL;
    }
    return Icons.ELEMENT_GENERIC;
  }

  public List<FormElementViewModel> getChildren() {
    List<Object> childNodes = new ArrayList<>();
    if (node instanceof Screen screen) {
      childNodes.addAll(screen.getScreenElements());
    }
    else if (node instanceof Section section) {
      childNodes.addAll(section.getScreenElements());
    }
    else if (node instanceof MultiColumnSection section) {
      childNodes.addAll(section.getScreenElements());
    }
    else if (node instanceof ControlGrid grid) {
      childNodes.addAll(grid.getRow());
    }
    else if (node instanceof Row row) {
      childNodes.addAll(row.getCell());
    }
    else if (node instanceof EmbeddedRepeat repeat && repeat.getControlGrid() != null) {
      childNodes.add(repeat.getControlGrid());
    }
    else if (node instanceof DetachedRepeat repeat && repeat.getDetailScreen() != null) {
      childNodes.add(repeat.getDetailScreen());
    }
    // InlineRepeat, CustomScreenElement, Control, TextCell, ExpressionCell: no children - InlineRepeat's rows
    // are generated at runtime from the bound group, not authored here (matches the SME reference).

    List<FormElementViewModel> children = new ArrayList<>();
    for (Object child : childNodes) {
      children.add(new FormElementViewModel(child, node));
    }
    return children;
  }

  @Override
  public String toString() {
    return getName();
  }
}
