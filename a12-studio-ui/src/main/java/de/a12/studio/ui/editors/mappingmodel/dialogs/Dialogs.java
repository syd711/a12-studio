package de.a12.studio.ui.editors.mappingmodel.dialogs;

import de.a12.studio.models.mappingmodel.MappingSource;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.Optional;

public class Dialogs {

  private Dialogs() {
  }

  /**
   * Opens the Source Model dialog for a new, not-yet-attached {@link MappingSource}; the caller only attaches
   * it to the model's Source list once this resolves with OK, so Cancel needs no undo.
   */
  public static Optional<MappingSource> showSourceModelForAdd(Stage owner) {
    MappingSource sourceModel = new MappingSource();
    return showSourceModel(owner, StudioBundle.get("add_source_model_title"), sourceModel) ? Optional.of(sourceModel) : Optional.empty();
  }

  /**
   * Opens the Source Model dialog for an existing, already-attached {@link MappingSource}. Returns whether OK
   * was pressed; {@code sourceModel} itself is only mutated on OK (see {@link SourceModelDialogController}), so
   * Cancel leaves it untouched.
   */
  public static boolean showSourceModelForEdit(Stage owner, MappingSource sourceModel) {
    return showSourceModel(owner, StudioBundle.get("edit_source_model_title"), sourceModel);
  }

  private static boolean showSourceModel(Stage owner, String title, MappingSource sourceModel) {
    FXMLLoader fxmlLoader = new FXMLLoader(SourceModelDialogController.class.getResource("source-model-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("sourcemodel-dialog", fxmlLoader, owner, title);
    SourceModelDialogController controller = (SourceModelDialogController) stage.getUserData();
    controller.initDialog(stage, sourceModel);
    stage.showAndWait();
    return controller.isConfirmed();
  }
}
