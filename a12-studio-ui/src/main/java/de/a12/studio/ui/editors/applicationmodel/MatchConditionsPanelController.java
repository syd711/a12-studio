package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.applicationmodel.MatchCondition;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Edits a {@link de.a12.studio.models.applicationmodel.Scene}'s {@link MatchCondition} list: a compact,
 * inline-editable table of Key/Must Equal/Is Set, matching the SME reference's "Match Conditions" table.
 * Follows the same GridPane-row pattern as {@link de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController} and {@link
 * ActivityPanelController}, but isn't wired through {@link de.a12.studio.ui.editors.AbstractPropertyEditor}
 * since it only ever edits a plain application-model POJO embedded in the Add/Edit Scene dialog, not a
 * document-model {@link de.a12.studio.models.documentmodel.Element}.
 */
public class MatchConditionsPanelController {

  private static final List<String> SUGGESTED_KEYS = List.of("engine", "module", "instance", "model", "linkForm", "name");

  private static final List<String> IS_SET_VALUES = List.of("true", "false");

  @FXML
  private GridPane grid;

  @FXML
  private Label emptyLabel;

  private List<MatchCondition> matchConditions;

  private Runnable onChange = () -> {
  };

  public void setMatchConditions(@NonNull List<MatchCondition> matchConditions) {
    this.matchConditions = matchConditions;
    rebuildRows();
  }

  /**
   * Invoked after every add/remove/reorder/edit, so the owning Scene dialog can re-run its own validation
   * (e.g. "at least one match condition is required").
   */
  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  @FXML
  private void onAdd() {
    matchConditions.add(new MatchCondition());
    rebuildRows();
    onChange.run();
  }

  private void rebuildRows() {
    grid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    boolean empty = matchConditions.isEmpty();
    grid.setVisible(!empty);
    grid.setManaged(!empty);
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);

    for (int index = 0; index < matchConditions.size(); index++) {
      addRow(matchConditions.get(index), index, matchConditions.size());
    }
  }

  private void addRow(MatchCondition matchCondition, int index, int rowCount) {
    ComboBox<String> keyField = new ComboBox<>();
    keyField.setId("matchConditionKey-" + index);
    keyField.setEditable(true);
    keyField.setMaxWidth(Double.MAX_VALUE);
    keyField.getItems().setAll(SUGGESTED_KEYS);
    keyField.setValue(matchCondition.getKey());
    keyField.valueProperty().addListener((observable, oldValue, newValue) -> {
      matchCondition.setKey(newValue);
      onChange.run();
    });

    TextField mustEqualField = new TextField(matchCondition.getMustEqual());
    mustEqualField.setId("matchConditionMustEqual-" + index);
    mustEqualField.setMaxWidth(Double.MAX_VALUE);
    mustEqualField.textProperty().addListener((observable, oldValue, newValue) -> {
      matchCondition.setMustEqual(newValue.isEmpty() ? null : newValue);
      onChange.run();
    });

    ComboBox<String> isSetField = new ComboBox<>();
    isSetField.setId("matchConditionIsSet-" + index);
    isSetField.setMaxWidth(Double.MAX_VALUE);
    isSetField.getItems().setAll(IS_SET_VALUES);
    isSetField.setValue(matchCondition.getIsSet() != null ? matchCondition.getIsSet().toString() : null);
    isSetField.valueProperty().addListener((observable, oldValue, newValue) -> {
      matchCondition.setIsSet(newValue == null || newValue.isEmpty() ? null : Boolean.valueOf(newValue));
      onChange.run();
    });

    grid.addRow(index + 1, keyField, mustEqualField, isSetField, createActionsBox(matchCondition, index, rowCount));
  }

  private HBox createActionsBox(MatchCondition matchCondition, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button copyButton = RowFactory.createActionButton(Icons.COPY, "Duplicate", () -> {
      MatchCondition copy = new MatchCondition();
      copy.setKey(matchCondition.getKey());
      copy.setMustEqual(matchCondition.getMustEqual());
      copy.setIsSet(matchCondition.getIsSet());
      matchConditions.add(index + 1, copy);
      rebuildRows();
      onChange.run();
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this match condition?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        matchConditions.remove(matchCondition);
        rebuildRows();
        onChange.run();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(matchConditions, fromIndex, toIndex);
    rebuildRows();
    onChange.run();
  }
}
