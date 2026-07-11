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

  default void modelFocusRequested(@NonNull ModelFocusRequestedEvent event) {

  }
}
