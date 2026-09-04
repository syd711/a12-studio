package de.a12.studio.ui.util;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Live "Enforce Model Suffixes" check shared by every dialog that lets the user type a new model's
 * filename (see the "New Model" dialogs), so the error surfaces immediately instead of only after
 * creation via {@code ModelSuffixValidator}'s Problems-panel run. Reads the setting straight from
 * {@code <project>/settings.json} (like {@code NewModelFactory#resolveDefaultLocales}) so it works from
 * a target folder alone, without needing the {@code Studio} singleton's current project.
 */
public final class ModelSuffixValidation {

  private ModelSuffixValidation() {
  }

  /**
   * Returns the error message to show when {@code name} doesn't end with {@code modelType}'s expected
   * suffix and the setting is enabled for the project {@code targetFolder} belongs to; empty otherwise
   * (setting disabled, blank name, or the suffix already matches).
   */
  public static Optional<String> validate(@NonNull ProjectItem targetFolder, @Nullable ModelType modelType, @Nullable String name) {
    if (modelType == null || name == null || name.isBlank() || modelType.getSuffix() == null) {
      return Optional.empty();
    }
    if (!ProjectRootSettings.load(targetFolder.getProjectFolder()).getGeneral().isEnforceModelSuffixes()) {
      return Optional.empty();
    }
    String expectedSuffix = "_" + modelType.getSuffix();
    if (name.trim().endsWith(expectedSuffix)) {
      return Optional.empty();
    }
    return Optional.of(StudioBundle.get("validation.enforce_model_suffix", modelType.getDisplayName(), expectedSuffix));
  }
}
