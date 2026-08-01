package de.a12.studio.ui.editors.mappingmodel.dialogs;

import de.a12.studio.models.mappingmodel.MappingSource;
import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

/**
 * Modal dialog for adding/editing a single {@link MappingSource}, opened from {@link
 * de.a12.studio.ui.editors.propertyeditors.SourceModelsPanelController} by clicking a row (or its Edit
 * button/Add button). Content is intentionally empty for now - the Name/Model/Repetitions/Skip Document
 * Validation controls are a follow-up.
 */
public class SourceModelDialogController implements DialogController {

  private Stage stage;

  public void initDialog(Stage stage, @NonNull MappingSource sourceModel) {
    this.stage = stage;
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    stage.close();
  }
}
