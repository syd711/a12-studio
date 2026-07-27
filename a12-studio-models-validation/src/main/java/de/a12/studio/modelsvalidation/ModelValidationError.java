package de.a12.studio.modelsvalidation;

import de.a12.studio.models.A12Model;

/**
 * {@code property} identifies which specific concern of {@code elementId} this error is about (see {@link
 * ElementProperty}), e.g. its name vs. its data type vs. its pattern error text. Several sibling property
 * editor panels can be bound to the very same element at once (e.g. a document model field's General
 * Information, Type Definition and Data Type Configuration panels), each with its own error container; this
 * lets each one show only the errors it's actually responsible for instead of all of them showing whatever
 * error happens to exist for the element. {@code null} means the error isn't tied to one specific panel's
 * concern (e.g. cross-cutting header-level errors like a missing locale, matched via a fixed sentinel
 * element id rather than a real element).
 */
public record ModelValidationError(A12Model<?> model, String elementId, String property, String message, String severity) {

  public ModelValidationError(A12Model<?> model, String elementId, String message, String severity) {
    this(model, elementId, null, message, severity);
  }
}
