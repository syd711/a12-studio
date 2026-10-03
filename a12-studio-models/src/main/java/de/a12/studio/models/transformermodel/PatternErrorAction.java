package de.a12.studio.models.transformermodel;

import org.jspecify.annotations.Nullable;

/**
 * What the transformer's pattern-errors post-processing ({@code XmPatternErrorsLayer}) does with a String field whose
 * pattern equals {@link PatternError#getPattern()}. An absent action means {@link #UPDATE_MESSAGE}.
 */
public enum PatternErrorAction {

  /** Keep the XSD's pattern, replace the error message with {@link PatternError#getErrors()}. */
  UPDATE_MESSAGE,
  /** Replace the pattern with {@link PatternError#getReplacement()} and set the error message from the errors. */
  REPLACE,
  /** Remove the pattern and its error message from the generated Document Model. */
  SUPPRESS;

  /** The action for a stored {@code action} value, or null for none/unknown. */
  @Nullable
  public static PatternErrorAction fromValue(@Nullable String value) {
    for (PatternErrorAction action : values()) {
      if (action.name().equals(value)) {
        return action;
      }
    }
    return null;
  }
}
