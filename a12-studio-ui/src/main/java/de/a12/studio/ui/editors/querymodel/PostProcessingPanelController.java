package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModel;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

/**
 * Tab 2 ("Post Processing") of the Query Model editor: Sorting (embedded via {@link QuerySortingPanelController})
 * and Paging/Aggregation (embedded via {@link PagingPanelController}) - both their own {@code
 * AbstractPropertyEditor} panels editing {@link de.a12.studio.models.querymodel.QueryModelContent} directly.
 */
public class PostProcessingPanelController {

  @FXML
  private QuerySortingPanelController querySortingPanelController;

  @FXML
  private PagingPanelController pagingPanelController;

  public void load(@NonNull ProjectItem projectItem, @NonNull QueryModel model) {
    querySortingPanelController.load(projectItem, model);
    pagingPanelController.load(model);
  }
}
