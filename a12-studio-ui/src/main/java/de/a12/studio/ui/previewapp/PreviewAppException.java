package de.a12.studio.ui.previewapp;

/**
 * Raised when the real "Preview App" (the generated application preview shipped with the A12
 * installation, as opposed to the studio's own lightweight {@code PreviewServer}) cannot be
 * resolved or launched.
 */
public class PreviewAppException extends Exception {

  public PreviewAppException(String message) {
    super(message);
  }

  public PreviewAppException(String message, Throwable cause) {
    super(message, cause);
  }
}
