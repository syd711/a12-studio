package de.a12.studio.models.querymodel.ql;

import java.util.ArrayList;
import java.util.List;

/**
 * A Query Language expression as the tree SME's {@code parser.ts} builds from it ({@code ParsedTree}): every
 * operator is a {@link Call} of the function SME names it after ({@code ==} is {@code Equal}, {@code and} is
 * {@code And}, {@code !(...)} is {@code Not}, ...), so a type checker only has to know functions. Ranges are
 * code-point indexes into the source, {@code stop} inclusive (same as {@link QueryLanguageReferences}).
 *
 * <p>Literals keep SME's value model: a number is a {@link Double}, a string is the text between the quotes
 * without unescaping, a boolean is a {@link Boolean}, {@code Null} is {@code null}.
 */
public final class QueryLanguageTree {

  private QueryLanguageTree() {
  }

  public sealed interface Node permits Call, Literal, Field {

    int start();

    int stop();
  }

  public record Call(String callee, List<Node> arguments, int start, int stop) implements Node {
  }

  public record Literal(Object value, int start, int stop) implements Node {

    public boolean isNumber() {
      return value instanceof Double;
    }

    public boolean isString() {
      return value instanceof String;
    }

    public boolean isBoolean() {
      return value instanceof Boolean;
    }

    public boolean isNull() {
      return value == null;
    }
  }

  /** A bracketed field path without the brackets, e.g. {@code /Root/Name}. */
  public record Field(String path, int start, int stop) implements Node {
  }

  /**
   * @throws QueryLanguageException if {@code source} is not syntactically valid Query Language
   */
  public static Node parse(String source) {
    return expression(QueryLanguageSyntax.parse(source).expression());
  }

  private static Node expression(QLParser.ExpressionContext ctx) {
    if (ctx.andExpression() != null) {
      return logical("And", ctx.andExpression().atom(), ctx);
    }
    if (ctx.orExpression() != null) {
      return logical("Or", ctx.orExpression().atom(), ctx);
    }
    return atom(ctx.atom());
  }

  private static Node logical(String callee, List<QLParser.AtomContext> atoms, org.antlr.v4.runtime.ParserRuleContext ctx) {
    List<Node> arguments = new ArrayList<>();
    atoms.forEach(atom -> arguments.add(atom(atom)));
    return new Call(callee, arguments, ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
  }

  private static Node atom(QLParser.AtomContext ctx) {
    if (ctx.expression() != null) {
      Node inner = expression(ctx.expression());
      if (ctx.L_NOT() == null) {
        return inner;
      }
      return new Call("Not", List.of(inner), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
    }
    QLParser.PrimaryExpressionContext primary = ctx.primaryExpression();
    if (primary.binaryExpression() != null) {
      QLParser.BinaryExpressionContext binary = primary.binaryExpression();
      Node right = binary.valueExpression().literal() != null
          ? literal(binary.valueExpression().literal())
          : call(binary.valueExpression().callExpression());
      return new Call(operatorCallee(binary.binaryOperator().getText()), List.of(field(binary.fieldRef()), right),
          binary.getStart().getStartIndex(), binary.getStop().getStopIndex());
    }
    return call(primary.callExpression());
  }

  private static String operatorCallee(String raw) {
    return switch (raw) {
      case "==" -> "Equal";
      case "!=" -> "NotEqual";
      case "~" -> "SingleMatch";
      case "!~" -> "NotSingleMatch";
      case ">=" -> "GreaterThanOrEqual";
      case "<=" -> "LessThanOrEqual";
      default -> throw new QueryLanguageException("Unknown operator: " + raw);
    };
  }

  private static Call call(QLParser.CallExpressionContext ctx) {
    List<Node> arguments = new ArrayList<>();
    if (ctx.arguments() != null) {
      for (QLParser.ArgumentContext argument : ctx.arguments().argument()) {
        if (argument.expression() != null) {
          arguments.add(expression(argument.expression()));
        }
        else if (argument.literal() != null) {
          arguments.add(literal(argument.literal()));
        }
        else {
          arguments.add(field(argument.fieldRef()));
        }
      }
    }
    return new Call(ctx.callee().getText(), arguments, ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
  }

  private static Field field(QLParser.FieldRefContext ctx) {
    String text = ctx.getText();
    return new Field(text.substring(1, text.length() - 1), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
  }

  private static Literal literal(QLParser.LiteralContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int stop = ctx.getStop().getStopIndex();
    if (ctx.stringLiteral() != null) {
      String raw = ctx.stringLiteral().getText();
      return new Literal(raw.substring(1, raw.length() - 1), start, stop);
    }
    if (ctx.numberLiteral() != null) {
      return new Literal(Double.parseDouble(ctx.numberLiteral().getText()), start, stop);
    }
    if (ctx.booleanLiteral() != null) {
      return new Literal("True".equals(ctx.booleanLiteral().getText()), start, stop);
    }
    return new Literal(null, start, stop);
  }
}
