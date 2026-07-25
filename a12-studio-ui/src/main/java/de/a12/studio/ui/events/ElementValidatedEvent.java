package de.a12.studio.ui.events;

import de.a12.studio.modelsvalidation.ElementValidationError;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class ElementValidatedEvent {
  @NonNull
  private final String elementId;

  private final ElementValidationError error;

  public ElementValidatedEvent(@NonNull String elementId, ElementValidationError error) {
    this.elementId = elementId;
    this.error = error;
  }

  public @NonNull String getElementId() {
    return elementId;
  }

  public Optional<ElementValidationError> getError() {
    return Optional.ofNullable(error);
  }
}
