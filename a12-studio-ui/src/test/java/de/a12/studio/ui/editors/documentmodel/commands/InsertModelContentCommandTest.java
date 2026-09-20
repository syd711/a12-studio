package de.a12.studio.ui.editors.documentmodel.commands;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.ui.util.commandstack.CommandStack;
import de.a12.studio.ui.util.commandstack.CompositeCommand;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The undoable steps behind "Insert from Document Model" and the tree's bulk delete/paste. */
class InsertModelContentCommandTest {

  @Test
  void insertsGroupsTypeDefinitionsAndImportsAndTakesAllOfThemBackOnUndo() {
    DocumentModel model = model();
    List<Element> siblings = new ArrayList<>(List.of(group("First"), group("Last")));
    GroupElement inserted = group("Inserted");
    TypeDefinition typeDefinition = typeDefinition("typedef_1");
    ModelReference reference = new ModelReference();
    reference.setPurpose(ModelReference.PURPOSE_TYPE_DEFINITIONS);
    reference.setReference("Types_TdM");
    CommandStack stack = new CommandStack();

    stack.execute(new InsertModelContentCommand(model, siblings, 1, List.of(inserted), List.of(typeDefinition), List.of(reference)));

    assertEquals(List.of("First", "Inserted", "Last"), names(siblings));
    assertEquals(List.of(typeDefinition), model.getContent().getTypeDefinitions());
    assertEquals(List.of(reference), model.getModelReferences());

    stack.undo();
    assertEquals(List.of("First", "Last"), names(siblings));
    assertTrue(model.getContent().getTypeDefinitions().isEmpty());
    assertTrue(model.getModelReferences().isEmpty());

    stack.redo();
    assertEquals(List.of("First", "Inserted", "Last"), names(siblings));
    assertSame(typeDefinition, model.getContent().getTypeDefinitions().get(0));
  }

  @Test
  void anIndexBeyondTheEndAppends() {
    DocumentModel model = model();
    List<Element> siblings = new ArrayList<>(List.of(group("Only")));

    new InsertModelContentCommand(model, siblings, 99, List.of(group("A"), group("B")), List.of(), List.of()).execute();

    assertEquals(List.of("Only", "A", "B"), names(siblings));
  }

  @Test
  void aBulkDeleteIsOneUndoStepThatRestoresTheOriginalOrder() {
    List<Element> siblings = new ArrayList<>(List.of(group("A"), group("B"), group("C"), group("D"), group("E")));
    Element b = siblings.get(1);
    Element d = siblings.get(3);
    CommandStack stack = new CommandStack();

    stack.execute(new CompositeCommand(List.of(new DeleteNodeCommand<>(siblings, b), new DeleteNodeCommand<>(siblings, d))));

    assertEquals(List.of("A", "C", "E"), names(siblings));
    stack.undo();
    assertEquals(List.of("A", "B", "C", "D", "E"), names(siblings));
    assertTrue(!stack.canUndo(), "the whole bulk delete was a single step");
    stack.redo();
    assertEquals(List.of("A", "C", "E"), names(siblings));
  }

  @Test
  void aBulkPasteIsOneUndoStep() {
    List<Element> siblings = new ArrayList<>(List.of(group("A"), group("Z")));
    CommandStack stack = new CommandStack();

    stack.execute(new CompositeCommand(List.of(
        new AddNodeCommand<>(siblings, group("P1"), 1),
        new AddNodeCommand<>(siblings, group("P2"), 2))));

    assertEquals(List.of("A", "P1", "P2", "Z"), names(siblings));
    stack.undo();
    assertEquals(List.of("A", "Z"), names(siblings));
    assertTrue(!stack.canUndo());
  }

  private static DocumentModel model() {
    DocumentModel model = new DocumentModel();
    model.setId("Target_DM");
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(new ModelRoot());
    model.setContent(content);
    return model;
  }

  private static GroupElement group(String name) {
    GroupElement group = new GroupElement();
    group.setId("group_" + name);
    group.setName(name);
    group.setGroup(new GroupConfig());
    return group;
  }

  private static TypeDefinition typeDefinition(String id) {
    TypeDefinition typeDefinition = new TypeDefinition();
    typeDefinition.setId(id);
    typeDefinition.setName(id);
    typeDefinition.setFieldType(new StringFieldType());
    return typeDefinition;
  }

  private static List<String> names(List<? extends Element> elements) {
    return elements.stream().map(Element::getName).toList();
  }
}
