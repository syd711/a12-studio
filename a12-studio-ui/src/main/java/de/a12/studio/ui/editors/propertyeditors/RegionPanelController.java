package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RegionPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> REGIONS = List.of("APP", "CONTENT", "SIDEBAR", "MODAL");

  @FXML
  private ComboBox<String> regionCombo;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    regionCombo.getItems().addAll(REGIONS);
  }
}
