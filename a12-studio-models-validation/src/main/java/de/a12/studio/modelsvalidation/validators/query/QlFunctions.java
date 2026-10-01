package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Call;
import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Field;
import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Literal;
import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Node;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.Arg;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.BooleanExpressionType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.BooleanType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.Env;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.FieldInfo;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.FieldReferenceType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.FunctionType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.Kind;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.ListType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.NullType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.NumberOptions;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.NumberType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.ObjectType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.QlType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.Result;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.StringType;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Query Language functions and their signatures and validators - SME's {@code moduleSupport/qmm}
 * {@code functions.ts}, in the same order. Operators are functions too: {@link
 * de.a12.studio.models.querymodel.ql.QueryLanguageTree} turns {@code ==} into {@code Equal} etc.
 */
final class QlFunctions {

  private QlFunctions() {
  }

  private static final Map<String, FunctionType> FUNCTIONS = new LinkedHashMap<>();

  static FunctionType find(String callee) {
    return FUNCTIONS.get(callee);
  }

  static final BooleanExpressionType BOOLEAN_EXPRESSION = new BooleanExpressionType();

  private static final double YEAR_MIN = 1000;
  private static final double YEAR_MAX = 9999;
  private static final double MONTH_MAX = 12;

  // ---- signature helpers ----

  private static Arg arg(QlType type) {
    return new Arg(type);
  }

  private static List<Arg> sig(QlType... types) {
    List<Arg> args = new ArrayList<>();
    for (QlType type : types) {
      args.add(arg(type));
    }
    return args;
  }

  private static FieldReferenceType field(String type) {
    return new FieldReferenceType(type);
  }

  private static ObjectType object(String name) {
    return new ObjectType(name);
  }

  private static void register(String callee, Kind kind, boolean returnsBoolean, List<List<Arg>> signatures,
      java.util.function.BiFunction<Env, Call, List<QlDiagnostic>> validator) {
    FUNCTIONS.put(callee, new FunctionType(callee, kind, returnsBoolean, signatures, validator));
  }

  // ---- numbers of nodes, as JavaScript sees them ----

  /** {@code Number(node.value)} for a literal; NaN for anything that has no numeric value. */
  private static double jsNumber(Node node) {
    if (!(node instanceof Literal literal)) {
      return Double.NaN;
    }
    Object value = literal.value();
    if (value == null) {
      return 0;
    }
    if (value instanceof Double number) {
      return number;
    }
    if (value instanceof Boolean flag) {
      return flag ? 1 : 0;
    }
    String text = ((String) value).trim();
    if (text.isEmpty()) {
      return 0;
    }
    try {
      return Double.parseDouble(text);
    }
    catch (NumberFormatException e) {
      return Double.NaN;
    }
  }

  private static List<QlDiagnostic> combine(List<QlDiagnostic>... results) {
    List<QlDiagnostic> all = new ArrayList<>();
    for (List<QlDiagnostic> result : results) {
      if (result != null) {
        all.addAll(result);
      }
    }
    return all.isEmpty() ? null : all;
  }

  private static String monthName(int month) {
    return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
  }

  private static String candidates(List<String> values, String target, int limit) {
    return values.stream()
        .sorted(Comparator.comparingInt(value -> QlStrings.levenshtein(target, value)))
        .limit(limit)
        .map(QlStrings::quoted)
        .collect(Collectors.joining(", or "));
  }

  // ---- date fragments ----

  private static List<QlDiagnostic> validateOneParamSignatureDateFragments(Literal node, Call call) {
    double value = (Double) node.value();
    if (value > YEAR_MAX) {
      return List.of(QlDiagnostic.of(2012, node));
    }
    if (value > MONTH_MAX && value < YEAR_MIN) {
      return List.of(QlDiagnostic.of(2012, node));
    }
    return null;
  }

  private static List<QlDiagnostic> validateTwoParamsSignatureDateFragments(Literal dayOrMonthNode, Call call) {
    Node first = call.arguments().get(0);
    double yearOrMonth = jsNumber(first);
    double dayOrMonth = (Double) dayOrMonthNode.value();
    if (yearOrMonth <= MONTH_MAX) {
      // Format: MM-DD
      if (yearOrMonth != Math.rint(yearOrMonth)) {
        return null;
      }
      int month = (int) yearOrMonth;
      if (List.of(1, 3, 5, 7, 8, 10, 12).contains(month) && dayOrMonth > 31) {
        return List.of(QlDiagnostic.of(2013, dayOrMonthNode, monthName(month), "31"));
      }
      if (List.of(4, 6, 9, 11).contains(month) && dayOrMonth > 30) {
        return List.of(QlDiagnostic.of(2013, dayOrMonthNode, monthName(month), "30"));
      }
      if (month == 2 && dayOrMonth > 29) {
        return List.of(QlDiagnostic.of(2014, dayOrMonthNode));
      }
      return null;
    }
    if (yearOrMonth >= YEAR_MIN) {
      // Format: YYYY-MM
      if (dayOrMonth > MONTH_MAX) {
        return List.of(QlDiagnostic.of(2015, dayOrMonthNode));
      }
    }
    return null;
  }

