package de.a12.studio.ui.events;

import de.a12.studio.dataservices.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

public class ModelFocusRequestedEvent {
  @NonNull
  private final ProjectItem item;

  public ModelFocusRequestedEvent(@NonNull ProjectItem item) {
    this.item = item;
  }

  public @NonNull ProjectItem getItem() {
    return item;
  }
}
