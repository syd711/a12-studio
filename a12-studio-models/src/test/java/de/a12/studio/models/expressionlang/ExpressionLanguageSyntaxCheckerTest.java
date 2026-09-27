package de.a12.studio.models.expressionlang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Checked directly against {@code documentation/2606-06-doc/expression-expression-docs.md}'s own ANTLR
 * grammar declaration, and every real Overview Model expression column found under {@code
 * testing/workspaces} (13 of them, e.g. {@code City_Ov.json}, {@code Invoice_OM.json}, {@code
 * ProductBook_OM.json}) - this grammar accepts all of them, while {@link
 * de.a12.studio.models.documentmodel.rulelang.RuleLanguageSyntaxChecker} (the boolean Rule/Computation
 * condition language) rejects every single one, confirming they're genuinely different languages.
 */
class ExpressionLanguageSyntaxCheckerTest {

  @Test
  void acceptsPlainText() {
    assertNull(ExpressionLanguageSyntaxChecker.validate("\"Just plain text\""));
  }

  @Test
  void acceptsAFieldReference() {
    assertNull(ExpressionLanguageSyntaxChecker.validate("[FirstName] \" \" [LastName]"));
  }

  @Test
  void acceptsAGroupOperationWithDelimiter() {
    assertNull(ExpressionLanguageSyntaxChecker.validate(
        "kontext(Product){kontext(BookProperties){kontext(Authors,delimiter=\", \" ){[AuthorName]}}}"));
  }

  @Test
  void acceptsACaseOperationAndMultilingualValue() {
    assertNull(ExpressionLanguageSyntaxChecker.validate(
        "kontext(City){case[City]!=\"\" {(en: \"The city of \", de: \"Die Stadt \") [City]}}"));
  }

  @Test
  void reportsAnUnclosedGroupOperation() {
    assertNotNull(ExpressionLanguageSyntaxChecker.validate("kontext(City){"));
  }

  @Test
  void reportsAnUnclosedFieldValue() {
    assertNotNull(ExpressionLanguageSyntaxChecker.validate("[Unclosed"));
  }

  @Test
  void reportsAnEmptyGroupOperationName() {
    assertNotNull(ExpressionLanguageSyntaxChecker.validate("kontext() { [X] }"));
  }
}
