package de.a12.studio.models.treemodel;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelHeterogeneity;
import de.a12.studio.models.overviewmodel.Icon;
import de.a12.studio.models.treemodel.TreeNodeInheritance.Part;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeNodeInheritanceTest {

  private static DocumentModel documentModel(String id, String superTypes) {
    DocumentModel model = new DocumentModel();
    model.setId(id);
    if (superTypes != null) {
      Annotation annotation = new Annotation();
      annotation.setName(DocumentModelHeterogeneity.SUPER_TYPES_ANNOTATION);
      annotation.setValue(superTypes);
      model.getAnnotations().add(annotation);
    }
    return model;
  }

  private static TreeNode node(String documentModel) {
    TreeNode node = new TreeNode();
    node.setDocumentModelRef(documentModel);
    return node;
  }

  @Test
  void aNodeWhoseDocumentModelIsASubTypeOfAnotherNodesHasSomethingToInherit() {
    List<DocumentModel> models = List.of(documentModel("Product", null), documentModel("Book", "Product"), documentModel("Person", null));
    TreeNode product = node("Product");
    TreeNode book = node("Book");
    TreeNode person = node("Person");
    List<TreeNode> nodes = List.of(product, book, person);

    assertTrue(TreeNodeInheritance.isSubTypeNode(book, nodes, models));
    assertFalse(TreeNodeInheritance.isSubTypeNode(product, nodes, models), "the super type itself has nothing to inherit");
    assertFalse(TreeNodeInheritance.isSubTypeNode(person, nodes, models));
  }

  @Test
  void aSubTypeWithoutASuperTypeNodeHasNothingToInherit() {
    List<DocumentModel> models = List.of(documentModel("Product", null), documentModel("Book", "Product"));
    TreeNode book = node("Book");

    assertFalse(TreeNodeInheritance.isSubTypeNode(book, List.of(book), models));
  }

  @Test
  void settingAPartCreatesTheConfigurationAndClearingItKeepsTheEmptyInheritObject() {
    TreeNode node = node("Book");

    assertFalse(TreeNodeInheritance.isInherited(node, Part.ICON));
    TreeNodeInheritance.setInherited(node, Part.ICON, true);
    assertTrue(TreeNodeInheritance.isInherited(node, Part.ICON));
    assertTrue(TreeNodeInheritance.hasInheritedConfig(node));
    assertEquals(Map.of("icon", true), node.getConfiguration().get("inherit"));

    TreeNodeInheritance.setInherited(node, Part.ICON, false);
    assertFalse(TreeNodeInheritance.isInherited(node, Part.ICON));
    assertFalse(TreeNodeInheritance.hasInheritedConfig(node));
    assertEquals(Map.of(), node.getConfiguration().get("inherit"), "SME's own files carry \"inherit\": {}");
  }

  @Test
  void unsettingAPartOnANodeWithoutConfigurationLeavesItWithoutOne() {
    TreeNode node = node("Book");

    TreeNodeInheritance.setInherited(node, Part.STYLES, false);

    assertNull(node.getConfiguration());
  }

  @Test
  void clearingAPartRemovesWhatTheNodeDefinedItself() {
    TreeNode node = node("Book");
    Icon icon = new Icon();
    icon.setName("book");
    node.setIcon(icon);
    node.getStyles().add("s1");
    RowActivation rowActivation = new RowActivation();
    rowActivation.setType(RowActivation.TYPE_NON_INTERACTIVE);
    node.setRowActivation(rowActivation);
    node.setContextMenu(new TreeNodeContextMenu());
    node.getContextMenu().getGroups().add(new TreeNodeActionGroup());

    for (Part part : List.of(Part.ICON, Part.STYLES, Part.ROW_ACTIVATION, Part.CONTEXT_MENU)) {
      assertTrue(TreeNodeInheritance.hasContent(node, part), part.name());
      TreeNodeInheritance.clearContent(node, part);
      assertFalse(TreeNodeInheritance.hasContent(node, part), part.name());
    }
    assertNull(node.getIcon());
    assertNull(node.getContextMenu());
    assertNull(node.getRowActivation());
  }
}
