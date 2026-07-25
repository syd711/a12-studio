package de.a12.studio.dataservices.validation;

/**
 * elementId is null for problems reported against the document model itself (e.g. schema version) rather
 * than a specific element; {@link DMValidationService#validate} drops those, mirroring the kernel's
 * getElementProblems, which only surfaced element-sourced problems to the UI.
 */
record ValidationProblem(String elementId, String message, Severity severity) {
}
