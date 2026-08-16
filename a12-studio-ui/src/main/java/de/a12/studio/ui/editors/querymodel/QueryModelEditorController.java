package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

public class QueryModelEditorController extends AbstractEditorController {

  @FXML
  private QueryModelTreeController queryModelTreeController;

  @FXML
  private PostProcessingPanelController postProcessingPanelController;

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    QueryModel queryModel = (QueryModel) model;
    queryModelTreeController.load(projectItem, queryModel);
    postProcessingPanelController.load(projectItem, queryModel);
    updateSettingsErrorBadge();
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.QUERY;
  }
}
