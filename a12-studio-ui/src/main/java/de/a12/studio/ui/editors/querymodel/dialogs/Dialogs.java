package de.a12.studio.ui.editors.querymodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.querymodel.QuerySortBy;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

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

  public static Optional<QuerySort> showSortForAdd(Stage owner, ProjectItem projectItem) {
    QuerySort sort = new QuerySort();
    sort.getSortBy().setDirection(QuerySortBy.DIRECTION_ASC);
    return showSort(owner, StudioBundle.get("add_sort_title"), projectItem, sort) ? Optional.of(sort) : Optional.empty();
  }

  public static boolean showSortForEdit(Stage owner, ProjectItem projectItem, QuerySort sort) {
    return showSort(owner, StudioBundle.get("edit_sort_title"), projectItem, sort);
  }

  /**
   * Opens the sort entry editor for {@code sort}, editing it live so a Cancel can undo the changes (see {@link
   * QuerySortDialogController}).
   */
  private static boolean showSort(Stage owner, String title, ProjectItem projectItem, @NonNull QuerySort sort) {
    FXMLLoader fxmlLoader = new FXMLLoader(QuerySortDialogController.class.getResource("query-sort-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage(null, fxmlLoader, owner, title);
    QuerySortDialogController controller = (QuerySortDialogController) stage.getUserData();
    controller.init(stage, projectItem, sort);
    stage.showAndWait();
    return controller.isConfirmed();
  }
}
