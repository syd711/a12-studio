package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LayoutPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> LAYOUTS = List.of("ApplicationFrame", "MasterDetail", "Dashboard", "Stash", "Null");

  @FXML
  private ComboBox<String> layoutCombo;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    layoutCombo.getItems().addAll(LAYOUTS);
  }
}
