package de.a12.studio.ui.editors.contentmodel.dialogs;

import de.a12.studio.models.contentmodel.ContentModule;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Dialogs {

  private Dialogs() {
  }

  /**
   * Lets the user choose one of {@code modules} to add to the element described by {@code targetLabel}.
   *
   * @return the chosen type, empty when the dialog was cancelled
   */
  public static Optional<ContentModule> showInsertElement(Stage owner, @NonNull String targetLabel,
      @NonNull List<ContentModule> modules) {
    FXMLLoader fxmlLoader = new FXMLLoader(InsertElementDialogController.class.getResource("insert-element-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("insert-element-dialog", fxmlLoader, owner,
        StudioBundle.get("content_model_insert.title"));
    InsertElementDialogController controller = (InsertElementDialogController) stage.getUserData();
    controller.init(stage, targetLabel, modules);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed() ? Optional.of(controller.getSelected()) : Optional.empty();
  }

  /**
   * Lets the user edit one column of a table.
   *
   * @param resizingEnabled whether the table has "Enable resizing" on (SME only offers the minimum width then)
   * @return the edited copy of {@code column}, empty when the dialog was cancelled
   */
  public static Optional<Map<String, Object>> showTableColumn(Stage owner, @NonNull Map<String, Object> column,
      boolean resizingEnabled) {
    FXMLLoader fxmlLoader = new FXMLLoader(TableColumnDialogController.class.getResource("table-column-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("table-column-dialog", fxmlLoader, owner,
        StudioBundle.get("edit_column_title"));
    TableColumnDialogController controller = (TableColumnDialogController) stage.getUserData();
    controller.init(stage, column, resizingEnabled);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return Optional.ofNullable(controller.getResult());
  }
}
