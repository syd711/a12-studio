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
    for (StudioEventListener listener : new ArrayList<>(listeners)) {
      listener.projectOpened(event);
    }
  }

  public void fireProjectClosedEvent(@NonNull Project project) {
    ProjectClosedEvent event = new ProjectClosedEvent(project);
    for (StudioEventListener listener : listeners) {
      listener.projectClosed(event);
    }
  }

  public void fireModelOpenEvent(@NonNull ProjectItem projectItem) {
    ModelOpenedEvent event = new ModelOpenedEvent(projectItem);
    for (StudioEventListener listener : new ArrayList<>(listeners)) {
      listener.modelOpened(event);
    }
  }

  public void fireModelClosedEvent(@NonNull ProjectItem projectItem) {
    ModelClosedEvent event = new ModelClosedEvent(projectItem);
    for (StudioEventListener listener : listeners) {
      listener.modelClosed(event);
    }
  }

  public void fireModelSaveEvent(@NonNull ProjectItem projectItem) {
    ModelSaveEvent event = new ModelSaveEvent(projectItem);
    for (StudioEventListener listener : listeners) {
      listener.modelSaved(event);
    }
  }

  public void fireModelFocusRequestedEvent(@NonNull ProjectItem projectItem) {
    ModelFocusRequestedEvent event = new ModelFocusRequestedEvent(projectItem);
    for (StudioEventListener listener : listeners) {
      listener.modelFocusRequested(event);
    }
  }

  public void firePreferencesOpenRequestedEvent() {
    PreferencesOpenRequestedEvent event = new PreferencesOpenRequestedEvent();
    for (StudioEventListener listener : new ArrayList<>(listeners)) {
      listener.preferencesOpenRequested(event);
    }
  }
}
