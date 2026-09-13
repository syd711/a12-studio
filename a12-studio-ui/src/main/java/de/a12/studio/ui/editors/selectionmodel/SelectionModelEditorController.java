package de.a12.studio.ui.editors.selectionmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.selectionmodel.SelectionModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

/**
 * Edits a {@link SelectionModel}: three identically-shaped sections (Data/Computation/Validation, matching
 * every real fixture's field order), each a {@link SelectionCategoryPanelController} instance (which titles
 * itself from the category name passed to {@link SelectionCategoryPanelController#setCategory}). No tree/
 * live Document Model view - see {@link de.a12.studio.models.selectionmodel.SelectionModelContent}'s javadoc
 * for why (SME itself never persists a reference Document Model in the file, so there's nothing durable to
 * resolve a tree against); path specifications are edited as plain, pattern-validated text instead, the same
 * way {@code overviewmodel.Column.expression}/{@code QueryModelContent.filterDefinition} are.
 */
public class SelectionModelEditorController extends AbstractEditorController {

  @FXML
  private SelectionCategoryPanelController dataPanelController;

  @FXML
  private SelectionCategoryPanelController computationPanelController;

  @FXML
  private SelectionCategoryPanelController validationPanelController;

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    SelectionModel selectionModel = (SelectionModel) model;
    dataPanelController.setCategory(selectionModel, "Data", selectionModel.getContent().getData());
    computationPanelController.setCategory(selectionModel, "Computation", selectionModel.getContent().getComputation());
    validationPanelController.setCategory(selectionModel, "Validation", selectionModel.getContent().getValidation());
    updateSettingsErrorBadge();
  }

  @NonNull
  @Override
  public ModelType getModelType() {
    return ModelType.SELECTION;
  }
}
