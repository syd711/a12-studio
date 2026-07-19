package de.a12.studio.ui.events;

import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.dataservices.projects.settings.JsonSettings;
import de.a12.studio.dataservices.services.documentmodel.features.validation.ElementValidationError;
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

  public void fireModelDeletedEvent(@NonNull ProjectItem projectItem) {
    ModelDeletedEvent event = new ModelDeletedEvent(projectItem);
    for (StudioEventListener listener : new ArrayList<>(listeners)) {
      listener.modelDeleted(event);
    }
  }

  public void fireModelSaveEvent(@NonNull ProjectItem projectItem) {
    ModelSaveEvent event = new ModelSaveEvent(projectItem);
    for (StudioEventListener listener : listeners) {
      listener.modelSaved(event);
    }
  }

  public void fireElementValidatedEvent(@NonNull String elementId, ElementValidationError error) {
    ElementValidatedEvent event = new ElementValidatedEvent(elementId, error);
    for (StudioEventListener listener : new ArrayList<>(listeners)) {
      listener.elementValidated(event);
    }
  }

  public void fireModelFocusRequestedEvent(@NonNull ProjectItem projectItem) {
    ModelFocusRequestedEvent event = new ModelFocusRequestedEvent(projectItem);
    for (StudioEventListener listener : listeners) {
      listener.modelFocusRequested(event);
    }
  }

  public void firePreferencesOpenRequestedEvent() {
    firePreferencesOpenRequestedEvent(PreferencesOpenRequestedEvent.Section.AI_SETTINGS);
  }

  public void firePreferencesOpenRequestedEvent(PreferencesOpenRequestedEvent.@NonNull Section section) {
    PreferencesOpenRequestedEvent event = new PreferencesOpenRequestedEvent(section);
    for (StudioEventListener listener : new ArrayList<>(listeners)) {
      listener.preferencesOpenRequested(event);
    }
  }

  public void fireSettingsChangedEvent(@NonNull JsonSettings settings) {
    SettingsChangedEvent event = new SettingsChangedEvent(settings);
    for (StudioEventListener listener : new ArrayList<>(listeners)) {
      listener.settingsChanged(event);
    }
  }

  public void fireLocalesChangedEvent(@NonNull ProjectItem projectItem) {
    LocalesChangedEvent event = new LocalesChangedEvent(projectItem);
    for (StudioEventListener listener : new ArrayList<>(listeners)) {
      listener.localesChanged(event);
    }
  }
}
