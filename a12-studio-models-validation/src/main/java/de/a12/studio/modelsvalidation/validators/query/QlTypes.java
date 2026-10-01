package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Call;
import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Field;
import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Literal;
import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Node;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * The type system of SME's {@code moduleSupport/qmm} ({@code base/type-system.ts}): what an argument of a Query
 * Language function may be, and - for a function with several signatures - how a call is matched against them so
 * the mismatch that is reported is the most helpful one. Every {@code check}/{@code validate} returns {@code null}
 * when the node is fine, else the diagnostics (SME's {@code ValidateResult}).
 */
final class QlTypes {

  private QlTypes() {
  }

  /** What the binder learned about a field reference: the kernel's name of its field type ({@code StringType},
   * {@code DateFragmentType}, ...), its enumeration values and, for a date fragment, its format. */
  record FieldInfo(String type, List<String> enumerationValues, String formatOfFragment) {
  }

  /** The bound tree: a field reference's {@link FieldInfo}; unknown for a field the binder could not type. */
  interface Env {

    FieldInfo field(Field node);
  }

  /** One argument of a signature. */
  record Arg(QlType type) {
  }

  record Matched(Node node, Arg arg) {
  }

  record Mismatch(List<Arg> signature, List<QlDiagnostic> errors, int matchScore) {
  }

  /** SME's {@code Result<T, E>}: either a result or an error. */
  record Result<T, E>(T result, E error) {

    static <T, E> Result<T, E> ok(T result) {
      return new Result<>(result, null);
    }

    static <T, E> Result<T, E> fail(E error) {
      return new Result<>(null, error);
    }

    boolean failed() {
      return error != null;
    }
  }

  // ---------------------------------------------------------------------------------------------------------

  /** {@code DisplayType.fromNode}. */
  static String displayType(Node node) {
    if (node instanceof Field) {
      return "FieldReference";
    }
    if (node instanceof Literal literal) {
      if (literal.isNumber()) {
        return "Number";
      }
      if (literal.isString()) {
        return "String";
      }
      if (literal.isBoolean()) {
        return "Boolean";
      }
      return "Null";
    }
    Call call = (Call) node;
    FunctionType function = QlFunctions.find(call.callee());
    if (function == null) {
      return "Unknown";
    }
    return switch (function.kind) {
      case REGULAR -> call.callee() + "Function";
      case OPERATOR -> call.callee() + "Operator";
      case CONSTRUCTOR -> call.callee();
    };
  }

  /** JavaScript's {@code Number#toString} for the values these diagnostics print. */
  static String number(double value) {
    if (value == Math.rint(value) && !Double.isInfinite(value)) {
      return BigDecimal.valueOf(value).toBigInteger().toString();
    }
    return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
  }

  static QlDiagnostic expectedType(Node node, String expectedType) {
    return QlDiagnostic.of(2007, node, expectedType, displayType(node));
  }

  /** {@code createIncompatibleArgumentLength}. */
  static QlDiagnostic incompatibleArgumentLength(Call node, int minExpected, int maxExpected) {
    int actual = node.arguments().size();
    if (minExpected == maxExpected && actual != minExpected) {
      return QlDiagnostic.of(2004, node, node.callee(), String.valueOf(minExpected), String.valueOf(actual));
    }
    if (actual < minExpected) {
      return QlDiagnostic.of(2005, node, node.callee(), String.valueOf(minExpected), String.valueOf(actual));
    }
    if (actual > maxExpected) {
      return QlDiagnostic.of(2006, node, node.callee(), String.valueOf(maxExpected), String.valueOf(actual));
    }
    return null;
  }

  // ---------------------------------------------------------------------------------------------------------

  abstract static class QlType {

    private boolean optional;

    abstract String name();

    String displayName() {
      return name();
    }

    QlType optional() {
      optional = true;
      return this;
    }

    boolean isOptional() {
      return optional;
    }

    List<QlType> resolve() {
      return List.of(this);
    }

    /** Null if {@code node} is of this type, else why not. */
    abstract List<QlDiagnostic> check(Env env, Node node);

    /** Checks beyond the type, e.g. a number's range; {@code call} is the call {@code node} is an argument of. */
    List<QlDiagnostic> validate(Env env, Node node, Call call) {
      return null;
    }

    QlType union(QlType other) {
      return new UnionType(this, other);
    }

    final List<QlDiagnostic> mismatch(Node node) {
      return List.of(expectedType(node, displayName()));
    }
  }

  static final class ListType extends QlType {

    final QlType elementType;

    ListType(QlType elementType) {
      this.elementType = elementType;
    }

    @Override
    String name() {
      return "List";
    }

    @Override
    String displayName() {
      String result = elementType.displayName() + "[]";
      return isOptional() ? "(" + result + ")?" : result;
    }

    @Override
    List<QlDiagnostic> check(Env env, Node node) {
      throw new IllegalStateException("A list type is only matched through its element type.");
    }
  }

  static final class UnionType extends QlType {

    private final QlType type1;
    private final QlType type2;

    UnionType(QlType type1, QlType type2) {
      this.type1 = type1;
      this.type2 = type2;
    }

    @Override
    String name() {
      return "Union";
    }

    @Override
    String displayName() {
      String result = String.join(" | ", resolve().stream().map(QlType::displayName).toList());
      return isOptional() ? "(" + result + ")?" : result;
    }

    @Override
    List<QlType> resolve() {
      List<QlType> all = new ArrayList<>(type1.resolve());
      all.addAll(type2.resolve());
      return all;
    }

    @Override
    List<QlDiagnostic> check(Env env, Node node) {
      if (type1.check(env, node) == null || type2.check(env, node) == null) {
        return null;
      }
      return mismatch(node);
    }
  }

  /** A number, optionally an integer within {@code min}..{@code max} and passing {@code validator}. */
  record NumberOptions(boolean integer, Double min, Double max,
                       BiFunction<Literal, Call, List<QlDiagnostic>> validator) {

    static NumberOptions of(boolean integer, Double min, Double max) {
      return new NumberOptions(integer, min, max, null);
    }
  }

  static final class NumberType extends QlType {

    private final NumberOptions options;

    NumberType() {
      this(null);
    }

    NumberType(NumberOptions options) {
      this.options = options;
    }

    @Override
    String name() {
      return "Number";
    }

    @Override
    List<QlDiagnostic> check(Env env, Node node) {
      return node instanceof Literal literal && literal.isNumber() ? null : mismatch(node);
    }

    @Override
    List<QlDiagnostic> validate(Env env, Node node, Call call) {
      if (!(node instanceof Literal literal) || !literal.isNumber()) {
        return mismatch(node);
      }
      if (options == null) {
        return null;
      }
      double value = (Double) literal.value();
      if (options.integer() && value != Math.rint(value)) {
        return List.of(QlDiagnostic.of(2001, node, number(value)));
      }
      if (options.min() != null && value < options.min()) {
        return List.of(QlDiagnostic.of(2002, node, number(options.min()), number(value)));
      }
      if (options.max() != null && value > options.max()) {
        return List.of(QlDiagnostic.of(2003, node, number(options.max()), number(value)));
      }
      if (options.validator() != null) {
        return options.validator().apply(literal, call);
      }
      return null;
    }
  }

  static final class StringType extends QlType {

    @Override
    String name() {
      return "String";
    }

    @Override
    List<QlDiagnostic> check(Env env, Node node) {
      return node instanceof Literal literal && literal.isString() ? null : mismatch(node);
    }
  }

  static final class NullType extends QlType {

    @Override
    String name() {
      return "Null";
    }

    @Override
    List<QlDiagnostic> check(Env env, Node node) {
      return node instanceof Literal literal && literal.isNull() ? null : mismatch(node);
    }
  }

  static final class BooleanType extends QlType {

    @Override
    String name() {
      return "Boolean";
    }

    @Override
    List<QlDiagnostic> check(Env env, Node node) {
      return node instanceof Literal literal && literal.isBoolean() ? null : mismatch(node);
    }
  }

  /** A field reference, optionally of one kind of field ({@code NumberType}, {@code StringType}, ...). */
  static final class FieldReferenceType extends QlType {

    final String fieldTypeName;

    FieldReferenceType(String fieldTypeName) {
      this.fieldTypeName = fieldTypeName;
    }

    @Override
    String name() {
      return "FieldReference";
    }

    @Override
    String displayName() {
      return (fieldTypeName == null ? "" : fieldTypeName.replaceFirst("Type", "")) + "Field";
    }

    @Override
    List<QlDiagnostic> check(Env env, Node node) {
      if (!(node instanceof Field field)) {
        return mismatch(node);
      }
      if (fieldTypeName == null) {
        return null;
      }
      FieldInfo info = env.field(field);
      if (info == null || info.type() == null || fieldTypeName.equals(info.type())) {
        return null;
      }
      return List.of(QlDiagnostic.of(2010, node, fieldTypeName, info.type()));
    }
  }

  /** The result of a constructor function: a call of exactly that function. */
  static final class ObjectType extends QlType {

    private final String objectName;

    ObjectType(String objectName) {
      this.objectName = objectName;
    }

    @Override
    String name() {
      return "Object";
    }

    @Override
    String displayName() {
      return objectName;
    }

    @Override
    List<QlDiagnostic> check(Env env, Node node) {
      return node instanceof Call call && call.callee().equals(objectName) ? null : mismatch(node);
    }
  }

  /** A call of a function that returns a boolean. */
  static final class BooleanExpressionType extends QlType {

    @Override
    String name() {
      return "BaseType";
    }

    @Override
    String displayName() {
      return "BooleanExpression";
    }

    @Override
    List<QlDiagnostic> check(Env env, Node node) {
      if (!(node instanceof Call call)) {
        return mismatch(node);
      }
      FunctionType function = QlFunctions.find(call.callee());
      if (function == null) {
        return List.of(QlDiagnostic.of(2000, node, call.callee()));
      }
      return function.returnsBoolean ? null : mismatch(node);
    }
  }

  // ---------------------------------------------------------------------------------------------------------

  enum Kind { REGULAR, OPERATOR, CONSTRUCTOR }

  /** A function with its signatures (overloads) and an optional validator of a call that matched one. */
  static final class FunctionType {

    final String callee;
    final Kind kind;
    final boolean returnsBoolean;
    final List<List<Arg>> signatures;
    final BiFunction<Env, Call, List<QlDiagnostic>> validator;

    FunctionType(String callee, Kind kind, boolean returnsBoolean, List<List<Arg>> signatures,
        BiFunction<Env, Call, List<QlDiagnostic>> validator) {
      this.callee = callee;
      this.kind = kind;
      this.returnsBoolean = returnsBoolean;
      this.signatures = signatures;
      this.validator = validator;
    }

    List<QlDiagnostic> validate(Env env, Call call) {
      return validator == null ? null : validator.apply(env, call);
    }

    /** Matches {@code call} against the signatures: the matched arguments, or what is wrong per signature. */
    Result<List<Matched>, List<Mismatch>> match(Env env, Call call) {
      if (signatures.stream().anyMatch(signature -> signature.stream().anyMatch(arg -> arg.type() instanceof ListType))) {
        return matchListTypeArguments(env, call);
      }
      Result<List<Matched>, List<Mismatch>> result = matchNonListTypeArguments(env, call);
      if (!result.failed()) {
        return result;
      }
      if (signatures.stream().anyMatch(signature -> signature.get(0).type() instanceof FieldReferenceType)) {
        return computeMismatchErrorsForFieldFirstFunction(env, call, result.error());
      }
      return result;
    }

    private Result<List<Matched>, List<Mismatch>> computeMismatchErrorsForFieldFirstFunction(Env env, Call call,
        List<Mismatch> errors) {
      if (call.arguments().isEmpty() || !(call.arguments().get(0) instanceof Field fieldNode)) {
        return Result.fail(errors);
      }
      FieldInfo info = env.field(fieldNode);
      if (info == null || info.type() == null) {
        return Result.fail(errors);
      }
      List<String> expectedFieldTypes = signatures.stream()
          .map(signature -> ((FieldReferenceType) signature.get(0).type()).fieldTypeName)
          .map(name -> name == null ? "AnyType" : name)
          .toList();
      if (expectedFieldTypes.contains("AnyType") || expectedFieldTypes.contains(info.type())) {
        return Result.fail(errors);
      }
      QlDiagnostic diagnostic = expectedFieldTypes.size() == 1
          ? QlDiagnostic.of(2010, fieldNode, expectedFieldTypes.get(0), info.type())
          : QlDiagnostic.of(2009, fieldNode, String.join(", ", expectedFieldTypes), info.type());
      return Result.fail(List.of(new Mismatch(signatures.get(0), List.of(diagnostic), 1)));
    }

    private Result<List<Matched>, List<Mismatch>> matchNonListTypeArguments(Env env, Call call) {
      List<Mismatch> errors = new ArrayList<>();
      for (List<Arg> signature : signatures) {
        int firstOptional = -1;
        for (int i = 0; i < signature.size(); i++) {
          if (signature.get(i).type().isOptional()) {
            firstOptional = i;
            break;
          }
        }
        int minArguments = firstOptional != -1 ? firstOptional : signature.size();
        int maxArguments = signature.size();

        QlDiagnostic incompatibleLength = incompatibleArgumentLength(call, minArguments, maxArguments);
        if (incompatibleLength != null) {
          errors.add(new Mismatch(signature, List.of(incompatibleLength), 1000));
          continue;
        }

        List<QlDiagnostic> signatureErrors = new ArrayList<>();
        int matchScore = 0;
        List<Matched> matched = new ArrayList<>();
        for (int index = 0; index < call.arguments().size(); index++) {
          Node argument = call.arguments().get(index);
          Arg arg = signature.get(index);
          List<QlDiagnostic> result = arg.type().check(env, argument);
          if (result != null) {
            matchScore += result.size() * (arg.type() instanceof FieldReferenceType ? 3 : 1);
            signatureErrors.addAll(result);
          }
          else {
            matched.add(new Matched(argument, arg));
          }
        }
        if (signatureErrors.isEmpty()) {
          return Result.ok(matched);
        }
        errors.add(new Mismatch(signature, signatureErrors, matchScore));
      }
      return Result.fail(errors);
    }

    /** Variable-length arguments (And, Or, Match): a greedy match of the arguments to the list types. */
    private Result<List<Matched>, List<Mismatch>> matchListTypeArguments(Env env, Call call) {
      if (signatures.size() > 1) {
        throw new IllegalStateException("Function signatures with list types can not be overloaded.");
      }
      List<Arg> signature = signatures.get(0);

      Map<String, QlType> flattenTypes = new LinkedHashMap<>();
      for (Arg arg : signature) {
        QlType type = arg.type() instanceof ListType list ? list.elementType : arg.type();
        flattenTypes.putIfAbsent(type.name(), type);
      }

      for (Node argument : call.arguments()) {
        if (flattenTypes.values().stream().noneMatch(type -> type.check(env, argument) == null)) {
          String expected = String.join(" or ", flattenTypes.values().stream().map(QlType::displayName).toList());
          return Result.fail(List.of(new Mismatch(signature, List.of(expectedType(argument, expected)), 1)));
        }
      }

      List<QlDiagnostic> errors = new ArrayList<>();
      List<Matched> result = new ArrayList<>();
      int argumentTypeIndex = 0;
      for (int argumentNodeIndex = 0; argumentNodeIndex < call.arguments().size(); ) {
        Node argumentNode = call.arguments().get(argumentNodeIndex);
        if (argumentTypeIndex >= signature.size()) {
          errors.add(QlDiagnostic.of(2037, argumentNode, displayType(argumentNode)));
          break;
        }
        Arg argumentType = signature.get(argumentTypeIndex);
        List<QlDiagnostic> checkError = argumentType.type() instanceof ListType list
            ? list.elementType.check(env, argumentNode)
            : argumentType.type().check(env, argumentNode);
        if (checkError != null) {
          argumentTypeIndex++;
          continue;
        }
        if (!(argumentType.type() instanceof ListType)) {
          argumentTypeIndex++;
        }
        argumentNodeIndex++;
        result.add(new Matched(argumentNode, argumentType));
      }

      while (argumentTypeIndex < signature.size() && signature.get(argumentTypeIndex).type() instanceof ListType) {
        argumentTypeIndex++;
      }
      if (argumentTypeIndex < signature.size()) {
        errors.add(QlDiagnostic.of(2038, call, signature.get(argumentTypeIndex).type().displayName()));
      }
      if (!errors.isEmpty()) {
        return Result.fail(List.of(new Mismatch(signature, errors, errors.size())));
      }
      return Result.ok(result);
    }
  }
}
