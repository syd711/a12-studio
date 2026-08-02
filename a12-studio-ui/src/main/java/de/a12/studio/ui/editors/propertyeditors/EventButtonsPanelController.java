package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.formmodel.ButtonGroup;
import de.a12.studio.models.formmodel.ButtonStyling;
import de.a12.studio.models.formmodel.EventButton;
import de.a12.studio.models.formmodel.Icon;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
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
 * Edits a {@link ButtonGroup}'s {@link EventButton} entries (e.g. a {@code HeaderFooterBox}'s major or minor
 * buttons): a compact, fully inline-editable table of Event/Priority/Destructive/Icon, matching the SME
 * reference's "Major Buttons"/"Minor Buttons" tables. Reusable for either list by calling {@link
 * #setButtonGroup} with the corresponding group and {@link #setTitle} with the desired heading. Entries in
 * the underlying {@link ButtonGroup#getButton()} that aren't an {@link EventButton} (e.g. a NavigationButton)
 * are left untouched but not rendered, since this panel only ever adds/edits event buttons.
 */
public class EventButtonsPanelController extends AbstractPropertyEditor {

  private static final List<String> PRIORITIES = List.of("PRIMARY", "SECONDARY");
  private static final String DEFAULT_PRIORITY = "SECONDARY";

  @FXML
  private GridPane buttonsGrid;

  @FXML
  private Label emptyLabel;

  private ButtonGroup buttonGroup;

  public void setButtonGroup(@NonNull ButtonGroup buttonGroup) {
    this.buttonGroup = buttonGroup;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    EventButton eventButton = new EventButton();
    eventButton.setButtonStyling(newButtonStyling());
    getButtons().add(eventButton);
    rebuildRows();
    commitChange();
  }

  private List<de.a12.studio.models.formmodel.Button> getButtons() {
    return buttonGroup.getButton();
  }

  private void rebuildRows() {
    buttonsGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    List<de.a12.studio.models.formmodel.Button> buttons = getButtons();
    boolean empty = buttons.isEmpty();
    buttonsGrid.setVisible(!empty);
    buttonsGrid.setManaged(!empty);
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);

    for (int index = 0; index < buttons.size(); index++) {
      if (buttons.get(index) instanceof EventButton eventButton) {
        addRow(eventButton, index, buttons.size());
      }
    }
  }

  private void addRow(EventButton eventButton, int index, int rowCount) {
    ButtonStyling styling = getOrCreateButtonStyling(eventButton);

    TextField eventField = new TextField();
    eventField.setId("eventButtonEvent-" + index);
    eventField.setMaxWidth(Double.MAX_VALUE);
    setFieldValue(eventField, eventButton.getEvent());
    bindTextField(eventField, (el, value) -> eventButton.setEvent(value.isEmpty() ? null : value));

    ComboBox<String> priorityField = new ComboBox<>();
    priorityField.setId("eventButtonPriority-" + index);
    priorityField.setMaxWidth(Double.MAX_VALUE);
    priorityField.getItems().setAll(PRIORITIES);
    setFieldValue(priorityField, styling.getPriority());
    bindComboBox(priorityField, (el, value) -> styling.setPriority(value));

    CheckBox destructiveField = new CheckBox();
    destructiveField.setId("eventButtonDestructive-" + index);
    setFieldValue(destructiveField, Boolean.TRUE.equals(styling.getDestructive()));
    bindCheckBox(destructiveField, (el, value) -> styling.setDestructive(value ? Boolean.TRUE : null));

    TextField iconField = new TextField();
    iconField.setId("eventButtonIcon-" + index);
    iconField.setMaxWidth(Double.MAX_VALUE);
    setFieldValue(iconField, styling.getIcon() != null ? styling.getIcon().getName() : null);
    bindTextField(iconField, (el, value) -> setIconName(styling, value));

    buttonsGrid.addRow(index + 1, eventField, priorityField, destructiveField, iconField, createActionsBox(eventButton, index, rowCount));
  }

  private static void setIconName(ButtonStyling styling, String value) {
    if (value == null || value.isEmpty()) {
      styling.setIcon(null);
      return;
    }
    Icon icon = styling.getIcon();
    if (icon == null) {
      icon = new Icon();
      styling.setIcon(icon);
    }
    icon.setName(value);
  }

  private static ButtonStyling getOrCreateButtonStyling(EventButton eventButton) {
    ButtonStyling styling = eventButton.getButtonStyling();
    if (styling == null) {
      styling = newButtonStyling();
      eventButton.setButtonStyling(styling);
    }
    return styling;
  }

  private static ButtonStyling newButtonStyling() {
    ButtonStyling styling = new ButtonStyling();
    styling.setPriority(DEFAULT_PRIORITY);
    return styling;
  }

  private HBox createActionsBox(EventButton eventButton, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this button?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getButtons().remove(eventButton);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getButtons(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }
}
