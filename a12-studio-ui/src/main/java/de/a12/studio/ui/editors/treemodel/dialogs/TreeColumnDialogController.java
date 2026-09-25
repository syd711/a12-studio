package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.ui.components.DialogController;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Add/edit dialog for a single {@link TreeColumn}, opened from {@link
 * de.a12.studio.ui.editors.treemodel.TreeColumnsPanelController} by clicking a column row or its Add button.
 * Edits Name, Width, Fixed Width and Pin Direction - the fields the row list already surfaces. Always builds
 * a brand new {@link TreeColumn} instance on OK (see {@link #getResult()}) rather than mutating the one passed
 * to {@link #init}, mirroring {@link de.a12.studio.ui.editors.applicationmodel.dialogs.SubregionDialogController};
 * the caller is responsible for copying its fields onto the existing column (edit, preserving its own {@code id})
 * or appending it with a freshly generated id (add).
 */
public class TreeColumnDialogController implements DialogController {

  private static final List<String> PIN_DIRECTIONS = Arrays.asList(null, "left", "right");

  @FXML
  private TextField nameField;

  @FXML
  private TextField widthField;

  @FXML
  private CheckBox fixedWidthField;

  @FXML
  private ComboBox<String> pinDirectionCombo;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private TreeColumn built;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    pinDirectionCombo.setItems(FXCollections.observableArrayList(PIN_DIRECTIONS));
    pinDirectionCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null ? "(None)" : capitalize(value);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });

    okButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> isBlank(nameField.getText()) || parseWidth(widthField.getText()) == null,
        nameField.textProperty(), widthField.textProperty()));
    nameField.requestFocus();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    TreeColumn column = new TreeColumn();
    column.setName(nameField.getText().trim());
    column.setWidth(parseWidth(widthField.getText()));
    column.setFixedWidth(fixedWidthField.isSelected() ? Boolean.TRUE : null);
    column.setPinDirection(pinDirectionCombo.getValue());
    built = column;
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  void init(Stage stage, TreeColumn existing) {
    this.stage = stage;
    nameField.setText(existing != null ? existing.getName() : "");
    widthField.setText(existing != null && existing.getWidth() != null ? String.valueOf(existing.getWidth()) : "1");
    fixedWidthField.setSelected(existing != null && Boolean.TRUE.equals(existing.getFixedWidth()));
    pinDirectionCombo.setValue(existing != null ? existing.getPinDirection() : null);
  }

  Optional<TreeColumn> getResult() {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      return Optional.ofNullable(built);
    }
    return Optional.empty();
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static String capitalize(String value) {
    return value == null || value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
  }

  private static Integer parseWidth(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      return Integer.valueOf(text.trim());
    }
    catch (NumberFormatException e) {
      return null;
    }
  }
}
