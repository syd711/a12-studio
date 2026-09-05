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
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Edits one slot (left or right) of an {@link de.a12.studio.models.overviewmodel.OverviewModel}'s
 * {@code subHeaderBox}: a mixed list of {@link BoxElement}s - a {@link ButtonElement} configured like {@link
 * de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController}'s rows, or a position-only marker
 * ({@link SearchElement}, {@link FilterElement}, {@link MultiSelectionElement}) with no further configuration
 * - per the SME reference's Subheader documentation ("By clicking ADD ... create a respective action type:
 * Button, Search, Filter, or Multi-Selection"). The Action Type is chosen once, from {@link #addButton}'s
 * menu, when a row is created - it isn't editable afterward, so the Action Type column is a plain label like
 * every other column. Reused for both Major (right slot) and Minor (left slot) via {@link #configure}. Footer
 * is Button-only, so it uses the simpler {@link de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController}
 * instead.
 */
public class SubheaderSlotPanelController extends AbstractPropertyEditor {

  private static final String DEFAULT_PRIORITY = "SECONDARY";

  @FXML
  private GridPane rowsGrid;
  @FXML
  private Label emptyLabel;
  @FXML
  private MenuButton addButton;

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
    initAddMenu();
    rebuildRows();
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  // Rebuilt (via setAll, so re-running configure() on the same instance doesn't duplicate items) instead of
  // declared in FXML because each item's action needs to close over the specific element type it creates.
  private void initAddMenu() {
    addButton.getItems().setAll(
        createAddMenuItem(StudioBundle.get("subheader_slot.type_button"), ButtonElement::new),
        createAddMenuItem(StudioBundle.get("subheader_slot.type_search"), SearchElement::new),
        createAddMenuItem(StudioBundle.get("subheader_slot.type_filter"), FilterElement::new),
        createAddMenuItem(StudioBundle.get("subheader_slot.type_multi_selection"), MultiSelectionElement::new));
  }

  private MenuItem createAddMenuItem(String label, Supplier<BoxElement> factory) {
    MenuItem item = new MenuItem(label);
    item.setOnAction(event -> {
      rows.add(factory.get());
      rebuildRows();
      notifyChanged();
    });
    return item;
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
    Label typeLabel = new Label(displayNameFor(element));
    typeLabel.setId("subheaderSlotType-" + index);
    typeLabel.setMaxWidth(Double.MAX_VALUE);

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

    rowsGrid.addRow(index + 1, typeLabel, eventLabel, priorityLabel, destructiveLabel, iconLabel, createActionsBox(element, button, index, rowCount));
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

  private static String displayNameFor(BoxElement element) {
    if (element instanceof SearchElement) {
      return StudioBundle.get("subheader_slot.type_search");
    }
    if (element instanceof FilterElement) {
      return StudioBundle.get("subheader_slot.type_filter");
    }
    if (element instanceof MultiSelectionElement) {
      return StudioBundle.get("subheader_slot.type_multi_selection");
    }
    return StudioBundle.get("subheader_slot.type_button");
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

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(button));
    editButton.setDisable(button == null);

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
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
