package de.a12.studio.models.structuralmappingmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class SmmBlockTreeTest {

  private static FieldMapping fieldMapping(String source, String target) {
    FieldMapping fieldMapping = new FieldMapping();
    fieldMapping.setSourceFieldFullName(source);
    fieldMapping.setTargetFieldFullName(target);
    return fieldMapping;
  }

  private static ResolutionStrategy fold(String sourceGroup, String targetGroup) {
    ResolutionStrategy strategy = new ResolutionStrategy();
    strategy.setType(ResolutionStrategyType.FOLD);
    strategy.setSourceGroupFullName(sourceGroup);
    strategy.setTargetGroupFullName(targetGroup);
    return strategy;
  }

  private static ResolutionStrategy slice(String sourceGroup, String targetGroup, String sourceField, String targetField) {
    ResolutionStrategy strategy = fold(sourceGroup, targetGroup);
    strategy.setType(ResolutionStrategyType.SLICE);
    Slice slice = new Slice();
    slice.setSourceFieldFullName(sourceField);
    slice.setTargetFieldFullName(targetField);
    strategy.setSlice(slice);
    return strategy;
  }

  /** The block of SME's Intern to Employee example, reduced: addresses are folded, the names are plain. */
  private static MappingBlock internBlock() {
    MappingBlock block = new MappingBlock();
    block.getResolutionStrategies().add(fold("/In/Person/Addresses", "/Out/Person/Addresses"));
    block.getFieldMappings().add(fieldMapping("/In/Person/Addresses/City", "/Out/Person/Addresses/City"));
    block.getFieldMappings().add(fieldMapping("/In/Person/Addresses/Street", "/Out/Person/Addresses/Street"));
    block.getFieldMappings().add(fieldMapping("/In/Person/FirstName", "/Out/Person/FirstName"));
    return block;
  }

  @Test
  void placesFieldMappingsBelowTheStrategyOfTheirRepeatingGroup() {
    SmmBlockTree tree = new SmmBlockTree(internBlock(), 0);

    assertEquals(2, tree.roots().size());
    SmmNode strategy = tree.roots().get(0);
    assertEquals(SmmNode.Kind.FOLD, strategy.kind());
    assertEquals(2, strategy.children().size());
    assertEquals("/Out/Person/Addresses/City", SmmPath.format(strategy.children().get(0).targetPath()));
    // FirstName is not inside the addresses, so it stays at the top.
    assertEquals(SmmNode.Kind.FIELD_MAPPING, tree.roots().get(1).kind());
    assertNull(tree.roots().get(1).parent());
  }

  @Test
  void nestsStrategiesByTargetAndSourceGroup() {
    MappingBlock block = new MappingBlock();
    // Deliberately in the wrong order: the deeper strategy first.
    block.getResolutionStrategies().add(fold("/In/Orders/Items", "/Out/Orders/Items"));
    block.getResolutionStrategies().add(fold("/In/Orders", "/Out/Orders"));
    block.getFieldMappings().add(fieldMapping("/In/Orders/Items/Sku", "/Out/Orders/Items/Sku"));

    SmmBlockTree tree = new SmmBlockTree(block, 0);

    assertEquals(1, tree.roots().size());
    SmmNode orders = tree.roots().get(0);
    assertEquals("/Out/Orders", SmmPath.format(orders.targetPath()));
    assertEquals("/Out/Orders/Items", SmmPath.format(orders.children().get(0).targetPath()));
    assertEquals(1, orders.children().get(0).children().size());
    // The pointer is the position in the flat list (file order), not in the tree.
    assertEquals("/content/MappingBlocks[1]/ResolutionStrategies[2]", orders.pointer());
    assertEquals("/content/MappingBlocks[1]/FieldMappings[1]", orders.children().get(0).children().get(0).pointer());
  }

  @Test
  void laysTwoMappingsOntoTheSameTargetSideBySide() {
    MappingBlock block = new MappingBlock();
    block.getFieldMappings().add(fieldMapping("/In/A", "/Out/Joined"));
    block.getFieldMappings().add(fieldMapping("/In/B", "/Out/Joined"));

    SmmBlockTree tree = new SmmBlockTree(block, 0);
    List<SmmNode> tags = tree.tagsOfRow(SmmPath.parse("/Out/Joined"));

    assertEquals(2, tags.size());
    assertEquals(0, tags.get(0).position());
    assertEquals(1, tags.get(1).position());
    assertEquals(0, tags.get(1).gap());
    assertEquals(2, tree.columnCount());
  }

  @Test
  void showsTheLookupFieldOfASliceInItsOwnRow() {
    MappingBlock block = new MappingBlock();
    block.getResolutionStrategies().add(slice("/In/Rows", "/Out/Rows", "/In/Rows/Key", "/Out/Rows/Key"));
    block.getFieldMappings().add(fieldMapping("/In/Rows/Value", "/Out/Rows/Value"));

    SmmBlockTree tree = new SmmBlockTree(block, 0);

    assertEquals(1, tree.tagsOfRow(SmmPath.parse("/Out/Rows")).size());
    List<SmmNode> keyRow = tree.tagsOfRow(SmmPath.parse("/Out/Rows/Key"));
    assertEquals(1, keyRow.size());
    assertEquals(SmmNode.Kind.SLICE, keyRow.get(0).kind());
  }

  @Test
  void deletingAStrategyRemovesWhatDependsOnItAndEmptyLeftovers() {
    StructuralMappingModelContent content = new StructuralMappingModelContent();
    content.getMappingBlocks().add(internBlock());
    MappingBlock second = new MappingBlock();
    second.getFieldMappings().add(fieldMapping("/In/X", "/Out/X"));
    content.getMappingBlocks().add(second);

    SmmNode strategy = SmmOperations.trees(content).get(0).roots().get(0);
    SmmOperations.delete(content, strategy);

    assertEquals(2, content.getMappingBlocks().size());
    assertTrue(content.getMappingBlocks().get(0).getResolutionStrategies().isEmpty());
    assertEquals(1, content.getMappingBlocks().get(0).getFieldMappings().size());
    assertEquals("/In/Person/FirstName", content.getMappingBlocks().get(0).getFieldMappings().get(0).getSourceFieldFullName());
  }

  @Test
  void deletingTheLastFieldMappingOfAStrategyRemovesTheStrategyAndTheBlock() {
    StructuralMappingModelContent content = new StructuralMappingModelContent();
    MappingBlock block = new MappingBlock();
    block.getResolutionStrategies().add(fold("/In/Addresses", "/Out/Addresses"));
    block.getFieldMappings().add(fieldMapping("/In/Addresses/City", "/Out/Addresses/City"));
    content.getMappingBlocks().add(block);

    SmmOperations.delete(content, SmmOperations.trees(content).get(0).roots().get(0).children().get(0));

    assertTrue(content.getMappingBlocks().isEmpty());
  }

  @Test
  void clearingAGroupMakesTheMarksBelowItRedundant() {
    StructuralMappingModelContent content = new StructuralMappingModelContent();
    SmmOperations.setGroupCleared(content, "/Out/Addresses/Phones", true);
    SmmOperations.setGroupCleared(content, "/Out/Addresses", true);

    assertEquals(1, content.getGroupsToClearOnFirstFill().size());
    assertTrue(SmmOperations.isGroupCleared(content, "/Out/Addresses"));
    assertTrue(SmmOperations.isClearedByAncestor(content, "/Out/Addresses/Phones"));

    SmmOperations.setGroupCleared(content, "/Out/Addresses", false);
    assertTrue(content.getGroupsToClearOnFirstFill().isEmpty());
  }

  @Test
  void updatingToAFoldDropsTheSlice() {
    ResolutionStrategy strategy = slice("/In/Rows", "/Out/Rows", "/In/Rows/Key", "/Out/Rows/Key");

    SmmOperations.updateResolutionStrategy(strategy, ResolutionStrategyType.FOLD, "/In/Rows", null, null);

    assertEquals(ResolutionStrategyType.FOLD, strategy.getType());
    assertNull(strategy.getSlice());
  }

  @Test
  void relativePathsAreLabelledLikeSme() {
    assertEquals(".", SmmPath.relativeTo(SmmPath.parse("/A/B"), SmmPath.parse("/A/B")));
    assertEquals("C/D", SmmPath.relativeTo(SmmPath.parse("/A/B"), SmmPath.parse("/A/B/C/D")));
    assertEquals("/A/B/C", SmmPath.relativeTo(List.of(), SmmPath.parse("/A/B/C")));
    assertEquals("/X/Y", SmmPath.relativeTo(SmmPath.parse("/A/B"), SmmPath.parse("/X/Y")));
  }

  @Test
  void cutsKernelPointersDownToTheElement() {
    assertEquals("/content/MappingBlocks[2]/FieldMappings[3]", SmmPointers.normalize("/content[1]/MappingBlocks[2]/FieldMappings[3]/targetFieldFullName[1]"));
    assertEquals("/content/MappingBlocks[1]/ResolutionStrategies[1]", SmmPointers.normalize("/content[1]/MappingBlocks[1]/ResolutionStrategies[1]/Slice[1]/sourceFieldFullName[1]"));
    assertEquals("/content/MappingBlocks[1]", SmmPointers.normalize("/content[1]/MappingBlocks[1]"));
    assertEquals("/content/GroupsToClearOnFirstFill[2]", SmmPointers.normalize("/content[1]/GroupsToClearOnFirstFill[2]/fullName[1]"));
    assertEquals("/content", SmmPointers.normalize(null));
  }
}
