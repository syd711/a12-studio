package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.RowAction;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits {@link OverviewModel}'s row activation ({@code content.defaultRowAction}), which lives on the model's
 * content rather than a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the
 * model-header pattern used by e.g. {@link RowHeightActionColumnWidthPanelController}.
 */
public class RowActivationPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String ROW_ACTIVATION_DEFAULT = "Default Engine Behavior";
  private static final String ROW_ACTIVATION_EVENT = "Event";
  private static final String ROW_ACTIVATION_NON_INTERACTIVE = "Non Interactive";

  @FXML
  private ComboBox<String> rowActivationTypeField;
  @FXML
  private Label rowActivationInfoIcon;
  @FXML
  private TextField rowActivationEventField;

  private OverviewModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(rowActivationInfoIcon,
        "Default Engine Behavior: the Overview Engine's built-in row-click behavior applies. Event: clicking a row "
            + "triggers the given event. Non Interactive: rows are explicitly not clickable.");

    rowActivationTypeField.getItems().setAll(ROW_ACTIVATION_DEFAULT, ROW_ACTIVATION_EVENT, ROW_ACTIVATION_NON_INTERACTIVE);
    rowActivationTypeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      applyRowActivationType(newValue);
      refreshEventFieldVisibility(newValue);
      commitHeaderChange();
    });

    rowActivationEventField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      RowAction rowAction = model.getContent().getDefaultRowAction();
      if (rowAction == null) {
        return;
      }
      rowAction.setEvent(newValue == null || newValue.isBlank() ? null : newValue);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      RowAction rowAction = model.getContent().getDefaultRowAction();
      String type;
      if (rowAction == null || !Boolean.TRUE.equals(rowAction.getCustom())) {
        type = ROW_ACTIVATION_DEFAULT;
      }
      else if (rowAction.getEvent() != null && !rowAction.getEvent().isBlank()) {
        type = ROW_ACTIVATION_EVENT;
      }
      else {
        type = ROW_ACTIVATION_NON_INTERACTIVE;
      }
      rowActivationTypeField.setValue(type);
      rowActivationEventField.setText(rowAction != null && rowAction.getEvent() != null ? rowAction.getEvent() : "");
      refreshEventFieldVisibility(type);
    }
    finally {
      updatingFromModel = false;
    }
  }

  private void applyRowActivationType(String type) {
    if (ROW_ACTIVATION_DEFAULT.equals(type)) {
      model.getContent().setDefaultRowAction(null);
      return;
    }
    RowAction rowAction = model.getContent().getDefaultRowAction();
    if (rowAction == null) {
      rowAction = new RowAction();
      model.getContent().setDefaultRowAction(rowAction);
    }
    rowAction.setCustom(true);
    if (ROW_ACTIVATION_EVENT.equals(type)) {
      rowAction.setEvent(rowActivationEventField.getText());
    }
    else {
      rowAction.setEvent(null);
    }
  }

  private void refreshEventFieldVisibility(String type) {
    boolean showEvent = ROW_ACTIVATION_EVENT.equals(type);
    rowActivationEventField.setVisible(showEvent);
    rowActivationEventField.setManaged(showEvent);
  }
}
