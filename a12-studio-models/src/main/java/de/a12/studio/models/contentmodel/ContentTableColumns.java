package de.a12.studio.models.contentmodel;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Column operations on a Content Model {@code Table}. A table's {@code props.columns} and the cells of its head,
 * body and foot rows are index-aligned, so, as in SME (which keeps them aligned in a middleware), inserting,
 * removing or moving a column also inserts, removes or moves the cell at that index in every row. Rows inside a
 * repeatable {@code Group} count as rows of their section.
 */
public final class ContentTableColumns {

  private static final Map<String, String> CELL_TYPE_BY_SECTION = Map.of(
      "TableHead", "TableHeadCell", "TableBody", "TableBodyCell", "TableFoot", "TableFootCell");

  private ContentTableColumns() {
  }

  /** The live list of column maps of {@code table} (created empty when the table has none). */
  @SuppressWarnings("unchecked")
  public static @NonNull List<Map<String, Object>> columns(@NonNull ContentElement table) {
    if (table.getProps() == null) {
      table.setProps(new LinkedHashMap<>());
    }
    Object existing = table.getProps().get("columns");
    if (existing instanceof List<?> list) {
      return (List<Map<String, Object>>) list;
    }
    List<Map<String, Object>> created = new ArrayList<>();
    table.getProps().put("columns", created);
    return created;
  }

  /** Inserts a new column at {@code index} (with an empty cell in every row) and returns it. */
  public static @NonNull Map<String, Object> insert(@NonNull ContentElement table, int index, String pinning) {
    List<Map<String, Object>> columns = columns(table);
    int position = Math.max(0, Math.min(index, columns.size()));
    Map<String, Object> column = new LinkedHashMap<>();
    column.put("id", ContentElementDefaults.newId());
    column.put("width", 1);
    if (pinning != null) {
      column.put("pinning", pinning);
    }
    columns.add(position, column);
    for (Section section : sections(table)) {
      for (ContentElement row : section.rows()) {
        if (row.getChildren() == null) {
          row.setChildren(new ArrayList<>());
        }
        int at = Math.min(position, row.getChildren().size());
        row.getChildren().add(at, newCell(section.cellType(), row));
      }
    }
    return column;
  }

  /** Removes the column at {@code index} together with the cell at that index in every row. */
  public static void remove(@NonNull ContentElement table, int index) {
    List<Map<String, Object>> columns = columns(table);
    if (index < 0 || index >= columns.size()) {
      return;
    }
    columns.remove(index);
    for (Section section : sections(table)) {
      for (ContentElement row : section.rows()) {
        if (row.getChildren() != null && index < row.getChildren().size()) {
          row.getChildren().remove(index);
        }
      }
    }
  }

  /** Moves the column at {@code from} (and its cells) so that it ends up at index {@code to}. */
  public static void move(@NonNull ContentElement table, int from, int to) {
    List<Map<String, Object>> columns = columns(table);
    if (from < 0 || to < 0 || from >= columns.size() || to >= columns.size() || from == to) {
      return;
    }
    columns.add(to, columns.remove(from));
    for (Section section : sections(table)) {
      for (ContentElement row : section.rows()) {
        List<ContentElement> cells = row.getChildren();
        if (cells != null && from < cells.size() && to < cells.size()) {
          cells.add(to, cells.remove(from));
        }
      }
    }
  }

  /**
   * Sets the pinning of the column at {@code index} ({@code "left"}, {@code "right"} or {@code null} for none) and
   * reorders like SME does: left-pinned columns first, the column last among its group's neighbours, right-pinned
   * columns last. Returns the column's new index.
   */
  public static int setPinning(@NonNull ContentElement table, int index, String pinning) {
    List<Map<String, Object>> columns = columns(table);
    if (index < 0 || index >= columns.size()) {
      return index;
    }
    Map<String, Object> column = columns.get(index);
    if (pinning == null) {
      column.remove("pinning");
    }
    else {
      column.put("pinning", pinning);
    }
    List<Map<String, Object>> left = new ArrayList<>();
    List<Map<String, Object>> none = new ArrayList<>();
    List<Map<String, Object>> right = new ArrayList<>();
    for (Map<String, Object> other : columns) {
      if (other == column) {
        continue;
      }
      ("left".equals(other.get("pinning")) ? left : "right".equals(other.get("pinning")) ? right : none).add(other);
    }
    List<Map<String, Object>> ordered = new ArrayList<>(left);
    if ("left".equals(pinning)) {
      ordered.add(column);
      ordered.addAll(none);
      ordered.addAll(right);
    }
    else if ("right".equals(pinning)) {
      ordered.addAll(none);
      ordered.addAll(right);
      ordered.add(column);
    }
    else {
      ordered.addAll(none);
      ordered.add(column);
      ordered.addAll(right);
    }
    int target = ordered.indexOf(column);
    // Apply as a series of single moves so the cells follow the columns exactly.
    int current = index;
    while (current != target) {
      int next = current < target ? current + 1 : current - 1;
      move(table, current, next);
      current = next;
    }
    return target;
  }

  /** The text of the head cell above column {@code index}, if the table has one. */
  public static String headLabel(@NonNull ContentElement table, int index) {
    for (Section section : sections(table)) {
      if (!"TableHeadCell".equals(section.cellType())) {
        continue;
      }
      for (ContentElement row : section.rows()) {
        if (row.getChildren() != null && index < row.getChildren().size()) {
          Object content = row.getChildren().get(index).getProps() == null ? null
              : row.getChildren().get(index).getProps().get("content");
          return content instanceof String text ? text : null;
        }
      }
    }
    return null;
  }

  private static ContentElement newCell(String type, ContentElement row) {
    ContentElement cell = new ContentElement();
    cell.setId(ContentElementDefaults.newId());
    cell.setType(type);
    cell.setNamespace(row.getNamespace() != null ? row.getNamespace() : ContentElementDefaults.DEFAULT_NAMESPACE);
    cell.setProps(new LinkedHashMap<>());
    cell.setChildren(new ArrayList<>());
    return cell;
  }

  private record Section(String cellType, List<ContentElement> rows) {
  }

  private static List<Section> sections(ContentElement table) {
    List<Section> sections = new ArrayList<>();
    if (table.getChildren() == null) {
      return sections;
    }
    for (ContentElement child : table.getChildren()) {
      String cellType = CELL_TYPE_BY_SECTION.get(child.getType());
      if (cellType != null) {
        List<ContentElement> rows = new ArrayList<>();
        collectRows(child, rows);
        sections.add(new Section(cellType, rows));
      }
    }
    return sections;
  }

  private static void collectRows(ContentElement parent, List<ContentElement> rows) {
    if (parent.getChildren() == null) {
      return;
    }
    for (ContentElement child : parent.getChildren()) {
      if (child.getType() != null && child.getType().endsWith("Row")) {
        rows.add(child);
      }
      else if ("Group".equals(child.getType())) {
        collectRows(child, rows);
      }
    }
  }
}
