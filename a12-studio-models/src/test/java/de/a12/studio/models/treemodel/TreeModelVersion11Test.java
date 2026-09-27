package de.a12.studio.models.treemodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The tree model 11.0.0 shapes SME's installed editor writes; see "Tree Model: full gap review" in the comparison doc. */
class TreeModelVersion11Test {

  private static final String FIXTURE = "/treemodel/TreeModelVersion11.json";

  @Test
  void roundTripsEveryVersion11Shape() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), FIXTURE, TreeModel.class);
  }

  @Test
  void columnWidthKeepsItsDecimalsAndItsFormat() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), FIXTURE, TreeModel.class);
    List<TreeColumn> columns = model.getContent().getColumns();

    assertEquals(2.5, columns.get(0).getWidth());
    assertEquals(0.3, columns.get(1).getWidth());
    assertEquals(1.0, columns.get(2).getWidth());

    JsonNode saved = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model))
        .get("content").get("columns");
    assertTrue(saved.get(0).get("width").isDouble(), "2.5 stays a decimal, it used to be cut to 2");
    assertEquals(2.5, saved.get(0).get("width").asDouble());
    assertEquals(0.3, saved.get(1).get("width").asDouble());
    assertTrue(saved.get(2).get("width").isInt(), "a whole width stays `1`");
    assertTrue(saved.get(3).get("width").isDouble(), "`1.0` stays `1.0`");
  }

  @Test
  void settingAWidthWritesWholeNumbersAsIntegers() {
    TreeColumn column = new TreeColumn();
    column.setWidth(2.0);
    assertTrue(column.getWidthNode().isInt());
    column.setWidth(1.5);
    assertTrue(column.getWidthNode().isDouble());
    column.setWidth(null);
    assertNull(column.getWidth());
  }

  @Test
  void columnHeaderAndAppearanceAreTypedProperties() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), FIXTURE, TreeModel.class);
    TreeColumn column = model.getContent().getColumns().get(0);

    assertEquals("Name", column.getLabel().get(0).getText());
    assertEquals(Boolean.TRUE, column.getLabelHidden());
    assertEquals("label", column.getIcon().getName());
    assertEquals("outlined", column.getIcon().getTheme());
    assertEquals("center", column.getAlignment().getHeader().getHorizontal());
    assertEquals("top", column.getAlignment().getContent().getVertical());
    assertEquals(List.of("s1"), column.getStyles().getHeader());
    assertEquals(List.of("s1", "s2"), column.getStyles().getContent());
    assertTrue(column.getExtras().isEmpty(), "nothing of a column is left to `extras`");
  }

  @Test
  void configurationHasScreenReaderColumnSubtitleAndVirtualRoot() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), FIXTURE, TreeModel.class);
    TreeConfiguration configuration = model.getContent().getConfiguration();

    assertEquals("column-1", configuration.getScreenReaderColumnRef());
    assertEquals("Alle Produkte", configuration.getSubtitle().get(1).getText());
    TreeVirtualRoot virtualRoot = configuration.getVirtualRoot();
    assertEquals("Catalog", virtualRoot.getLabel().get(0).getText());
    assertEquals(2, virtualRoot.getActions().size());
    assertEquals(TreeNodeActionGroup.TYPE_ADD, virtualRoot.getContextMenu().getGroups().get(0).getType());
    assertTrue(configuration.getExtras().isEmpty(), "no configuration key is left to `extras`");
    assertTrue(virtualRoot.getExtras().isEmpty());
  }

  @Test
  void aConfigurationWithoutThemDoesNotGainThem() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), "/treemodel/TreeModel.json", TreeModel.class);
    JsonNode configuration = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model))
        .get("content").get("configuration");

    for (String key : List.of("screenReaderColumnRef", "subtitle", "virtualRoot")) {
      assertFalse(configuration.has(key), key + " is only written when set");
    }
  }

  @Test
  void rowActivationHasTheThreeTypesAndIsInheritable() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), FIXTURE, TreeModel.class);
    TreeNode product = model.getContent().getNodes().get(0);
    TreeNode book = model.getContent().getNodes().get(1);

    assertEquals(RowActivation.TYPE_INSERT, product.getRowActivation().getType());
    assertEquals(TreeNodeAction.POSITION_BELOW, product.getRowActivation().getPosition());
    assertEquals("Product_DM", product.getRowActivation().getDocumentModelRef());
    assertTrue(TreeNodeInheritance.isInherited(product, TreeNodeInheritance.Part.ROW_ACTIVATION));
    assertEquals("event_toggle_expansion", book.getRowActivation().getEvent());
    assertTrue(product.getExtras().isEmpty(), "rowActivation is a real property");
  }

  @Test
  void aLegacyDefaultRowActionSurvivesAsAnExtraWithoutBeingMigrated() throws Exception {
    String json = ModelRoundTrip.readResource(getClass(), FIXTURE)
        .replace("\"rowActivation\": {\"type\": \"event\", \"event\": \"event_toggle_expansion\"}",
            "\"defaultRowAction\": {\"custom\": true, \"event\": \"event_open\"}");
    TreeModel model = JsonSettings.objectMapper.readValue(json, TreeModel.class);

    TreeNode book = model.getContent().getNodes().get(1);
    assertNull(book.getRowActivation());
    assertTrue(book.getExtras().containsKey("defaultRowAction"), "kept, not dropped");
  }

  @Test
  void eventCandidatesFollowTheContextAndTheLinkDocumentModel() {
    List<String> row = TreeEvents.candidates(TreeEvents.Context.ROW, false);
    assertTrue(row.contains("event_open_node"));
    assertTrue(row.contains("event_paste_below"));
    assertFalse(TreeEvents.candidates(TreeEvents.Context.ROW, true).contains("event_paste_below"));
    assertTrue(TreeEvents.candidates(TreeEvents.Context.ROW, true).contains("event_delete_node"));

    assertEquals(List.of("event_add_root_node", "event_expand_whole_tree", "event_collapse_whole_tree", "event_paste"),
        TreeEvents.candidates(TreeEvents.Context.HEADER, false));
    assertEquals(List.of("event_delete_nodes"), TreeEvents.candidates(TreeEvents.Context.MULTI_SELECTION, true));
    assertTrue(TreeEvents.candidates(TreeEvents.Context.MULTI_SELECTION, false).contains("event_cut_nodes"));
    assertTrue(TreeEvents.candidates(TreeEvents.Context.ROW_ACTIVATION, true).contains("event_toggle_expansion"));

    assertTrue(TreeEvents.isCopyPaste(TreeEvents.Context.ROW, "event_cut_node"));
    assertFalse(TreeEvents.isCopyPaste(TreeEvents.Context.ROW, "event_delete_node"));
    assertTrue(TreeEvents.isCopyPaste(TreeEvents.Context.HEADER, "event_paste"));
    assertTrue(TreeEvents.isKnown(TreeEvents.Context.ROW_ACTIVATION, "event_toggle_expansion"));
    assertFalse(TreeEvents.isKnown(TreeEvents.Context.ROW, "selectPerson"));
  }

  @Test
  void aRelationshipWithALinkDocumentModelIsDetected() throws Exception {
    TreeModel model = ModelRoundTrip.load(getClass(), FIXTURE, TreeModel.class);
    RelationshipModel plain = new RelationshipModel();
    plain.setContent(new RelationshipModelContent());
    RelationshipModel withLink = new RelationshipModel();
    withLink.setContent(new RelationshipModelContent());
    withLink.getContent().setLinkDocumentModelValue("Link_DM");

    assertFalse(TreeEvents.hasLinkDocumentModel(model, Map.of("ProductProduct_Re", plain)::get));
    assertTrue(TreeEvents.hasLinkDocumentModel(model, Map.of("ProductProduct_Re", withLink)::get));
    assertFalse(TreeEvents.hasLinkDocumentModel(model, id -> null));
  }

  @Test
  void columnsAreSortedByPinDirectionAndNeverSwappedAcrossThem() {
    TreeColumn right = column("right", "right");
    TreeColumn plainA = column("a", null);
    TreeColumn left = column("left", "left");
    TreeColumn plainB = column("b", null);
    List<TreeColumn> columns = new ArrayList<>(List.of(right, plainA, left, plainB));

    assertFalse(TreeColumns.isSortedByPinDirection(columns));
    TreeColumns.sortByPinDirection(columns);
    assertEquals(List.of(left, plainA, plainB, right), columns);
    assertTrue(TreeColumns.isSortedByPinDirection(columns));

    assertTrue(TreeColumns.isIllegalMove(columns, 0, 1), "left-pinned past an unpinned one");
    assertFalse(TreeColumns.isIllegalMove(columns, 1, 1), "two unpinned ones");
    assertTrue(TreeColumns.isIllegalMove(columns, 2, 1), "unpinned past a right-pinned one");
    assertFalse(TreeColumns.isIllegalMove(columns, 0, -1), "off the list is no swap");
  }

  @Test
  void theDefaultDeleteActionHasATextForEveryLocale() {
    TreeNodeAction action = TreeNodeActions.newDeleteAction(List.of("en", "de", "fr"));

    assertEquals("event_delete_node", action.getEvent());
    assertEquals(Boolean.TRUE, action.getDestructive());
    assertEquals(Boolean.FALSE, action.getPrimary());
    assertEquals(Boolean.TRUE, action.getLabelHidden());
    assertEquals("delete_forever", action.getIcon().getName());
    assertEquals(3, action.getLabel().size());
    assertEquals("Delete", action.getLabel().get(0).getText());
    assertEquals("Löschen", action.getLabel().get(1).getText());
    assertEquals("", action.getLabel().get(2).getText(), "a locale SME has no text for stays empty");
    assertEquals(3, action.getConfirmation().getMessage().size());
  }

  private static TreeColumn column(String name, String pinDirection) {
    TreeColumn column = new TreeColumn();
    column.setName(name);
    column.setPinDirection(pinDirection);
    return column;
  }
}
