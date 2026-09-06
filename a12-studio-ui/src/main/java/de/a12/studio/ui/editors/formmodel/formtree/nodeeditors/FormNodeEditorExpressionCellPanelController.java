package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.ExpressionCell;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Editor for a selected {@link ExpressionCell} node (a computed-text cell inside a {@code ControlGrid} row):
 * name and the raw expression string. Previously an {@link ExpressionCell} could be added via the tree/
 * drag-drop but had no editor pane at all, so its expression couldn't be changed once created.
 */
public class FormNodeEditorExpressionCellPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField nameField;
  @FXML
  private TextArea expressionArea;

  private ExpressionCell cell;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    bindTextField(nameField, (el, value) -> cell.setName(value));
    bindTextArea(expressionArea, (el, value) -> cell.setExpression(value));
  }

  public void setExpressionCell(@NonNull ExpressionCell cell) {
    this.cell = cell;
    setFieldValue(nameField, cell.getName());
    setFieldValue(expressionArea, cell.getExpression());
  }
}
