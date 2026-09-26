package de.a12.studio.models.contentmodel;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates the element SME's insert panel creates for a type: a fresh id, the type's default props
 * ({@link ContentElementDefaults}) and, for the types that are only useful with their parts (a Table with head, body
 * and foot, a Grid with a row, an Expandable with its collapsed and expanded state, ...), those child elements, as the
 * {@code propertiesCreator} of each of SME's element modules does.
 */
public final class ContentElementFactory {

  private static final String[] TABLE_HEAD_LABELS = {"ID", "First Name", "Last Name", "Address", "Country"};

  // The sample rows SME puts into a new table: id, first name, last name, address, country
  private static final String[][] TABLE_SAMPLE_ROWS = {
      {"1001", "Paul", "Smith", "1234 Elm Street", "USA"},
      {"1002", "John", "Doe", "5678 Oak Street", "Spain"},
      {"1003", "Jane", "Naomi", "91011 Pine Street", "England"}};

  private ContentElementFactory() {
  }

  /**
   * A new element of {@code module}.
   *
   * @param tableColumns the number of columns of the table the element is inserted into; only table rows use it (they
   *                     get one empty cell per column, which SME's insert middleware does the same way)
   */
  public static @NonNull ContentElement create(@NonNull ContentModule module, int tableColumns) {
    ContentElement element = node(module.namespace(), module.type());
    if (!ContentElementLibrary.NAMESPACE.equals(module.namespace())) {
      return element;
    }
    switch (module.type()) {
      case "Table" -> element.setChildren(tableParts());
      case "TableHeadRow" -> element.setChildren(emptyCells("TableHeadCell", tableColumns));
      case "TableBodyRow" -> element.setChildren(emptyCells("TableBodyCell", tableColumns));
      case "TableFootRow" -> element.setChildren(emptyCells("TableFootCell", tableColumns));
      case "Grid" -> element.getChildren().add(create(gridRow(), 0));
      case "GridRow" -> element.getChildren().add(node("GridColumn"));
      case "InteractiveTile" -> element.getChildren().add(tileBox());
      case "InteractiveList" -> {
        for (int i = 1; i <= 3; i++) {
          ContentElement item = node("InteractiveListItem");
          item.getProps().put("text", "List Item " + i);
          element.getChildren().add(item);
        }
      }
      case "OrderedList", "UnorderedList" -> {
        for (int i = 0; i < 3; i++) {
          element.getChildren().add(listItem());
        }
      }
      case "ListItem" -> element.getChildren().add(node("Paragraph"));
      case "Expandable" -> {
        element.getChildren().add(withChildren(node("ExpandableCollapsed"), titleWithHeading()));
        element.getChildren().add(withChildren(node("ExpandableExpanded"), titleWithHeading(), contentWithText()));
      }
      case "ExpandableCollapsed" -> element.getChildren().add(titleWithHeading());
      case "ExpandableExpanded" -> {
        element.getChildren().add(titleWithHeading());
        element.getChildren().add(contentWithText());
      }
      case "ExpandableTitle" -> element.getChildren().add(node("Heading"));
      case "ExpandableContent" -> {
        element.getChildren().add(node("Heading"));
        element.getChildren().add(node("Paragraph"));
      }
      case "ButtonGroup" -> {
        element.getChildren().add(button("Button 1", true));
        element.getChildren().add(button("Button 2", false));
      }
      case "ButtonGroupContainer" -> {
        element.getChildren().add(withChildren(buttonGroup("left"), button("Button label", true), button("Button label", false)));
        element.getChildren().add(withChildren(buttonGroup("right"), button("Button label", false)));
      }
      default -> {
      }
    }
    return element;
  }

  private static ContentModule gridRow() {
    return ContentElementLibrary.find(ContentElementLibrary.NAMESPACE, "GridRow").orElseThrow();
  }

  /** A bare element: id in SME's {@code Type-uid} form, the type's default props, an empty child list. */
  private static ContentElement node(String type) {
    return node(ContentElementLibrary.NAMESPACE, type);
  }

  private static ContentElement node(String namespace, String type) {
    ContentElement element = new ContentElement();
    element.setId(type + "-" + ContentElementDefaults.newId());
    element.setNamespace(namespace);
    element.setType(type);
    element.setProps(ContentElementDefaults.defaultProps(type));
    element.setChildren(new ArrayList<>());
    return element;
  }

  private static ContentElement withChildren(ContentElement parent, ContentElement... children) {
    parent.getChildren().addAll(List.of(children));
    return parent;
  }

  private static ContentElement titleWithHeading() {
    return withChildren(node("ExpandableTitle"), node("Heading"));
  }

  private static ContentElement contentWithText() {
    return withChildren(node("ExpandableContent"), node("Paragraph"));
  }

  private static ContentElement listItem() {
    return withChildren(node("ListItem"), node("Paragraph"));
  }

  private static ContentElement button(String label, boolean primary) {
    ContentElement button = node("Button");
    button.getProps().put("label", label);
    if (primary) {
      button.getProps().put("primary", true);
    }
    return button;
  }

  private static ContentElement buttonGroup(String alignment) {
    ContentElement group = node("ButtonGroup");
    group.getProps().put("alignment", alignment);
    return group;
  }

  /** The box of a new Interactive Tile: a heading line and a text, with the tile's padding. */
  private static ContentElement tileBox() {
    ContentElement box = node("Box");
    Map<String, Object> style = new LinkedHashMap<>();
    style.put("width", "100%");
    style.put("display", "flex");
    style.put("height", "fit-content");
    style.put("flexDirection", "column");
    style.put("gap", "0px");
    style.put("padding", "8px");
    style.put("backgroundRepeat", "no-repeat");
    box.getProps().put("style", style);
    ContentElement heading = node("Paragraph");
    heading.getProps().remove("style");
    LexicalText.setText(heading, "Tile Heading");
    return withChildren(box, heading, node("Paragraph"));
  }

  private static List<ContentElement> emptyCells(String cellType, int count) {
    List<ContentElement> cells = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cells.add(node(cellType));
    }
    return cells;
  }

  /** Head (labels), body (sample rows) and an empty foot; the columns themselves are the table's default props. */
  private static List<ContentElement> tableParts() {
    ContentElement head = node("TableHead");
    head.getChildren().add(row("TableHeadRow", "TableHeadCell", TABLE_HEAD_LABELS));
    ContentElement body = node("TableBody");
    for (String[] sample : TABLE_SAMPLE_ROWS) {
      body.getChildren().add(row("TableBodyRow", "TableBodyCell", sample));
    }
    return new ArrayList<>(List.of(head, body, node("TableFoot")));
  }

  private static ContentElement row(String rowType, String cellType, String[] contents) {
    ContentElement row = node(rowType);
    for (String content : contents) {
      ContentElement cell = node(cellType);
      cell.getProps().put("content", content);
      row.getChildren().add(cell);
    }
    return row;
  }
}
