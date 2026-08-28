package de.a12.studio.ui.events;

import de.a12.studio.models.projects.Project;
import org.jspecify.annotations.NonNull;

/**
 * Fired once {@link de.a12.studio.ui.tabs.TabPaneController} has finished restoring the
 * previously-open tabs for a {@link ProjectOpenedEvent} - this happens asynchronously, one tab
 * per FX pulse, so it always arrives strictly after {@code projectOpened} listener dispatch has
 * returned.
 */
public class TabsRestoredEvent {

  private final Project project;

  public TabsRestoredEvent(@NonNull Project project) {
    this.project = project;
  }

  public Project getProject() {
    return project;
  }
}
