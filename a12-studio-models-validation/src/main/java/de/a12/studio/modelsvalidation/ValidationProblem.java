package de.a12.studio.modelsvalidation;

/**
 *
 */
public record ValidationProblem(String elementId, String message, Severity severity) {
}
