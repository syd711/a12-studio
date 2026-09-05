package de.a12.studio.models.querymodel.ql;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import de.a12.studio.models.querymodel.operator.AndOperator;
import de.a12.studio.models.querymodel.operator.DateFragmentRangeOperator;
import de.a12.studio.models.querymodel.operator.DateRangeOperator;
import de.a12.studio.models.querymodel.operator.DoubleRangeOperator;
import de.a12.studio.models.querymodel.operator.ExactMatchOperator;
import de.a12.studio.models.querymodel.operator.HasOperator;
import de.a12.studio.models.querymodel.operator.NotOperator;
import de.a12.studio.models.querymodel.operator.Operator;
import de.a12.studio.models.querymodel.operator.OrOperator;
import de.a12.studio.models.querymodel.operator.SimpleSearchOperator;
import de.a12.studio.models.querymodel.operator.UndefinedMatchOperator;
import de.a12.studio.models.util.JsonSettings;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.StringNode;

/**
 * Compiles a Query Language expression (QL.g4, ported from SME's moduleSupport/qmm) into an {@link Operator} tree
 * - the Java equivalent of SME's parser+binder+checker+emitter pipeline collapsed into one pass, since (unlike
 * SME) this port does not yet resolve field paths against a real Document Model schema: the only place SME's
 * checker result would otherwise be needed is disambiguating {@code double_range}/{@code date_range}/
 * {@code datefragment_range} for {@code >=}/{@code <=}/{@code InRange}, and that's already fully determined by
 * the value's own syntax (a number literal vs. a Date/Time/DateTime call vs. a DateFragment call) - see
 * docs/sme-reference-comparison.md "Query Model" section. Field/function validity (e.g. "does this field exist",
 * "is Has's target role real") is therefore NOT checked here; only syntactic well-formedness is.
 */
public final class QueryLanguageEmitter {

