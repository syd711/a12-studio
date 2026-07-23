package de.a12.studio.models.projects.settings;

import de.a12.studio.models.Locale;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code general} section of a project-root {@code settings.json}.
 * Serialised/deserialised by Jackson inside {@link ProjectRootSettings}.
 */
public class GeneralSettings {

  public enum RelationshipEngineMode {
    legacy,
    standard
  }

  private RelationshipEngineMode relationshipEngineMode = RelationshipEngineMode.legacy;

  private boolean showMetaDataInUIModels = true;

  private List<Locale> locales = new ArrayList<>();

  public RelationshipEngineMode getRelationshipEngineMode() {
    return relationshipEngineMode;
  }

  public void setRelationshipEngineMode(RelationshipEngineMode relationshipEngineMode) {
    this.relationshipEngineMode = relationshipEngineMode;
  }

  public boolean isShowMetaDataInUIModels() {
    return showMetaDataInUIModels;
  }

  public void setShowMetaDataInUIModels(boolean showMetaDataInUIModels) {
    this.showMetaDataInUIModels = showMetaDataInUIModels;
  }

  public List<Locale> getLocales() {
    return locales;
  }

  public void setLocales(List<Locale> locales) {
    this.locales = locales;
  }
}
