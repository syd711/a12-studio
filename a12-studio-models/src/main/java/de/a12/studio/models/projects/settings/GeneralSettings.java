package de.a12.studio.models.projects.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Locale;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code general} section of a project-root {@code settings.json}.
 * Serialised/deserialised by Jackson inside {@link ProjectRootSettings}.
 */
public class GeneralSettings {

  @Slf4j
  public enum RelationshipEngineMode {
    legacy,
    standard;

    // Tolerates "new" (the natural opposite of "legacy" a user hand-editing settings.json would reach
    // for, though the app itself has never written it) as an alias for "standard", and any other
    // unrecognised value by falling back to the default instead of throwing. JsonSettings#fromJson
    // discards the *entire* enclosing settings object on any deserialization failure here, so a single
    // bad value in this field would otherwise silently wipe unrelated settings (locales, deployment
    // exclusions, etc.) back to their defaults the next time the project is saved.
    @JsonCreator
    public static RelationshipEngineMode fromValue(String value) {
      if ("new".equalsIgnoreCase(value)) {
        return standard;
      }
      for (RelationshipEngineMode mode : values()) {
        if (mode.name().equalsIgnoreCase(value)) {
          return mode;
        }
      }
      log.warn("Unknown relationship engine mode: {}, defaulting to legacy", value);
      return legacy;
    }
  }

  private RelationshipEngineMode relationshipEngineMode = RelationshipEngineMode.legacy;

  private boolean showMetaDataInUIModels = true;

  private List<Locale> locales = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> deploymentExclusions = new ArrayList<>();

  private boolean enforceModelSuffixes = false;

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

  public List<String> getDeploymentExclusions() {
    return deploymentExclusions;
  }

  public void setDeploymentExclusions(List<String> deploymentExclusions) {
    this.deploymentExclusions = deploymentExclusions;
  }

  public boolean isEnforceModelSuffixes() {
    return enforceModelSuffixes;
  }

  public void setEnforceModelSuffixes(boolean enforceModelSuffixes) {
    this.enforceModelSuffixes = enforceModelSuffixes;
  }
}
