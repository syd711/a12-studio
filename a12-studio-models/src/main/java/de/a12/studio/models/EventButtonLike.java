package de.a12.studio.models;

// Structural shape shared by the various "event button" model classes (e.g. overviewmodel.Button,
// overviewmodel.ButtonElement) that all render as the same Event/Priority/Destructive/Icon row in property
// editors like EventButtonsPanelController, regardless of which concrete model class backs them.
public interface EventButtonLike {

  String getEvent();

  void setEvent(String event);

  Boolean getPrimary();

  void setPrimary(Boolean primary);

  Boolean getDestructive();

  void setDestructive(Boolean destructive);

  String getIconName();

  void setIconName(String name);
}
