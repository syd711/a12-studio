package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
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
 * Edits {@link OverviewModel}'s "Features": full-text search, row count and paging size, all living on
 * {@link OverviewConfiguration} rather than a single {@link de.a12.studio.models.documentmodel.Element}, so
 * it follows the model-header pattern used by e.g. {@link FormWidthPanelController}.
 */
public class OverviewFeaturesPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int DEFAULT_PAGING_SIZE = 10;

  @FXML
  private CheckBox showFullTextSearchField;
  @FXML
  private CheckBox showRowCountField;
  @FXML
  private Spinner<Integer> pagingSizeField;

  private OverviewModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    pagingSizeField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, DEFAULT_PAGING_SIZE));
    WidgetFactory.restrictToNumericInput(pagingSizeField.getEditor());

    showFullTextSearchField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setShowFullTextSearch(newValue);
      commitHeaderChange();
    });
    showRowCountField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setShowRowCount(newValue ? Boolean.TRUE : null);
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
      showFullTextSearchField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getShowFullTextSearch()));
      showRowCountField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getShowRowCount()));
      pagingSizeField.getValueFactory().setValue(
          configuration != null && configuration.getPagingSize() != null ? configuration.getPagingSize() : DEFAULT_PAGING_SIZE);
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
