package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.ConfirmFieldType;
import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.DateFragmentFieldType;
import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.DateTimeFieldType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TimeFieldType;
import de.a12.studio.models.querymodel.ql.QueryLanguageException;
import de.a12.studio.models.querymodel.ql.QueryLanguageTree;
import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Call;
import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Field;
import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Literal;
import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Node;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.Arg;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.Env;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.FieldInfo;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.FunctionType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.Matched;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.Mismatch;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.Result;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterReferenceChecker.Models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type checker of a Query Language filter: a port of SME's {@code moduleSupport/qmm} binder and checker
 * ({@code binder.ts}, {@code checker.ts}, {@code functions.ts}, {@code base/type-system.ts}), which
 * {@link QueryFilterReferenceChecker} deliberately leaves out. It checks that every function exists, gets the
 * right number and kinds of arguments (overloads are resolved to the most helpful mismatch), that a comparison's
 * value fits the field's type, and the values themselves: numeric ranges, dates that exist, ranges whose bounds
 * are in order, enumeration values the field declares, date fragments of the field's format, no empty strings.
 *
 * <p>Like SME, it stops after binding if the filter has binding problems (an unresolvable field or
 * relationship): those are reported without any type findings. {@link QueryFilterReferenceChecker} words the same
 * binding problems for the Query editor; a caller that shows both should show the type findings only when that
 * one reports nothing. A field the checker cannot type (a framework meta field) matches any field requirement.
 *
 * <p>Instances cache one {@link ElementIndex} per Document Model, so create one per validation run.
 */
public final class QueryFilterTypeChecker {

  private final Models models;
  private final Map<String, ElementIndex> indexes = new HashMap<>();

  public QueryFilterTypeChecker(Models models) {
    this.models = models;
  }

  /**
   * @param scopeModel the Document Model the filter's own field paths are evaluated against; null if it could
   *                   not be determined (its fields then count as untyped)
   * @return the findings in SME's order (a call before its arguments, the top-level "is a boolean expression"
   *         check last); empty for a blank or syntactically invalid filter - see {@link
   *         QueryFilterDefinitionSyntaxValidator}
   */
  public List<QlDiagnostic> check(String filterDefinition, DocumentModel scopeModel) {
    if (filterDefinition == null || filterDefinition.isBlank()) {
      return List.of();
    }
    Node root;
    try {
      root = QueryLanguageTree.parse(filterDefinition);
    }
    catch (QueryLanguageException e) {
      return List.of();
    }

    Binder binder = new Binder();
    binder.visit(root, scopeModel);
    if (!binder.diagnostics.isEmpty()) {
      return binder.diagnostics;
    }

    Checker checker = new Checker(binder.fields::get);
    checker.visit(root);
    List<QlDiagnostic> topLevel = QlFunctions.BOOLEAN_EXPRESSION.check(checker.env, root);
    if (topLevel != null) {
      checker.errors.addAll(topLevel);
    }
    return checker.errors;
  }

  // ---------------------------------------------------------------------------------------------------------

  /** {@code binder.ts}: resolves every field reference in the scope it is evaluated in, and the scopes of {@code
   * Has(...)} constraints. */
  private final class Binder {

    final List<QlDiagnostic> diagnostics = new ArrayList<>();
    final Map<Field, FieldInfo> fields = new IdentityHashMap<>();

    void visit(Node node, DocumentModel scope) {
      switch (node) {
        case Field field -> bindField(field, scope);
        case Literal literal -> {
        }
        case Call call -> visitCall(call, scope);
      }
    }

    private void visitCall(Call call, DocumentModel scope) {
      Node targetNode = null;
      DocumentModel targetModel = null;
      Node linkNode = null;
      DocumentModel linkModel = null;
      if ("Has".equals(call.callee())) {
        HasScopes scopes = resolveHas(call, scope);
        if (scopes != null) {
          targetNode = scopes.targetNode;
          targetModel = scopes.targetModel;
          linkNode = scopes.linkNode;
          linkModel = scopes.linkModel;
        }
      }
      for (Node argument : call.arguments()) {
        if (argument == targetNode) {
          visit(argument, targetModel);
        }
        else if (argument == linkNode) {
          visit(argument, linkModel);
        }
        else {
          visit(argument, scope);
        }
      }
    }

    private void bindField(Field node, DocumentModel scope) {
      if (scope == null || scope.getContent() == null || scope.getContent().getModelRoot() == null
          || QueryElementResolution.isMetaPath(node.path())) {
        return;
      }
      ElementIndex index = indexFor(scope);
      Element element = index.resolveAbsolutePath(node.path()).orElse(null);
      if (element == null) {
        List<String> candidates = QueryFilterReferenceChecker.bestMatchFieldPaths(index, node.path());
        diagnostics.add(QlDiagnostic.of(2024, node, QlStrings.bracketed(node.path()), scope.getId(),
            candidates.stream().map(QlStrings::bracketed).collect(Collectors.joining(" or "))));
        return;
      }
      if (!(element instanceof FieldElement fieldElement)) {
        diagnostics.add(QlDiagnostic.of(2023, node, QlStrings.bracketed(node.path()), scope.getId()));
        return;
      }
      if (isNotIndexed(element)) {
        diagnostics.add(QlDiagnostic.of(2036, node));
        return;
      }
      fields.put(node, fieldInfo(index, fieldElement));
    }

