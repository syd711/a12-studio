package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.TextCell;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Editor for a selected {@link TextCell} node (a decorated static text cell inside a {@code ControlGrid}
 * row): name, decoration variant ("INFO"/"WARNING"/"SUCCESS"/"ERROR") and localized content text. Previously
 * a {@link TextCell} could be added via the tree/drag-drop but had no editor pane at all, so its fields
 * couldn't be changed once created.
 */
public class FormNodeEditorTextCellPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField nameField;
  @FXML
  private ComboBox<String> decorationCombo;
  @FXML
  private LocalizedTextPanelController contentController;

  private TextCell cell;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    decorationCombo.getItems().setAll(null, "INFO", "WARNING", "SUCCESS", "ERROR");
    contentController.configureCustom("textCellContent", StudioBundle.get("content"));

    bindTextField(nameField, (el, value) -> cell.setName(value));
    bindComboBox(decorationCombo, (el, value) -> cell.setDecoration(value));
  }

  public void setTextCell(@NonNull TextCell cell) {
    this.cell = cell;
    setFieldValue(nameField, cell.getName());
    setFieldValue(decorationCombo, cell.getDecoration());
    contentController.setCustom(() -> texts(cell.getContent()), () -> ensureContent(cell).getText());
  }

  private static List<Label> texts(@Nullable TextContainer c) {
    return c != null ? c.getText() : List.of();
  }

  private static TextContainer ensureContent(@NonNull TextCell cell) {
    if (cell.getContent() == null) {
      cell.setContent(new TextContainer());
    }
    return cell.getContent();
  }
}
