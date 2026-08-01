package de.a12.studio.ui.events;

import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

public class ModelRenamedEvent {
  @NonNull
  private final String oldPath;
  @NonNull
  private final ProjectItem item;

  public ModelRenamedEvent(@NonNull String oldPath, @NonNull ProjectItem item) {
    this.oldPath = oldPath;
    this.item = item;
  }

  public @NonNull String getOldPath() {
    return oldPath;
  }

  public @NonNull ProjectItem getItem() {
    return item;
  }
}
