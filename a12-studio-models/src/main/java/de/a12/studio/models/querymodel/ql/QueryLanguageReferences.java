package de.a12.studio.models.querymodel.ql;

import java.util.ArrayList;
import java.util.List;

/**
 * What a Query Language expression refers to, in source order, without compiling it: the bracketed field paths
 * ({@code [/Root/Name]}) evaluated against the expression's own Document Model, and every {@code Has(relationship,
 * role, constraint, linkConstraint)} call together with the nested scopes its constraints are evaluated in (the
 * role's target Document Model, and the relationship's link Document Model) - the same scoping SME's binder
 * applies (see docs/sme-reference-comparison.md "Query Model" section). Resolving these against real models is
 * left to the caller (see {@code QueryFilterReferenceChecker} in {@code a12-studio-models-validation}), since
 * this module knows nothing about a project.
 */
public final class QueryLanguageReferences {

  private QueryLanguageReferences() {
  }

  public sealed interface Reference permits FieldReference, HasCall {
  }

  /**
   * A bracketed field path such as {@code /Root/Name}, without the brackets. {@code start}/{@code stop} delimit
   * the whole {@code [/Root/Name]} token in the source: code-point indexes (as ANTLR counts them, not UTF-16
   * units), {@code stop} inclusive.
   */
  public record FieldReference(String path, int start, int stop) implements Reference {
  }

  /**
   * A {@code Has(...)} call; {@code constraint}/{@code linkConstraint} are the nested scopes, or {@code null}
   * when the argument is absent or {@code Null}. {@code relationshipStart}/{@code relationshipStop} delimit the
   * relationship's string literal including its quotes (same indexing as {@link FieldReference}).
   */
  public record HasCall(String relationshipModel, String targetRole, List<Reference> constraint,
      List<Reference> linkConstraint, int relationshipStart, int relationshipStop) implements Reference {
  }

  /** {@code text} replaces the source range {@code start..stop} (same indexing as {@link FieldReference}). */
  public record Replacement(int start, int stop, String text) {
  }

  /**
   * {@code source} with every {@code replacements} range replaced. Ranges must not overlap; they come from {@link
   * #extract}, which counts code points where {@link String} counts UTF-16 units, hence the conversion.
   */
  public static String replace(String source, List<Replacement> replacements) {
    StringBuilder result = new StringBuilder(source);
    replacements.stream().sorted(java.util.Comparator.comparingInt(Replacement::start).reversed()).forEach(replacement ->
        result.replace(source.offsetByCodePoints(0, replacement.start()),
            source.offsetByCodePoints(0, replacement.stop() + 1), replacement.text()));
    return result.toString();
  }

  /**
   * @return the references of {@code source}'s own scope, in source order
   * @throws QueryLanguageException if {@code source} is not syntactically valid Query Language
   */
  public static List<Reference> extract(String source) {
    List<Reference> references = new ArrayList<>();
    collect(QueryLanguageSyntax.parse(source).expression(), references);
    return references;
  }

  private static void collect(QLParser.ExpressionContext ctx, List<Reference> out) {
    if (ctx.andExpression() != null) {
      ctx.andExpression().atom().forEach(atom -> collect(atom, out));
    } else if (ctx.orExpression() != null) {
      ctx.orExpression().atom().forEach(atom -> collect(atom, out));
    } else {
      collect(ctx.atom(), out);
    }
  }

  private static void collect(QLParser.AtomContext ctx, List<Reference> out) {
    if (ctx.expression() != null) {
      collect(ctx.expression(), out);
      return;
    }
    QLParser.PrimaryExpressionContext primary = ctx.primaryExpression();
    if (primary.binaryExpression() != null) {
      out.add(fieldReference(primary.binaryExpression().fieldRef()));
    } else {
      collect(primary.callExpression(), out);
    }
  }

  private static void collect(QLParser.CallExpressionContext ctx, List<Reference> out) {
    List<QLParser.ArgumentContext> args = ctx.arguments() == null ? List.of() : ctx.arguments().argument();
    if ("Has".equals(ctx.callee().getText())) {
      collectHas(args, out);
      return;
    }
    // Match(field..., "text"...) and InRange(field, from, to): their field arguments live in this scope.
    for (QLParser.ArgumentContext arg : args) {
      if (arg.fieldRef() != null) {
        out.add(fieldReference(arg.fieldRef()));
      }
    }
  }

  private static void collectHas(List<QLParser.ArgumentContext> args, List<Reference> out) {
    if (args.size() < 2 || !isString(args.get(0)) || !isString(args.get(1))) {
      return; // not a well-formed Has; the emitter already reports that as a syntax problem
    }
    out.add(new HasCall(stringValue(args.get(0)), stringValue(args.get(1)),
        args.size() >= 3 ? scopeOf(args.get(2)) : null, args.size() >= 4 ? scopeOf(args.get(3)) : null,
        args.get(0).getStart().getStartIndex(), args.get(0).getStop().getStopIndex()));
  }

  private static List<Reference> scopeOf(QLParser.ArgumentContext arg) {
    if (arg.expression() == null) {
      return null; // Null literal: no constraint
    }
    List<Reference> scope = new ArrayList<>();
    collect(arg.expression(), scope);
    return scope;
  }

  private static boolean isString(QLParser.ArgumentContext arg) {
    return arg.literal() != null && arg.literal().stringLiteral() != null;
  }

  private static String stringValue(QLParser.ArgumentContext arg) {
    String raw = arg.literal().stringLiteral().getText();
    return raw.substring(1, raw.length() - 1).replace("\\\"", "\"");
  }

  private static FieldReference fieldReference(QLParser.FieldRefContext ctx) {
    String text = ctx.getText();
    return new FieldReference(text.substring(1, text.length() - 1), ctx.getStart().getStartIndex(),
        ctx.getStop().getStopIndex());
  }
}
