package de.a12.studio.ui.editors.documentmodel.commands;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.RuleConfig;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.ui.util.commandstack.CommandStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @Test
  void renameAlsoRewritesTheReferencesOfOtherModelsAsPartOfTheSameStep() {
    QueryModel query = query("Shop_DM", "/Shop/Order/Amount");
    QueryModel unrelated = query("Other_DM", "/Shop/Order/Amount");
    RecordingStore store = new RecordingStore(model, query, unrelated);

    commandStack.execute(new RefactoringCommand(model, new RenameElementCommand(amount, "Total"), store));

    assertRule("../Total", "[Total] > 100");
    assertEquals(List.of("/Shop/Order/Total"), query.getContent().getFields());
    assertEquals(List.of("/Shop/Order/Amount"), unrelated.getContent().getFields());
    assertEquals(List.of(query), store.changed);

    commandStack.undo();

    assertEquals("Amount", amount.getName());
    assertRule("../Amount", "[Amount] > 100");
    assertEquals(List.of("/Shop/Order/Amount"), query.getContent().getFields());
    assertEquals(List.of(query, query), store.changed);

    commandStack.redo();

    assertRule("../Total", "[Total] > 100");
    assertEquals(List.of("/Shop/Order/Total"), query.getContent().getFields());
    assertEquals(List.of(query, query, query), store.changed);
  }

  @Test
  void aModelThatWasReloadedInTheMeantimeIsNotWrittenToOnUndoOrRedo() {
    QueryModel query = query("Shop_DM", "/Shop/Order/Amount");
    RecordingStore store = new RecordingStore(model, query);

    commandStack.execute(new RefactoringCommand(model, new RenameElementCommand(amount, "Total"), store));
    assertEquals(List.of("/Shop/Order/Total"), query.getContent().getFields());

    store.replaceWithReloadedCopies();
    commandStack.undo();

    // The old instance is nobody's model any more: it keeps what it had, and nothing was saved for it.
    assertEquals(List.of("/Shop/Order/Total"), query.getContent().getFields());
    assertEquals(List.of(query), store.changed);
    assertEquals("Amount", amount.getName());
    assertRule("../Amount", "[Amount] > 100");
  }

  @Test
  void withoutAStoreOnlyTheModelItselfIsRefactored() {
    QueryModel query = query("Shop_DM", "/Shop/Order/Amount");

    commandStack.execute(new RefactoringCommand(model, new RenameElementCommand(amount, "Total")));

    assertRule("../Total", "[Total] > 100");
    assertEquals(List.of("/Shop/Order/Amount"), query.getContent().getFields());
    assertTrue(commandStack.canUndo());
  }

  private static QueryModel query(String targetModel, String field) {
    QueryModelContent content = new QueryModelContent();
    content.setTargetDocumentModel(targetModel);
    content.getFields().add(field);
    QueryModel query = new QueryModel();
    query.setId("Query_for_" + targetModel);
    query.setContent(content);
    return query;
  }

  /** A project of in-memory models that remembers which of them were reported as changed, in order. */
  private static final class RecordingStore implements ProjectModelStore {

    private List<A12Model<?>> models;
    final List<A12Model<?>> changed = new ArrayList<>();

    RecordingStore(A12Model<?>... models) {
      this.models = new ArrayList<>(List.of(models));
    }

    @Override
    public List<A12Model<?>> models() {
      return models;
    }

    @Override
    public void changed(A12Model<?> model) {
      changed.add(model);
    }

    /** What a reload from disk does: same ids, new instances. */
    void replaceWithReloadedCopies() {
      List<A12Model<?>> reloaded = new ArrayList<>();
      for (A12Model<?> model : models) {
        reloaded.add(model instanceof QueryModel ? query(((QueryModel) model).getContent().getTargetDocumentModel(),
            "/Shop/Order/Total") : model);
      }
      models = reloaded;
    }
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
