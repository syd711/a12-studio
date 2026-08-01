package de.a12.studio.ui.editors.combineddocumentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import org.jspecify.annotations.NonNull;

/**
 * Placeholder editor for {@link CombinedDocumentModel}: opens the model in an otherwise empty tab. Field
 * editing and validation are added later.
 */
public class CombinedDocumentModelEditorController extends AbstractEditorController {

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    updateSettingsErrorBadge();
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.COMBINATION;
  }
}
