package de.a12.studio.ui.editors.relationshipmodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.ProjectModelFolders;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Folder-selection dialog for {@link Dialogs#showGenerateDocumentModelsFolder}: a single Location combo, reusing
 * the same folder chooser as {@link de.a12.studio.ui.projecttree.dialogs.NewModelDialogController} (see {@link
 * ProjectModelFolders#configureLocationCombo}), so the Relationship Model editor's "Generate Document Models"
 * action offers the same folder-picking UI/default as creating a new model.
 */
public class GenerateDocumentModelsDialogController implements DialogController {

  @FXML
  private ComboBox<ProjectItem> locationCombo;

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  void init(Stage stage, @NonNull ProjectItem targetFolder) {
    this.stage = stage;
    ProjectModelFolders.configureLocationCombo(locationCombo, targetFolder);
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  Optional<ProjectItem> getResult() {
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return Optional.empty();
    }
    return Optional.ofNullable(locationCombo.getValue());
  }
}