  /** {@code DateFragmentFunctionParams}: the format a {@code DateFragment(...)} call has, and its parts. */
  record DateFragmentParams(String format, double first, double second) {
  }

  private static Result<String, List<QlDiagnostic>> resolveDateFragmentFormat(Call node) {
    List<Node> args = node.arguments();
    if (args.size() == 1) {
      double value = jsNumber(args.get(0));
      if (value <= 12) {
        return Result.ok("MM");
      }
      if (value >= 1000) {
        return Result.ok("YYYY");
      }
      return Result.fail(List.of(QlDiagnostic.of(2032, args.get(0))));
    }
    if (args.size() == 2) {
      double value = jsNumber(args.get(0));
      if (value <= 12) {
        return Result.ok("MM-DD");
      }
      if (value >= 1000) {
        return Result.ok("YYYY-MM");
      }
      return Result.fail(List.of(QlDiagnostic.of(2032, args.get(0))));
    }
    return Result.fail(List.of(QlTypes.incompatibleArgumentLength(node, 1, 2)));
  }

  private static Result<DateFragmentParams, List<QlDiagnostic>> resolveDateFragment(Call node) {
    Result<String, List<QlDiagnostic>> format = resolveDateFragmentFormat(node);
    if (format.failed()) {
      return Result.fail(format.error());
    }
    double first = jsNumber(node.arguments().get(0));
    double second = node.arguments().size() > 1 ? jsNumber(node.arguments().get(1)) : Double.NaN;
    return Result.ok(new DateFragmentParams(format.result(), first, second));
  }

  private static int compareDateFragments(DateFragmentParams a, DateFragmentParams b) {
    return switch (a.format()) {
      case "YYYY", "MM" -> Double.compare(a.first() - b.first(), 0);
      default -> a.first() - b.first() != 0 ? Double.compare(a.first() - b.first(), 0) : Double.compare(a.second() - b.second(), 0);
    };
  }

  private static List<QlDiagnostic> validateDateFragmentCompatible(Env env, Node fieldNode, Node valueNode) {
    if (!(fieldNode instanceof Field field)) {
      return null;
    }
    FieldInfo info = env.field(field);
    if (info == null || !"DateFragmentType".equals(info.type()) || info.formatOfFragment() == null
        || !(valueNode instanceof Call call) || !"DateFragment".equals(call.callee())) {
      return null;
    }
    Result<String, List<QlDiagnostic>> format = resolveDateFragmentFormat(call);
    if (format.failed()) {
      return null; // an invalid format already reports its own error
    }
    if (!format.result().equals(info.formatOfFragment().toUpperCase(Locale.ROOT))) {
      return List.of(QlDiagnostic.of(2016, valueNode, info.formatOfFragment(), format.result()));
    }
    return null;
  }

  private static List<QlDiagnostic> validateEnumerationFieldCompatible(Env env, Node fieldNode, Node valueNode) {
    if (!(fieldNode instanceof Field field) || !(valueNode instanceof Literal literal) || !literal.isString()) {
      return null;
    }
    FieldInfo info = env.field(field);
    if (info == null || !"EnumerationType".equals(info.type()) || info.enumerationValues() == null) {
      return null;
    }
    String value = (String) literal.value();
    if (info.enumerationValues().contains(value)) {
      return null;
    }
    return List.of(QlDiagnostic.of(2035, valueNode, "[" + field.path() + "]", candidates(info.enumerationValues(), value, 3)));
  }

  // ---- ordering of range bounds ----

  private static List<QlDiagnostic> validateNumberOrder(Node first, Node second) {
    if (first instanceof Literal a && a.isNumber() && second instanceof Literal b && b.isNumber()
        && (Double) a.value() > (Double) b.value()) {
      return List.of(QlDiagnostic.of(2018, second));
    }
    return null;
  }

  private static double dateValue(Node day, Node month, Node year) {
    double d = jsNumber(day);
    double m = jsNumber(month);
    double y = jsNumber(year);
    if (Double.isNaN(d) || Double.isNaN(m) || Double.isNaN(y)) {
      return Double.NaN;
    }
    // JavaScript's Date rolls over out-of-range months and days; so does this.
    return LocalDate.of((int) y, 1, 1).plusMonths((long) m - 1).plusDays((long) d - 1).toEpochDay();
  }

