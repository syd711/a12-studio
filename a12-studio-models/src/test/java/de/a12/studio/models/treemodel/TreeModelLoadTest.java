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
  void loadsAndRoundTripsTheLevelByLevelInitialExpansionPageSizeAndWholeTreeFlag() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), "/treemodel/TreeModelLevelByLevelStrategy.json", TreeModel.class);

    TreeConfiguration configuration = model.getContent().getConfiguration();
    ExpansionStrategy strategy = configuration.getExpansionStrategy();
    assertEquals(ExpansionStrategy.LEVEL_BY_LEVEL, strategy.getType());
    assertEquals(InitialExpansion.LEVEL_LIMIT, strategy.getInitialExpansion().getType());
    assertEquals(2, strategy.getInitialExpansion().getLevel());
    assertEquals(java.util.List.of("node-ce5b8"), strategy.getInitialExpansion().getAffectedNodeRefs());
    assertEquals(5, strategy.getPageSize());
    assertEquals(Boolean.TRUE, configuration.getWholeTreeExpansion());
    assertTrue(strategy.getExtras().isEmpty(), "initialExpansion and pageSize are real properties, not extras");
    assertFalse(configuration.getExtras().containsKey("wholeTreeExpansion"));

    ModelRoundTrip.assertRoundTrip(getClass(), "/treemodel/TreeModelLevelByLevelStrategy.json", TreeModel.class);
  }

  @Test
  void switchingTheStrategyDropsTheKeysOfTheOtherOne() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), "/treemodel/TreeModelLevelByLevelStrategy.json", TreeModel.class);
    ExpansionStrategy strategy = model.getContent().getConfiguration().getExpansionStrategy();

    strategy.switchTypeTo(ExpansionStrategy.LEVEL_BY_LEVEL);
    assertNotNull(strategy.getInitialExpansion(), "the same type changes nothing");
    assertEquals(5, strategy.getPageSize());

    strategy.switchTypeTo(ExpansionStrategy.TREE);
    assertNull(strategy.getInitialExpansion());
    assertNull(strategy.getPageSize());
    assertEquals(java.util.List.of(), strategy.getExpansionDepths());

    ExpansionDepth depth = new ExpansionDepth();
    depth.setRelationshipModel("TeamTeam_Re");
    strategy.getExpansionDepths().add(depth);
    strategy.switchTypeTo(ExpansionStrategy.LEVEL_BY_LEVEL);
    assertNull(strategy.getExpansionDepths());

    JsonNode json = JsonSettings.objectMapper.valueToTree(strategy);
    assertEquals("level_by_level", json.get("type").asString());
    assertFalse(json.has("expansionDepths"));
    assertFalse(json.has("initialExpansion"));
    assertFalse(json.has("pageSize"));
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

  @Test
  void loadsAndRoundTripsTheNodeConfiguration() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), "/treemodel/TreeModelNodeConfiguration.json", TreeModel.class);

    TreeNode product = model.getContent().getNodes().get(0);
    assertEquals("category", product.getIcon().getName());
    assertEquals("rounded", product.getIcon().getTheme());
    assertEquals(3, product.getChildRelationshipConfigurations().size());
    assertEquals("ProductProduct_Re", product.getChildRelationshipConfigurations().get(0).getRelationshipModelRef());
    assertEquals("Parent", product.getChildRelationshipConfigurations().get(0).getParentRole());
    assertNull(product.getChildRelationshipConfigurations().get(0).getColumns(), "an absent columns key stays absent");
    assertTrue(product.getChildRelationshipConfigurations().get(1).getColumns().isEmpty(), "an explicit [] stays explicit");
    assertEquals("field_link", product.getChildRelationshipConfigurations().get(2).getColumns().get(0).getElementRef());

    TreeNodeAction event = product.getActions().get(0);
    assertEquals(java.util.List.of("s1"), event.getStyles());
    assertEquals("a", event.getAnnotations().get(0).getName());
    assertNull(event.getPosition());
    TreeNodeAction insert = product.getActions().get(1);
    assertTrue(insert.isInsert());
    assertEquals(TreeNodeAction.POSITION_AS_CHILD, insert.getPosition());
    assertEquals("Book_DM", insert.getDocumentModelRef());
    assertTrue(insert.getUseLabelFromDocumentModel());
    assertTrue(insert.getUseGlobalIcon());
    assertTrue(insert.getExtras().isEmpty(), "the insert fields are real properties, not extras");

    assertEquals(2, product.getContextMenu().getGroups().size());
    assertEquals("Actions", product.getContextMenu().getGroups().get(0).getName());
    assertEquals("en", product.getContextMenu().getGroups().get(0).getTitle().get(0).getLocale());
    assertNull(product.getContextMenu().getGroups().get(0).getType());
    assertEquals(TreeNodeActionGroup.TYPE_ADD, product.getContextMenu().getGroups().get(1).getType());
    assertEquals(Boolean.TRUE, product.getDefaultRowAction().getCustom());
    assertEquals("event_open", product.getDefaultRowAction().getEvent());
    assertEquals("Open the product", product.getRowTitle().get(0).getText());
    assertEquals(java.util.List.of("h_semiBoldFontWeight"), product.getStyles());
    assertTrue(product.getExtras().isEmpty(), "icon, contextMenu, defaultRowAction, rowTitle and styles are real properties");

    TreeNode book = model.getContent().getNodes().get(1);
    assertTrue(TreeNodeInheritance.isInherited(book, TreeNodeInheritance.Part.ICON));
    assertTrue(TreeNodeInheritance.isInherited(book, TreeNodeInheritance.Part.CONTEXT_MENU));
    assertFalse(TreeNodeInheritance.isInherited(book, TreeNodeInheritance.Part.STYLES));
    assertNull(book.getIcon());
    assertNull(book.getContextMenu());
    assertNull(book.getDefaultRowAction().getEvent(), "a custom row action without an event is not interactive");

    ModelRoundTrip.assertRoundTrip(getClass(), "/treemodel/TreeModelNodeConfiguration.json", TreeModel.class);
  }

  @Test
  void aNodeWithoutTheOptionalPartsDoesNotGainThem() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), "/treemodel/TreeModel.json", TreeModel.class);

    JsonNode node = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model)).get("content").get("nodes").get(0);
    for (String key : java.util.List.of("icon", "contextMenu", "defaultRowAction", "rowTitle", "styles")) {
      assertFalse(node.has(key), key + " is only written when set");
    }
    for (String key : java.util.List.of("position", "documentModelRef", "useGlobalIcon", "styles", "annotations")) {
      assertFalse(node.get("actions").get(0).has(key), key + " is only written on an action that has it");
    }
  }
}
