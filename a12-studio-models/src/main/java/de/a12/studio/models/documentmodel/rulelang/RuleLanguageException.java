package de.a12.studio.models.documentmodel.rulelang;

/** A Rule/Computation condition language string failed to parse - see {@link RuleLanguageSyntaxChecker}. */
public class RuleLanguageException extends RuntimeException {

  public RuleLanguageException(String message) {
    super(message);
  }
}