    /** {@code HasFunctionParams.resolve} plus {@code Binder.resolveDocumentModels}; null if the call has a problem
     * (reported), in which case its constraints are bound against the enclosing scope, like SME does. */
    private HasScopes resolveHas(Call call, DocumentModel scope) {
      List<Node> args = call.arguments();
      QlDiagnostic arity = QlTypes.incompatibleArgumentLength(call, 2, 4);
      if (arity != null) {
        diagnostics.add(arity);
        return null;
      }
      FunctionType has = QlFunctions.find("Has");
      for (int i = 0; i < args.size(); i++) {
        List<QlDiagnostic> mismatch = has.signatures.get(0).get(i).type().check(NO_FIELDS, args.get(i));
        if (mismatch != null) {
          diagnostics.addAll(mismatch);
          return null;
        }
      }
      Literal relationshipNode = (Literal) args.get(0);
      Literal targetRoleNode = (Literal) args.get(1);
      Node constraint = args.size() > 2 && !(args.get(2) instanceof Literal) ? args.get(2) : null;
      Node linkConstraint = args.size() > 3 && !(args.get(3) instanceof Literal) ? args.get(3) : null;

      String relationshipName = (String) relationshipNode.value();
      String targetRole = (String) targetRoleNode.value();
      RelationshipModel relationshipModel = models.relationshipModel(relationshipName);
      if (relationshipModel == null || relationshipModel.getContent() == null) {
        List<String> similar = models.relationshipModels().stream().map(RelationshipModel::getId)
            .sorted(Comparator.comparingInt(id -> QlStrings.levenshtein(relationshipName, id))).limit(2).toList();
        if (similar.isEmpty()) {
          diagnostics.add(QlDiagnostic.of(2025, relationshipNode, relationshipName));
        }
        else {
          diagnostics.add(QlDiagnostic.of(2033, relationshipNode, relationshipName,
              similar.stream().map(QlStrings::quoted).collect(Collectors.joining(" or "))));
        }
        return null;
      }

      List<EntityCharacteristic> characteristics = relationshipModel.getContent().getEntityCharacteristics();
      EntityCharacteristic targetCharacteristic = characteristics.stream()
          .filter(characteristic -> targetRole.equals(characteristic.getRole())).findFirst().orElse(null);
      List<String> expectedTargetRoles = QueryFilterReferenceChecker.expectedTargetRoles(characteristics,
          scope != null ? scope.getId() : null);
      if (targetCharacteristic == null) {
        if (expectedTargetRoles.size() == 1) {
          diagnostics.add(QlDiagnostic.of(2026, targetRoleNode, targetRole, QlStrings.quoted(expectedTargetRoles.get(0))));
        }
        else {
          diagnostics.add(QlDiagnostic.of(2027, targetRoleNode, targetRole,
              expectedTargetRoles.stream().map(QlStrings::quoted).collect(Collectors.joining(" or "))));
        }
        return null;
      }
      if (!expectedTargetRoles.contains(targetRole)) {
        diagnostics.add(QlDiagnostic.of(2027, targetRoleNode, targetRole,
            expectedTargetRoles.stream().map(QlStrings::quoted).collect(Collectors.joining(" or "))));
        return null;
      }

      DocumentModel targetModel = models.documentModel(targetCharacteristic.getDocumentModel());
      if (targetModel == null) {
        diagnostics.add(QlDiagnostic.of(2029, targetRoleNode, targetCharacteristic.getDocumentModel()));
        return null;
      }

      DocumentModel linkModel = null;
      if (linkConstraint != null) {
        String linkModelId = relationshipModel.getContent().getLinkDocumentModelValue();
        if (linkModelId == null) {
          diagnostics.add(QlDiagnostic.of(2030, linkConstraint, relationshipModel.getId()));
          return null;
        }
        linkModel = models.documentModel(linkModelId);
        if (linkModel == null) {
          diagnostics.add(QlDiagnostic.of(2031, linkConstraint, linkModelId));
          return null;
        }
      }
      return new HasScopes(constraint, targetModel, linkConstraint, linkModel);
    }
  }

  private record HasScopes(Node targetNode, DocumentModel targetModel, Node linkNode, DocumentModel linkModel) {
  }

  private static final Env NO_FIELDS = field -> null;

  private static boolean isNotIndexed(Element element) {
    return element.getAnnotations().stream()
        .filter(annotation -> "indexed".equals(annotation.getName()))
        .map(Annotation::getValue)
        .anyMatch("false"::equals);
  }

