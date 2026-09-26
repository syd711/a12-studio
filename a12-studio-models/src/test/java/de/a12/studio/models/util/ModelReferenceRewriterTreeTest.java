package de.a12.studio.models.util;

import de.a12.studio.models.treemodel.TreeModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renaming a Document or Relationship Model rewrites every reference a Tree Model holds on it (see {@link
 * ModelReferenceRewriter}): the node types' Document Models, the relationship of each child relationship
 * configuration and of each expansion depth, the Document Model of an insert action and the header references. The
 * node ids the initial expansion names are ids inside the tree, not models, and stay.
 */
class ModelReferenceRewriterTreeTest {

  private static final String TREE = """
      {"header": {"id": "Team_TM", "modelType": "tree", "modelVersion": "11.0.0", "modelReferences": [
         {"purpose": "document-model-for-tree", "modelType": "document", "alias": "DM1", "reference": "Team_DM"},
         {"purpose": "relationship-model-for-tree", "modelType": "relationship", "alias": "RM1", "reference": "TeamTeam_Re"}]},
       "content": {
         "configuration": {"rootRef": "crc-1",
           "expansionStrategy": {"type": "tree", "expansionDepths": [{"relationshipModel": "TeamTeam_Re", "maxDepth": 5}]}},
         "columns": [{"id": "column-1", "name": "Name", "width": 1}],
         "nodes": [{"id": "node-1", "documentModelRef": "Team_DM", "configuration": {},
                    "childRelationshipConfigurations": [
                      {"id": "crc-1", "relationshipModelRef": "TeamTeam_Re", "parentRole": "Parent"},
                      {"id": "crc-2", "relationshipModelRef": "TeamPerson_Re", "parentRole": "Team"}],
                    "actions": [{"type": "insert", "documentModelRef": "Team_DM"}]}]}}
      """;

  @Test
  void aRenamedRelationshipModelIsFollowedByTheChildRelationshipConfigurationsAndTheExpansionDepths() {
    TreeModel tree = JsonSettings.objectMapper.readValue(TREE, TreeModel.class);

    boolean changed = ModelReferenceRewriter.rewriteReferences(tree, Map.of("TeamTeam_Re", "TeamParent_Re"));

    assertTrue(changed);
    List<String> relationships = tree.getContent().getNodes().get(0).getChildRelationshipConfigurations().stream()
        .map(configuration -> configuration.getRelationshipModelRef()).toList();
    assertEquals(List.of("TeamParent_Re", "TeamPerson_Re"), relationships, "only the renamed relationship changes");
    assertEquals("TeamParent_Re", tree.getContent().getConfiguration().getExpansionStrategy().getExpansionDepths().get(0).getRelationshipModel());
    assertEquals("TeamParent_Re", tree.getModelReferences().get(1).getReference());
    assertEquals("crc-1", tree.getContent().getConfiguration().getRootRef(), "configuration ids are not model ids");
  }

  @Test
  void aRenamedDocumentModelIsFollowedByTheNodeTypesAndTheirInsertActions() {
    TreeModel tree = JsonSettings.objectMapper.readValue(TREE, TreeModel.class);

    boolean changed = ModelReferenceRewriter.rewriteReferences(tree, Map.of("Team_DM", "Squad_DM"));

    assertTrue(changed);
    assertEquals("Squad_DM", tree.getContent().getNodes().get(0).getDocumentModelRef());
    assertEquals("Squad_DM", tree.getModelReferences().get(0).getReference());
    assertEquals("Squad_DM", tree.getContent().getNodes().get(0).getActions().get(0).getDocumentModelRef());
  }

  @Test
  void anUnrelatedRenameChangesNothing() {
    TreeModel tree = JsonSettings.objectMapper.readValue(TREE, TreeModel.class);

    assertFalse(ModelReferenceRewriter.rewriteReferences(tree, Map.of("Other_Re", "Renamed_Re")));
  }
}
