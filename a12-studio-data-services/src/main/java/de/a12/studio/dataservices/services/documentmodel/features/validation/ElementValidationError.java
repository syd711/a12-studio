package de.a12.studio.dataservices.services.documentmodel.features.validation;

/**
 * UI-safe view of a {@link DocumentModelErrors} entry: plain strings only, so callers outside this module
 * (e.g. a12-studio-ui) don't need the kernel jars that {@link DocumentModelErrors#getSeverity()} exposes.
 */
public record ElementValidationError(String elementId, String message, String severity) {
}
