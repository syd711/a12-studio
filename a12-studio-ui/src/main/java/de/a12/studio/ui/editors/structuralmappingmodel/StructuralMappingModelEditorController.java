package de.a12.studio.ui.editors.structuralmappingmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import org.jspecify.annotations.NonNull;

/**
 * Placeholder editor for {@link StructuralMappingModel}: opens the model in an otherwise empty tab. Field
 * editing and validation are added later.
 */
public class StructuralMappingModelEditorController extends AbstractEditorController {

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    updateSettingsErrorBadge();
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.STRUCTURALMAPPING;
  }
}
