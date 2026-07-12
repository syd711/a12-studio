package de.a12.studio.ui.editors.propertyeditors.widgets;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class StringFieldController implements Initializable {

  @FXML
  private Label label;

  @FXML
  private TextField textField;

  public Label getLabel() {
    return label;
  }

  public TextField getTextField() {
    return textField;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }
}
