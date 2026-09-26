package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The expansion strategy and whole-tree expansion rules of SME's Tree meta model. */
class TreeExpansionValidatorsTest {

  private static final String SUB_HEADER_POPUP = "{\"leftSlot\": [], \"rightSlot\": [{\"type\": \"expand_all_popup\"}]}";

  private static TreeModel tree(String configuration, String subHeaderBox) {
    String json = """
        {"header": {"id": "Team_TM", "modelType": "tree", "modelVersion": "11.0.0"},
         "content": {"configuration": %s, "subHeaderBox": %s,
           "columns": [{"id": "column-1", "name": "Name", "width": 1}],
           "nodes": [{"id": "node-1", "documentModelRef": "Team_DM", "configuration": {},
                      "childRelationshipConfigurations": [{"id": "crc-1", "relationshipModelRef": "TeamTeam_Re", "parentRole": "Parent"}]},
                     {"id": "node-2", "documentModelRef": "Person_DM", "configuration": {}}]}}
        """.formatted(configuration, subHeaderBox);
    return JsonSettings.objectMapper.readValue(json, TreeModel.class);
  }

  private static List<ModelValidationError> strategyErrors(String expansionStrategy) {
    TreeModel model = tree("{\"expansionStrategy\": " + expansionStrategy + "}", "{\"leftSlot\": [], \"rightSlot\": []}");
    return new TreeExpansionStrategyValidator().validate(model, TestModels.context(model));
  }

  private static List<ModelValidationError> wholeTreeErrors(String configuration, String subHeaderBox) {
    TreeModel model = tree(configuration, subHeaderBox);
    return new TreeWholeTreeExpansionValidator().validate(model, TestModels.context(model));
  }

  // ---- expansion strategy: tree ----

  @Test
  void aTreeStrategyNeedsAtLeastOneExpansionDepth() {
    assertEquals(1, strategyErrors("{\"type\": \"tree\", \"expansionDepths\": []}").size());
    List<ModelValidationError> missing = strategyErrors("{\"type\": \"tree\"}");
    assertEquals(1, missing.size());
    assertTrue(missing.get(0).message().contains("Expansion Depths must not be empty"));
  }

  @Test
  void anExpansionDepthNeedsARelationshipOfTheNodeTypesAndEachOnlyOnce() {
    assertEquals(0, strategyErrors("{\"type\": \"tree\", \"expansionDepths\": [{\"relationshipModel\": \"TeamTeam_Re\", \"maxDepth\": 5}]}").size());

    List<ModelValidationError> unknown = strategyErrors("{\"type\": \"tree\", \"expansionDepths\": [{\"relationshipModel\": \"Other_Re\", \"maxDepth\": 1}]}");
    assertEquals(1, unknown.size());
    assertTrue(unknown.get(0).message().contains("Other_Re"));

    List<ModelValidationError> duplicate = strategyErrors("{\"type\": \"tree\", \"expansionDepths\": ["
        + "{\"relationshipModel\": \"TeamTeam_Re\", \"maxDepth\": 5}, {\"relationshipModel\": \"TeamTeam_Re\", \"maxDepth\": 1}]}");
    assertEquals(1, duplicate.size());
    assertTrue(duplicate.get(0).message().contains("more than one expansion depth"));
  }

  @Test
  void theLevelByLevelKeysAreNotCheckedForATreeStrategy() {
    assertEquals(0, strategyErrors("{\"type\": \"tree\", \"expansionDepths\": [{\"relationshipModel\": \"TeamTeam_Re\", \"maxDepth\": 1}],"
        + " \"initialExpansion\": {\"type\": \"level_limit\"}, \"pageSize\": 0}").size());
  }

  // ---- expansion strategy: level by level ----

  @Test
  void aLevelByLevelStrategyWithoutInitialExpansionOrPageSizeIsFine() {
    assertEquals(0, strategyErrors("{\"type\": \"level_by_level\"}").size());
    assertEquals(0, strategyErrors("{\"type\": \"level_by_level\", \"initialExpansion\": {\"type\": \"all_levels\"}, \"pageSize\": 10}").size());
  }

