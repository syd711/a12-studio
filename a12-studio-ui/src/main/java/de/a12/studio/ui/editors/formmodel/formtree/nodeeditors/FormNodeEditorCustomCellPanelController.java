package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.CustomCell;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Editor for a selected {@link CustomCell} node (a custom-component cell inside a {@code ControlGrid} row):
 * name only - {@link de.a12.studio.models.formmodel.Cell} carries no style/hideCondition/annotations fields
 * for any cell type to edit beyond that.
 */
public class FormNodeEditorCustomCellPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField nameField;

  private CustomCell cell;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    bindTextField(nameField, (el, value) -> cell.setName(value));
  }

  public void setCustomCell(@NonNull CustomCell cell) {
    this.cell = cell;
    setFieldValue(nameField, cell.getName());
  }
}
