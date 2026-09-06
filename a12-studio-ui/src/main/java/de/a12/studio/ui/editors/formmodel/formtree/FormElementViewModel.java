package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.ButtonPanel;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.CustomCell;
import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.ExpressionCell;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.GenericRepeatOverviewColumn;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.formmodel.TextCell;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
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

  // Used to resolve the real name of Controls/Repeats/Repeat Overview Columns, which are normally left unnamed
  // and instead reference their bound Document Model element via elementRef/groupRef - null if the Form Model
  // has no (resolvable) data-binding Document Model, in which case such nodes fall back to showing the raw
  // reference.
  private final @Nullable ElementIndex elementIndex;

  private List<String> errorMessages = List.of();

  public FormElementViewModel(@NonNull Object node, @Nullable Object parentNode, @Nullable ElementIndex elementIndex) {
    this.node = node;
    this.parentNode = parentNode;
    this.elementIndex = elementIndex;
  }

  public boolean hasError() {
    return !errorMessages.isEmpty();
  }

  /** The messages of every validation error reported against this node's id, for display in a row tooltip. */
  public List<String> getErrorMessages() {
    return errorMessages;
  }

  public void setErrorMessages(@NonNull List<String> errorMessages) {
    this.errorMessages = errorMessages;
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
    if (node instanceof RepeatOverviewColumn column) {
      return column.getId();
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
    if (node instanceof GenericRepeatOverviewColumn column && column.getConfig().get("name") instanceof String name) {
      return name;
    }
    return null;
  }

  /**
   * The tree label: the node's own {@code name} if set, else - for a {@link Control}, {@link AbstractRepeat} or
   * {@link FieldBasedRepeatOverviewColumn}, which are commonly left unnamed since their real identity is the
   * Document Model element they bind to - the name of that element ({@code elementRef}/{@code groupRef}) resolved
   * against {@link #elementIndex}, falling back to the raw reference if it can't be resolved, else for a
   * {@link Row} the placeholder {@code "<Row>"}, else the node's own {@code id} as a last resort.
   */
  public String getName() {
    String name = rawName();
    if (name != null && !name.isBlank()) {
      return name;
    }
    if (node instanceof Control control && control.getElementRef() != null && !control.getElementRef().isBlank()) {
      return resolveDocumentElementName(control.getElementRef());
    }
    if (node instanceof AbstractRepeat repeat && repeat.getGroupRef() != null && !repeat.getGroupRef().isBlank()) {
      return resolveDocumentElementName(repeat.getGroupRef());
    }
    if (node instanceof FieldBasedRepeatOverviewColumn column
        && column.getElementRef() != null && !column.getElementRef().isBlank()) {
      return resolveDocumentElementName(column.getElementRef());
    }
    if (node instanceof Row) {
      return "<Row>";
    }
    String id = getId();
    return id != null ? id : "<" + getTypeLabel() + ">";
  }

  /**
   * The referenced Document Model element's own name if it can be resolved - following an Include's compound id
   * (e.g. {@code include_7c34e_field_0b84d}) into the included model via {@link ElementIndex#resolveDisplayPath},
   * then taking the last path segment (the element's own name, not its full path) - else the raw reference
   * itself, unchanged (a dangling reference, or no Document Model to resolve against).
   */
  private String resolveDocumentElementName(@NonNull String elementRef) {
    if (elementIndex == null) {
      return elementRef;
    }
    String path = elementIndex.resolveDisplayPath(elementRef);
    if (path == null) {
      return elementRef;
    }
    int lastSlash = path.lastIndexOf('/');
    String name = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    return name.isBlank() ? elementRef : name;
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
    if (node instanceof ButtonPanel) {
      return "Button Panel";
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
    if (node instanceof CustomCell) {
      return "Custom";
    }
    if (node instanceof RepeatOverviewColumn) {
      return "Column";
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
    if (node instanceof ButtonPanel) {
      return Icons.FORM_BUTTON_PANEL;
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
    if (node instanceof CustomCell) {
      return Icons.FORM_CUSTOM_CELL;
    }
    if (node instanceof RepeatOverviewColumn) {
      return Icons.FORM_CONTROL;
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
    else if (node instanceof AbstractRepeat repeat) {
      // Rows aren't authored here - they're generated at runtime from the bound group (matches the SME
      // reference) - but the authored overview columns, and the Embedded/Detached repeat's own detail
      // widget, are shown as children.
      childNodes.addAll(repeat.getRepeatOverviewColumn());
      if (node instanceof EmbeddedRepeat embeddedRepeat && embeddedRepeat.getControlGrid() != null) {
        childNodes.add(embeddedRepeat.getControlGrid());
      }
      else if (node instanceof DetachedRepeat detachedRepeat && detachedRepeat.getDetailScreen() != null) {
        childNodes.add(detachedRepeat.getDetailScreen());
      }
    }
    // CustomScreenElement, ButtonPanel, Control, TextCell, ExpressionCell, CustomCell, RepeatOverviewColumn:
    // no children shown in this tree (ButtonPanel's own buttons are edited via a panel, not tree nodes).

    List<FormElementViewModel> children = new ArrayList<>();
    for (Object child : childNodes) {
      children.add(new FormElementViewModel(child, node, elementIndex));
    }
    return children;
  }

  @Override
  public String toString() {
    return getName();
  }
}
