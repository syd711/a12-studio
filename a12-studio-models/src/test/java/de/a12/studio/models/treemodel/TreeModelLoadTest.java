package de.a12.studio.models.treemodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.ModelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeModelLoadTest {

  @Test
  void loadsTreeModel() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), "/treemodel/TreeModel.json", TreeModel.class);

    assertEquals("TreeModel", model.getId());
    assertEquals(ModelType.TREE, model.getModelType());
    assertEquals("11.0.0", model.getModelVersion());
    assertEquals("document-model-for-tree", model.getModelReferences().get(0).getPurpose());

    TreeModelContent content = model.getContent();
    assertNotNull(content);
    assertEquals("column-025fb", content.getConfiguration().getHierarchicalColumnRef());
    assertEquals("level_by_level", content.getConfiguration().getExpansionStrategy().getType());
    assertTrue(content.getSubHeaderBox().getLeftSlot().isEmpty());

    assertEquals(1, content.getNodes().size());
    TreeNode node = content.getNodes().get(0);
    assertEquals("node-ce5b8", node.getId());
    assertEquals("Company_DM", node.getDocumentModelRef());
    assertEquals(1, node.getColumns().size());
    assertEquals("column-025fb", node.getColumns().get(0).getColumnRef());
    assertEquals("abc6a6767a60488754aace2accb73824_field_cdaf9", node.getColumns().get(0).getElementRef());

    TreeNodeAction action = node.getActions().get(0);
    assertTrue(action.getDestructive());
    assertEquals("event", action.getType());
    assertEquals("event_delete_node", action.getEvent());
    assertEquals("delete_forever", action.getIcon().getName());
    assertEquals("Delete", action.getLabel().get(1).getText());
    assertEquals("Delete Node", action.getConfirmation().getTitle().get(1).getText());

    assertEquals(1, content.getColumns().size());
    TreeColumn column = content.getColumns().get(0);
    assertEquals("column-025fb", column.getId());
    assertEquals("Name", column.getName());
    assertEquals(1, column.getWidth());
    assertEquals("left", column.getPinDirection());
    assertFalse(column.getFixedWidth());
  }

  @Test
  void roundTripsTreeModel() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/treemodel/TreeModel.json", TreeModel.class);
  }
}
