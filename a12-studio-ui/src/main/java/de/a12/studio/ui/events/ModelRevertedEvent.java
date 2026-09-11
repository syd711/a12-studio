package de.a12.studio.ui.events;

import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

/**
 * Fired after a version-control revert restores {@code item}'s file to its unmodified (HEAD)
 * content - {@code item} has already been {@link ProjectItem#reload() reloaded} by the time this
 * fires, so listeners see the reverted content. Unlike {@link ModelRenamedEvent}, the file's path
 * never changes, only its content does, so listeners that key off a path change (e.g. {@link
 * de.a12.studio.ui.util.RecentEditsTracker}) must not be driven off this event the way they are
 * off a rename.
 */
public class ModelRevertedEvent {
  @NonNull
  private final ProjectItem item;

  public ModelRevertedEvent(@NonNull ProjectItem item) {
    this.item = item;
  }

  public @NonNull ProjectItem getItem() {
    return item;
  }
}
