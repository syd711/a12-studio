package de.a12.studio.kernel;

/**
 * A problem the kernel found in one condition text, with its position.
 *
 * @param line        1-based line within the condition text
 * @param startColumn column where the offending text starts, as the kernel reports it
 * @param endColumn   column where it ends
 * @param message     the kernel's message, e.g. an unexpected-token error ending in {@code [MVK_UNEXPECTED_TOKEN]}
 */
public record KernelProblem(int line, int startColumn, int endColumn, String message) {
}
