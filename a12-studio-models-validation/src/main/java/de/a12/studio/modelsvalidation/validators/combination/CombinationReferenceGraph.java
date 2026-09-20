package de.a12.studio.modelsvalidation.validators.combination;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Port of SME's {@code CombModelReferenceHelper.modelCausesOrHasLoop}
 * ({@code client/src/modules/combinationModel/references/combModelReferenceHelper.ts}): does selecting
 * {@code candidate} as base/additive model of the Combined Document Model being validated create a reference
 * loop? Works on the reference graph alone - no Document Model expansion is involved.
 *
 * <p>SME collects the models transitively reachable from the candidate and reports a loop when that set holds
 * the candidate itself (it has a loop of its own) or the model being validated (it leads back). What counts as
 * an edge, copied from SME:
 * <ul>
 *   <li>a Combined Document Model: its base model, plus - per step - the Additive Model of an {@code Addition}
 *   step (expanded further, <em>every</em> header reference of it followed) and the Selection/Decoration models
 *   of the other steps (recorded but not expanded);</li>
 *   <li>a Document Model: its header references. The walk starts from the candidate with only {@code include}
 *   references counting; the one exception is below the Additive Model of an {@code Addition} step of a
 *   Combined Document Model on the way, where every purpose counts.</li>
 * </ul>
 * Differences to SME, both only in edge cases: the walk tracks (model, purpose filter) pairs, so a model first
 * reached through an include-only path is still expanded when an additive step reaches it with every purpose;
 * and a model that is not in the project is a leaf (SME would throw), the missing-reference rules report it.
 */
final class CombinationReferenceGraph {

  /** A loop, as the chain of model ids that closes it, e.g. {@code [Base_Cm, Other_Cm, Base_Cm]}. */
  record Loop(List<String> path) {

    String describe() {
      return String.join(" -> ", path);
    }
  }

  private enum Purposes {
    INCLUDE_ONLY,
    ALL
  }

  private record Edge(String target, Purposes purposes, boolean expand) {
  }

  private record Node(String id, Purposes purposes, List<String> path) {
  }

  private CombinationReferenceGraph() {
  }

  /**
   * @param self      the Combined Document Model being validated (its id is the "parent" of SME's check)
   * @param candidate the id selected as base model or additive model
   * @return the loop if selecting {@code candidate} creates one (or the candidate already has one)
   */
  static Optional<Loop> findLoop(CombinedDocumentModel self, String candidate, ValidationContext context) {
    String parentId = self.getId();
    if (candidate.equals(parentId)) {
      return Optional.of(new Loop(List.of(parentId, parentId)));
    }

    Set<String> visited = new HashSet<>();
    Deque<Node> queue = new ArrayDeque<>();
    queue.add(new Node(candidate, Purposes.INCLUDE_ONLY, List.of(candidate)));
    visited.add(key(candidate, Purposes.INCLUDE_ONLY));
    while (!queue.isEmpty()) {
      Node node = queue.poll();
      for (Edge edge : edges(context.findOtherModel(node.id()), node.purposes())) {
        List<String> path = new ArrayList<>(node.path());
        path.add(edge.target());
        if (edge.target().equals(parentId)) {
          path.add(0, parentId);
          return Optional.of(new Loop(path));
        }
        if (edge.target().equals(candidate)) {
          return Optional.of(new Loop(path));
        }
        if (edge.expand() && visited.add(key(edge.target(), edge.purposes()))) {
          queue.add(new Node(edge.target(), edge.purposes(), path));
        }
      }
    }
    return Optional.empty();
  }

  private static List<Edge> edges(A12Model<?> model, Purposes purposes) {
    List<Edge> edges = new ArrayList<>();
    if (model instanceof CombinedDocumentModel combined && combined.getContent() != null) {
      addEdge(edges, combined.getContent().getBaseModelId(), purposes, true);
      for (CombinationStep step : combined.getContent().getCombinationSteps()) {
        if (step.getType() == null) {
          continue;
        }
        switch (step.getType()) {
          case ADDITION -> {
            if (step.getAdditiveModel() != null) {
              addEdge(edges, step.getAdditiveModel().getDmId(), Purposes.ALL, true);
            }
          }
          case SELECTION -> {
            if (step.getSelectionModel() != null) {
              addEdge(edges, step.getSelectionModel().getSmId(), purposes, false);
            }
          }
          case DECORATION_FOR_FIELDS, DECORATION_FOR_GROUPS -> {
            if (step.getDecorationModel() != null) {
              addEdge(edges, step.getDecorationModel().getDmId(), purposes, false);
            }
            if (step.getSelectionModel() != null) {
              addEdge(edges, step.getSelectionModel().getSmId(), purposes, false);
            }
          }
        }
      }
    }
    else if (model instanceof DocumentModel) {
      for (ModelReference reference : model.getModelReferences()) {
        if (purposes == Purposes.INCLUDE_ONLY && !ModelReference.PURPOSE_INCLUDE.equals(reference.getPurpose())) {
          continue;
        }
        addEdge(edges, reference.getReference(), purposes, true);
      }
    }
    return edges;
  }

  private static void addEdge(List<Edge> edges, String target, Purposes purposes, boolean expand) {
    if (target != null && !target.isBlank()) {
      edges.add(new Edge(target, purposes, expand));
    }
  }

  private static String key(String id, Purposes purposes) {
    return purposes + ":" + id;
  }
}
