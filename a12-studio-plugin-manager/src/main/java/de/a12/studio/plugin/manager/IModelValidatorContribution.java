package de.a12.studio.plugin.manager;

import de.a12.studio.models.ModelType;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Extension point interface for plugins that contribute a {@link ModelValidator} to the
 * application's validation pipeline.
 *
 * <p>Implement this interface and register it in your {@code plugin.json} under the extension
 * point name {@code "modelValidator"}. {@link #createValidator()} is called once per opened
 * project, and the resulting validator is registered against every {@link ModelType} returned by
 * {@link #getModelTypes()}.
 *
 * <p>Example plugin.json entry:
 * <pre>
 * {
 *   "extensionPoints": [
 *     { "name": "modelValidator", "class": "com.example.plugin.MyValidatorContribution" }
 *   ]
 * }
 * </pre>
 */
public interface IModelValidatorContribution {

  /**
   * The model types the contributed validator should run against.
   *
   * @return non-empty list of model types
   */
  @NonNull
  List<ModelType> getModelTypes();

  /**
   * Creates a fresh validator instance for the currently opening project.
   *
   * @return the validator to register
   */
  @NonNull
  ModelValidator createValidator();
}
