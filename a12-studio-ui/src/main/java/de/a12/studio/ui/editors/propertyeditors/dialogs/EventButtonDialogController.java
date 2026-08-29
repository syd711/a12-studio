package de.a12.studio.ui.editors.propertyeditors.dialogs;

import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Modal dialog for creating/editing a single {@link de.a12.studio.models.EventButtonLike} row's Event,
 * Priority, Destructive and Icon fields, opened via Add/Edit from {@link
 * de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController}.
 */
public class EventButtonDialogController implements DialogController {

  private static final List<String> PRIORITIES = List.of("PRIMARY", "SECONDARY");
  private static final String DEFAULT_PRIORITY = "SECONDARY";

  @FXML
  private TextField eventField;

  @FXML
  private ComboBox<String> priorityField;

  @FXML
  private CheckBox destructiveField;

  @FXML
  private TextField iconField;

  @FXML
  private Button okButton;

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  public void initDialog(Stage stage, String event, boolean primary, boolean destructive, String iconName) {
    this.stage = stage;
    eventField.setText(event == null ? "" : event);
    priorityField.getItems().setAll(PRIORITIES);
    priorityField.setValue(primary ? "PRIMARY" : DEFAULT_PRIORITY);
    destructiveField.setSelected(destructive);
    iconField.setText(iconName == null ? "" : iconName);
    okButton.disableProperty().bind(eventField.textProperty().isEmpty());
    eventField.requestFocus();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  public Optional<ButtonType> getResult() {
    return result;
  }

  public String getEvent() {
    return eventField.getText();
  }

  public boolean isPrimary() {
    return "PRIMARY".equals(priorityField.getValue());
  }

  public boolean isDestructive() {
    return destructiveField.isSelected();
  }

  public String getIconName() {
    return iconField.getText();
  }
}
