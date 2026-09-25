package de.a12.studio.models.contentmodel;

import de.a12.studio.models.ModelRoundTrip;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContentTableColumnsTest {

  private static ContentElement table() throws Exception {
    ContentModel model = ModelRoundTrip.load(ContentTableColumnsTest.class, "/contentmodel/WelcomePage_CM.json", ContentModel.class);
    return find(model.getContent().getRoot());
  }

  private static ContentElement find(ContentElement element) {
    if ("Table".equals(element.getType())) {
      return element;
    }
    if (element.getChildren() != null) {
      for (ContentElement child : element.getChildren()) {
        ContentElement found = find(child);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private static List<String> contents(ContentElement section, int rowIndex) {
    return section.getChildren().get(rowIndex).getChildren().stream()
        .map(cell -> (String) cell.getProps().get("content")).toList();
  }

  @Test
  void insertingAColumnAddsACellToEveryRow() throws Exception {
    ContentElement table = table();
    int before = ContentTableColumns.columns(table).size();

    ContentTableColumns.insert(table, 1, null);

    assertEquals(before + 1, ContentTableColumns.columns(table).size());
    for (ContentElement section : table.getChildren()) {
      for (ContentElement row : section.getChildren()) {
        assertEquals(before + 1, row.getChildren().size(), section.getType());
      }
    }
    ContentElement newHeadCell = table.getChildren().get(0).getChildren().get(0).getChildren().get(1);
    assertEquals("TableHeadCell", newHeadCell.getType());
    assertEquals("TableBodyCell", table.getChildren().get(1).getChildren().get(0).getChildren().get(1).getType());
    assertNull(newHeadCell.getProps().get("content"));
  }

  @Test
  void removingAColumnRemovesItsCells() throws Exception {
    ContentElement table = table();
    List<String> head = contents(table.getChildren().get(0), 0);

    ContentTableColumns.remove(table, 1);

    assertEquals(List.of(head.get(0), head.get(2)), contents(table.getChildren().get(0), 0));
    assertEquals(2, ContentTableColumns.columns(table).size());
  }

  @Test
  void movingAColumnMovesItsCellsAlong() throws Exception {
    ContentElement table = table();
    List<String> head = contents(table.getChildren().get(0), 0);
    Object movedId = ContentTableColumns.columns(table).get(0).get("id");

    ContentTableColumns.move(table, 0, 2);

    assertEquals(List.of(head.get(1), head.get(2), head.get(0)), contents(table.getChildren().get(0), 0));
    assertEquals(movedId, ContentTableColumns.columns(table).get(2).get("id"));
  }

  @Test
  void pinningReordersLikeSmeAndTheCellsFollow() throws Exception {
    ContentElement table = table();
    List<String> head = contents(table.getChildren().get(0), 0);

    // Column 2 pinned left goes behind the existing left-pinned column 0, in front of the others.
    int newIndex = ContentTableColumns.setPinning(table, 2, "left");

    assertEquals(1, newIndex);
    assertEquals("left", ContentTableColumns.columns(table).get(1).get("pinning"));
    assertEquals(List.of(head.get(0), head.get(2), head.get(1)), contents(table.getChildren().get(0), 0));

    // Unpinning the first column puts it behind the remaining left-pinned one and in front of the unpinned ones.
    ContentTableColumns.setPinning(table, 0, null);
    assertNull(ContentTableColumns.columns(table).get(1).get("pinning"));
  }

  @Test
  void readsTheHeadCellTextAsLabel() throws Exception {
    assertEquals("Role", ContentTableColumns.headLabel(table(), 1));
    assertNull(ContentTableColumns.headLabel(table(), 9));
  }
}
