package de.a12.studio.ui.events;

import de.a12.studio.models.projects.Project;
import org.jspecify.annotations.NonNull;

public class ProjectClosedEvent {

  private final Project project;

  public ProjectClosedEvent(@NonNull Project project) {
    this.project = project;
  }

  public Project getProject() {
    return project;
  }
}
