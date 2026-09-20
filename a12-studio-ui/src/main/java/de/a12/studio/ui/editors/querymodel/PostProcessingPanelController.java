package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModel;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

/**
 * Tab 2 ("Post Processing") of the Query Model editor: Sorting (embedded via {@link QuerySortingPanelController}),
 * Paging (via {@link PagingPanelController}) and Aggregation (via {@link QueryAggregationPanelController}) - each
 * its own {@code AbstractPropertyEditor} panel editing {@link de.a12.studio.models.querymodel.QueryModelContent}
 * directly.
 */
public class PostProcessingPanelController {

  @FXML
  private QuerySortingPanelController querySortingPanelController;

  @FXML
  private PagingPanelController pagingPanelController;

  @FXML
  private QueryAggregationPanelController queryAggregationPanelController;

  public void load(@NonNull ProjectItem projectItem, @NonNull QueryModel model) {
    querySortingPanelController.load(projectItem, model);
    pagingPanelController.load(model);
    queryAggregationPanelController.load(projectItem, model);
  }

  /** Releases what the panels hold on to (a still-debounced edit); called when the editor's tab closes. */
  public void destroy() {
    queryAggregationPanelController.destroy();
  }
}
