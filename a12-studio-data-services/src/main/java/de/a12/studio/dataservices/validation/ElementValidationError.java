package de.a12.studio.dataservices.validation;

/**
 */
public record ElementValidationError(String elementId, String message, String severity) {
}
