package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.applicationmodel.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Edits a {@link Scene}'s {@link Case} list: a non-inline-editable row list (row = Name summary, edited via a
 * modal dialog), matching the SME reference's "Cases" table. Follows the same row-list pattern as {@link
 * SceneChangePanelController}'s onEnter/onExit tables. Isn't wired through {@link
 * de.a12.studio.ui.editors.AbstractPropertyEditor} for the same reason as {@link
 * MatchConditionsPanelController}.
 */
public class CasesPanelController {

  @FXML
  private VBox rows;

  @FXML
  private Label emptyLabel;

  private Scene scene;

  private Runnable onChange = () -> {
  };

  public void setScene(@NonNull Scene scene) {
    this.scene = scene;
    rebuildRows();
  }

  /**
   * Invoked after every add/remove/reorder/edit, so the owning Scene dialog can keep its Default Case
   * dropdown's items in sync with the current case names.
   */
  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  @FXML
  private void onAdd() {
    Dialogs.showCaseForAdd(Studio.stage).ifPresent(newCase -> {
      scene.getCases().add(newCase);
      rebuildRows();
      onChange.run();
    });
  }

  private void rebuildRows() {
    rows.getChildren().clear();

    List<Case> cases = scene.getCases();
    boolean empty = cases.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);

    for (int index = 0; index < cases.size(); index++) {
      rows.getChildren().add(createRow(cases.get(index), index, cases.size()));
    }
  }

  private HBox createRow(Case caseObj, int index, int rowCount) {
    Label nameLabel = new Label(caseObj.getName() == null ? "" : caseObj.getName());
    nameLabel.setId("case-" + index);
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);

    HBox row = new HBox(10.0, nameLabel, createActionsBox(caseObj, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    nameLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        onEditCase(caseObj);
      }
    });
    return row;
  }

  private HBox createActionsBox(Case caseObj, int index, int rowCount) {
    VBox moveButtonsBox = createMoveButtonsBox(index, rowCount);

    Button editButton = createActionButton(Icons.PENCIL, "Edit", () -> onEditCase(caseObj));
    Button copyButton = createActionButton(Icons.COPY, "Duplicate", () -> {
      scene.getCases().add(index + 1, cloneCase(caseObj));
      rebuildRows();
      onChange.run();
    });
    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> onDeleteCase(caseObj));

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void onEditCase(Case caseObj) {
    if (Dialogs.showCaseForEdit(Studio.stage, caseObj)) {
      rebuildRows();
      onChange.run();
    }
  }

  private void onDeleteCase(Case caseObj) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this case?", null, null, "Delete");
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }
    scene.getCases().remove(caseObj);
    rebuildRows();
    onChange.run();
  }

  private VBox createMoveButtonsBox(int index, int rowCount) {
    Button moveUpButton = createActionButton(Icons.ARROW_UP, "Move Up", () -> moveCase(index, index - 1));
    moveUpButton.setDisable(index == 0);
    moveUpButton.getStyleClass().addAll("move-button", "move-button-top");

    Button moveDownButton = createActionButton(Icons.ARROW_DOWN, "Move Down", () -> moveCase(index, index + 1));
    moveDownButton.setDisable(index == rowCount - 1);
    moveDownButton.getStyleClass().addAll("move-button", "move-button-bottom");

    return new VBox(1, moveUpButton, moveDownButton);
  }

  private void moveCase(int fromIndex, int toIndex) {
    Collections.swap(scene.getCases(), fromIndex, toIndex);
    rebuildRows();
    onChange.run();
  }

  private static Case cloneCase(@NonNull Case caseObj) {
    String json = JsonSettings.objectMapper.writeValueAsString(caseObj);
    return JsonSettings.objectMapper.readValue(json, Case.class);
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
