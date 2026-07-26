package de.a12.studio.ui.events;

import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

public class SettingsChangedEvent {
  @NonNull
  private final JsonSettings settings;

  public SettingsChangedEvent(@NonNull JsonSettings settings) {
    this.settings = settings;
  }

  public @NonNull JsonSettings getSettings() {
    return settings;
  }
}