  private static double timeValue(Node hour, Node minute, Node second) {
    return jsNumber(hour) * 3600 + jsNumber(minute) * 60 + jsNumber(second);
  }

  private static boolean isCall(Node node, String callee, int arguments) {
    return node instanceof Call call && call.callee().equals(callee) && call.arguments().size() == arguments;
  }

  private static List<QlDiagnostic> validateTemporalOrder(Node first, Node second) {
    if (!(first instanceof Call a) || !(second instanceof Call b)) {
      return null;
    }
    if (isCall(a, "Date", 3) && isCall(b, "Date", 3)) {
      double x = dateValue(a.arguments().get(0), a.arguments().get(1), a.arguments().get(2));
      double y = dateValue(b.arguments().get(0), b.arguments().get(1), b.arguments().get(2));
      return x > y ? List.of(QlDiagnostic.of(2018, second)) : null;
    }
    if (isCall(a, "Time", 3) && isCall(b, "Time", 3)) {
      double x = timeValue(a.arguments().get(0), a.arguments().get(1), a.arguments().get(2));
      double y = timeValue(b.arguments().get(0), b.arguments().get(1), b.arguments().get(2));
      return x > y ? List.of(QlDiagnostic.of(2018, second)) : null;
    }
    if (isCall(a, "DateTime", 2) && isCall(b, "DateTime", 2)
        && a.arguments().get(0) instanceof Call dateA && b.arguments().get(0) instanceof Call dateB
        && a.arguments().get(1) instanceof Call timeA && b.arguments().get(1) instanceof Call timeB
        && isCall(dateA, "Date", 3) && isCall(dateB, "Date", 3) && isCall(timeA, "Time", 3) && isCall(timeB, "Time", 3)) {
      double dx = dateValue(dateA.arguments().get(0), dateA.arguments().get(1), dateA.arguments().get(2));
      double dy = dateValue(dateB.arguments().get(0), dateB.arguments().get(1), dateB.arguments().get(2));
      double tx = timeValue(timeA.arguments().get(0), timeA.arguments().get(1), timeA.arguments().get(2));
      double ty = timeValue(timeB.arguments().get(0), timeB.arguments().get(1), timeB.arguments().get(2));
      return dx > dy || (dx == dy && tx > ty) ? List.of(QlDiagnostic.of(2018, second)) : null;
    }
    if (a.callee().equals("DateFragment") && b.callee().equals("DateFragment")) {
      return validateDateFragmentOrder(a, b);
    }
    return null;
  }

  private static List<QlDiagnostic> validateDateFragmentOrder(Call first, Call second) {
    Result<String, List<QlDiagnostic>> firstFormat = resolveDateFragmentFormat(first);
    Result<String, List<QlDiagnostic>> secondFormat = resolveDateFragmentFormat(second);
    if (firstFormat.failed()) {
      return firstFormat.error();
    }
    if (secondFormat.failed()) {
      return secondFormat.error();
    }
    if (!firstFormat.result().equals(secondFormat.result())) {
      return List.of(QlDiagnostic.of(2017, second, firstFormat.result(), secondFormat.result()));
    }
    Result<DateFragmentParams, List<QlDiagnostic>> a = resolveDateFragment(first);
    Result<DateFragmentParams, List<QlDiagnostic>> b = resolveDateFragment(second);
    if (a.failed()) {
      return a.error();
    }
    if (b.failed()) {
      return b.error();
    }
    return compareDateFragments(a.result(), b.result()) > 0 ? List.of(QlDiagnostic.of(2018, second)) : null;
  }

  // ---- registry ----

  private static final List<List<Arg>> COMPARISON_SIGNATURES = List.of(
      sig(field("NumberType"), new NumberType()),
      sig(field("TimeType"), object("Time")),
      sig(field("DateType"), object("Date")),
      sig(field("DateTimeType"), object("DateTime")),
      sig(field("DateFragmentType"), object("DateFragment")),
      sig(field("DateRangeType"), object("DateRange")));

  private static List<List<Arg>> equalitySignatures() {
    List<List<Arg>> signatures = new ArrayList<>();
    signatures.add(sig(field("BooleanType"), new BooleanType().union(new NullType())));
    signatures.add(sig(field("ConfirmType"), new BooleanType().union(new NullType())));
    signatures.add(sig(field("StringType"), new StringType().union(new NullType())));
    signatures.add(sig(field("EnumerationType"), new StringType().union(new NullType())));
    signatures.add(sig(field("NumberType"), new NumberType().union(new NullType())));
    for (String[] pair : new String[][] {{"TimeType", "Time"}, {"DateType", "Date"}, {"DateTimeType", "DateTime"},
        {"DateFragmentType", "DateFragment"}, {"DateRangeType", "DateRange"}}) {
      signatures.add(sig(field(pair[0]), object(pair[1]).union(new NullType())));
    }
    return signatures;
  }

