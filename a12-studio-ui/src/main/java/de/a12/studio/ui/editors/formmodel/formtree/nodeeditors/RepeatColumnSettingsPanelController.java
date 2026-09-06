package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.TableStyle;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * "Column Settings" property editor for a selected {@link AbstractRepeat} node: edits
 * {@link AbstractRepeat#getTableStyle()} — specifically {@link TableStyle#getRowHeight()} and
 * {@link TableStyle#getTableHeight()} (optional integer pixel values; 0 means "not set").
 */
public class RepeatColumnSettingsPanelController implements Initializable {

  private static final int DEFAULT = 0;
  private static final int MAX = 9999;

  @FXML
  private Spinner<Integer> rowHeightSpinner;

  @FXML
  private Spinner<Integer> tableHeightSpinner;

  @FXML
  private Spinner<Integer> cardHeightSpinner;

  @FXML
  private Spinner<Integer> actionColumnWidthSpinner;

  private AbstractRepeat repeat;
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    rowHeightSpinner.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(DEFAULT, MAX, DEFAULT));
    rowHeightSpinner.setEditable(true);

    tableHeightSpinner.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(DEFAULT, MAX, DEFAULT));
    tableHeightSpinner.setEditable(true);

    rowHeightSpinner.valueProperty().addListener((obs, old, val) -> {
      if (updatingFromModel || repeat == null) return;
      ensureTableStyle().setRowHeight(val == null || val == 0 ? null : val);
      commitChange();
    });

    tableHeightSpinner.valueProperty().addListener((obs, old, val) -> {
      if (updatingFromModel || repeat == null) return;
      ensureTableStyle().setTableHeight(val == null || val == 0 ? null : val);
      commitChange();
    });

    cardHeightSpinner.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(DEFAULT, MAX, DEFAULT));
    cardHeightSpinner.setEditable(true);
    cardHeightSpinner.valueProperty().addListener((obs, old, val) -> {
      if (updatingFromModel || repeat == null) return;
      ensureTableStyle().setCardHeight(val == null || val == 0 ? null : val);
      commitChange();
    });

    actionColumnWidthSpinner.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(DEFAULT, MAX, DEFAULT));
    actionColumnWidthSpinner.setEditable(true);
    actionColumnWidthSpinner.valueProperty().addListener((obs, old, val) -> {
      if (updatingFromModel || repeat == null) return;
      ensureTableStyle().setActionColumnWidth(val == null || val == 0 ? null : val);
      commitChange();
    });
  }

  public void setRepeat(@NonNull AbstractRepeat repeat) {
    this.repeat = repeat;
    updatingFromModel = true;
    try {
      TableStyle ts = repeat.getTableStyle();
      rowHeightSpinner.getValueFactory().setValue(ts != null && ts.getRowHeight() != null ? ts.getRowHeight() : DEFAULT);
      tableHeightSpinner.getValueFactory().setValue(ts != null && ts.getTableHeight() != null ? ts.getTableHeight() : DEFAULT);
      cardHeightSpinner.getValueFactory().setValue(ts != null && ts.getCardHeight() != null ? ts.getCardHeight() : DEFAULT);
      actionColumnWidthSpinner.getValueFactory().setValue(ts != null && ts.getActionColumnWidth() != null ? ts.getActionColumnWidth() : DEFAULT);
    } finally {
      updatingFromModel = false;
    }
  }

  private TableStyle ensureTableStyle() {
    if (repeat.getTableStyle() == null) {
      repeat.setTableStyle(new TableStyle());
    }
    return repeat.getTableStyle();
  }

  private void commitChange() {
    ProjectItem item = Studio.getSelectedProjectItem();
    if (item == null) return;
    item.save();
    StudioEventManager.getInstance().fireModelSavedEvent(item);
  }
}
