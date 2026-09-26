package de.a12.studio.models.contentmodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.contentmodel.ContentInsertion.Position;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The parent and child rules applied to a whole tree and to the edits SME's editor guards (move, cut, duplicate, paste).
 * Trees are built with {@link ContentElementFactory}, so they are what SME's insert creates.
 */
class ContentStructureTest {

  private static ContentElement create(String type) {
    return ContentElementFactory.create(ContentElementLibrary.find(ContentElementLibrary.NAMESPACE, type).orElseThrow(), 0);
  }

  private static ContentElement box(ContentElement... children) {
    ContentElement box = create("Box");
    box.getChildren().addAll(List.of(children));
    return box;
  }

  private static ContentElement child(ContentElement parent, int... path) {
    ContentElement current = parent;
    for (int index : path) {
      current = current.getChildren().get(index);
    }
    return current;
  }

  // ---- violations ----

  @Test
  void aTreeBuiltByTheInsertFactoryHasNoViolations() {
    ContentElement root = box(create("Table"), create("Grid"), create("OrderedList"), create("Expandable"),
        create("ButtonGroupContainer"), create("InteractiveTile"), create("InteractiveList"), create("Paragraph"));

    assertEquals(List.of(), ContentStructure.violations(root));
  }

  @Test
  void theFixtureTreesHaveNoViolations() throws Exception {
    ContentModel welcome = ModelRoundTrip.load(getClass(), "/contentmodel/WelcomePage_CM.json", ContentModel.class);

    assertEquals(List.of(), ContentStructure.violations(welcome.getContent().getRoot()),
        "real tables have cells in their rows, which SME's own rule for rows would not allow");
  }

  @Test
  void anElementInsideTheWrongParentIsReportedWithThatParent() {
    ContentElement head = create("TableHead");
    ContentElement root = box(head);

    List<ContentStructure.Violation> violations = ContentStructure.violations(root);

    assertEquals(1, violations.size());
    assertEquals(head, violations.get(0).element());
    assertEquals(ContentStructure.Kind.PARENT_NOT_ALLOWED, violations.get(0).kind());
    assertEquals("Box", violations.get(0).parentType());
  }

  @Test
  void childrenThatBreakTheChildRuleAreReportedOnTheParent() {
    ContentElement table = create("Table");
    table.getChildren().remove(2);
    ContentElement root = box(table);

    List<ContentStructure.Violation> violations = ContentStructure.violations(root);

    assertEquals(1, violations.size());
    assertEquals(table, violations.get(0).element());
    assertEquals(ContentStructure.Kind.CHILDREN_NOT_ALLOWED, violations.get(0).kind());
    assertEquals(List.of("TableHead", "TableBody"), violations.get(0).childTypes());
  }

  @Test
  void aGridMayHoldOnlyRowsAndTheGroupsAndConditionalsAroundThem() {
    ContentElement grid = create("Grid");
    ContentElement conditional = create("Conditional");
    conditional.getChildren().add(create("GridRow"));
    grid.getChildren().add(conditional);
    ContentElement root = box(grid);
    assertEquals(List.of(), ContentStructure.violations(root), "a Conditional is looked through");

    grid.getChildren().add(create("Paragraph"));

    assertEquals(1, ContentStructure.violations(root).size());
    assertEquals(ContentStructure.Kind.CHILDREN_NOT_ALLOWED, ContentStructure.violations(root).get(0).kind());
  }

  @Test
  void aRepeatableGroupAroundTheBodyRowsOfATableIsFine() {
    ContentElement table = create("Table");
    ContentElement body = child(table, 1);
    ContentElement group = create("Group");
    group.getChildren().add(body.getChildren().remove(0));
    body.getChildren().clear();
    body.getChildren().add(group);

    assertEquals(List.of(), ContentStructure.violations(box(table)));
  }

  @Test
  void anElementOfAnUnknownTypeIsNotChecked() {
    ContentElement plugin = create("Box");
    plugin.setNamespace("com.example.plugin");
    plugin.setType("Fancy");
    plugin.getChildren().add(create("TableHead"));

    // the TableHead below it still has a parent the rules see, and that one is not allowed
    List<ContentStructure.Violation> violations = ContentStructure.violations(box(plugin));

    assertEquals(1, violations.size());
    assertEquals("TableHead", violations.get(0).element().getType());
  }

  // ---- edits ----

  @Test
  void theRequiredPartsOfATableCannotBeRemovedMovedOrCopied() {
    ContentElement table = create("Table");
    ContentElement root = box(table);
    ContentElement head = child(table, 0);
    ContentElement body = child(table, 1);

    assertFalse(ContentStructure.canRemove(root, head));
    assertFalse(ContentStructure.canRemove(root, body));
    assertFalse(ContentStructure.canMove(root, head, 1));
    assertFalse(ContentStructure.canMove(root, body, -1));
    assertFalse(ContentStructure.canDuplicate(root, head));
    assertFalse(ContentStructure.canRemove(root, root), "the root has no parent to be removed from");
  }

  @Test
  void theRowsOfATableBodyCanBeRemovedMovedAndCopied() {
    ContentElement table = create("Table");
    ContentElement root = box(table);
    ContentElement firstRow = child(table, 1, 0);

    assertTrue(ContentStructure.canRemove(root, firstRow));
    assertTrue(ContentStructure.canMove(root, firstRow, 1));
    assertFalse(ContentStructure.canMove(root, firstRow, -1), "it is the first one already");
    assertTrue(ContentStructure.canDuplicate(root, firstRow), "the cells of the row do not count");
  }