  private ElementIndex indexFor(DocumentModel model) {
    return indexes.computeIfAbsent(model.getId(), id -> new ElementIndex(model, models.documentModels()));
  }

  static FieldInfo fieldInfo(ElementIndex index, FieldElement element) {
    FieldType type = element.getField() == null ? null : index.effectiveFieldType(element.getField().getFieldType());
    return switch (type) {
      case BooleanFieldType t -> new FieldInfo("BooleanType", null, null);
      case ConfirmFieldType t -> new FieldInfo("ConfirmType", null, null);
      case StringFieldType t -> new FieldInfo("StringType", null, null);
      case NumberFieldType t -> new FieldInfo("NumberType", null, null);
      case DateFieldType t -> new FieldInfo("DateType", null, null);
      case DateTimeFieldType t -> new FieldInfo("DateTimeType", null, null);
      case TimeFieldType t -> new FieldInfo("TimeType", null, null);
      case DateRangeFieldType t -> new FieldInfo("DateRangeType", null, null);
      case DateFragmentFieldType t -> new FieldInfo("DateFragmentType", null,
          t.getDateFragmentType() == null ? null : t.getDateFragmentType().getFormatOfFragment());
      case EnumerationFieldType t -> new FieldInfo("EnumerationType",
          t.getEnumerationType() == null ? List.of() : t.getEnumerationType().getValues().stream()
              .map(value -> value.getValue()).filter(Objects::nonNull).toList(), null);
      case null, default -> new FieldInfo(null, null, null);
    };
  }

  // ---------------------------------------------------------------------------------------------------------

  /** {@code checker.ts}. */
  private static final class Checker {

    final List<QlDiagnostic> errors = new ArrayList<>();
    final Env env;

    Checker(Env env) {
      this.env = env;
    }

    void visit(Node node) {
      if (node instanceof Literal literal) {
        if (literal.isString() && ((String) literal.value()).isEmpty()) {
          errors.add(QlDiagnostic.of(2034, literal));
        }
      }
      else if (node instanceof Call call) {
        List<QlDiagnostic> result = checkCall(call);
        if (result != null) {
          errors.addAll(result);
        }
        call.arguments().forEach(this::visit);
      }
    }

    private List<QlDiagnostic> checkCall(Call call) {
      FunctionType function = QlFunctions.find(call.callee());
      if (function == null) {
        return List.of(QlDiagnostic.of(2000, call, call.callee()));
      }
      Result<List<Matched>, List<QlDiagnostic>> matched = matchedArguments(call, function);
      if (matched.failed()) {
        return matched.error();
      }
      List<QlDiagnostic> argumentResults = new ArrayList<>();
      for (Matched match : matched.result()) {
        List<QlDiagnostic> validation = match.arg().type().validate(env, match.node(), call);
        if (validation != null) {
          argumentResults.addAll(validation);
        }
      }
      if (!argumentResults.isEmpty()) {
        return argumentResults;
      }
      return function.validate(env, call);
    }

    private Result<List<Matched>, List<QlDiagnostic>> matchedArguments(Call call, FunctionType function) {
      Result<List<Matched>, List<Mismatch>> matched = function.match(env, call);
      if (!matched.failed()) {
        return Result.ok(matched.result());
      }
      List<Mismatch> mismatches = matched.error();
      // Exactly one signature with errors: that one.
      if (mismatches.size() == 1) {
        return Result.fail(mismatches.get(0).errors());
      }

      int minArguments = function.signatures.stream()
          .mapToInt(signature -> (int) signature.stream().filter(arg -> !arg.type().isOptional()).count()).min().orElse(0);
      int maxArguments = function.signatures.stream().mapToInt(List::size).max().orElse(0);
      QlDiagnostic incompatibleLength = QlTypes.incompatibleArgumentLength(call, minArguments, maxArguments);
      if (incompatibleLength != null) {
        return Result.fail(List.of(incompatibleLength));
      }

      // All signatures have the same length: the unique best match gives the most specific error.
      Set<Integer> lengths = new HashSet<>();
      function.signatures.forEach(signature -> lengths.add(signature.size()));
      if (lengths.size() == 1) {
        List<Mismatch> sorted = new ArrayList<>(mismatches);
        sorted.sort(Comparator.comparingInt(Mismatch::matchScore));
        Mismatch best = sorted.get(0);
        if (sorted.stream().skip(1).noneMatch(other -> other.matchScore() == best.matchScore())) {
          return Result.fail(best.errors());
        }
      }

      String actualSignature = "(" + call.arguments().stream().map(QlTypes::displayType).collect(Collectors.joining(", ")) + ")";
      String expectedSignatures = function.signatures.stream()
          .map(signature -> "(" + signature.stream().map(Arg::type).map(type -> type.displayName()).collect(Collectors.joining(", ")) + ")")
          .collect(Collectors.joining(", "));
      return Result.fail(List.of(QlDiagnostic.of(2008, call, call.callee(), expectedSignatures, actualSignature)));
    }
  }
}
