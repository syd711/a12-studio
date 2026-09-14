package de.a12.studio.models.documentmodel.rulelang;

import java.util.Optional;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

/**
 * Syntax-only check of the a12 kernel's Rule/Computation condition language (RuleLang.g4), reproducing the
 * wording of the kernel's own parser-level messages - see
 * {@code documentation/2606-06-doc/kernel-kernel-documentation-ba-en.md} section "8.1 Grammar":
 * <ul>
 *   <li>MVK_INCOMPLETE_INPUT - "The condition is not complete (yet)."
 *   <li>MVK_EXPECTED_TOKEN_NOT_FOUND - "Corrupt input or condition not complete (yet): Expected 'X'."
 *   <li>MVK_UNEXPECTED_TOKEN - "Corrupt input or condition not complete (yet): Unexpected found 'X'."
 *   <li>MVK_LEXER_STANDARD_ERROR - "Character 'X' at position N is incorrect."
 * </ul>
 * The kernel's own distinction between the first two - whether running out of input still lets it name one
 * specific missing token, or not - depends on internal parser state this ANTLR-based check doesn't have; any
 * syntax error found once the token stream is exhausted (the offending token is EOF) is reported as
 * MVK_INCOMPLETE_INPUT regardless, which is still an accurate description either way.
 */
public final class RuleLanguageSyntaxChecker {

  private RuleLanguageSyntaxChecker() {
  }

  /** Returns {@code null} if {@code source} is syntactically valid Rule/Computation condition text, or the
   * kernel-style error message (see class doc) for the first problem found otherwise. */
  public static String validate(String source) {
    try {
      check(source);
      return null;
    }
    catch (RuleLanguageException e) {
      return e.getMessage();
    }
  }

  /** @throws RuleLanguageException if {@code source} is not syntactically valid Rule/Computation condition text. */
  public static void check(String source) {
    RuleLangLexer lexer = new RuleLangLexer(CharStreams.fromString(source));
    lexer.removeErrorListeners();
    lexer.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        throw new RuleLanguageException(lexerErrorMessage(msg, charPositionInLine));
      }
    });

    RuleLangParser parser = new RuleLangParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        throw new RuleLanguageException(parserErrorMessage(offendingSymbol, msg));
      }
    });

    // Throws (via the listeners above, on the first problem found) rather than collecting every error like
    // QueryLanguageEmitter does - this check only ever needs to show one message at a time (RichtextEditorController's
    // error container), and stopping at the first error also avoids cascading nonsense from ANTLR's own
    // error-recovery continuing to parse after a syntax error.
    parser.program();
  }

  private static String lexerErrorMessage(String antlrMessage, int charPositionInLine) {
    String character = extractQuoted(antlrMessage).orElse(antlrMessage);
    return "Character '" + character + "' at position " + (charPositionInLine + 1) + " is incorrect. [MVK_LEXER_STANDARD_ERROR]";
  }

  private static String parserErrorMessage(Object offendingSymbol, String antlrMessage) {
    if (offendingSymbol instanceof Token token && token.getType() == Token.EOF) {
      return "The condition is not complete (yet). [MVK_INCOMPLETE_INPUT]";
    }
    if (antlrMessage.startsWith("missing ")) {
      String expected = extractQuoted(antlrMessage).orElse("?");
      return "Corrupt input or condition not complete (yet): Expected '" + expected + "'. [MVK_EXPECTED_TOKEN_NOT_FOUND]";
    }
    String found = offendingSymbol instanceof Token token ? token.getText() : extractQuoted(antlrMessage).orElse("?");
    return "Corrupt input or condition not complete (yet): Unexpected found '" + found + "'. [MVK_UNEXPECTED_TOKEN]";
  }

  /** The first single-quoted substring in an ANTLR error message, e.g. {@code "'('"} out of {@code "missing
   * '(' at '<EOF>'"} - ANTLR always quotes the token text it names this way (see {@code
   * DefaultErrorStrategy#getTokenErrorDisplay}/{@code Lexer#notifyListeners}). */
  private static Optional<String> extractQuoted(String message) {
    int start = message.indexOf('\'');
    int end = start < 0 ? -1 : message.indexOf('\'', start + 1);
    if (start < 0 || end < 0) {
      return Optional.empty();
    }
    return Optional.of(message.substring(start + 1, end));
  }
}
