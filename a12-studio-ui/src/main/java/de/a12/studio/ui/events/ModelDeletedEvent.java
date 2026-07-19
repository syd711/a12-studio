package de.a12.studio.ui.events;

import de.a12.studio.dataservices.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

public class ModelDeletedEvent {
  @NonNull
  private final ProjectItem item;

  public ModelDeletedEvent(@NonNull ProjectItem item) {
    this.item = item;
  }

  public @NonNull ProjectItem getItem() {
    return item;
  }
}
