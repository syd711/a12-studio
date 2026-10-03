package de.a12.studio.models.structuralmappingmodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * The edits SME offers on a Structural Mapping Model that need no kernel: deleting a mapping (with the cleanup
 * SME does afterwards), the per-group "clear on first fill" flag, and changing a resolution strategy. Everything
 * works on the flat {@link MappingBlock} lists, which are what the file stores.
 */
public final class SmmOperations {

  private SmmOperations() {
  }

  /** The tree of every mapping block, in file order. */
  public static List<SmmBlockTree> trees(StructuralMappingModelContent content) {
    List<SmmBlockTree> trees = new ArrayList<>();
    for (int i = 0; i < content.getMappingBlocks().size(); i++) {
      trees.add(new SmmBlockTree(content.getMappingBlocks().get(i), i));
    }
    return trees;
  }

  /**
   * Removes a field mapping or a resolution strategy together with everything that depends on it, then what SME
   * cleans up afterwards: resolution strategies left without any field mapping beneath them, and mapping blocks
   * left empty.
   */
  public static void delete(StructuralMappingModelContent content, SmmNode node) {
    MappingBlock block = content.getMappingBlocks().get(node.blockIndex());
    Set<Object> removed = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    for (SmmNode doomed : node.subtree()) {
      removed.add(doomed.isFieldMapping() ? doomed.fieldMapping() : doomed.resolutionStrategy());
    }
    block.getFieldMappings().removeIf(removed::contains);
    block.getResolutionStrategies().removeIf(removed::contains);
    removeEmptyStrategiesAndBlocks(content);
  }

  /** Removes strategies without a field mapping beneath them (repeatedly, as removing one can empty its parent) and empty blocks. */
  public static void removeEmptyStrategiesAndBlocks(StructuralMappingModelContent content) {
    for (MappingBlock block : content.getMappingBlocks()) {
      boolean removedOne = true;
      while (removedOne) {
        removedOne = false;
        SmmBlockTree tree = new SmmBlockTree(block, 0);
        for (SmmNode node : tree.nodes()) {
          if (node.isResolutionStrategy() && node.children().isEmpty()) {
            block.getResolutionStrategies().remove(node.resolutionStrategy());
            removedOne = true;
          }
        }
      }
    }
    content.getMappingBlocks().removeIf(block -> block.getFieldMappings().isEmpty() && block.getResolutionStrategies().isEmpty());
  }

  // ---- clear on first fill -----------------------------------------------------------------------

  public static boolean isGroupCleared(StructuralMappingModelContent content, String groupPath) {
    return content.getGroupsToClearOnFirstFill().stream().anyMatch(group -> groupPath.equals(group.getFullName()));
  }

  /** Whether a group above {@code groupPath} is cleared, which clears {@code groupPath} with it. */
  public static boolean isClearedByAncestor(StructuralMappingModelContent content, String groupPath) {
    List<String> path = SmmPath.parse(groupPath);
    return content.getGroupsToClearOnFirstFill().stream()
        .anyMatch(group -> SmmPath.isBelow(path, SmmPath.parse(group.getFullName())));
  }

  /**
   * Sets whether the group is cleared before data is filled into it for the first time. Marking a group makes the
   * marks of the groups below it redundant (they are cleared with it), so those are dropped.
   */
  public static void setGroupCleared(StructuralMappingModelContent content, String groupPath, boolean cleared) {
    List<GroupToClearOnFirstFill> groups = content.getGroupsToClearOnFirstFill();
    if (cleared) {
      if (!isGroupCleared(content, groupPath)) {
        GroupToClearOnFirstFill group = new GroupToClearOnFirstFill();
        group.setFullName(groupPath);
        groups.add(group);
      }
      List<String> path = SmmPath.parse(groupPath);
      groups.removeIf(group -> SmmPath.isBelow(SmmPath.parse(group.getFullName()), path));
    }
    else {
      groups.removeIf(group -> groupPath.equals(group.getFullName()));
    }
  }

  // ---- resolution strategies ---------------------------------------------------------------------

  /** Applies what the resolution strategy dialog edits; the slice fields are only kept for a Slice. */
  public static void updateResolutionStrategy(ResolutionStrategy strategy, ResolutionStrategyType type, String sourceGroup,
      String sliceSourceField, String sliceTargetField) {
    strategy.setType(type);
    strategy.setSourceGroupFullName(sourceGroup);
    if (type == ResolutionStrategyType.SLICE) {
      Slice slice = strategy.getSlice() != null ? strategy.getSlice() : new Slice();
      slice.setSourceFieldFullName(sliceSourceField != null ? sliceSourceField : "");
      slice.setTargetFieldFullName(sliceTargetField != null ? sliceTargetField : "");
      strategy.setSlice(slice);
    }
    else {
      strategy.setSlice(null);
    }
  }

  // ---- what is mapped -------------------------------------------------------------------------------

  /** The full names of the source fields some field mapping reads (or some slice looks up). */
  public static Set<String> mappedSourceElements(StructuralMappingModelContent content) {
    Set<String> paths = new HashSet<>();
    for (MappingBlock block : content.getMappingBlocks()) {
      block.getFieldMappings().forEach(fieldMapping -> paths.add(fieldMapping.getSourceFieldFullName()));
      block.getResolutionStrategies().stream().map(ResolutionStrategy::getSlice).filter(java.util.Objects::nonNull)
          .forEach(slice -> paths.add(slice.getSourceFieldFullName()));
    }
    return paths;
  }

  /** The full names of the target fields and groups some field mapping or resolution strategy writes (or some slice looks up). */
  public static Set<String> mappedTargetElements(StructuralMappingModelContent content) {
    Set<String> paths = new HashSet<>();
    for (MappingBlock block : content.getMappingBlocks()) {
      block.getFieldMappings().forEach(fieldMapping -> paths.add(fieldMapping.getTargetFieldFullName()));
      for (ResolutionStrategy strategy : block.getResolutionStrategies()) {
        paths.add(strategy.getTargetGroupFullName());
        if (strategy.getSlice() != null) {
          paths.add(strategy.getSlice().getTargetFieldFullName());
        }
      }
    }
    return paths;
  }

  /** How many times each source field is read (SME shows it as {@code Name (2)} in the source tree). */
  public static java.util.Map<String, Integer> sourceFieldUsages(StructuralMappingModelContent content) {
    java.util.Map<String, Integer> usages = new java.util.HashMap<>();
    for (MappingBlock block : content.getMappingBlocks()) {
      block.getFieldMappings().forEach(fieldMapping -> usages.merge(fieldMapping.getSourceFieldFullName(), 1, Integer::sum));
      block.getResolutionStrategies().stream().map(ResolutionStrategy::getSlice).filter(java.util.Objects::nonNull)
          .forEach(slice -> usages.merge(slice.getSourceFieldFullName(), 1, Integer::sum));
    }
    return usages;
  }
}
