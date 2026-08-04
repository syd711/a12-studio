package de.a12.studio.ui.preferences.dialogs;

import de.a12.studio.ui.util.StudioBundle;

import de.a12.studio.models.projects.settings.annotations.AnnotationDataSet;
import de.a12.studio.ui.preferences.AnnotationDataSetSupport;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.Optional;

public class Dialogs {

  /**
   * Opens the Edit ({@code editable=true}) or Export ({@code editable=false}) dialog for {@code original},
   * always operating on a deep copy so tree pruning never mutates the persisted/live source. Returns the
   * (possibly renamed/pruned) copy iff the user confirmed (Save/Export), empty on Cancel.
   */
  public static Optional<AnnotationDataSet> showAnnotationDataSetEditor(Stage owner, AnnotationDataSet original, boolean editable) {
    AnnotationDataSet workingCopy = AnnotationDataSetSupport.deepCopy(original);
    FXMLLoader fxmlLoader = new FXMLLoader(AnnotationDataSetTreeDialogController.class.getResource("annotation-dataset-tree-dialog.fxml"));
fxmlLoader.setResources(StudioBundle.getBundle());
    String title = editable ? "Edit Annotation Set" : "Export Annotation Set";
    Stage stage = WidgetFactory.createDialogStage("dialog-annotation-dataset-editor", fxmlLoader, owner, title);
    AnnotationDataSetTreeDialogController controller = (AnnotationDataSetTreeDialogController) stage.getUserData();
    controller.init(stage, editable, workingCopy);
    stage.showAndWait();
    return controller.applyResultTo(workingCopy) ? Optional.of(workingCopy) : Optional.empty();
  }
}
