package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.querymodel.QueryPaging;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits a {@link QueryModel}'s {@code content.paging} and {@code content.aggregateResults}. Not bound to a
 * single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by e.g.
 * {@link QuerySortingPanelController}.
 */
public class PagingPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int DEFAULT_PAGE_NUMBER = 0;
  private static final int DEFAULT_PAGE_SIZE = 10;

  @FXML
  private Spinner<Integer> pageNumberField;
  @FXML
  private Spinner<Integer> pageSizeField;

  @FXML
  private CheckBox aggregateResultsField;

  private QueryModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    pageNumberField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, DEFAULT_PAGE_NUMBER));
    WidgetFactory.restrictToNumericInput(pageNumberField.getEditor());
    pageSizeField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, DEFAULT_PAGE_SIZE));
    WidgetFactory.restrictToNumericInput(pageSizeField.getEditor());

    pageNumberField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      ensurePaging().setPageNumber(newValue);
      commitHeaderChange();
    });
    pageSizeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      ensurePaging().setPageSize(newValue);
      commitHeaderChange();
    });

    aggregateResultsField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      content().setAggregateResults(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
    });
  }

  public void load(@NonNull QueryModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      QueryPaging paging = content().getPaging();
      pageNumberField.getValueFactory().setValue(paging != null && paging.getPageNumber() != null ? paging.getPageNumber() : DEFAULT_PAGE_NUMBER);
      pageSizeField.getValueFactory().setValue(paging != null && paging.getPageSize() != null ? paging.getPageSize() : DEFAULT_PAGE_SIZE);
      aggregateResultsField.setSelected(Boolean.TRUE.equals(content().getAggregateResults()));
    }
    finally {
      updatingFromModel = false;
    }
  }

  private QueryModelContent content() {
    return model.getContent();
  }

  private QueryPaging ensurePaging() {
    if (content().getPaging() == null) {
      content().setPaging(new QueryPaging());
    }
    return content().getPaging();
  }
}
