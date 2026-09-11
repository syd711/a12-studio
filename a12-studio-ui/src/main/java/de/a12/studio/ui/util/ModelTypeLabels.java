package de.a12.studio.ui.util;

import de.a12.studio.models.ModelType;

/**
 * Localized display names for {@link ModelType}, looked up from {@code messages.properties} via
 * {@link ModelType#getSuffix()} (e.g. {@code model_type_name.DM} for {@link ModelType#DOCUMENT}).
 * {@code ModelType} itself has no display name; it lives in a UI-independent module, so this lookup
 * stays here instead.
 */
public final class ModelTypeLabels {

  private ModelTypeLabels() {
  }

  public static String getDisplayName(ModelType modelType) {
    if (modelType == null || modelType.getSuffix() == null) {
      return "";
    }
    return StudioBundle.get("model_type_name." + modelType.getSuffix().toUpperCase());
  }
}
