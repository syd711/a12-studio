package de.a12.studio.ui.events;

import org.jspecify.annotations.NonNull;

public class PreferencesOpenRequestedEvent {

  public enum Section {
    AI_SETTINGS,
    ANNOTATION_SETS,
    GENERAL_SETTINGS,
    A12_INSTALLATION
  }

  @NonNull
  private final Section section;

  public PreferencesOpenRequestedEvent() {
    this(Section.AI_SETTINGS);
  }

  public PreferencesOpenRequestedEvent(@NonNull Section section) {
    this.section = section;
  }

  public @NonNull Section getSection() {
    return section;
  }
}
