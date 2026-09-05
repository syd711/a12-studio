package de.a12.studio.models.querymodel.ql;

import java.util.ArrayList;
import java.util.List;

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
import tools.jackson.databind.JsonNode;

/**
 * Renders an {@link Operator} tree back into Query Language source text - the reverse of
 * {@link QueryLanguageEmitter}, combining SME's separate importer (JSON -> parse tree) and formatter
 * (parse tree -> pretty text) stages into one direct step, since QL has no comments/whitespace metadata worth
 * preserving through an intermediate tree.
 *
 * <p>Two shapes {@link QueryLanguageEmitter} can never produce have no clean QL surface syntax and are formatted
 * with a documented, lossy fallback instead of failing: {@code exact_match} with a {@code values} list (expanded
 * to an {@code or} of individual {@code ==} comparisons) and a boolean-sourced {@code exact_match} value (rendered
 * as a quoted string, since a stringified "true"/"false" is indistinguishable from a genuine string field value
 * without resolving the field's type).
 */
public final class QueryLanguageFormatter {

  public String format(Operator operator) {
    return formatOperator(operator);
  }

  private String formatOperator(Operator operator) {
    if (operator instanceof AndOperator and) {
      return and.getOperands().stream().map(this::formatOperand).reduce((a, b) -> a + " and " + b).orElse("");
    }
    if (operator instanceof OrOperator or) {
      return or.getOperands().stream().map(this::formatOperand).reduce((a, b) -> a + " or " + b).orElse("");
    }
    if (operator instanceof NotOperator not) {
      return "!(" + formatOperator(not.getOperand()) + ")";
    }
    if (operator instanceof ExactMatchOperator exactMatch) {
      return formatExactMatch(exactMatch);
    }
    if (operator instanceof UndefinedMatchOperator undefinedMatch) {
      return "[" + undefinedMatch.getField() + "] == Null";
    }
    if (operator instanceof DoubleRangeOperator range) {
      return formatDoubleRange(range);
    }
    if (operator instanceof DateRangeOperator range) {
      return formatDateRange(range);
    }
    if (operator instanceof DateFragmentRangeOperator range) {
      return formatDateFragmentRange(range);
    }
    if (operator instanceof SimpleSearchOperator search) {
      return formatSimpleSearch(search);
    }
    if (operator instanceof HasOperator has) {
      return formatHas(has);
    }
    throw new QueryLanguageException("No query language syntax for operator: " + operator.getOperator());
  }

  /** And/Or nested inside And/Or need parens (the grammar's "atom" has no bare and/or form); Not already
   * self-wraps as "!(...)", and every other operator is already primaryExpression-shaped. */
  private String formatOperand(Operator operand) {
    if (operand instanceof AndOperator || operand instanceof OrOperator) {
      return "(" + formatOperator(operand) + ")";
    }
    return formatOperator(operand);
  }

  private String formatExactMatch(ExactMatchOperator exactMatch) {
    String field = "[" + exactMatch.getField() + "]";

    if (exactMatch.getValue() != null) {
      return field + " == " + formatScalar(exactMatch.getValue());
    }

    List<String> values = exactMatch.getValues();
    if (values != null && !values.isEmpty()) {
      String or = values.stream().map(value -> field + " == " + quote(value)).reduce((a, b) -> a + " or " + b).orElseThrow();
      return values.size() > 1 ? "(" + or + ")" : or;
    }

    throw new QueryLanguageException("exact_match requires value or values, field " + exactMatch.getField());
  }

  private static String formatScalar(JsonNode value) {
    if (value.isNumber()) {
      return value.toString();
    }
    // Ambiguous by design: a stringified boolean and a genuine string value are indistinguishable here without
    // the field's type, so both render as a quoted string literal.
    return quote(value.asString());
  }

  private String formatDoubleRange(DoubleRangeOperator range) {
    String field = "[" + range.getField() + "]";
    Double from = range.getFrom();
    Double to = range.getTo();

    if (from != null && to != null) {
      return "InRange(" + field + ", " + formatNumber(from) + ", " + formatNumber(to) + ")";
    }
    if (from != null) {
      return field + " >= " + formatNumber(from);
    }
    if (to != null) {
      return field + " <= " + formatNumber(to);
    }
    throw new QueryLanguageException("double_range requires from and/or to, field " + range.getField());
  }

  private static String formatNumber(double value) {
    if (value == Math.rint(value) && !Double.isInfinite(value)) {
      return Long.toString((long) value);
    }
    return Double.toString(value);
  }

