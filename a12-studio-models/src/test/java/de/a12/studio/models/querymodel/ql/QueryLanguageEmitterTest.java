package de.a12.studio.models.querymodel.ql;

import java.util.List;

import de.a12.studio.models.querymodel.operator.AndOperator;
import de.a12.studio.models.querymodel.operator.DateFragmentRangeOperator;
import de.a12.studio.models.querymodel.operator.DateRangeOperator;
import de.a12.studio.models.querymodel.operator.DoubleRangeOperator;
import de.a12.studio.models.querymodel.operator.ExactMatchOperator;
import de.a12.studio.models.querymodel.operator.HasOperator;
import de.a12.studio.models.querymodel.operator.NotOperator;
import de.a12.studio.models.querymodel.operator.OrOperator;
import de.a12.studio.models.querymodel.operator.SimpleSearchOperator;
import de.a12.studio.models.querymodel.operator.UndefinedMatchOperator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies {@link QueryLanguageEmitter} against the same sample expressions exercised by SME's own
 * moduleSupport/qmm/test/core/checker/*.test.ts and functions.ts documentation examples - see
 * docs/sme-reference-comparison.md "Query Model" section.
 */
class QueryLanguageEmitterTest {

  private final QueryLanguageEmitter emitter = new QueryLanguageEmitter();

  @Test
  void emitsExactMatchForStringEquality() {
    ExactMatchOperator op = assertInstanceOf(ExactMatchOperator.class,
        emitter.emit("[/Person/FirstName] == \"John Doe\""));

    assertEquals("/Person/FirstName", op.getField());
    assertEquals("John Doe", op.getValue().asString());
  }

  @Test
  void emitsExactMatchWithARawNumberForNumericEquality() {
    ExactMatchOperator op = assertInstanceOf(ExactMatchOperator.class, emitter.emit("[/Fields/Price] == 100"));

    assertEquals("/Fields/Price", op.getField());
    assertEquals(100, op.getValue().asInt());
  }

  @Test
  void emitsUndefinedMatchForNullEquality() {
    UndefinedMatchOperator op = assertInstanceOf(UndefinedMatchOperator.class, emitter.emit("[/Fields/sport] == Null"));

    assertEquals("/Fields/sport", op.getField());
  }

  @Test
  void emitsDoubleRangeForGreaterOrEqual() {
    DoubleRangeOperator op = assertInstanceOf(DoubleRangeOperator.class, emitter.emit("[/LinkFields/TimeShare] >= 20"));

    assertEquals("/LinkFields/TimeShare", op.getField());
    assertEquals(20.0, op.getFrom());
    assertNull(op.getTo());
  }

  @Test
  void emitsDoubleRangeForLessOrEqual() {
    DoubleRangeOperator op = assertInstanceOf(DoubleRangeOperator.class, emitter.emit("[/price] <= 100"));

    assertNull(op.getFrom());
    assertEquals(100.0, op.getTo());
  }

  @Test
  void emitsNotWrappingExactMatchForNotEqual() {
    NotOperator not = assertInstanceOf(NotOperator.class, emitter.emit("[/BusinessPartnerRoot/Country] != \"Italy\""));
    ExactMatchOperator inner = assertInstanceOf(ExactMatchOperator.class, not.getOperand());

    assertEquals("/BusinessPartnerRoot/Country", inner.getField());
    assertEquals("Italy", inner.getValue().asString());
  }

  @Test
  void emitsSimpleSearchForSingleMatch() {
    SimpleSearchOperator op = assertInstanceOf(SimpleSearchOperator.class, emitter.emit("[/title] ~ \"important\""));

    assertEquals(List.of("/title"), op.getFields());
    assertEquals("important", op.getValue());
  }

  @Test
  void emitsNotWrappingSimpleSearchForNotSingleMatch() {
    NotOperator not = assertInstanceOf(NotOperator.class, emitter.emit("[/title] !~ \"important\""));
    assertInstanceOf(SimpleSearchOperator.class, not.getOperand());
  }

