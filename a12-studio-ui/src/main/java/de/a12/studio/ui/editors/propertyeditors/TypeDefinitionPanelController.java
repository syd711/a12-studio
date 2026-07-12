package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TypeDefinitionPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> DATA_TYPES = List.of("String", "Number", "Boolean", "Date", "Object");

  @FXML
  private ComboBox dataTypeCombo;

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
    super.initialize(url, resourceBundle);

    dataTypeComboBox.getItems().addAll(DATA_TYPES);
    dataTypeComboBox.getSelectionModel().selectFirst();
  }
}
