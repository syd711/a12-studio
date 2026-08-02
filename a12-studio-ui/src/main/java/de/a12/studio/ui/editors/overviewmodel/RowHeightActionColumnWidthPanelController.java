package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits {@link OverviewModel}'s row height and action column width, both living on {@link
 * OverviewConfiguration} rather than a single {@link de.a12.studio.models.documentmodel.Element}, so it
 * follows the model-header pattern used by e.g. {@link OverviewFeaturesPanelController}.
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
    WidgetFactory.restrictToNumericInput(actionColumnWidthField);

    rowHeightField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setRowHeight(newValue);
      commitHeaderChange();
    });
    actionColumnWidthField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setActionColumnWidth(newValue == null || newValue.isBlank() ? null : Integer.valueOf(newValue));
      commitHeaderChange();
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
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }
}
