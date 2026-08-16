package de.a12.studio.ui.editors.querymodel.dialogs;

import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

public class Dialogs {

  private Dialogs() {
  }

  /**
   * Opens the Filter Definition editor for {@code content}, editing {@link
   * QueryModelContent#getFilterDefinition()} live so a Cancel can undo the change (see {@link
   * QueryFilterDefinitionDialogController}). Returns whether the dialog was confirmed.
   */
  public static boolean showFilterDefinition(Stage owner, @NonNull QueryModelContent content) {
    FXMLLoader fxmlLoader = new FXMLLoader(QueryFilterDefinitionDialogController.class.getResource("query-filter-definition-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage(null, fxmlLoader, owner, StudioBundle.get("edit_filter_definition"));
    QueryFilterDefinitionDialogController controller = (QueryFilterDefinitionDialogController) stage.getUserData();
    controller.init(stage, content);
    stage.setOnHidden(event -> controller.destroy());
    stage.showAndWait();
    return controller.isConfirmed();
  }
}
