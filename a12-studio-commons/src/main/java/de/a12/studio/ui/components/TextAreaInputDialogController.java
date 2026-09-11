package de.a12.studio.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.Optional;

public class TextAreaInputDialogController implements DialogController {

  @FXML
  private Label textLabel;

  @FXML
  private Label descriptionLabel;

  @FXML
  private Button cancelButton;

  @FXML
  private TextArea textArea;

  private String inputText;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  private Stage stage;

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  public void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  public void initDialog(Stage stage, String innerTitle, String description, String defaultValue) {
    this.stage = stage;
    this.textLabel.setText(innerTitle);

    this.descriptionLabel.setText(description != null ? description : "");
    this.descriptionLabel.setVisible(description != null);
    this.descriptionLabel.setManaged(description != null);

    if (defaultValue != null) {
      this.textArea.setText(defaultValue);
      this.inputText = defaultValue;
    }
    textArea.selectAll();
    textArea.requestFocus();

    textArea.textProperty().addListener((observable, oldValue, newValue) -> inputText = newValue);
  }

  public Optional<ButtonType> getResult() {
    return result;
  }

  public String getText() {
    return inputText;
  }
}
