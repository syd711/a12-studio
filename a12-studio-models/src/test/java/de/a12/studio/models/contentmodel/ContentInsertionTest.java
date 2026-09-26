package de.a12.studio.models.contentmodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.contentmodel.ContentInsertion.Position;
import de.a12.studio.models.contentmodel.ContentRuleEvaluator.Node;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentInsertionTest {

  private static ContentElement el(String type, ContentElement... children) {
    ContentElement element = new ContentElement();
    element.setId(type + "-" + ContentElementDefaults.newId());
    element.setNamespace(ContentElementLibrary.NAMESPACE);
    element.setType(type);
    element.setChildren(new ArrayList<>(List.of(children)));
    return element;
  }

  private static ContentElement form(String type) {
    ContentElement element = el(type);
    element.setNamespace(ContentElementLibrary.FORM_ELEMENTS_NAMESPACE);
    return element;
  }

  private static Set<String> types(ContentElement root, ContentElement target, Position position) {
    return ContentInsertion.insertableModules(root, target, position).stream()
        .map(ContentModule::type).collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
  }

  private static Set<String> childTypes(ContentElement root, ContentElement target) {
    return types(root, target, Position.AS_CHILD);
  }

  @Test
  void aBoxTakesContentButNotPartsOfOtherElements() {
    ContentElement box = el("Box", el("Paragraph"));

    Set<String> types = childTypes(box, box);

    assertTrue(types.containsAll(Set.of("Box", "Paragraph", "Heading", "Table", "Grid", "Button", "TextLine", "Group", "Conditional")));
    assertFalse(types.contains("GridRow"));
    assertFalse(types.contains("GridColumn"));
    assertFalse(types.contains("TableHead"));
    assertFalse(types.contains("ListItem"));
    assertFalse(types.contains("ExpandableTitle"));
    assertFalse(types.contains("ButtonGroup"));
  }

  @Test
  void leafElementsTakeNothing() {
    ContentElement box = el("Box", el("Paragraph"), form("TextLine"), el("Button"));

    for (ContentElement leaf : box.getChildren()) {
      assertEquals(Set.of(), childTypes(box, leaf), leaf.getType());
    }
  }

  @Test
  void aGridTakesRowsAndGroupsAndConditionalsOnly() {
    ContentElement grid = el("Grid", el("GridRow", el("GridColumn")));

    assertEquals(Set.of("GridRow", "Group", "Conditional"), childTypes(grid, grid));
  }

  @Test
  void aGridRowTakesColumnsAndAColumnAlmostAnything() {
    ContentElement column = el("GridColumn");
    ContentElement row = el("GridRow", column);
    ContentElement grid = el("Grid", row);

    assertEquals(Set.of("GridColumn"), childTypes(grid, row));
    Set<String> inColumn = childTypes(grid, column);
    assertTrue(inColumn.containsAll(Set.of("Box", "Paragraph", "GridRow", "Grid")));
    assertFalse(inColumn.contains("GridColumn"));
  }

  @Test
  void aCompleteTableTakesNothingMore() {
    ContentElement table = el("Table", el("TableHead"), el("TableBody"), el("TableFoot"));
    ContentElement box = el("Box", table);

    assertEquals(Set.of(), childTypes(box, table));
  }

  @Test
  void aTableTakesItsMissingSectionInOrderOnly() {
    ContentElement table = el("Table", el("TableHead"), el("TableBody"));
    ContentElement box = el("Box", table);

    assertEquals(Set.of("TableFoot"), childTypes(box, table));
    // The foot has to come last: nothing goes above the head
    assertEquals(Set.of(), types(box, table.getChildren().get(0), Position.ABOVE));
    assertEquals(Set.of(), types(box, table.getChildren().get(0), Position.BELOW));
    assertEquals(Set.of("TableFoot"), types(box, table.getChildren().get(1), Position.BELOW));
  }

  @Test
  void aTableHeadTakesOneRow() {
    ContentElement head = el("TableHead");
    ContentElement table = el("Table", head, el("TableBody"), el("TableFoot"));

    assertEquals(Set.of("TableHeadRow"), childTypes(table, head));

    head.getChildren().add(el("TableHeadRow"));

    assertEquals(Set.of(), childTypes(table, head));
  }

  @Test
  void aTableBodyTakesRowsOrOneGroupOfOneRow() {
    ContentElement body = el("TableBody");
    ContentElement table = el("Table", el("TableHead"), body, el("TableFoot"));

    assertEquals(Set.of("TableBodyRow", "Group"), childTypes(table, body));

    body.getChildren().add(el("TableBodyRow"));

    assertEquals(Set.of("TableBodyRow"), childTypes(table, body));
  }

  @Test
  void aGroupInsideATableBodyTakesOneRow() {
    ContentElement group = el("Group");
    ContentElement body = el("TableBody", group);
    ContentElement table = el("Table", el("TableHead"), body, el("TableFoot"));

    assertEquals(Set.of("TableBodyRow"), childTypes(table, group));

    group.getChildren().add(el("TableBodyRow"));

    assertEquals(Set.of(), childTypes(table, group));
  }

  @Test
  void aGroupIsLookedThroughSoItsChildrenGetTheGroupsParentsRules() {
    ContentElement group = el("Group", el("GridRow"));
    ContentElement grid = el("Grid", group);

    // Adding into the group behaves like adding into the grid
    assertEquals(Set.of("GridRow", "Group", "Conditional"), childTypes(grid, group));
    // ... and next to a row inside the group too
    assertEquals(Set.of("GridRow", "Group", "Conditional"), types(grid, group.getChildren().get(0), Position.BELOW));
  }

  @Test
  void insertingNextToAnElementUsesItsParent() {
    ContentElement column = el("GridColumn");
    ContentElement row = el("GridRow", column);
    ContentElement grid = el("Grid", row);

    assertEquals(Set.of("GridColumn"), types(grid, column, Position.ABOVE));
    assertEquals(Set.of("GridColumn"), types(grid, column, Position.BELOW));
    assertEquals(Set.of("GridRow", "Group", "Conditional"), types(grid, row, Position.BELOW));
  }

  @Test
  void nothingGoesNextToTheRoot() {
    ContentElement box = el("Box");

    assertEquals(Set.of(), types(box, box, Position.ABOVE));
    assertEquals(Set.of(), types(box, box, Position.BELOW));
    assertFalse(ContentInsertion.canInsert(box, box, Position.BELOW));
    assertTrue(ContentInsertion.canInsert(box, box, Position.AS_CHILD));
  }

  @Test
  void anExpandableIsCompleteWithItsTwoStates() {
    ContentElement collapsed = el("ExpandableCollapsed", el("ExpandableTitle"));
    ContentElement expanded = el("ExpandableExpanded", el("ExpandableTitle"));
    ContentElement expandable = el("Expandable", collapsed, expanded);

    assertEquals(Set.of(), childTypes(expandable, expandable));
    assertEquals(Set.of(), childTypes(expandable, collapsed));
    assertEquals(Set.of("ExpandableContent"), childTypes(expandable, expanded));

    ContentElement half = el("Expandable", el("ExpandableCollapsed", el("ExpandableTitle")));
    assertEquals(Set.of("ExpandableExpanded"), childTypes(half, half));
  }

  @Test
  void aButtonGroupContainerNeedsExactlyTwoGroups() {
    ContentElement container = el("ButtonGroupContainer", el("ButtonGroup"));
    ContentElement box = el("Box", container);

    assertEquals(Set.of("ButtonGroup"), childTypes(box, container));

    container.getChildren().add(el("ButtonGroup"));

    assertEquals(Set.of(), childTypes(box, container));
  }

  @Test
  void aButtonGroupTakesButtons() {
    ContentElement group = el("ButtonGroup", el("Button"));
    ContentElement container = el("ButtonGroupContainer", group, el("ButtonGroup"));

    assertEquals(Set.of("Button", "Group", "Conditional"), childTypes(container, group));
  }

  @Test
  void formElementsTakeNoChildrenButAMessageGroupContainerTakesAnything() {
    ContentElement box = el("Box", form("TextLine"), form("MessageGroupContainer"));

    assertEquals(Set.of(), childTypes(box, box.getChildren().get(0)));
    assertTrue(childTypes(box, box.getChildren().get(1)).contains("TextLine"));
  }

  @Test
  void unknownParentTypesTakeNothing() {
    ContentElement custom = el("SomethingFromAPlugin");
    ContentElement box = el("Box", custom);

    assertEquals(Set.of(), childTypes(box, custom));
  }

  @Test
  void actionsAreNeverOffered() {
    ContentElement box = el("Box");

    Set<String> types = childTypes(box, box);

    for (String action : List.of("SaveAction", "CancelAction", "CommitAction", "AddRowAction", "DeleteRowAction")) {
      assertFalse(types.contains(action), action);
    }
  }

  @Test
  void theDialogListsCategoriesInSmesOrder() {
    ContentElement box = el("Box");

    List<ContentModule> modules = ContentInsertion.insertableModules(box, box, Position.AS_CHILD);

    List<String> categories = modules.stream().map(ContentModule::category).distinct().toList();
    assertEquals(List.of("Layout", "Content", "General", "Form Elements"), categories);
    assertEquals(List.of("MediaQuery", "Box", "Expandable", "InteractiveTile", "Grid"),
        modules.stream().filter(module -> module.category().equals("Layout")).map(ContentModule::type).toList());
    // Listed elements first, in SME's order; the rest by id
    List<String> content = modules.stream().filter(module -> module.category().equals("Content")).map(ContentModule::type).toList();
    assertEquals(List.of("Heading", "Paragraph", "MessageBox", "OrderedList", "UnorderedList", "Image", "Video", "Link", "Icon", "Tooltip"),
        content.subList(0, 10));
    assertEquals(List.of("Conditional", "FieldOutput", "Group", "InteractiveList", "Table"), content.subList(10, content.size()));
    assertEquals("TextArea", modules.stream().filter(module -> module.category().equals("Form Elements"))
        .map(ContentModule::type).toList().get(10));
  }

  @Test
  void everyExistingElementOfARealModelConformsToItsParentsRule() throws Exception {
    ContentModel model = ModelRoundTrip.load(ContentInsertionTest.class, "/contentmodel/WelcomePage_CM.json", ContentModel.class);

    assertConforms(model.getContent().getRoot());
  }

  /** The child lists of a real model must pass the rules, or the port of SME's rules is wrong somewhere. */
  private static void assertConforms(ContentElement element) {
    if (element.getChildren() == null) {
      return;
    }
    // Table rows get their cells from the table's columns, they are not "inserted" into the row
    if (element.getType() != null && !element.getType().endsWith("Row")) {
      ContentModule module = ContentElementLibrary.find(element).orElse(null);
      if (module != null) {
        List<Node> nodes = simplified(element, module.keepTransitiveChildren());
        assertTrue(ContentRuleEvaluator.matches(nodes, module.childRule()),
            element.getType() + " " + element.getId() + " has children " + nodes.stream().map(Node::moduleId).toList());
      }
    }
    element.getChildren().forEach(ContentInsertionTest::assertConforms);
  }

  private static List<Node> simplified(ContentElement parent, boolean keepTransitive) {
    List<Node> nodes = new ArrayList<>();
    for (ContentElement child : parent.getChildren()) {
      if (ContentElementLibrary.isTransitive(child) && !keepTransitive) {
        nodes.addAll(simplified(child, false));
      }
      else {
        nodes.add(new Node(ContentModule.moduleId(child.getNamespace() != null ? child.getNamespace() : ContentElementLibrary.NAMESPACE, child.getType()),
            child.getChildren() == null ? List.of() : simplified(child, keepTransitive)));
      }
    }
    return nodes;
  }
}
