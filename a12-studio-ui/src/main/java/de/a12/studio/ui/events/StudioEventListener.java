package de.a12.studio.ui.events;

import org.jspecify.annotations.NonNull;

public interface StudioEventListener {
  default void projectOpened(@NonNull ProjectOpenedEvent event) {

  }

  default void modelOpened(@NonNull ModelOpenedEvent event) {

  }
}
