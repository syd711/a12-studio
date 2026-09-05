package de.a12.studio.models.querymodel.ql;

/** A query-language string failed to parse, or an {@link de.a12.studio.models.querymodel.operator.Operator} tree
 * uses a shape the formatter can't express as query-language text. */
public class QueryLanguageException extends RuntimeException {

  public QueryLanguageException(String message) {
    super(message);
  }
}
