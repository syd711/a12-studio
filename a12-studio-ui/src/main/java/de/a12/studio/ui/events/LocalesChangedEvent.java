package de.a12.studio.ui.events;

import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

public class LocalesChangedEvent {
  @NonNull
  private final ProjectItem item;

  public LocalesChangedEvent(@NonNull ProjectItem item) {
    this.item = item;
  }

  public @NonNull ProjectItem getItem() {
    return item;
  }
}
