package de.a12.studio.ui.editors.documentmodel.commands;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.RuleConfig;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.ui.util.commandstack.CommandStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A rename or move plus the reference rewrite it triggers is one step on the {@link CommandStack}. */
class RefactoringCommandTest {

  private final FieldElement amount = field("Amount");
  private final RuleElement rule = rule("AmountRule", "../Amount", "[Amount] > 100");
  private final GroupElement order = group("Order", amount, rule);
  private final GroupElement archive = group("Archive");
  private final GroupElement shop = group("Shop", order, archive);
  private final DocumentModel model = model(shop);
  private final CommandStack commandStack = new CommandStack();

  @Test
  void renameRewritesReferencesAndUndoRedoTreatBothAsOneStep() {
    commandStack.execute(new RefactoringCommand(model, new RenameElementCommand(amount, "Total")));

    assertEquals("Total", amount.getName());
    assertRule("../Total", "[Total] > 100");

    commandStack.undo();

    assertEquals("Amount", amount.getName());
    assertRule("../Amount", "[Amount] > 100");

    commandStack.redo();

    assertEquals("Total", amount.getName());
    assertRule("../Total", "[Total] > 100");
  }

  @Test
  void moveRewritesReferencesAndUndoRedoTreatBothAsOneStep() {
    commandStack.execute(new RefactoringCommand(model,
        new MoveNodeCommand(children(order), children(archive), amount, 0)));

    assertEquals(List.of(archive.getGroup().getElements().get(0)), List.of(amount));
    assertRule("../../Archive/Amount", "[../Archive/Amount] > 100");

    commandStack.undo();

    assertEquals(List.of(amount, rule), children(order));
    assertRule("../Amount", "[Amount] > 100");

    commandStack.redo();

    assertRule("../../Archive/Amount", "[../Archive/Amount] > 100");
  }

  @Test
  void aCommandNotWrappedLeavesTheReferencesAlone() {
    commandStack.execute(new RenameElementCommand(amount, "Total"));

    assertRule("../Amount", "[Amount] > 100");
  }

  private void assertRule(String errorEntityRelPath, String errorCondition) {
    assertEquals(errorEntityRelPath, rule.getRule().getErrorEntityRelPath());
    assertEquals(errorCondition, rule.getRule().getErrorCondition());
  }

  private static List<Element> children(GroupElement group) {
    return group.getGroup().getElements();
  }

  private static FieldElement field(String name) {
    FieldElement field = new FieldElement();
    field.setId("field_" + name);
    field.setName(name);
    return field;
  }

  private static RuleElement rule(String name, String errorEntityRelPath, String errorCondition) {
    RuleElement rule = new RuleElement();
    rule.setId("rule_" + name);
    rule.setName(name);
    RuleConfig config = new RuleConfig();
    config.setErrorEntityRelPath(errorEntityRelPath);
    config.setErrorCondition(errorCondition);
    rule.setRule(config);
    return rule;
  }

  private static GroupElement group(String name, Element... elements) {
    GroupElement group = new GroupElement();
    group.setId("group_" + name);
    group.setName(name);
    GroupConfig config = new GroupConfig();
    config.setElements(new ArrayList<>(List.of(elements)));
    group.setGroup(config);
    return group;
  }

  private static DocumentModel model(GroupElement rootGroup) {
    ModelRoot root = new ModelRoot();
    root.setRootGroups(new ArrayList<>(List.of(rootGroup)));
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(root);
    DocumentModel model = new DocumentModel();
    model.setId("Shop_DM");
    model.setContent(content);
    return model;
  }
}
