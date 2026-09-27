package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.overview.OverviewInfiniteScrollingValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits {@link OverviewModel}'s row height and action column width, both living on {@link
 * OverviewConfiguration} rather than a single {@link de.a12.studio.models.documentmodel.Element}, so it
 * follows the model-header pattern used by e.g. {@link OverviewSearchAndFiltersPanelController}. Both fields are
 * required while Infinite Scrolling is active ({@link OverviewInfiniteScrollingValidator}) - {@link #refresh()}
 * re-checks that whenever {@link PagingBehaviourPanelController}'s behaviour switch changes (including its own
 * seeding of a default Row Height, see there), since this panel's own fields don't otherwise know about it.
 */
public class RowHeightActionColumnWidthPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int DEFAULT_ROW_HEIGHT = 32;

  @FXML
  private Spinner<Integer> rowHeightField;
  @FXML
  private TextField actionColumnWidthField;

  private OverviewModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    rowHeightField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, DEFAULT_ROW_HEIGHT));
    WidgetFactory.restrictToNumericInput(rowHeightField.getEditor());
    WidgetFactory.restrictToDecimalInput(actionColumnWidthField);

    rowHeightField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setRowHeight(newValue);
      commitHeaderChange();
      refreshValidationError();
    });
    actionColumnWidthField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (newValue == null || newValue.isBlank()) {
        ensureConfiguration().setActionColumnWidth(null);
        commitHeaderChange();
        refreshValidationError();
        return;
      }
      try {
        ensureConfiguration().setActionColumnWidth(Double.valueOf(newValue));
      }
      catch (NumberFormatException e) {
        // Mid-typing input such as "-" or "." isn't a number yet - keep the stored value until it is.
        return;
      }
      commitHeaderChange();
      refreshValidationError();
    });
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      OverviewConfiguration configuration = model.getContent().getConfiguration();
      rowHeightField.getValueFactory().setValue(
          configuration != null && configuration.getRowHeight() != null ? configuration.getRowHeight() : DEFAULT_ROW_HEIGHT);
      actionColumnWidthField.setText(
          configuration != null && configuration.getActionColumnWidth() != null ? String.valueOf(configuration.getActionColumnWidth()) : "");
    }
    finally {
      updatingFromModel = false;
    }
    refreshValidationError();
  }

  /** Called by the owning editor whenever {@link PagingBehaviourPanelController}'s behaviour switch changes,
   * since a value it just seeded (or cleared the requirement for) isn't otherwise reflected here. */
  public void refresh() {
    if (model == null) {
      return;
    }
    updatingFromModel = true;
    try {
      OverviewConfiguration configuration = model.getContent().getConfiguration();
      rowHeightField.getValueFactory().setValue(
          configuration != null && configuration.getRowHeight() != null ? configuration.getRowHeight() : DEFAULT_ROW_HEIGHT);
    }
    finally {
      updatingFromModel = false;
    }
    refreshValidationError();
  }

  private void refreshValidationError() {
    if (model == null) {
      return;
    }
    List<ModelValidationError> errors =
        Studio.getValidationService().validateElement(model, OverviewInfiniteScrollingValidator.ROW_HEIGHT_ELEMENT_ID);
    if (errors.isEmpty()) {
      errors = Studio.getValidationService().validateElement(model, OverviewInfiniteScrollingValidator.ACTION_COLUMN_WIDTH_ELEMENT_ID);
    }
    if (errors.isEmpty()) {
      hideError();
    }
    else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }
}
