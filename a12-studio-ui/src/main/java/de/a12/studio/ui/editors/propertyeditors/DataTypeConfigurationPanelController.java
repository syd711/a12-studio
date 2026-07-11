package de.a12.studio.ui.editors.propertyeditors;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class DataTypeConfigurationPanelController {

  @FXML
  private TextField minLengthField;

  @FXML
  private TextField maxLengthField;

  @FXML
  private TextField patternField;

  @FXML
  private CheckBox lineBreaksCheckBox;

  @FXML
  private CheckBox alphabeticalSortingCheckBox;
}