  @Test
  void anInitialExpansionNeedsATypeAndForALevelLimitTheNumberOfLevels() {
    assertEquals(1, strategyErrors("{\"type\": \"level_by_level\", \"initialExpansion\": {}}").size());
    assertEquals(1, strategyErrors("{\"type\": \"level_by_level\", \"initialExpansion\": {\"type\": \"level_limit\"}}").size());
    assertEquals(1, strategyErrors("{\"type\": \"level_by_level\", \"initialExpansion\": {\"type\": \"level_limit\", \"level\": 0}}").size());
    assertEquals(0, strategyErrors("{\"type\": \"level_by_level\", \"initialExpansion\": {\"type\": \"level_limit\", \"level\": 2}}").size());
  }

  @Test
  void theNodeTypesOfAnInitialExpansionMustExistAndBeUnique() {
    assertEquals(0, strategyErrors("{\"type\": \"level_by_level\", \"initialExpansion\": {\"type\": \"all_levels\", \"affectedNodeRefs\": [\"node-1\", \"node-2\"]}}").size());

    List<ModelValidationError> missing = strategyErrors("{\"type\": \"level_by_level\", \"initialExpansion\": {\"type\": \"all_levels\", \"affectedNodeRefs\": [\"node-gone\"]}}");
    assertEquals(1, missing.size());
    assertTrue(missing.get(0).message().contains("node-gone"));

    List<ModelValidationError> duplicate = strategyErrors("{\"type\": \"level_by_level\", \"initialExpansion\": {\"type\": \"all_levels\", \"affectedNodeRefs\": [\"node-1\", \"node-1\"]}}");
    assertEquals(1, duplicate.size());
    assertTrue(duplicate.get(0).message().contains("more than once"));
  }

  @Test
  void aPageSizeBelowOneIsAnError() {
    List<ModelValidationError> errors = strategyErrors("{\"type\": \"level_by_level\", \"pageSize\": 0}");
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Page Size must be at least 1"));
  }

  @Test
  void aTreeWithoutAStrategyOrConfigurationIsNotChecked() {
    assertEquals(0, strategyErrors("{}").size());
    TreeModel model = JsonSettings.objectMapper.readValue(
        "{\"header\": {\"id\": \"Team_TM\", \"modelType\": \"tree\"}, \"content\": {\"columns\": [], \"nodes\": []}}", TreeModel.class);
    assertEquals(0, new TreeExpansionStrategyValidator().validate(model, TestModels.context(model)).size());
    assertEquals(0, new TreeWholeTreeExpansionValidator().validate(model, TestModels.context(model)).size());
  }

  // ---- whole tree expansion ----

  @Test
  void anExpandAllPopupNeedsTheWholeTreeFlag() {
    List<ModelValidationError> errors = wholeTreeErrors("{}", SUB_HEADER_POPUP);
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("requires \"Enable Expand/Collapse The Whole Tree\""));

    String both = "{\"leftSlot\": [{\"type\": \"expand_all_popup\"}], \"rightSlot\": [{\"type\": \"expand_all_popup\"}]}";
    assertEquals(2, wholeTreeErrors("{}", both).size(), "each element is reported, in either slot");
  }

  @Test
  void theWholeTreeFlagNeedsExactlyOneExpandAllPopup() {
    String flag = "{\"wholeTreeExpansion\": true}";
    assertEquals(0, wholeTreeErrors(flag, SUB_HEADER_POPUP).size());
    assertEquals(0, wholeTreeErrors(flag, "{\"leftSlot\": [{\"type\": \"expand_all_popup\"}], \"rightSlot\": []}").size(), "the minor slot counts too");

    List<ModelValidationError> none = wholeTreeErrors(flag, "{\"leftSlot\": [], \"rightSlot\": []}");
    assertEquals(1, none.size());
    assertTrue(none.get(0).message().contains("requires an Expand All PopUp element"));

    List<ModelValidationError> two = wholeTreeErrors(flag, "{\"leftSlot\": [{\"type\": \"expand_all_popup\"}], \"rightSlot\": [{\"type\": \"expand_all_popup\"}]}");
    assertEquals(1, two.size());
    assertTrue(two.get(0).message().contains("Only one Expand All PopUp"));
  }

  @Test
  void withoutTheFlagAndWithoutAPopupThereIsNothingToReport() {
    assertEquals(0, wholeTreeErrors("{}", "{\"leftSlot\": [], \"rightSlot\": []}").size());
  }
}
