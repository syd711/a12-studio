package de.a12.studio.models.expressionlang;

/** An Expression language string failed to parse - see {@link ExpressionLanguageSyntaxChecker}. */
public class ExpressionLanguageException extends RuntimeException {

  public ExpressionLanguageException(String message) {
    super(message);
  }
}
