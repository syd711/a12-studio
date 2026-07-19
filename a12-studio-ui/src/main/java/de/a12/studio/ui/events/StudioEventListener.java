package de.a12.studio.ui.events;

import org.jspecify.annotations.NonNull;

public interface StudioEventListener {
  default void projectOpened(@NonNull ProjectOpenedEvent event) {

  }

  default void projectClosed(@NonNull ProjectClosedEvent event) {

  }

  default void modelOpened(@NonNull ModelOpenedEvent event) {

  }

  default void modelClosed(@NonNull ModelClosedEvent event) {

  }

  default void modelDeleted(@NonNull ModelDeletedEvent event) {

  }

  default void modelFocusRequested(@NonNull ModelFocusRequestedEvent event) {

  }

  default void modelSaved(@NonNull ModelSaveEvent event) {

  }

  default void elementValidated(@NonNull ElementValidatedEvent event) {

  }

  default void preferencesOpenRequested(@NonNull PreferencesOpenRequestedEvent event) {

  }

  default void settingsChanged(@NonNull SettingsChangedEvent event) {

  }

  default void localesChanged(@NonNull LocalesChangedEvent event) {

  }
}
