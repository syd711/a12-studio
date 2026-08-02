package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits {@link OverviewModel}'s paging behaviour: whether the table uses classic pagination or infinite
 * scrolling, and (for pagination) the page size. Both live on {@link OverviewConfiguration} rather than a
 * single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by
 * e.g. {@link RowHeightActionColumnWidthPanelController}. {@code pagingSize} is only meaningful for
 * pagination - selecting Infinite Scrolling clears it and disables the spinner, matching the reference
 * implementation's export behavior of dropping {@code pagingSize} in favor of {@code enableInfiniteScroll}.
 */
public class PagingBehaviourPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String BEHAVIOUR_PAGINATION = "Pagination";
  private static final String BEHAVIOUR_INFINITE_SCROLLING = "Infinite Scrolling";

  private static final int DEFAULT_PAGING_SIZE = 10;

  @FXML
  private ComboBox<String> behaviourField;
  @FXML
  private Spinner<Integer> pagingSizeField;

  private OverviewModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    behaviourField.getItems().setAll(List.of(BEHAVIOUR_PAGINATION, BEHAVIOUR_INFINITE_SCROLLING));

    pagingSizeField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, DEFAULT_PAGING_SIZE));
    WidgetFactory.restrictToNumericInput(pagingSizeField.getEditor());

    behaviourField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      boolean infiniteScrolling = BEHAVIOUR_INFINITE_SCROLLING.equals(newValue);
      OverviewConfiguration configuration = ensureConfiguration();
      configuration.setEnableInfiniteScroll(infiniteScrolling ? Boolean.TRUE : null);
      configuration.setPagingSize(infiniteScrolling ? null : DEFAULT_PAGING_SIZE);
      updatingFromModel = true;
      try {
        pagingSizeField.getValueFactory().setValue(DEFAULT_PAGING_SIZE);
      }
      finally {
        updatingFromModel = false;
      }
      pagingSizeField.setDisable(infiniteScrolling);
      commitHeaderChange();
    });
    pagingSizeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setPagingSize(newValue);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      OverviewConfiguration configuration = model.getContent().getConfiguration();
      boolean infiniteScrolling = configuration != null && Boolean.TRUE.equals(configuration.getEnableInfiniteScroll());
      behaviourField.setValue(infiniteScrolling ? BEHAVIOUR_INFINITE_SCROLLING : BEHAVIOUR_PAGINATION);
      pagingSizeField.getValueFactory().setValue(
          configuration != null && configuration.getPagingSize() != null ? configuration.getPagingSize() : DEFAULT_PAGING_SIZE);
      pagingSizeField.setDisable(infiniteScrolling);
    }
    finally {
      updatingFromModel = false;
    }
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }
}