  private static List<QlDiagnostic> equalityValidator(Env env, Call call) {
    Node fieldNode = call.arguments().get(0);
    Node valueNode = call.arguments().get(1);
    return combine(validateEnumerationFieldCompatible(env, fieldNode, valueNode),
        validateDateFragmentCompatible(env, fieldNode, valueNode));
  }

  private static NumberType fragmentFirstArgument() {
    return new NumberType(new NumberOptions(true, 1d, null, QlFunctions::validateOneParamSignatureDateFragments));
  }

  static {
    register("Time", Kind.CONSTRUCTOR, false, List.of(sig(
        new NumberType(NumberOptions.of(true, 0d, 23d)),
        new NumberType(NumberOptions.of(true, 0d, 59d)),
        new NumberType(NumberOptions.of(true, 0d, 59d)))), null);

    register("Date", Kind.CONSTRUCTOR, false, List.of(sig(
        new NumberType(NumberOptions.of(true, 1d, 31d)),
        new NumberType(NumberOptions.of(true, 1d, 12d)),
        new NumberType(NumberOptions.of(true, YEAR_MIN, YEAR_MAX)))), (env, call) -> {
      int day = (int) jsNumber(call.arguments().get(0));
      int month = (int) jsNumber(call.arguments().get(1));
      int year = (int) jsNumber(call.arguments().get(2));
      try {
        LocalDate.of(year, month, day);
        return null;
      }
      catch (DateTimeException e) {
        return List.of(QlDiagnostic.of(2011, call, String.valueOf(year), String.valueOf(month), String.valueOf(day)));
      }
    });

    register("DateTime", Kind.CONSTRUCTOR, false, List.of(sig(object("Date"), object("Time"))), null);

    register("DateFragment", Kind.CONSTRUCTOR, false, List.of(
        sig(fragmentFirstArgument()),
        sig(fragmentFirstArgument(),
            new NumberType(new NumberOptions(true, 1d, null, QlFunctions::validateTwoParamsSignatureDateFragments)))), null);

    register("DateRange", Kind.CONSTRUCTOR, false, List.of(
        sig(object("Date"), object("Date")),
        sig(object("DateFragment"), object("DateFragment"))),
        (env, call) -> validateTemporalOrder(call.arguments().get(0), call.arguments().get(1)));

    register("GreaterThanOrEqual", Kind.OPERATOR, true, COMPARISON_SIGNATURES, null);
    register("LessThanOrEqual", Kind.OPERATOR, true, COMPARISON_SIGNATURES, null);
    register("Equal", Kind.OPERATOR, true, equalitySignatures(), QlFunctions::equalityValidator);
    register("NotEqual", Kind.OPERATOR, true, equalitySignatures(), QlFunctions::equalityValidator);

    List<List<Arg>> singleMatch = List.of(sig(field(null), new StringType()));
    register("SingleMatch", Kind.OPERATOR, true, singleMatch, null);
    register("NotSingleMatch", Kind.OPERATOR, true, singleMatch, null);

    register("Has", Kind.REGULAR, true, List.of(sig(
        new StringType(),
        new StringType(),
        BOOLEAN_EXPRESSION.union(new NullType()).optional(),
        BOOLEAN_EXPRESSION.union(new NullType()).optional())), null);

    register("Match", Kind.REGULAR, true, List.of(sig(
        new ListType(field(null)),
        new StringType(),
        new ListType(new StringType()))), null);

    register("And", Kind.OPERATOR, true, List.of(sig(new ListType(BOOLEAN_EXPRESSION))), null);
    register("Or", Kind.OPERATOR, true, List.of(sig(new ListType(BOOLEAN_EXPRESSION))), null);
    register("Not", Kind.OPERATOR, true, List.of(sig(BOOLEAN_EXPRESSION)), null);

    register("InRange", Kind.REGULAR, true, List.of(
        sig(field("NumberType"), new NumberType(), new NumberType()),
        sig(field("TimeType"), object("Time"), object("Time")),
        sig(field("DateType"), object("Date"), object("Date")),
        sig(field("DateTimeType"), object("DateTime"), object("DateTime")),
        sig(field("DateFragmentType"), object("DateFragment"), object("DateFragment"))), (env, call) -> {
      Node fieldNode = call.arguments().get(0);
      Node from = call.arguments().get(1);
      Node to = call.arguments().get(2);
      return combine(validateDateFragmentCompatible(env, fieldNode, from),
          validateDateFragmentCompatible(env, fieldNode, to),
          validateNumberOrder(from, to),
          validateTemporalOrder(from, to));
    });
  }
}
