package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.dataservices.models.A12Model;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.Nullable;

public class DataTypeConfigurationPanelController extends AbstractPropertyEditor {

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
