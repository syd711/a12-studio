package de.a12.studio.kernel;

import org.jspecify.annotations.Nullable;

/**
 * One notification from the A12 kernel, translated into a12-studio terms so that no kernel type leaves this module.
 *
 * @param severity    how serious the kernel considers the finding
 * @param message     the kernel's message text, e.g. {@code L1:4-12 ... [MVK_UNEXPECTED_TOKEN]}
 * @param elementPath path of the model element the finding is about, or {@code null} if the kernel gave none
 */
public record KernelFinding(Severity severity, String message, @Nullable String elementPath) {

  public enum Severity {
    ERROR, WARNING, INFO
  }

  public boolean isError() {
    return severity == Severity.ERROR;
  }
}