  private String formatDateRange(DateRangeOperator range) {
    String field = "[" + range.getField() + "]";

    if (range.getValue() != null) {
      String[] interval = range.getValue().split("/", 2);
      String value = interval.length == 2
          ? "DateRange(" + formatTemporalToken(interval[0]) + ", " + formatTemporalToken(interval[1]) + ")"
          : formatTemporalToken(range.getValue());
      return field + " == " + value;
    }

    String from = range.getFrom();
    String to = range.getTo();
    if (from != null && to != null) {
      return "InRange(" + field + ", " + formatTemporalToken(from) + ", " + formatTemporalToken(to) + ")";
    }
    if (from != null) {
      return field + " >= " + formatTemporalToken(from);
    }
    if (to != null) {
      return field + " <= " + formatTemporalToken(to);
    }
    throw new QueryLanguageException("date_range requires from/to or value, field " + range.getField());
  }

  private static String formatTemporalToken(String token) {
    if (token.contains("T")) {
      String[] parts = token.split("T", 2);
      return "DateTime(" + formatDateToken(parts[0]) + ", " + formatTimeToken(parts[1]) + ")";
    }
    if (token.matches("\\d{2}:\\d{2}:\\d{2}")) {
      return formatTimeToken(token);
    }
    return formatDateToken(token);
  }

  private static String formatDateToken(String isoDate) {
    String[] parts = isoDate.split("-");
    int year = Integer.parseInt(parts[0]);
    int month = Integer.parseInt(parts[1]);
    int day = Integer.parseInt(parts[2]);
    return "Date(" + day + ", " + month + ", " + year + ")";
  }

  private static String formatTimeToken(String isoTime) {
    String[] parts = isoTime.split(":");
    return "Time(" + Integer.parseInt(parts[0]) + ", " + Integer.parseInt(parts[1]) + ", " + Integer.parseInt(parts[2]) + ")";
  }

  private String formatDateFragmentRange(DateFragmentRangeOperator range) {
    String field = "[" + range.getField() + "]";
    String from = range.getFrom();
    String to = range.getTo();

    if (from != null && to != null) {
      return "InRange(" + field + ", " + formatDateFragmentToken(from) + ", " + formatDateFragmentToken(to) + ")";
    }
    if (from != null) {
      return field + " >= " + formatDateFragmentToken(from);
    }
    if (to != null) {
      return field + " <= " + formatDateFragmentToken(to);
    }
    throw new QueryLanguageException("datefragment_range requires from and/or to, field " + range.getField());
  }

  // A DateFragment(a, b) call re-emits to "MM-DD" or "YYYY-MM" purely based on the magnitude of `a`, so parsing
  // either shape back through the same two-argument call syntax round-trips correctly without needing to know
  // which format it originally was.
  private static String formatDateFragmentToken(String fragment) {
    String[] parts = fragment.split("-");
    if (parts.length == 2) {
      return "DateFragment(" + Integer.parseInt(parts[0]) + ", " + Integer.parseInt(parts[1]) + ")";
    }
    return "DateFragment(" + Integer.parseInt(fragment) + ")";
  }

  private String formatSimpleSearch(SimpleSearchOperator search) {
    List<String> fields = search.getFields();
    String value = search.getValue();
    List<String> values = search.getValues();

    if (fields != null && fields.size() == 1 && value != null && values == null) {
      return "[" + fields.get(0) + "] ~ " + quote(value);
    }

    List<String> args = new ArrayList<>();
    if (fields != null) {
      fields.forEach(field -> args.add("[" + field + "]"));
    }
    if (value != null) {
      args.add(quote(value));
    }
    if (values != null) {
      values.forEach(v -> args.add(quote(v)));
    }
    return "Match(" + String.join(", ", args) + ")";
  }

  private String formatHas(HasOperator has) {
    StringBuilder text = new StringBuilder("Has(")
        .append(quote(has.getRelationshipModel())).append(", ").append(quote(has.getTargetRole()));

    if (has.getLinkDocumentConstraint() != null) {
      text.append(", ").append(has.getConstraint() != null ? formatOperator(has.getConstraint()) : "Null");
      text.append(", ").append(formatOperator(has.getLinkDocumentConstraint()));
    } else if (has.getConstraint() != null) {
      text.append(", ").append(formatOperator(has.getConstraint()));
    }
    return text.append(")").toString();
  }

  private static String quote(String value) {
    return "\"" + value.replace("\"", "\\\"") + "\"";
  }
}
