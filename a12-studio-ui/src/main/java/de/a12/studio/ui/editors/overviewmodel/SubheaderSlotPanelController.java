package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.EventButtonLike;
import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.FilterElement;
import de.a12.studio.models.overviewmodel.MultiSelectionElement;
import de.a12.studio.models.overviewmodel.SearchElement;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
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
 * Edits one slot (left or right) of an {@link de.a12.studio.models.overviewmodel.OverviewModel}'s
 * {@code subHeaderBox}: a mixed list of {@link BoxElement}s - a {@link ButtonElement} configured like {@link
 * de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController}'s rows, or a position-only marker
 * ({@link SearchElement}, {@link FilterElement}, {@link MultiSelectionElement}) with no further configuration
 * - per the SME reference's Subheader documentation ("By clicking ADD ... create a respective action type:
 * Button, Search, Filter, or Multi-Selection"). Changing a row's Action Type replaces that list entry with a
 * freshly constructed instance of the corresponding subtype, since {@link BoxElement} is a sealed/polymorphic
 * hierarchy rather than one class with a mutable type flag. Reused for both Major (right slot) and Minor (left
 * slot) via {@link #configure}. Footer is Button-only, so it uses the simpler {@link
 * de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController} instead.
 */
public class SubheaderSlotPanelController extends AbstractPropertyEditor {

  private static final String TYPE_BUTTON = "Button";
  private static final String TYPE_SEARCH = "Search";
  private static final String TYPE_FILTER = "Filter";
  private static final String TYPE_MULTI_SELECTION = "Multi-Selection";
  private static final List<String> TYPE_OPTIONS = List.of(TYPE_BUTTON, TYPE_SEARCH, TYPE_FILTER, TYPE_MULTI_SELECTION);

  private static final List<String> PRIORITIES = List.of("PRIMARY", "SECONDARY");
  private static final String DEFAULT_PRIORITY = "SECONDARY";

  @FXML
  private GridPane rowsGrid;
  @FXML
  private Label emptyLabel;

  private List<BoxElement> rows;

  public void configure(@NonNull String title, @NonNull String settingsKeySuffix, @NonNull List<BoxElement> rows) {
    setTitle(title);
    setSettingsKeySuffix(settingsKeySuffix);
    this.rows = rows;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    rows.add(new ButtonElement());
    rebuildRows();
    commitHeaderChange();
  }

  private void rebuildRows() {
    rowsGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    boolean empty = rows.isEmpty();
    rowsGrid.setVisible(!empty);
    rowsGrid.setManaged(!empty);
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);

    for (int index = 0; index < rows.size(); index++) {
      addRow(rows.get(index), index, rows.size());
    }
  }

  private void addRow(BoxElement element, int index, int rowCount) {
    ComboBox<String> typeField = new ComboBox<>();
    typeField.setId("subheaderSlotType-" + index);
    typeField.setMaxWidth(Double.MAX_VALUE);
    typeField.getItems().setAll(TYPE_OPTIONS);
    setFieldValue(typeField, displayNameFor(element));
    bindComboBox(typeField, (el, value) -> changeRowType(element, value));

    boolean isButton = element instanceof EventButtonLike;
    EventButtonLike button = isButton ? (EventButtonLike) element : null;

    TextField eventField = new TextField();
    eventField.setId("subheaderSlotEvent-" + index);
    eventField.setMaxWidth(Double.MAX_VALUE);
    eventField.setDisable(!isButton);
    if (isButton) {
      setFieldValue(eventField, button.getEvent());
      bindTextField(eventField, (el, value) -> button.setEvent(value.isEmpty() ? null : value));
    }

    ComboBox<String> priorityField = new ComboBox<>();
    priorityField.setId("subheaderSlotPriority-" + index);
    priorityField.setMaxWidth(Double.MAX_VALUE);
    priorityField.setDisable(!isButton);
    if (isButton) {
      priorityField.getItems().setAll(PRIORITIES);
      setFieldValue(priorityField, Boolean.TRUE.equals(button.getPrimary()) ? "PRIMARY" : DEFAULT_PRIORITY);
      bindComboBox(priorityField, (el, value) -> button.setPrimary("PRIMARY".equals(value)));
    }

    CheckBox destructiveField = new CheckBox();
    destructiveField.setId("subheaderSlotDestructive-" + index);
    destructiveField.setDisable(!isButton);
    if (isButton) {
      setFieldValue(destructiveField, Boolean.TRUE.equals(button.getDestructive()));
      bindCheckBox(destructiveField, (el, value) -> button.setDestructive(value ? Boolean.TRUE : null));
    }

    TextField iconField = new TextField();
    iconField.setId("subheaderSlotIcon-" + index);
    iconField.setMaxWidth(Double.MAX_VALUE);
    iconField.setDisable(!isButton);
    if (isButton) {
      setFieldValue(iconField, button.getIconName());
      bindTextField(iconField, (el, value) -> button.setIconName(value.isEmpty() ? null : value));
    }

    rowsGrid.addRow(index + 1, typeField, eventField, priorityField, destructiveField, iconField, createActionsBox(element, index, rowCount));
  }

  private void changeRowType(BoxElement oldElement, String typeDisplayName) {
    int index = rows.indexOf(oldElement);
    if (index < 0) {
      return;
    }
    BoxElement newElement = switch (typeDisplayName) {
      case TYPE_SEARCH -> new SearchElement();
      case TYPE_FILTER -> new FilterElement();
      case TYPE_MULTI_SELECTION -> new MultiSelectionElement();
      default -> new ButtonElement();
    };
    rows.set(index, newElement);
    rebuildRows();
  }

  private static String displayNameFor(BoxElement element) {
    if (element instanceof SearchElement) {
      return TYPE_SEARCH;
    }
    if (element instanceof FilterElement) {
      return TYPE_FILTER;
    }
    if (element instanceof MultiSelectionElement) {
      return TYPE_MULTI_SELECTION;
    }
    return TYPE_BUTTON;
  }

  private HBox createActionsBox(BoxElement element, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this entry?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        rows.remove(element);
        rebuildRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(rows, fromIndex, toIndex);
    rebuildRows();
    commitHeaderChange();
  }
}
