package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.FilterElement;
import de.a12.studio.models.overviewmodel.MultiSelectionElement;
import de.a12.studio.models.overviewmodel.OverviewButtonLike;
import de.a12.studio.models.overviewmodel.SearchElement;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.editors.propertyeditors.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import de.a12.studio.ui.util.StudioBundle;

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

  private static final String DEFAULT_PRIORITY = "SECONDARY";

  @FXML
  private GridPane rowsGrid;
  @FXML
  private Label emptyLabel;

  private List<BoxElement> rows;

  // Notified after every structural change (add/reorder/delete/type change), so the owning editor can keep
  // sibling panels whose validation derives from this list (e.g. the Multi-Selection panel's "exactly one
  // Multi-Selection element" check) in sync.
  private Runnable onChange = () -> {
  };

  public void configure(@NonNull String title, @NonNull String settingsKeySuffix, @NonNull List<BoxElement> rows) {
    setTitle(title);
    setSettingsKeySuffix(settingsKeySuffix);
    this.rows = rows;
    rebuildRows();
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  @FXML
  private void onAdd() {
    rows.add(new ButtonElement());
    rebuildRows();
    notifyChanged();
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

    boolean isButton = element instanceof OverviewButtonLike;
    OverviewButtonLike button = isButton ? (OverviewButtonLike) element : null;

    Label eventLabel = new Label(isButton ? button.getEvent() : "");
    eventLabel.setId("subheaderSlotEvent-" + index);
    eventLabel.setMaxWidth(Double.MAX_VALUE);

    Label priorityLabel = new Label(isButton ? (Boolean.TRUE.equals(button.getPrimary()) ? "PRIMARY" : DEFAULT_PRIORITY) : "");
    priorityLabel.setId("subheaderSlotPriority-" + index);

    Label destructiveLabel = new Label(isButton && Boolean.TRUE.equals(button.getDestructive()) ? "✓" : "");
    destructiveLabel.setId("subheaderSlotDestructive-" + index);

    Label iconLabel = new Label(isButton ? button.getIconName() : "");
    iconLabel.setId("subheaderSlotIcon-" + index);

    if (isButton) {
      makeClickableToEdit(eventLabel, button);
      makeClickableToEdit(priorityLabel, button);
      makeClickableToEdit(destructiveLabel, button);
      makeClickableToEdit(iconLabel, button);
    }

    rowsGrid.addRow(index + 1, typeField, eventLabel, priorityLabel, destructiveLabel, iconLabel, createActionsBox(element, button, index, rowCount));
  }

  private void makeClickableToEdit(Label label, OverviewButtonLike button) {
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(button);
      }
    });
  }

  private void openEditDialog(OverviewButtonLike button) {
    Dialogs.showEventButtonForEdit(Studio.stage, button).ifPresent(edited -> {
      int index = rows.indexOf(button);
      if (index >= 0) {
        rows.set(index, (BoxElement) edited);
        rebuildRows();
        notifyChanged();
      }
    });
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
    onChange.run();
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

  private HBox createActionsBox(BoxElement element, OverviewButtonLike button, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_entry"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        rows.remove(element);
        rebuildRows();
        notifyChanged();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox);
    if (button != null) {
      Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(button));
      actionsBox.getChildren().add(editButton);
    }
    actionsBox.getChildren().add(deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(rows, fromIndex, toIndex);
    rebuildRows();
    notifyChanged();
  }

  private void notifyChanged() {
    commitHeaderChange();
    onChange.run();
  }
}
