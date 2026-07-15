package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.dataservices.models.A12Model;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GeneralInformationPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> DATA_TYPES = List.of("String", "Number", "Boolean", "Date", "Object");

  @FXML
  private TextField nameField;

  @FXML
  private TextField idField;

  @FXML
  private TextField pathField;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);
  }
}
