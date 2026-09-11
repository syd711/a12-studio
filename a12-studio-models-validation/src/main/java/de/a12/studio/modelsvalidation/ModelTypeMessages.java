package de.a12.studio.modelsvalidation;

import de.a12.studio.models.ModelType;

/**
 * Localized display names for {@link ModelType}, looked up from {@code validation-messages.properties}
 * via {@link ModelType#getSuffix()} (e.g. {@code model_type_name.DM} for {@link ModelType#DOCUMENT}).
 * Mirrors {@code de.a12.studio.ui.util.ModelTypeLabels} but kept in this module so validators stay
 * independent of the UI module, like {@link ValidationMessages} itself.
 */
public final class ModelTypeMessages {

  private ModelTypeMessages() {
  }

  public static String getDisplayName(ModelType modelType) {
    if (modelType == null || modelType.getSuffix() == null) {
      return "model";
    }
    return ValidationMessages.get("model_type_name." + modelType.getSuffix());
  }
}
