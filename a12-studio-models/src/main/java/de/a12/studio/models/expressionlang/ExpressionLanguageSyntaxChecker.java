package de.a12.studio.models.expressionlang;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Syntax-only check of the a12 platform's "Expression" language (see {@code ExpressionLang.g4}'s own javadoc for
 * why this is a separate grammar from {@link de.a12.studio.models.documentmodel.rulelang.RuleLanguageSyntaxChecker}'s
 * Rule/Computation condition language) - the text-templating language behind an Overview Model's expression
 * columns ({@code Column.expression}) and any other field that builds a display text from Document Model values
 * (e.g. a Print Model text element), per {@code documentation/2606-06-doc/expression-expression-docs.md}.
 */
public final class ExpressionLanguageSyntaxChecker {

  private ExpressionLanguageSyntaxChecker() {
  }

  /** Returns {@code null} if {@code source} is syntactically valid Expression language text, or a description
   * of the first problem found otherwise. */
  public static String validate(String source) {
    try {
      check(source);
      return null;
    }
    catch (ExpressionLanguageException e) {
      return e.getMessage();
    }
  }

  /** @throws ExpressionLanguageException if {@code source} is not syntactically valid Expression language text. */
  public static void check(String source) {
    ExpressionLangLexer lexer = new ExpressionLangLexer(CharStreams.fromString(source));
    lexer.removeErrorListeners();
    lexer.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        throw new ExpressionLanguageException("Character at position " + (charPositionInLine + 1) + " is incorrect: " + msg);
      }
    });

    ExpressionLangParser parser = new ExpressionLangParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        throw new ExpressionLanguageException("The expression is not complete or contains an error near position "
            + (charPositionInLine + 1) + ": " + msg);
      }
    });

    // Throws on the first problem found, same reasoning as RuleLanguageSyntaxChecker: this check only ever
    // needs to show one message at a time in the owning RuleEditorController's error container.
    parser.program();
  }
}
