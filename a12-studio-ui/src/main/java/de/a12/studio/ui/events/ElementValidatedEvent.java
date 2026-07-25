package de.a12.studio.ui.events;

import de.a12.studio.modelsvalidation.ModelValidationError;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class ElementValidatedEvent {
  @NonNull
  private final String elementId;

  private final ModelValidationError error;

  public ElementValidatedEvent(@NonNull String elementId, ModelValidationError error) {
    this.elementId = elementId;
    this.error = error;
  }

  public @NonNull String getElementId() {
    return elementId;
  }

  public Optional<ModelValidationError> getError() {
    return Optional.ofNullable(error);
  }
}
