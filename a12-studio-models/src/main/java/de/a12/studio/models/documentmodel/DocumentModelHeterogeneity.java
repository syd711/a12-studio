package de.a12.studio.models.documentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Document Models' heterogeneity (super type / sub type) graph, mirroring SME's {@code
 * heterogeneityGraph.ts} ({@code findReachableSuperTypes}): a Document Model declares its super types in a
 * {@code superTypes} header annotation and its sub types in {@code subTypes} (both comma-separated ids). Edges
 * point towards the super types, and an entry in {@code subTypes} contributes the reverse edge.
 * <p>
 * Only the models passed in take part. SME's {@code heterogeneityGraph.ts} builds the graph from entries of type
 * {@code document} only, so a Combination Model has no reachable super types there even if it carries a {@code
 * superTypes} annotation; the Tree Model's sub type resolution ({@code getSubTypesInfo}) uses every standalone Document
 * Model type, Combination Models included - callers pass the models their SME counterpart works on.
 */
public final class DocumentModelHeterogeneity {

  public static final String SUPER_TYPES_ANNOTATION = "superTypes";
  public static final String SUB_TYPES_ANNOTATION = "subTypes";

  private DocumentModelHeterogeneity() {
  }

  /**
   * The ids of the direct and transitive super types of {@code sourceId}, in breadth-first order and without
   * {@code sourceId} itself. Empty if {@code sourceId} is not one of {@code documentModels} (SME's {@code
   * undefined}, which its callers treat as "no super types").
   */
  public static List<String> reachableSuperTypes(Collection<? extends A12Model<?>> documentModels, String sourceId) {
    Map<String, List<String>> graph = superTypeGraph(documentModels);
    if (sourceId == null || !graph.containsKey(sourceId)) {
      return List.of();
    }

    List<String> result = new ArrayList<>();
    Set<String> visited = new HashSet<>(List.of(sourceId));
    Deque<String> queue = new ArrayDeque<>(List.of(sourceId));
    while (!queue.isEmpty()) {
      for (String next : graph.getOrDefault(queue.poll(), List.of())) {
        if (visited.add(next)) {
          result.add(next);
          queue.add(next);
        }
      }
    }
    return result;
  }

  /**
   * The ids of the Document Models that are a direct sub type of {@code superId}: those declaring it in their {@code
   * superTypes} annotation and those {@code superId} lists in its {@code subTypes} annotation (SME's {@code
   * resolveSubTypes}). Empty if {@code superId} is not one of {@code documentModels}.
   */
  public static List<String> directSubTypes(Collection<? extends A12Model<?>> documentModels, String superId) {
    Map<String, List<String>> graph = superTypeGraph(documentModels);
    if (superId == null || !graph.containsKey(superId)) {
      return List.of();
    }
    List<String> result = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : graph.entrySet()) {
      if (entry.getValue().contains(superId) && !result.contains(entry.getKey())) {
        result.add(entry.getKey());
      }
    }
    return result;
  }

  /** Whether {@code model} is marked {@code abstract} (header annotation {@code abstract = true}). */
  public static boolean isAbstract(A12Model<?> model) {
    if (model == null || model.getAnnotations() == null) {
      return false;
    }
    return model.getAnnotations().stream()
        .anyMatch(annotation -> "abstract".equals(annotation.getName()) && "true".equals(annotation.getValue()));
  }

  private static Map<String, List<String>> superTypeGraph(Collection<? extends A12Model<?>> documentModels) {
    Map<String, List<String>> graph = new LinkedHashMap<>();
    for (A12Model<?> model : documentModels) {
      if (model.getId() != null) {
        graph.put(model.getId(), new ArrayList<>());
      }
    }
    for (A12Model<?> model : documentModels) {
      if (model.getId() == null) {
        continue;
      }
      graph.get(model.getId()).addAll(annotationIds(model, SUPER_TYPES_ANNOTATION));
      for (String subType : annotationIds(model, SUB_TYPES_ANNOTATION)) {
        List<String> successors = graph.get(subType);
        if (successors != null) {
          successors.add(model.getId());
        }
      }
    }
    return graph;
  }

  private static List<String> annotationIds(A12Model<?> model, String annotationName) {
    List<String> ids = new ArrayList<>();
    if (model.getAnnotations() == null) {
      return ids;
    }
    for (Annotation annotation : model.getAnnotations()) {
      if (annotationName.equals(annotation.getName()) && annotation.getValue() != null) {
        for (String id : annotation.getValue().split(",")) {
          if (!id.isBlank()) {
            ids.add(id.trim());
          }
        }
      }
    }
    return ids;
  }
}
