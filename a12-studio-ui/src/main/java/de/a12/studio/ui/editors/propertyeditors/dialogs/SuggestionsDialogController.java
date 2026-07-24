package de.a12.studio.ui.editors.propertyeditors.dialogs;

import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.Icons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Modal dialog for editing a single locale's list of {@link de.a12.studio.models.documentmodel.StringTypeOptions}
 * suggestion text items: add, edit and remove entries. Row structure is rebuilt from scratch on every
 * structural change, matching {@link de.a12.studio.ui.editors.propertyeditors.DataTypeEnumerationConfigurationPanelController}.
 * Changes are only applied to the underlying model by the caller once {@link #getResult()} is {@link ButtonType#OK}.
 */
public class SuggestionsDialogController implements DialogController {

  @FXML
  private VBox itemRows;

  @FXML
  private Label emptyLabel;

  @FXML
  private Button okButton;

  private Stage stage;

  private final List<String> values = new ArrayList<>();

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  public void initDialog(Stage stage, List<String> initialValues) {
    this.stage = stage;
    values.clear();
    values.addAll(initialValues);
    rebuildRows();
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

  public Optional<ButtonType> getResult() {
    return result;
  }

  /**
   * The edited values, with blank entries dropped so an item left empty by the user doesn't get persisted.
   */
  public List<String> getValues() {
    return values.stream().filter(value -> value != null && !value.isBlank()).toList();
  }

  @FXML
  private void onAddItem() {
    values.add("");
    rebuildRows();
    Platform.runLater(() -> itemRows.getChildren().get(itemRows.getChildren().size() - 1).lookup(".text-field").requestFocus());
  }

  private void rebuildRows() {
    itemRows.getChildren().clear();

    boolean empty = values.isEmpty();
    itemRows.setVisible(!empty);
    itemRows.setManaged(!empty);
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);

    for (int index = 0; index < values.size(); index++) {
      itemRows.getChildren().add(createRow(index));
    }
  }

  private HBox createRow(int index) {
    TextField textField = new TextField(values.get(index));
    textField.setId("suggestion-" + index);
    textField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(textField, Priority.ALWAYS);
    textField.textProperty().addListener((observable, oldValue, newValue) -> values.set(index, newValue));

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      values.remove(index);
      rebuildRows();
    });

    HBox row = new HBox(8.0, textField, deleteButton);
    row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    return row;
  }

  private static Button createActionButton(String iconLiteral, String tooltip, Runnable action) {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(16);
    icon.getStyleClass().add("toolbar-icon");

    Button button = new Button();
    button.getStyleClass().add("default-button");
    button.setGraphic(icon);
    button.setTooltip(new Tooltip(tooltip));
    button.setOnAction(event -> action.run());
    return button;
  }
}
