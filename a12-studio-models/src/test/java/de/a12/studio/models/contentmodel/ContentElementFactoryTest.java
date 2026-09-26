package de.a12.studio.models.contentmodel;

import de.a12.studio.models.contentmodel.ContentRuleEvaluator.Node;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentElementFactoryTest {

  private static ContentModule module(String type) {
    return ContentElementLibrary.modules().stream().filter(module -> module.type().equals(type)).findFirst().orElseThrow();
  }

  private static ContentElement create(String type) {
    return ContentElementFactory.create(module(type), 5);
  }

  private static List<String> childTypes(ContentElement element) {
    return element.getChildren().stream().map(ContentElement::getType).toList();
  }

  @Test
  void aNewElementHasATypedIdTheNamespaceAndItsDefaultProps() {
    ContentElement paragraph = create("Paragraph");

    assertTrue(paragraph.getId().matches("Paragraph-[0-9a-f]{8}"), paragraph.getId());
    assertEquals(ContentElementLibrary.NAMESPACE, paragraph.getNamespace());
    assertEquals("Paragraph", paragraph.getType());
    assertNotNull(paragraph.getProps().get("tree"));
    assertEquals(List.of(), paragraph.getChildren());
  }

  @Test
  void aFormElementIsCreatedInTheFormEngineNamespaceWithoutAFieldYet() {
    ContentElement textLine = create("TextLine");

    assertEquals("com.mgmtp.a12.formengine", textLine.getNamespace());
    assertEquals("", textLine.getProps().get("elementId"));
    assertEquals(List.of(), textLine.getProps().get("annotations"));
  }

  @Test
  void aTableComesWithHeadBodyAndFootAndFiveColumns() {
    ContentElement table = create("Table");

    assertEquals(List.of("TableHead", "TableBody", "TableFoot"), childTypes(table));
    assertEquals(5, ContentTableColumns.columns(table).size());
    ContentElement head = table.getChildren().get(0);
    ContentElement body = table.getChildren().get(1);
    assertEquals(1, head.getChildren().size());
    assertEquals(List.of("ID", "First Name", "Last Name", "Address", "Country"),
        head.getChildren().get(0).getChildren().stream().map(cell -> cell.getProps().get("content")).toList());
    assertEquals(3, body.getChildren().size());
    body.getChildren().forEach(row -> assertEquals(5, row.getChildren().size()));
    assertEquals(0, table.getChildren().get(2).getChildren().size());
  }

  @Test
  void aTableRowGetsOneCellPerColumnOfItsTable() {
    ContentElement row = ContentElementFactory.create(module("TableBodyRow"), 3);

    assertEquals(List.of("TableBodyCell", "TableBodyCell", "TableBodyCell"), childTypes(row));
    assertEquals(List.of(), ContentElementFactory.create(module("TableBodyRow"), 0).getChildren());
    assertEquals(List.of("TableHeadCell", "TableHeadCell"), childTypes(ContentElementFactory.create(module("TableHeadRow"), 2)));
    assertEquals(List.of("TableFootCell"), childTypes(ContentElementFactory.create(module("TableFootRow"), 1)));
  }

  @Test
  void theStructuralElementsComeWithTheirParts() {
    assertEquals(List.of("GridRow"), childTypes(create("Grid")));
    assertEquals(List.of("GridColumn"), childTypes(create("GridRow")));
    assertEquals(List.of("ExpandableCollapsed", "ExpandableExpanded"), childTypes(create("Expandable")));
    assertEquals(List.of("ListItem", "ListItem", "ListItem"), childTypes(create("OrderedList")));
    assertEquals(List.of("ListItem", "ListItem", "ListItem"), childTypes(create("UnorderedList")));
    assertEquals(List.of("Paragraph"), childTypes(create("ListItem")));
    assertEquals(List.of("InteractiveListItem", "InteractiveListItem", "InteractiveListItem"), childTypes(create("InteractiveList")));
    assertEquals(List.of("Box"), childTypes(create("InteractiveTile")));
    assertEquals(List.of("Button", "Button"), childTypes(create("ButtonGroup")));
    ContentElement container = create("ButtonGroupContainer");
    assertEquals(List.of("ButtonGroup", "ButtonGroup"), childTypes(container));
    assertEquals(List.of("left", "right"), container.getChildren().stream().map(group -> group.getProps().get("alignment")).toList());
  }

  @Test
  void theTilesHeadingParagraphSaysTileHeading() {
    ContentElement box = create("InteractiveTile").getChildren().get(0);

    assertEquals("Tile Heading", LexicalText.getText(box.getChildren().get(0)));
  }

  @Test
  void everyNewElementIsAValidParentAndAllIdsAreUnique() {
    Set<String> ids = new HashSet<>();
    for (ContentModule module : ContentElementLibrary.modules()) {
      ContentElement element = create(module.type());
      collectIds(element, ids);
      // Rows get their cells from the table, so their (no children) rule is not applied to them
      if (!module.type().endsWith("Row")) {
        List<Node> nodes = element.getChildren().stream().map(ContentElementFactoryTest::simplified).toList();
        assertTrue(ContentRuleEvaluator.matches(nodes, module.childRule()),
            module.type() + " is created with children " + nodes.stream().map(Node::moduleId).toList());
      }
    }
  }

  private static Node simplified(ContentElement element) {
    return new Node(ContentModule.moduleId(element.getNamespace(), element.getType()),
        element.getChildren().stream().map(ContentElementFactoryTest::simplified).toList());
  }

  private static void collectIds(ContentElement element, Set<String> ids) {
    assertTrue(ids.add(element.getId()), "duplicate id " + element.getId());
    element.getChildren().forEach(child -> collectIds(child, ids));
  }
}