  @Test
  void emitsAndOfTwoAtoms() {
    AndOperator op = assertInstanceOf(AndOperator.class,
        emitter.emit("[/Person/FirstName] == \"John Doe\" and [/LinkFields/TimeShare] >= 20"));

    assertEquals(2, op.getOperands().size());
    assertInstanceOf(ExactMatchOperator.class, op.getOperands().get(0));
    assertInstanceOf(DoubleRangeOperator.class, op.getOperands().get(1));
  }

  @Test
  void emitsOrOfTwoAtoms() {
    OrOperator op = assertInstanceOf(OrOperator.class,
        emitter.emit("[/Person/FirstName] == \"John Doe\" or [/Person/FirstName] == \"Jane Doe\""));

    assertEquals(2, op.getOperands().size());
  }

  @Test
  void emitsNotForParenthesizedNegation() {
    NotOperator op = assertInstanceOf(NotOperator.class, emitter.emit("!([/Person/FirstName] == \"John Doe\")"));
    assertInstanceOf(ExactMatchOperator.class, op.getOperand());
  }

  @Test
  void emitsNestedOrInsideAndUsingParentheses() {
    AndOperator and = assertInstanceOf(AndOperator.class,
        emitter.emit("([/A] == \"x\" or [/B] == \"y\") and [/C] == \"z\""));

    assertEquals(2, and.getOperands().size());
    assertInstanceOf(OrOperator.class, and.getOperands().get(0));
    assertInstanceOf(ExactMatchOperator.class, and.getOperands().get(1));
  }

  @Test
  void emitsHasWithTwoArguments() {
    HasOperator op = assertInstanceOf(HasOperator.class, emitter.emit("Has(\"TeamPerson\", \"Person\")"));

    assertEquals("TeamPerson", op.getRelationshipModel());
    assertEquals("Person", op.getTargetRole());
    assertNull(op.getConstraint());
    assertNull(op.getLinkDocumentConstraint());
  }

  @Test
  void emitsHasWithADocumentConstraint() {
    HasOperator op = assertInstanceOf(HasOperator.class,
        emitter.emit("Has(\"TeamPerson\", \"Person\", [/Person/FirstName] == \"John Doe\")"));

    ExactMatchOperator constraint = assertInstanceOf(ExactMatchOperator.class, op.getConstraint());
    assertEquals("/Person/FirstName", constraint.getField());
    assertNull(op.getLinkDocumentConstraint());
  }

  @Test
  void emitsHasWithNullDocumentConstraintAndALinkDocumentConstraint() {
    HasOperator op = assertInstanceOf(HasOperator.class,
        emitter.emit("Has(\"TeamPerson\", \"Person\", Null, [/LinkFields/TimeShare] >= 20)"));

    assertNull(op.getConstraint());
    assertInstanceOf(DoubleRangeOperator.class, op.getLinkDocumentConstraint());
  }

  @Test
  void emitsDateRangeFromADateConstructorComparison() {
    // Date's surface argument order is (day, month, year); wire format is year-month-day.
    DateRangeOperator op = assertInstanceOf(DateRangeOperator.class, emitter.emit("[/expiresAt] >= Date(15, 8, 2024)"));

    assertEquals("2024-08-15", op.getFrom());
  }

  @Test
  void emitsExactMatchWithADateStringForDateEquality() {
    ExactMatchOperator op = assertInstanceOf(ExactMatchOperator.class, emitter.emit("[/Contract/SignedAt] == Date(1, 1, 2020)"));

    assertEquals("2020-01-01", op.getValue().asString());
  }

  @Test
  void emitsDateRangeFromATimeConstructorComparison() {
    DateRangeOperator op = assertInstanceOf(DateRangeOperator.class, emitter.emit("[/appointmentTime] >= Time(9, 0, 0)"));

    assertEquals("09:00:00", op.getFrom());
  }

