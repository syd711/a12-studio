package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.querymodel.ql.QueryLanguageException;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences.FieldReference;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences.HasCall;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences.Reference;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves what a Query Language filter refers to against the project's real models - the Java counterpart of
 * SME's {@code moduleSupport/qmm} binder/checker, restricted to <em>existence</em> (no type checking; {@link
 * de.a12.studio.models.querymodel.ql.QueryLanguageEmitter} deliberately does none either - SME's own {@code
 * checker.ts}/{@code functions.ts}/{@code base/type-system.ts} do carry a real, much larger type-checking layer
 * function-signature overload resolution and per-argument validators - not ported here; see "Query Model" in
 * docs/sme-reference-comparison.md for why that's tracked as its own, separately-sized follow-up):
 * <ul>
 *   <li>every {@code [/Path/To/Field]} must resolve, in the Document Model the filter is evaluated against, to a
 *   field that is not annotated {@code indexed = false} (SME diagnostics 2023/2024/2036); an unresolved path's
 *   error message includes up to 2 "did you mean" suggestions - the closest indexable field paths by plain
 *   Levenshtein distance, no threshold - mirroring {@code resolver.ts}'s {@code getBestMatchFields} exactly
 *   (see {@link #bestMatchFieldPaths});</li>
 *   <li>{@code Has(relationship, role, constraint, linkConstraint)}: the relationship must exist, the role must
 *   exist in it and be reachable from the current Document Model (all roles for a self-referencing relationship,
 *   otherwise the roles of the <em>other</em> Document Model(s) - {@code Resolver.getExpectedTargetRoles}), and
 *   its {@code constraint} is checked in the role's Document Model, its {@code linkConstraint} in the
 *   relationship's link Document Model (2025-2031).</li>
 * </ul>
 * A scope whose Document Model cannot be determined (a broken hop further up) has its field paths skipped rather
 * than reported a second time, but the {@code Has} calls nested in it are still checked. Syntax problems are not
 * reported here - see {@link QueryFilterDefinitionSyntaxValidator}. Shared by that validator's semantic sibling
 * {@link QueryFilterDefinitionReferenceValidator} and the Query editor's per-keystroke filter check.
 *
 * <p>Type checking (see {@link #checkAll}) is {@link QueryFilterTypeChecker}'s job; this class stays on existence.
 *
 * <p>Instances cache one {@link ElementIndex} per Document Model, so create one per validation run (or per editor
 * load), not per keystroke.
 */
public final class QueryFilterReferenceChecker {

  /** The project models a filter can refer to. */
  public record Models(List<DocumentModel> documentModels, List<RelationshipModel> relationshipModels) {

    public static Models of(ValidationContext context) {
      return new Models(context.otherDocumentModels(), context.otherModels().stream()
          .filter(RelationshipModel.class::isInstance).map(RelationshipModel.class::cast).toList());
    }

    public DocumentModel documentModel(String id) {
      return id == null ? null : documentModels.stream().filter(model -> id.equals(model.getId())).findFirst().orElse(null);
    }

    public RelationshipModel relationshipModel(String id) {
      return id == null ? null : relationshipModels.stream().filter(model -> id.equals(model.getId())).findFirst().orElse(null);
    }

    /** The Document Model {@code role} plays in {@code relationshipModelId}, or null if the relationship, the role
     * or that Document Model does not exist. */
    public DocumentModel roleDocumentModel(String relationshipModelId, String role) {
      RelationshipModel relationshipModel = relationshipModel(relationshipModelId);
      if (relationshipModel == null || relationshipModel.getContent() == null || role == null) {
        return null;
      }
      return relationshipModel.getContent().getEntityCharacteristics().stream()
          .filter(characteristic -> role.equals(characteristic.getRole()))
          .map(characteristic -> documentModel(characteristic.getDocumentModel()))
          .findFirst().orElse(null);
    }
  }

  private final Models models;
  private final Map<String, ElementIndex> indexes = new HashMap<>();

  private final QueryFilterTypeChecker typeChecker;

  public QueryFilterReferenceChecker(Models models) {
    this.models = models;
    this.typeChecker = new QueryFilterTypeChecker(models);
  }

  /**
   * {@link #check} and, only if that finds nothing, the findings of {@link QueryFilterTypeChecker} (function
   * names, argument counts and types, value ranges, enumeration values, ...) - SME reports binding problems
   * without any type findings too. What the Query editor and the Query validators show.
   */
  public List<String> checkAll(String filterDefinition, DocumentModel scopeModel) {
    List<String> references = check(filterDefinition, scopeModel);
    if (!references.isEmpty()) {
      return references;
    }
    return typeChecker.check(filterDefinition, scopeModel).stream().map(QlDiagnostic::message).toList();
  }

  /**
   * @param filterDefinition a Query Language expression; blank or syntactically invalid input yields no messages
   * @param scopeModel the Document Model the expression's own field paths are evaluated against (the query's
   *                   target Document Model, or a link's resolved one); null if it could not be determined
   * @return one human-readable message per unresolved reference, in source order
   */
  public List<String> check(String filterDefinition, DocumentModel scopeModel) {
    if (filterDefinition == null || filterDefinition.isBlank()) {
      return List.of();
    }
    List<Reference> references;
    try {
      references = QueryLanguageReferences.extract(filterDefinition);
    } catch (QueryLanguageException e) {
      return List.of();
    }
    List<String> messages = new ArrayList<>();
    checkScope(references, scopeModel, messages);
    return messages;
  }

  private void checkScope(List<Reference> references, DocumentModel scopeModel, List<String> messages) {
    for (Reference reference : references) {
      switch (reference) {
        case FieldReference field -> checkField(field.path(), scopeModel, messages);
        case HasCall has -> checkHas(has, scopeModel, messages);
      }
    }
  }

  private void checkField(String path, DocumentModel scopeModel, List<String> messages) {
    if (scopeModel == null || scopeModel.getContent() == null || scopeModel.getContent().getModelRoot() == null
        || QueryElementResolution.isMetaPath(path)) {
      return;
    }
    ElementIndex index = indexFor(scopeModel);
    Element element = index.resolveAbsolutePath(path).orElse(null);
    if (element == null) {
      List<String> candidates = bestMatchFieldPaths(index, path);
      messages.add(candidates.isEmpty()
          ? ValidationMessages.get("validation.queryFilterReference.unknownField", path, scopeModel.getId())
          : ValidationMessages.get("validation.queryFilterReference.unknownFieldWithSuggestions", path, scopeModel.getId(),
              candidates.stream().map(candidate -> "[" + candidate + "]").collect(Collectors.joining(" or "))));
    } else if (!(element instanceof FieldElement)) {
      messages.add(ValidationMessages.get("validation.queryFilterReference.notAField", path, scopeModel.getId()));
    } else if (isNotIndexed(element)) {
      messages.add(ValidationMessages.get("validation.queryFilterReference.notIndexed", path, scopeModel.getId()));
    }
  }

  private void checkHas(HasCall has, DocumentModel scopeModel, List<String> messages) {
    DocumentModel targetModel = null;
    DocumentModel linkModel = null;

    RelationshipModel relationshipModel = models.relationshipModel(has.relationshipModel());
    if (relationshipModel == null || relationshipModel.getContent() == null) {
      messages.add(ValidationMessages.get("validation.queryRelationshipTraversal.unknownRelationship", has.relationshipModel()));
    } else {
      List<EntityCharacteristic> characteristics = relationshipModel.getContent().getEntityCharacteristics();
      EntityCharacteristic role = characteristics.stream()
          .filter(characteristic -> has.targetRole().equals(characteristic.getRole())).findFirst().orElse(null);
      if (role == null) {
        messages.add(ValidationMessages.get("validation.queryRelationshipTraversal.unknownRole",
            has.targetRole(), has.relationshipModel()));
      } else if (scopeModel != null && !expectedTargetRoles(characteristics, scopeModel.getId()).contains(has.targetRole())) {
        messages.add(ValidationMessages.get("validation.queryFilterReference.roleNotReachable", has.targetRole(),
            has.relationshipModel(), scopeModel.getId(),
            expectedTargetRoles(characteristics, scopeModel.getId()).stream().map(r -> '"' + r + '"').collect(Collectors.joining(" or "))));
      } else {
        targetModel = models.documentModel(role.getDocumentModel());
        if (targetModel == null) {
          messages.add(ValidationMessages.get("validation.queryFilterReference.documentModelMissing",
              role.getDocumentModel(), has.targetRole(), has.relationshipModel()));
        }
      }
      if (has.linkConstraint() != null) {
        linkModel = resolveLinkModel(relationshipModel, messages);
      }
    }

    if (has.constraint() != null) {
      checkScope(has.constraint(), targetModel, messages);
    }
    if (has.linkConstraint() != null) {
      checkScope(has.linkConstraint(), linkModel, messages);
    }
  }

  private DocumentModel resolveLinkModel(RelationshipModel relationshipModel, List<String> messages) {
    String linkModelId = relationshipModel.getContent().getLinkDocumentModelValue();
    if (linkModelId == null) {
      messages.add(ValidationMessages.get("validation.queryFilterReference.noLinkDocumentModel", relationshipModel.getId()));
      return null;
    }
    DocumentModel linkModel = models.documentModel(linkModelId);
    if (linkModel == null) {
      messages.add(ValidationMessages.get("validation.queryFilterReference.linkDocumentModelMissing",
          linkModelId, relationshipModel.getId()));
    }
    return linkModel;
  }

  /** Every role for a self-referencing relationship (one Document Model plays all roles), otherwise the roles
   * played by a Document Model other than {@code currentDocumentModelId} - SME's {@code
   * Resolver.getExpectedTargetRoles}. */
  static List<String> expectedTargetRoles(List<EntityCharacteristic> characteristics, String currentDocumentModelId) {
    Set<String> documentModelIds = new HashSet<>();
    characteristics.forEach(characteristic -> documentModelIds.add(characteristic.getDocumentModel()));
    boolean selfReference = documentModelIds.size() == 1;
    return characteristics.stream()
        .filter(characteristic -> selfReference || !Objects.equals(characteristic.getDocumentModel(), currentDocumentModelId))
        .map(EntityCharacteristic::getRole)
        .toList();
  }

  /**
   * SME's "did you mean" suggestions for an unresolved field path ({@code resolver.ts}'s {@code
   * getBestMatchFields}): every indexable field path in {@code index}'s model, ordered by plain Levenshtein
   * distance to {@code invalidPath} ascending, the closest {@value #MAX_SUGGESTED_FIELDS} kept - no distance
   * threshold, matching SME exactly (even a wildly dissimilar path still gets its closest matches suggested).
   */
  static List<String> bestMatchFieldPaths(ElementIndex index, String invalidPath) {
    return index.allElements().stream()
        .filter(FieldElement.class::isInstance)
        .filter(element -> !isNotIndexed(element))
        .map(index::getPath)
        .sorted(Comparator.comparingInt(candidate -> levenshteinDistance(invalidPath, candidate)))
        .limit(MAX_SUGGESTED_FIELDS)
        .toList();
  }

  private static final int MAX_SUGGESTED_FIELDS = 2;

  private static int levenshteinDistance(String a, String b) {
    int[] previous = new int[b.length() + 1];
    int[] current = new int[b.length() + 1];
    for (int j = 0; j <= b.length(); j++) {
      previous[j] = j;
    }
    for (int i = 1; i <= a.length(); i++) {
      current[0] = i;
      for (int j = 1; j <= b.length(); j++) {
        int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
      }
      int[] swap = previous;
      previous = current;
      current = swap;
    }
    return previous[b.length()];
  }

  private static boolean isNotIndexed(Element element) {
    return element.getAnnotations().stream()
        .filter(annotation -> "indexed".equals(annotation.getName()))
        .map(Annotation::getValue)
        .anyMatch("false"::equals);
  }

  private ElementIndex indexFor(DocumentModel model) {
    return indexes.computeIfAbsent(model.getId(), id -> new ElementIndex(model, models.documentModels()));
  }
}
