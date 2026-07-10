package de.a12.studio.ui.events;

import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.dataservices.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class StudioEventManager {
  private final static StudioEventManager INSTANCE = new StudioEventManager();

  private final List<StudioEventListener> listeners = new ArrayList<>();

  public static StudioEventManager getInstance() {
    return INSTANCE;
  }

  public void addListener(@NonNull StudioEventListener listener) {
    listeners.add(listener);
  }

  public void fireProjectOpenEvent(@NonNull Project project) {
    ProjectOpenedEvent event = new ProjectOpenedEvent(project);
    for (StudioEventListener listener : listeners) {
      listener.projectOpened(event);
    }
  }

  public void fireModelOpenEvent(@NonNull ProjectItem projectItem) {
    ModelOpenedEvent event = new ModelOpenedEvent(projectItem);
    for (StudioEventListener listener : listeners) {
      listener.modelOpened(event);
    }
  }
}