  @Test
  void emitsExactMatchWithACombinedDateTimeString() {
    ExactMatchOperator op = assertInstanceOf(ExactMatchOperator.class,
        emitter.emit("[/x] == DateTime(Date(15, 3, 2024), Time(14, 30, 0))"));

    assertEquals("2024-03-15T14:30:00", op.getValue().asString());
  }

  @Test
  void emitsDateFragmentRangeForASingleMonthArgument() {
    DateFragmentRangeOperator op = assertInstanceOf(DateFragmentRangeOperator.class, emitter.emit("[/eventMonth] >= DateFragment(3)"));

    assertEquals("03", op.getFrom());
  }

  @Test
  void emitsDateFragmentRangeForASingleYearArgument() {
    DateFragmentRangeOperator op = assertInstanceOf(DateFragmentRangeOperator.class, emitter.emit("[/eventYear] <= DateFragment(2024)"));

    assertEquals("2024", op.getTo());
  }

  @Test
  void emitsDateFragmentRangeForMonthDayArguments() {
    DateFragmentRangeOperator op = assertInstanceOf(DateFragmentRangeOperator.class, emitter.emit("[/x] >= DateFragment(3, 15)"));

    assertEquals("03-15", op.getFrom());
  }

  @Test
  void emitsDateFragmentRangeForYearMonthArguments() {
    DateFragmentRangeOperator op = assertInstanceOf(DateFragmentRangeOperator.class, emitter.emit("[/x] >= DateFragment(2024, 3)"));

    assertEquals("2024-03", op.getFrom());
  }

  @Test
  void emitsDoubleRangeForInRangeOnNumbers() {
    DoubleRangeOperator op = assertInstanceOf(DoubleRangeOperator.class, emitter.emit("InRange([/age], 18, 65)"));

    assertEquals(18.0, op.getFrom());
    assertEquals(65.0, op.getTo());
  }

  @Test
  void emitsDateRangeForInRangeOnDates() {
    DateRangeOperator op = assertInstanceOf(DateRangeOperator.class,
        emitter.emit("InRange([/birthDate], Date(1, 1, 1980), Date(31, 12, 1999))"));

    assertEquals("1980-01-01", op.getFrom());
    assertEquals("1999-12-31", op.getTo());
  }

  @Test
  void emitsDateFragmentRangeForInRangeOnDateFragments() {
    DateFragmentRangeOperator op = assertInstanceOf(DateFragmentRangeOperator.class,
        emitter.emit("InRange([/eventMonth], DateFragment(3), DateFragment(5))"));

    assertEquals("03", op.getFrom());
    assertEquals("05", op.getTo());
  }

  @Test
  void emitsSimpleSearchWithFieldsAndMultipleValuesForMatch() {
    SimpleSearchOperator op = assertInstanceOf(SimpleSearchOperator.class,
        emitter.emit("Match([/name], [/description], \"premium\", \"gold\")"));

    assertEquals(List.of("/name", "/description"), op.getFields());
    assertEquals(List.of("premium", "gold"), op.getValues());
  }

  @Test
  void emitsSimpleSearchWithASingleValueUsingValueNotValues() {
    SimpleSearchOperator op = assertInstanceOf(SimpleSearchOperator.class, emitter.emit("Match([/title], \"important\")"));

    assertEquals(List.of("/title"), op.getFields());
    assertEquals("important", op.getValue());
    assertNull(op.getValues());
  }

  @Test
  void emitsSimpleSearchWithNoFieldsForStringOnlyMatch() {
    SimpleSearchOperator op = assertInstanceOf(SimpleSearchOperator.class, emitter.emit("Match(\"urgent\", \"review\")"));

    assertNull(op.getFields());
    assertEquals(List.of("urgent", "review"), op.getValues());
  }

  @Test
  void throwsOnInvalidSyntax() {
    assertThrows(QueryLanguageException.class, () -> emitter.emit("[/Person/FirstName] ==="));
  }

  @Test
  void throwsOnUnknownFunctionName() {
    assertThrows(QueryLanguageException.class, () -> emitter.emit("Bogus(\"a\", \"b\")"));
  }
}
