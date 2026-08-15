package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.Button;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits a {@link Button}'s {@code name} and (read-only) {@code id} - SME's {@code I_Button-form.json} "General
 * Settings" section. Embedded in {@link de.a12.studio.ui.editors.formmodel.dialogs.FormButtonDialogController}.
 * Not tied to a single {@code Element}, so it follows the model-header pattern (a plain {@link #setButton}
 * entry point, {@code bindTextField}'s built-in commit) rather than {@link #setElement}.
 */
public class ButtonGeneralPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField nameField;

  @FXML
  private TextField idField;

  private Button button;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    bindTextField(nameField, (el, value) -> button.setName(value.isEmpty() ? null : value));
  }

  public void setButton(@NonNull Button button) {
    this.button = button;
    setFieldValue(nameField, button.getName());
    idField.setText(button.getId());
  }

  /** Lets the owning dialog disable its OK button while the (required) Name field is blank. */
  public StringProperty nameProperty() {
    return nameField.textProperty();
  }
}
