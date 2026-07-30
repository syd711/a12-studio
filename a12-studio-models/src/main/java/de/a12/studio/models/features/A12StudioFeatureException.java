package de.a12.studio.models.features;

/** Thrown by an {@link A12StudioProjectFeature} for a user-facing failure (invalid input, I/O failure, ...). */
public class A12StudioFeatureException extends Exception {

  public A12StudioFeatureException(String message) {
    super(message);
  }

  public A12StudioFeatureException(String message, Throwable cause) {
    super(message, cause);
  }
}
