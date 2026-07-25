package de.a12.studio.modelsvalidation;

import de.a12.studio.models.A12Model;

/**
 */
public record ModelValidationError(A12Model<?> model, String elementId, String message, String severity) {
}
