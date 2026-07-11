package de.a12.studio.ui.editors.propertyeditors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GeneralInformationPanelController implements Initializable {

  private static final List<String> DATA_TYPES = List.of("String", "Number", "Boolean", "Date", "Object");

  @FXML
  private TextField nameField;

  @FXML
  private TextField idField;

  @FXML
  private TextField pathField;

  @FXML
  private CheckBox typeDefinitionCheckBox;

  @FXML
  private ComboBox<String> dataTypeComboBox;

  @FXML
  private CheckBox globalCheckBox;

  @FXML
  private CheckBox transientCheckBox;

  @FXML
  private CheckBox requiredCheckBox;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataTypeComboBox.getItems().addAll(DATA_TYPES);
    dataTypeComboBox.getSelectionModel().selectFirst();
  }
}
