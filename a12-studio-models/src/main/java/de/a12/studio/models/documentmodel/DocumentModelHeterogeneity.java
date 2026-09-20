package de.a12.studio.models.documentmodel;

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
 * Like SME, only Document Models take part: a Combination Model is not a node (SME builds the graph from
 * entries of type {@code document}), so it has no reachable super types even if it carries a {@code superTypes}
 * annotation.
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
  public static List<String> reachableSuperTypes(Collection<DocumentModel> documentModels, String sourceId) {
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

  private static Map<String, List<String>> superTypeGraph(Collection<DocumentModel> documentModels) {
    Map<String, List<String>> graph = new LinkedHashMap<>();
    for (DocumentModel model : documentModels) {
      if (model.getId() != null) {
        graph.put(model.getId(), new ArrayList<>());
      }
    }
    for (DocumentModel model : documentModels) {
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

  private static List<String> annotationIds(DocumentModel model, String annotationName) {
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
