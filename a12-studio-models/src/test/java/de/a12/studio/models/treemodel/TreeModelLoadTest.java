package de.a12.studio.models.treemodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.overviewmodel.BoxElementType;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.ExpandAllPopupElement;
import de.a12.studio.models.overviewmodel.MultiSelectionElement;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

  @Test
  void loadsAndRoundTripsTheSubheaderAndFooterElements() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), "/treemodel/TreeModelActions.json", TreeModel.class);

    ElementBox subHeader = model.getContent().getSubHeaderBox();
    assertEquals(2, subHeader.getLeftSlot().size());
    assertInstanceOf(ExpandAllPopupElement.class, subHeader.getLeftSlot().get(0));
    assertEquals(BoxElementType.EXPAND_ALL_POPUP, subHeader.getLeftSlot().get(0).getType());
    assertInstanceOf(MultiSelectionElement.class, subHeader.getLeftSlot().get(1));
    ButtonElement button = assertInstanceOf(ButtonElement.class, subHeader.getRightSlot().get(0));
    assertEquals("button-026dc", button.getId());
    assertEquals("event_add_root_node", button.getEvent());
    assertEquals("add", button.getIconName());

    ButtonElement footerButton = assertInstanceOf(ButtonElement.class, model.getContent().getFooterBox().getRightSlot().get(0));
    assertEquals("button-8c1f2", footerButton.getId());
    assertTrue(footerButton.getPrimary());

    ModelRoundTrip.assertRoundTrip(getClass(), "/treemodel/TreeModelActions.json", TreeModel.class);
  }

  @Test
  void loadsAndRoundTripsTheTreeStrategyExpansionDepths() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), "/treemodel/TreeModelTreeStrategy.json", TreeModel.class);

    ExpansionStrategy strategy = model.getContent().getConfiguration().getExpansionStrategy();
    assertEquals(ExpansionStrategy.TREE, strategy.getType());
    assertEquals(2, strategy.getExpansionDepths().size());
    assertEquals("TeamTeam_Re", strategy.getExpansionDepths().get(0).getRelationshipModel());
    assertEquals(5, strategy.getExpansionDepths().get(0).getMaxDepth());
    assertEquals(1, strategy.getExpansionDepths().get(1).getMaxDepth());
    assertTrue(strategy.getExtras().isEmpty(), "expansionDepths is a real property, not an extra");

    ModelRoundTrip.assertRoundTrip(getClass(), "/treemodel/TreeModelTreeStrategy.json", TreeModel.class);
  }

  @Test
  void aLevelByLevelStrategyWithoutDepthsDoesNotGainTheKey() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), "/treemodel/TreeModel.json", TreeModel.class);

    assertNull(model.getContent().getConfiguration().getExpansionStrategy().getExpansionDepths());
  }

  @Test
  void rootHideLabelAndStylesAreTypedFieldsThatRoundTrip() throws Exception {
    ObjectNode tree = (ObjectNode) JsonSettings.objectMapper.readTree(
        ModelRoundTrip.readResource(getClass(), "/treemodel/TreeModel.json"));
    ObjectNode content = (ObjectNode) tree.get("content");
    ((ObjectNode) content.get("configuration")).put("rootRef", "crc-1").put("labelHidden", true);
    content.putArray("styles").add("h_semiBoldFontWeight");

    TreeModel model = JsonSettings.objectMapper.readValue(tree.toString(), TreeModel.class);

    assertEquals("crc-1", model.getContent().getConfiguration().getRootRef());
    assertEquals(Boolean.TRUE, model.getContent().getConfiguration().getLabelHidden());
    assertEquals(java.util.List.of("h_semiBoldFontWeight"), model.getContent().getStyles());
    assertFalse(model.getContent().getExtras().containsKey("styles"));
    assertFalse(model.getContent().getConfiguration().getExtras().containsKey("rootRef"));

    JsonNode resaved = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model));
    assertEquals(content, resaved.get("content"));
  }

  @Test
  void absentRootHideLabelAndStylesStayAbsent() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), "/treemodel/TreeModel.json", TreeModel.class);
    assertNull(model.getContent().getConfiguration().getRootRef());
    assertNull(model.getContent().getConfiguration().getLabelHidden());
    assertTrue(model.getContent().getStyles().isEmpty());

    JsonNode content = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model)).get("content");
    assertFalse(content.has("styles"));
    assertFalse(content.get("configuration").has("rootRef"));
    assertFalse(content.get("configuration").has("labelHidden"));
  }
}
