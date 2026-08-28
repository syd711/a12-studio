package de.a12.studio.plugin.applicationgroups;

import de.a12.studio.models.ModelType;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.plugin.manager.IModelValidatorContribution;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ApplicationGroupValidatorContribution implements IModelValidatorContribution {

  @Override
  @NonNull
  public List<ModelType> getModelTypes() {
    return List.of(ModelType.values());
  }

  @Override
  @NonNull
  public ModelValidator createValidator() {
    return new ApplicationGroupFilenamePrefixValidator();
  }
}