  @Test
  void theOnlyHeadRowCannotBeCopiedButCanBeRemoved() {
    ContentElement table = create("Table");
    ContentElement root = box(table);
    ContentElement headRow = child(table, 0, 0);

    assertFalse(ContentStructure.canDuplicate(root, headRow), "a head has at most one row");
    assertTrue(ContentStructure.canRemove(root, headRow));
  }

  @Test
  void siblingsThatAreNotBoundByOrderMoveFreely() {
    ContentElement first = create("Paragraph");
    ContentElement second = create("Heading");
    ContentElement root = box(first, second);

    assertTrue(ContentStructure.canMove(root, first, 1));
    assertTrue(ContentStructure.canDuplicate(root, first));
    assertTrue(ContentStructure.canRemove(root, second));
  }

  @Test
  void anExistingViolationDoesNotBlockAnUnrelatedEditButANewOneIsStillRefused() {
    ContentElement broken = create("Table");
    broken.getChildren().remove(2);
    ContentElement fine = create("Table");
    ContentElement paragraph = create("Paragraph");
    ContentElement root = box(broken, fine, paragraph);

    assertTrue(ContentStructure.canRemove(root, paragraph));
    assertTrue(ContentStructure.canMove(root, paragraph, -1));
    assertTrue(ContentStructure.canDuplicate(root, child(fine, 1, 0)), "a body row of the other table");
    assertFalse(ContentStructure.canDuplicate(root, child(fine, 0)), "a second head would break the other table");
    assertTrue(ContentStructure.canRemove(root, child(broken, 1)), "an element that is in violation already can be edited");
  }

  @Test
  void pasteAsksTheRulesOfTheTargetParent() {
    ContentElement paragraph = create("Paragraph");
    ContentElement target = create("Box");
    ContentElement root = box(target, paragraph);

    assertTrue(ContentStructure.canPaste(root, create("Paragraph"), target, Position.AS_CHILD));
    assertTrue(ContentStructure.canPaste(root, create("Paragraph"), paragraph, Position.ABOVE));
    assertTrue(ContentStructure.canPaste(root, create("Paragraph"), paragraph, Position.BELOW));
    assertFalse(ContentStructure.canPaste(root, create("Paragraph"), paragraph, Position.AS_CHILD), "a paragraph has no children");
    assertFalse(ContentStructure.canPaste(root, create("TableHead"), target, Position.AS_CHILD), "a head belongs in a table");
    assertFalse(ContentStructure.canPaste(root, create("ListItem"), root, Position.AS_CHILD));
    assertFalse(ContentStructure.canPaste(root, create("Paragraph"), root, Position.ABOVE), "nothing goes above the root");
    assertFalse(ContentStructure.canPaste(root, create("Paragraph"), create("Paragraph"), Position.AS_CHILD), "not in this tree");
  }

  @Test
  void aPastedListItemNeedsAList() {
    ContentElement list = create("UnorderedList");
    ContentElement root = box(list);
    ContentElement item = create("ListItem");

    assertTrue(ContentStructure.canPaste(root, item, list, Position.AS_CHILD));
    assertTrue(ContentStructure.canPaste(root, item, child(list, 0), Position.BELOW));
  }

  @Test
  void relocatingChecksTheNewPlaceAndNeverMovesAnElementIntoItself() {
    ContentElement paragraph = create("Paragraph");
    ContentElement inner = box(paragraph);
    ContentElement other = create("Box");
    ContentElement root = box(inner, other);

    assertTrue(ContentStructure.canRelocate(root, paragraph, other, Position.AS_CHILD));
    assertTrue(ContentStructure.canRelocate(root, paragraph, other, Position.ABOVE));
    assertFalse(ContentStructure.canRelocate(root, paragraph, paragraph, Position.BELOW));
    assertFalse(ContentStructure.canRelocate(root, inner, paragraph, Position.ABOVE), "into its own subtree");
    assertFalse(ContentStructure.canRelocate(root, inner, inner, Position.AS_CHILD));
    assertFalse(ContentStructure.canRelocate(root, root, other, Position.AS_CHILD), "the root stays");
    ContentElement table = create("Table");
    root.getChildren().add(table);
    assertFalse(ContentStructure.canRelocate(root, child(table, 1, 0), other, Position.AS_CHILD), "a body row needs a body");
  }

  // ---- consistency with the insert dialog ----

  @Test
  void whateverTheInsertDialogOffersCanBePastedAsTheElementTheFactoryBuilds() {
    // A few parents with different rules; the insert dialog's list and the paste check must not contradict each other.
    List<ContentElement> targets = new ArrayList<>();
    ContentElement table = create("Table");
    ContentElement grid = create("Grid");
    ContentElement expandable = create("Expandable");
    ContentElement list = create("OrderedList");
    ContentElement root = box(table, grid, expandable, list, create("Box"), create("Paragraph"));
    targets.addAll(List.of(root, table, grid, expandable, list, child(root, 4), child(root, 5)));

    for (ContentElement target : targets) {
      for (ContentModule module : ContentInsertion.insertableModules(root, target, Position.AS_CHILD)) {
        ContentElement created = ContentElementFactory.create(module, 0);
        assertTrue(ContentStructure.canPaste(root, created, target, Position.AS_CHILD),
            module.type() + " is offered for " + target.getType() + " but cannot be pasted there");
      }
    }
  }
}
