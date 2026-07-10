package de.a12.studio.ui.events;

import de.a12.studio.dataservices.projects.Project;
import org.jspecify.annotations.NonNull;

public class ProjectOpenedEvent {

  private final Project project;

  public ProjectOpenedEvent(@NonNull Project project) {
    this.project = project;
  }

  public Project getProject() {
    return project;
  }
}