  public Operator emit(String source) {
    QLLexer lexer = new QLLexer(CharStreams.fromString(source));
    QLParser parser = new QLParser(new CommonTokenStream(lexer));

    List<String> syntaxErrors = new ArrayList<>();
    parser.removeErrorListeners();
    parser.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        syntaxErrors.add("line " + line + ":" + charPositionInLine + " " + msg);
      }
    });

    QLParser.ProgramContext program = parser.program();
    if (!syntaxErrors.isEmpty()) {
      throw new QueryLanguageException("Invalid query language expression: " + String.join("; ", syntaxErrors));
    }

    return emitExpression(program.expression());
  }

  private Operator emitExpression(QLParser.ExpressionContext ctx) {
    if (ctx.andExpression() != null) {
      AndOperator and = new AndOperator();
      and.setOperands(ctx.andExpression().atom().stream().map(this::emitAtom).toList());
      return and;
    }
    if (ctx.orExpression() != null) {
      OrOperator or = new OrOperator();
      or.setOperands(ctx.orExpression().atom().stream().map(this::emitAtom).toList());
      return or;
    }
    return emitAtom(ctx.atom());
  }

  private Operator emitAtom(QLParser.AtomContext ctx) {
    if (ctx.L_NOT() != null) {
      return wrapNot(emitExpression(ctx.expression()));
    }
    if (ctx.expression() != null) {
      return emitExpression(ctx.expression());
    }
    return emitPrimary(ctx.primaryExpression());
  }

  private Operator emitPrimary(QLParser.PrimaryExpressionContext ctx) {
    if (ctx.binaryExpression() != null) {
      return emitBinary(ctx.binaryExpression());
    }
    return emitTopLevelCall(ctx.callExpression());
  }

  private Operator emitBinary(QLParser.BinaryExpressionContext ctx) {
    String field = fieldPath(ctx.fieldRef());
    QLParser.ValueExpressionContext value = ctx.valueExpression();

    return switch (ctx.binaryOperator().getText()) {
      case "==" -> emitEquals(field, value);
      case "!=" -> wrapNot(emitEquals(field, value));
      case ">=" -> emitComparison(field, value, true);
      case "<=" -> emitComparison(field, value, false);
      case "~" -> emitSingleMatch(field, value);
      case "!~" -> wrapNot(emitSingleMatch(field, value));
      default -> throw new QueryLanguageException("Unsupported binary operator: " + ctx.binaryOperator().getText());
    };
  }

  private Operator emitTopLevelCall(QLParser.CallExpressionContext ctx) {
    return switch (ctx.callee().getText()) {
      case "Has" -> emitHas(ctx);
      case "Match" -> emitMatch(ctx);
      case "InRange" -> emitInRange(ctx);
      default -> throw new QueryLanguageException(
          "Unknown function: " + ctx.callee().getText() + " (expected Has, Match or InRange)");
    };
  }

  private Operator emitEquals(String field, QLParser.ValueExpressionContext valueCtx) {
    if (valueCtx.literal() != null) {
      QLParser.LiteralContext literal = valueCtx.literal();

      if (literal.nullLiteral() != null) {
        UndefinedMatchOperator undefinedMatch = new UndefinedMatchOperator();
        undefinedMatch.setField(field);
        return undefinedMatch;
      }

      ExactMatchOperator exactMatch = new ExactMatchOperator();
      exactMatch.setField(field);

      if (literal.stringLiteral() != null) {
        exactMatch.setValue(StringNode.valueOf(unescapeString(literal.stringLiteral().getText())));
      } else if (literal.numberLiteral() != null) {
        exactMatch.setValue(parseNumberNode(literal.numberLiteral().getText()));
      } else if (literal.booleanLiteral() != null) {
        // SME's own emitter stringifies booleans for exact_match rather than emitting a JSON boolean.
        exactMatch.setValue(StringNode.valueOf(isTrueLiteral(literal.booleanLiteral().getText()) ? "true" : "false"));
      } else {
        throw new QueryLanguageException("Unsupported literal for ==: " + literal.getText());
      }
      return exactMatch;
    }

    ExactMatchOperator exactMatch = new ExactMatchOperator();
    exactMatch.setField(field);
    exactMatch.setValue(StringNode.valueOf(emitTemporalCallExpression(valueCtx.callExpression())));
    return exactMatch;
  }

  private Operator emitComparison(String field, QLParser.ValueExpressionContext valueCtx, boolean isGreaterOrEqual) {
    if (valueCtx.literal() != null) {
      if (valueCtx.literal().numberLiteral() == null) {
        throw new QueryLanguageException(
            ">= and <= require a number or a Date/Time/DateTime/DateFragment value, field " + field);
      }
      DoubleRangeOperator range = new DoubleRangeOperator();
      range.setField(field);
      double value = Double.parseDouble(valueCtx.literal().numberLiteral().getText());
      if (isGreaterOrEqual) {
        range.setFrom(value);
      } else {
        range.setTo(value);
      }
      return range;
    }

    QLParser.CallExpressionContext call = valueCtx.callExpression();
    if ("DateFragment".equals(call.callee().getText())) {
      DateFragmentRangeOperator range = new DateFragmentRangeOperator();
      range.setField(field);
      String fragment = emitDateFragment(call);
      if (isGreaterOrEqual) {
        range.setFrom(fragment);
      } else {
        range.setTo(fragment);
      }
      return range;
    }

    DateRangeOperator range = new DateRangeOperator();
    range.setField(field);
    String temporal = emitTemporalCallExpression(call);
    if (isGreaterOrEqual) {
      range.setFrom(temporal);
    } else {
      range.setTo(temporal);
    }
    return range;
  }

  private Operator emitSingleMatch(String field, QLParser.ValueExpressionContext valueCtx) {
    if (valueCtx.literal() == null || valueCtx.literal().stringLiteral() == null) {
      throw new QueryLanguageException("~ and !~ require a string literal, field " + field);
    }
    SimpleSearchOperator search = new SimpleSearchOperator();
    search.setFields(List.of(field));
    search.setValue(unescapeString(valueCtx.literal().stringLiteral().getText()));
    return search;
  }

  private Operator emitHas(QLParser.CallExpressionContext ctx) {
    List<QLParser.ArgumentContext> args = argumentsOf(ctx);
    if (args.size() < 2 || args.size() > 4) {
      throw new QueryLanguageException("Has expects 2 to 4 arguments, got " + args.size());
    }

    HasOperator has = new HasOperator();
    has.setRelationshipModel(stringArg(args, 0));
    has.setTargetRole(stringArg(args, 1));
    if (args.size() >= 3) {
      has.setConstraint(optionalConstraintArg(args.get(2)));
    }
    if (args.size() == 4) {
      has.setLinkDocumentConstraint(optionalConstraintArg(args.get(3)));
    }
    return has;
  }

  private Operator optionalConstraintArg(QLParser.ArgumentContext arg) {
    if (arg.literal() != null && arg.literal().nullLiteral() != null) {
      return null;
    }
    if (arg.expression() != null) {
      return emitExpression(arg.expression());
    }
    throw new QueryLanguageException("Expected Null or a constraint expression");
  }

  private Operator emitMatch(QLParser.CallExpressionContext ctx) {
    List<String> fields = new ArrayList<>();
    List<String> values = new ArrayList<>();

    for (QLParser.ArgumentContext arg : argumentsOf(ctx)) {
      if (arg.fieldRef() != null) {
        fields.add(fieldPath(arg.fieldRef()));
      } else if (arg.literal() != null && arg.literal().stringLiteral() != null) {
        values.add(unescapeString(arg.literal().stringLiteral().getText()));
      } else {
        throw new QueryLanguageException("Match arguments must be field references or string literals");
      }
    }
    if (values.isEmpty()) {
      throw new QueryLanguageException("Match requires at least one string value");
    }

    SimpleSearchOperator search = new SimpleSearchOperator();
    if (!fields.isEmpty()) {
      search.setFields(fields);
    }
    if (values.size() == 1) {
      search.setValue(values.get(0));
    } else {
      search.setValues(values);
    }
    return search;
  }

  private Operator emitInRange(QLParser.CallExpressionContext ctx) {
    List<QLParser.ArgumentContext> args = argumentsOf(ctx);
    if (args.size() != 3 || args.get(0).fieldRef() == null) {
      throw new QueryLanguageException("InRange expects (field, from, to)");
    }
    String field = fieldPath(args.get(0).fieldRef());
    QLParser.ArgumentContext fromArg = args.get(1);
    QLParser.ArgumentContext toArg = args.get(2);

    if (isNumberLiteral(fromArg) && isNumberLiteral(toArg)) {
      DoubleRangeOperator range = new DoubleRangeOperator();
      range.setField(field);
      range.setFrom(numberLiteralValue(fromArg));
      range.setTo(numberLiteralValue(toArg));
      return range;
    }

    QLParser.CallExpressionContext fromCall = requireCallArgument(fromArg);
    QLParser.CallExpressionContext toCall = requireCallArgument(toArg);

    if ("DateFragment".equals(fromCall.callee().getText())) {
      DateFragmentRangeOperator range = new DateFragmentRangeOperator();
      range.setField(field);
      range.setFrom(emitDateFragment(fromCall));
      range.setTo(emitDateFragment(toCall));
      return range;
    }

    DateRangeOperator range = new DateRangeOperator();
    range.setField(field);
    range.setFrom(emitTemporalCallExpression(fromCall));
    range.setTo(emitTemporalCallExpression(toCall));
    return range;
  }

  /** Emits the on-disk string form of a Date/Time/DateTime/DateFragment/DateRange constructor call. */
  private String emitTemporalCallExpression(QLParser.CallExpressionContext ctx) {
    List<QLParser.ArgumentContext> args = argumentsOf(ctx);

    return switch (ctx.callee().getText()) {
      case "Time" -> {
        requireArgumentCount(ctx, args, 3);
        yield "%02d:%02d:%02d".formatted(intArg(args, 0), intArg(args, 1), intArg(args, 2));
      }
      case "Date" -> {
        requireArgumentCount(ctx, args, 3);
        // surface order is (day, month, year); wire format is year-month-day.
        int day = intArg(args, 0);
        int month = intArg(args, 1);
        int year = intArg(args, 2);
        yield "%04d-%02d-%02d".formatted(year, month, day);
      }
      case "DateTime" -> {
        requireArgumentCount(ctx, args, 2);
        String date = emitTemporalCallExpression(requireCallArgument(args.get(0)));
        String time = emitTemporalCallExpression(requireCallArgument(args.get(1)));
        yield date + "T" + time;
      }
      case "DateFragment" -> emitDateFragment(ctx);
      case "DateRange" -> {
        requireArgumentCount(ctx, args, 2);
        String from = emitTemporalCallExpression(requireCallArgument(args.get(0)));
        String to = emitTemporalCallExpression(requireCallArgument(args.get(1)));
        yield from + "/" + to;
      }
      default -> throw new QueryLanguageException(
          "Unsupported temporal function: " + ctx.callee().getText()
              + " (expected Date, Time, DateTime, DateFragment or DateRange)");
    };
  }

  /** Single argument: month (<=12) or year (>=1000). Two arguments: (month, day) if month-first, else (year, month). */
  private String emitDateFragment(QLParser.CallExpressionContext ctx) {
    List<QLParser.ArgumentContext> args = argumentsOf(ctx);

    if (args.size() == 1) {
      int value = intArg(args, 0);
      return value <= 12 ? "%02d".formatted(value) : "%04d".formatted(value);
    }
    if (args.size() == 2) {
      int first = intArg(args, 0);
      int second = intArg(args, 1);
      return first <= 12 ? "%02d-%02d".formatted(first, second) : "%04d-%02d".formatted(first, second);
    }
    throw new QueryLanguageException("DateFragment expects 1 or 2 arguments, got " + args.size());
  }

  private static List<QLParser.ArgumentContext> argumentsOf(QLParser.CallExpressionContext ctx) {
    return ctx.arguments() == null ? List.of() : ctx.arguments().argument();
  }

  private static void requireArgumentCount(QLParser.CallExpressionContext ctx, List<QLParser.ArgumentContext> args,
      int expected) {
    if (args.size() != expected) {
      throw new QueryLanguageException(
          ctx.callee().getText() + " expects " + expected + " arguments, got " + args.size());
    }
  }

  private static boolean isNumberLiteral(QLParser.ArgumentContext arg) {
    return arg.literal() != null && arg.literal().numberLiteral() != null;
  }

  private static double numberLiteralValue(QLParser.ArgumentContext arg) {
    return Double.parseDouble(arg.literal().numberLiteral().getText());
  }

  private static int intArg(List<QLParser.ArgumentContext> args, int index) {
    QLParser.ArgumentContext arg = args.get(index);
    if (!isNumberLiteral(arg)) {
      throw new QueryLanguageException("Expected a number literal argument at position " + index);
    }
    return Integer.parseInt(arg.literal().numberLiteral().getText());
  }

  private static String stringArg(List<QLParser.ArgumentContext> args, int index) {
    QLParser.ArgumentContext arg = args.get(index);
    if (arg.literal() == null || arg.literal().stringLiteral() == null) {
      throw new QueryLanguageException("Expected a string literal argument at position " + index);
    }
    return unescapeString(arg.literal().stringLiteral().getText());
  }

  private static QLParser.CallExpressionContext requireCallArgument(QLParser.ArgumentContext arg) {
    if (arg.expression() != null) {
      QLParser.CallExpressionContext call = asCallExpression(arg.expression());
      if (call != null) {
        return call;
      }
    }
    throw new QueryLanguageException("Expected a Date/Time/DateTime/DateFragment/DateRange value");
  }

  private static QLParser.CallExpressionContext asCallExpression(QLParser.ExpressionContext ctx) {
    if (ctx.atom() == null || ctx.atom().primaryExpression() == null) {
      return null;
    }
    return ctx.atom().primaryExpression().callExpression();
  }

  private static NotOperator wrapNot(Operator operand) {
    NotOperator not = new NotOperator();
    not.setOperand(operand);
    return not;
  }

  private static String fieldPath(QLParser.FieldRefContext ctx) {
    String text = ctx.getText();
    return text.substring(1, text.length() - 1);
  }

  private static String unescapeString(String rawStringLiteral) {
    String inner = rawStringLiteral.substring(1, rawStringLiteral.length() - 1);
    return inner.replace("\\\"", "\"");
  }

  private static boolean isTrueLiteral(String rawBooleanLiteral) {
    return "True".equalsIgnoreCase(rawBooleanLiteral);
  }

  private static JsonNode parseNumberNode(String rawNumberLiteral) {
    try {
      return JsonSettings.objectMapper.readTree(rawNumberLiteral);
    } catch (RuntimeException e) {
      // The grammar accepts a couple of shapes plain JSON numbers don't (e.g. a trailing "5."), fall back to a
      // plain decimal for those instead of failing the whole compile.
      return DoubleNode.valueOf(Double.parseDouble(rawNumberLiteral));
    }
  }
}
