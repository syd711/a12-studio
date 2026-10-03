package de.a12.studio.models.structuralmappingmodel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The tree SME presents a {@link MappingBlock} as, derived from its flat {@code ResolutionStrategies} and
 * {@code FieldMappings} lists (which is all the file stores): a node belongs under the resolution strategy whose
 * target group is the nearest ancestor of its own target and whose source group contains its source. Ports SME's
 * {@code smmTransformation.insertIntoMappingBlock} and {@code mappingTreeLayoutCalculator}.
 */
public final class SmmBlockTree {

  private final int blockIndex;
  private final List<SmmNode> roots = new ArrayList<>();
  private final List<SmmNode> all = new ArrayList<>();

  public SmmBlockTree(MappingBlock block, int blockIndex) {
    this.blockIndex = blockIndex;
    build(block);
    layout();
  }

  public int blockIndex() {
    return blockIndex;
  }

  /** The top-level nodes, in the order SME shows them: strategies first (shallow targets first), then field mappings. */
  public List<SmmNode> roots() {
    return roots;
  }

  /** Every node of the block, depth first. */
  public List<SmmNode> nodes() {
    return all;
  }

  private void build(MappingBlock block) {
    List<SmmNode> strategies = new ArrayList<>();
    for (int i = 0; i < block.getResolutionStrategies().size(); i++) {
      strategies.add(new SmmNode(blockIndex, i, block.getResolutionStrategies().get(i)));
    }
    // Stable: strategies of the same target depth keep their file order.
    strategies.sort(Comparator.comparingInt(node -> node.targetPath().size()));

    List<SmmNode> placedStrategies = new ArrayList<>();
    for (SmmNode strategy : strategies) {
      insert(strategy, placedStrategies);
      placedStrategies.add(strategy);
    }
    for (int i = 0; i < block.getFieldMappings().size(); i++) {
      insert(new SmmNode(blockIndex, i, block.getFieldMappings().get(i)), placedStrategies);
    }
    roots.forEach(root -> all.addAll(root.subtree()));
  }

  private void insert(SmmNode node, List<SmmNode> placedStrategies) {
    SmmNode parent = findParent(node, placedStrategies);
    if (parent != null) {
      parent.children().add(node);
      node.setParent(parent);
    }
    else {
      roots.add(node);
    }
  }

  private static SmmNode findParent(SmmNode node, List<SmmNode> strategies) {
    SmmNode best = null;
    for (SmmNode candidate : strategies) {
      if (!SmmPath.isBelow(node.targetPath(), candidate.targetPath())) {
        continue;
      }
      boolean sourceFits = node.isFieldMapping()
          ? SmmPath.isBelow(node.sourcePath(), candidate.sourcePath())
          : SmmPath.isSameOrBelow(node.sourcePath(), candidate.sourcePath());
      if (!sourceFits) {
        continue;
      }
      if (best == null || isMoreSpecific(candidate, best, node)) {
        best = candidate;
      }
    }
    return best;
  }

  /** Deeper target first, then the longer shared source prefix (SME sorts by both, the first one wins). */
  private static boolean isMoreSpecific(SmmNode candidate, SmmNode best, SmmNode node) {
    int byTarget = Integer.compare(candidate.targetPath().size(), best.targetPath().size());
    if (byTarget != 0) {
      return byTarget > 0;
    }
    return commonPrefix(candidate.sourcePath(), node.sourcePath()) > commonPrefix(best.sourcePath(), node.sourcePath());
  }

  private static int commonPrefix(List<String> a, List<String> b) {
    int length = Math.min(a.size(), b.size());
    for (int i = 0; i < length; i++) {
      if (!a.get(i).equals(b.get(i))) {
        return i;
      }
    }
    return length;
  }

  // ---- layout ---------------------------------------------------------------------------------

  private record Layout(Map<String, Integer> positions, Map<String, Integer> spans) {
  }

  private void layout() {
    Map<String, Integer> positions = new HashMap<>();
    for (SmmNode root : roots) {
      positions = layout(root, 0, positions).positions();
    }
  }

  private Layout layout(SmmNode node, int initialPosition, Map<String, Integer> positions) {
    String target = SmmPath.format(node.targetPath());
    int position = positions.getOrDefault(target, initialPosition);
    if (node.isFieldMapping()) {
      node.setLayout(position, 1);
      Map<String, Integer> next = new HashMap<>(positions);
      next.put(target, position + 1);
      return new Layout(next, Map.of());
    }

    Map<String, Integer> current = new HashMap<>(positions);
    Map<String, Integer> spans = new HashMap<>();
    for (SmmNode child : node.children()) {
      Layout childLayout = layout(child, position, current);
      current = childLayout.positions();
      spans = merge(spans, childLayout.spans());
    }
    int span = Math.max(1, spans.values().stream().mapToInt(Integer::intValue).max().orElse(1));
    node.setLayout(position, span);
    Map<String, Integer> next = new HashMap<>(positions);
    next.put(target, position + span);
    return new Layout(next, Map.of(target, span));
  }

  private static Map<String, Integer> merge(Map<String, Integer> a, Map<String, Integer> b) {
    Map<String, Integer> result = new HashMap<>(a);
    b.forEach((key, value) -> result.merge(key, value, Integer::sum));
    return result;
  }

  // ---- queries --------------------------------------------------------------------------------

  /**
   * The tags of the row of one target element in this block, in column order, with {@link SmmNode#gap()} set to
   * the empty columns in front of each: the nodes that target the element, plus the slice strategies that use
   * the element as their lookup (target) field.
   */
  public List<SmmNode> tagsOfRow(List<String> targetPath) {
    List<SmmNode> tags = new ArrayList<>();
    for (SmmNode node : all) {
      if (node.targetPath().equals(targetPath) || isSliceLookupTarget(node, targetPath)) {
        tags.add(node);
      }
    }
    tags.sort(Comparator.comparingInt(SmmNode::position));
    int lastEnd = 0;
    for (SmmNode tag : tags) {
      tag.setGap(Math.max(0, tag.position() - lastEnd));
      lastEnd = tag.position() + tag.span();
    }
    return tags;
  }

  /** Whether {@code node} is a slice whose lookup field in the target document is {@code targetField}. */
  public static boolean isSliceLookupTarget(SmmNode node, List<String> targetField) {
    Slice slice = node.kind() == SmmNode.Kind.SLICE ? node.resolutionStrategy().getSlice() : null;
    return slice != null && SmmPath.parse(slice.getTargetFieldFullName()).equals(targetField);
  }

  /** How many tag columns this block needs at most in one row (the widest row, at least 1). */
  public int columnCount() {
    int columns = 1;
    for (SmmNode node : all) {
      columns = Math.max(columns, node.position() + node.span());
    }
    return columns;
  }
}
