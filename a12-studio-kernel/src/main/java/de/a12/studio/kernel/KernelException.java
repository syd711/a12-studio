package de.a12.studio.kernel;

/** A kernel failure (unreadable model, unresolvable include, ...) translated so no kernel exception type leaks out. */
public class KernelException extends RuntimeException {

  public KernelException(String message, Throwable cause) {
    super(message, cause);
  }
}
