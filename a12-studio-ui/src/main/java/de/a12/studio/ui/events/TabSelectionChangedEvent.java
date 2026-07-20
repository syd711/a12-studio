package de.a12.studio.ui.events;

import de.a12.studio.models.projects.ProjectItem;

public class TabSelectionChangedEvent {

  private final ProjectItem item;

  public TabSelectionChangedEvent(ProjectItem item) {
    this.item = item;
  }

  public ProjectItem getItem() {
    return item;
  }
}
