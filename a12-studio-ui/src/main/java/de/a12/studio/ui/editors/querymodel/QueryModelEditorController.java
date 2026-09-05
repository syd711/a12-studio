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
  private QuerySettingsPanelController querySettingsPanelController;

  @FXML
  private PostProcessingPanelController postProcessingPanelController;

  @FXML
  private void initialize() {
    querySettingsPanelController.setOnTargetModelChanged(() -> queryModelTreeController.load(projectItem, (QueryModel) projectItem.getModel()));
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    QueryModel queryModel = (QueryModel) model;
    queryModelTreeController.load(projectItem, queryModel);
    querySettingsPanelController.load(projectItem, queryModel);
    postProcessingPanelController.load(projectItem, queryModel);
    updateSettingsErrorBadge();
  }

  /**
   * Reloads the target Document Model's tree ({@link QueryModelTreeController}) whenever it - or another
   * Document Model - is saved elsewhere, so a Field added/renamed/removed there shows up immediately instead
   * of only after this tab is closed and reopened.
   */
  @Override
  protected void onDocumentModelChangedElsewhere() {
    loadModel(projectItem.getModel());
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.QUERY;
  }
}
