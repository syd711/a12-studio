package de.a12.studio.models.structuralmappingmodel;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * One node of the tree SME shows a mapping block as (see {@link SmmBlockTree}): a field mapping or a resolution
 * strategy (Fold/Slice) with the nodes that depend on it as children. A read-only view over the flat
 * {@link MappingBlock} lists, which stay what is stored and edited.
 */
public final class SmmNode {

  public enum Kind {
    FIELD_MAPPING, FOLD, SLICE
  }

  private final Kind kind;
  private final int blockIndex;
  private final int index;
  private final FieldMapping fieldMapping;
  private final ResolutionStrategy resolutionStrategy;
  private final List<String> targetPath;
  private final List<String> sourcePath;
  private final List<SmmNode> children = new ArrayList<>();
  private SmmNode parent;
  private int position;
  private int span = 1;
  private int gap;

  SmmNode(int blockIndex, int index, FieldMapping fieldMapping) {
    this.kind = Kind.FIELD_MAPPING;
    this.blockIndex = blockIndex;
    this.index = index;
    this.fieldMapping = fieldMapping;
    this.resolutionStrategy = null;
    this.targetPath = SmmPath.parse(fieldMapping.getTargetFieldFullName());
    this.sourcePath = SmmPath.parse(fieldMapping.getSourceFieldFullName());
  }

  SmmNode(int blockIndex, int index, ResolutionStrategy resolutionStrategy) {
    this.kind = resolutionStrategy.getType() == ResolutionStrategyType.SLICE ? Kind.SLICE : Kind.FOLD;
    this.blockIndex = blockIndex;
    this.index = index;
    this.fieldMapping = null;
    this.resolutionStrategy = resolutionStrategy;
    this.targetPath = SmmPath.parse(resolutionStrategy.getTargetGroupFullName());
    this.sourcePath = SmmPath.parse(resolutionStrategy.getSourceGroupFullName());
  }

  public Kind kind() {
    return kind;
  }

  public boolean isFieldMapping() {
    return kind == Kind.FIELD_MAPPING;
  }

  public boolean isResolutionStrategy() {
    return kind != Kind.FIELD_MAPPING;
  }

  /** 0-based index of the mapping block. */
  public int blockIndex() {
    return blockIndex;
  }

  /** 0-based index in the block's {@code FieldMappings} (field mapping) or {@code ResolutionStrategies} (strategy). */
  public int index() {
    return index;
  }

  public @Nullable FieldMapping fieldMapping() {
    return fieldMapping;
  }

  public @Nullable ResolutionStrategy resolutionStrategy() {
    return resolutionStrategy;
  }

  /** The target field of a field mapping, the target group of a strategy. */
  public List<String> targetPath() {
    return targetPath;
  }

  /** The source field of a field mapping, the source group of a strategy. */
  public List<String> sourcePath() {
    return sourcePath;
  }

  /** The kernel document pointer of this node, which is also its id in validation findings. */
  public String pointer() {
    return isFieldMapping() ? SmmPointers.fieldMapping(blockIndex, index) : SmmPointers.resolutionStrategy(blockIndex, index);
  }

  public @Nullable SmmNode parent() {
    return parent;
  }

  public List<SmmNode> children() {
    return children;
  }

  /** Column (0-based) of this node tag within its target row, the first tag of a row being at 0. */
  public int position() {
    return position;
  }

  /** How many tag columns this node tag covers: that of the widest strategy beneath it, at least 1. */
  public int span() {
    return span;
  }

  /** Empty tag columns before this node tag, relative to the end of the previous tag of the same row. */
  public int gap() {
    return gap;
  }

  void setParent(SmmNode parent) {
    this.parent = parent;
  }

  void setLayout(int position, int span) {
    this.position = position;
    this.span = span;
  }

  void setGap(int gap) {
    this.gap = gap;
  }

  /** This node and all nodes beneath it. */
  public List<SmmNode> subtree() {
    List<SmmNode> nodes = new ArrayList<>();
    collect(this, nodes);
    return nodes;
  }

  private static void collect(SmmNode node, List<SmmNode> nodes) {
    nodes.add(node);
    node.children.forEach(child -> collect(child, nodes));
  }

  @Override
  public String toString() {
    return kind + " " + SmmPath.format(sourcePath) + " -> " + SmmPath.format(targetPath);
  }
}
