package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Modal dialog for {@link de.a12.studio.ui.editors.documentmodel.DocumentModelActions}' "Create Overview Model
 * from Selection" context menu entry: prompts for the new model's name and lets the user deselect any of the
 * fields gathered from the tree selection before they become the new Overview Model's columns, one per checked
 * field, in the same order they were gathered (a Group's fields expanded recursively, see {@code
 * DocumentModelActions#collectFields}).
 */
public class CreateOverviewModelDialogController implements DialogController {

  /** One checkbox row: {@code id} is the Document Model element id that becomes the resulting {@code Column}'s
   * {@code elementRef}, {@code label} is its display path shown next to the checkbox. */
  public record FieldOption(String id, String label) {
  }

  public record Result(String name, List<String> selectedFieldIds) {
  }

  @FXML
  private TextField nameField;

  @FXML
  private Label pathLabel;

  @FXML
  private VBox fieldsBox;

  @FXML
  private Label fieldsEmptyLabel;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private List<FieldOption> fields = List.of();

  private final List<CheckBox> fieldCheckBoxes = new ArrayList<>();

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    nameField.textProperty().addListener((observable, oldValue, newValue) -> validate());
    nameField.requestFocus();
  }

  void init(Stage stage, @NonNull ProjectItem targetFolder, @NonNull List<FieldOption> fields, @NonNull String defaultName) {
    this.stage = stage;
    this.fields = fields;

    pathLabel.setText(targetFolder.getPath());
    pathLabel.setTooltip(WidgetFactory.createTooltip(targetFolder.getPath()));
    nameField.setText(defaultName);

    fieldsBox.getChildren().clear();
    fieldCheckBoxes.clear();
    for (FieldOption field : fields) {
      CheckBox checkBox = new CheckBox(field.label());
      checkBox.setSelected(true);
      checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> validate());
      fieldsBox.getChildren().add(checkBox);
      fieldCheckBoxes.add(checkBox);
    }
    boolean empty = fields.isEmpty();
    fieldsEmptyLabel.setVisible(empty);
    fieldsEmptyLabel.setManaged(empty);
    fieldsBox.setVisible(!empty);
    fieldsBox.setManaged(!empty);

    validate();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  Optional<Result> getResult() {
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return Optional.empty();
    }
    String name = nameField.getText();
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }

    List<String> selectedIds = new ArrayList<>();
    for (int i = 0; i < fields.size(); i++) {
      if (fieldCheckBoxes.get(i).isSelected()) {
        selectedIds.add(fields.get(i).id());
      }
    }
    return Optional.of(new Result(name.trim(), selectedIds));
  }

  private void validate() {
    boolean anySelected = fieldCheckBoxes.stream().anyMatch(CheckBox::isSelected);
    okButton.setDisable(!FileUtils.isValidWindowsFilename(nameField.getText()) || !anySelected);
  }
}
