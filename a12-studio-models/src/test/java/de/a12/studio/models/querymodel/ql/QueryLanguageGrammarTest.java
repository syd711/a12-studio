package de.a12.studio.models.querymodel.ql;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the QL.g4 grammar (ported from SME's moduleSupport/qmm/QL.g4, see
 * docs/sme-reference-comparison.md "Query Model" section) parses the same sample expressions SME's own
 * checker tests exercise (moduleSupport/qmm/test/core/checker/*.test.ts).
 */
class QueryLanguageGrammarTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "[/Person/FirstName] == \"John Doe\"",
      "[/LinkFields/TimeShare] >= 20",
      "Has(\"TeamPerson\", \"Person\")",
      "Has(\"TeamPerson\", \"Person\", [/Person/FirstName] == \"John Doe\")",
      "Has(\"TeamPerson\", \"Person\", Null, [/LinkFields/TimeShare] >= 20)",
      "[/Person/FirstName] == \"John Doe\" and [/LinkFields/TimeShare] >= 20",
      "[/Person/FirstName] == \"John Doe\" or [/Person/FirstName] == \"Jane Doe\"",
      "!([/Person/FirstName] == \"John Doe\")",
      "DateRange(Date(2024, 1, 1), Date(2024, 12, 31))",
  })
  void parsesValidExpressionWithoutSyntaxErrors(String expression) {
    QLParser parser = newParser(expression);
    parser.program();

    assertEquals(0, parser.getNumberOfSyntaxErrors(), "expected no syntax errors for: " + expression);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "[/Person/FirstName] ===",
      "Has(",
      "and [/Person/FirstName]",
  })
  void rejectsInvalidExpression(String expression) {
    assertTrue(countSyntaxErrors(expression) > 0);
  }

  private static int countSyntaxErrors(String expression) {
    QLParser parser = newParser(expression);
    parser.removeErrorListeners();
    parser.program();
    return parser.getNumberOfSyntaxErrors();
  }

  private static QLParser newParser(String expression) {
    QLLexer lexer = new QLLexer(CharStreams.fromString(expression));
    return new QLParser(new CommonTokenStream(lexer));
  }
}
