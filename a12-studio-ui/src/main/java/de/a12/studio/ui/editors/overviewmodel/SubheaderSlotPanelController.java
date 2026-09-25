package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.BoxElementType;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.ExpandAllPopupElement;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Edits one slot (left or right) of an {@link de.a12.studio.models.overviewmodel.OverviewModel}'s
 * {@code subHeaderBox}: a mixed list of {@link BoxElement}s - a {@link ButtonElement} configured like {@link
 * de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController}'s rows, or a {@link SearchElement},
 * {@link FilterElement} or {@link MultiSelectionElement} - per the SME reference's Subheader documentation
 * ("By clicking ADD ... create a respective action type: Button, Search, Filter, or Multi-Selection"). The
 * Action Type is chosen once, from {@link #addButton}'s menu, when a row is created - it isn't editable
 * afterward, so the Action Type column is a plain label. Every row is editable, as in SME (which gives all four
 * types the same fields): a Button through its full dialog, the other three through the same dialog without the
 * button-only Event/Confirmation/Priority/Icon block, so they still carry a label, description, styles and
 * annotations. Only an unrecognized (generic) element type has nothing to edit. Reused for both Major (right slot) and Minor (left slot) via {@link #configure}. Footer
 * is Button-only, so it uses the simpler {@link de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController}
 * instead. The Tree Model's Subheader reuses this panel with its own type set (Button, Multi-Selection, Expand All
 * PopUp), see {@link #configure(String, String, List, List)}.
 */
public class SubheaderSlotPanelController extends AbstractPropertyEditor {

  private static final String DEFAULT_PRIORITY = "SECONDARY";

  /** The Overview Model's Subheader element types, in the order of the Add menu. */
  public static final List<BoxElementType> OVERVIEW_TYPES =
      List.of(BoxElementType.BUTTON, BoxElementType.SEARCH, BoxElementType.FILTER, BoxElementType.MULTI_SELECTION);

  /** The Tree Model's Subheader element types, in the order of the Add menu. */
  public static final List<BoxElementType> TREE_TYPES =
      List.of(BoxElementType.BUTTON, BoxElementType.MULTI_SELECTION, BoxElementType.EXPAND_ALL_POPUP);

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

  // Lets the owning editor initialize a freshly created element, e.g. the Tree Model gives its buttons an id.
  private Consumer<BoxElement> onElementCreated = element -> {
  };

  private List<BoxElementType> availableTypes = OVERVIEW_TYPES;

  public void configure(@NonNull String title, @NonNull String settingsKeySuffix, @NonNull List<BoxElement> rows) {
    configure(title, settingsKeySuffix, rows, OVERVIEW_TYPES);
  }

  /** Like {@link #configure(String, String, List)}, but the Add menu only offers {@code availableTypes}. */
  public void configure(@NonNull String title, @NonNull String settingsKeySuffix, @NonNull List<BoxElement> rows,
      @NonNull List<BoxElementType> availableTypes) {
    setTitle(title);
    setSettingsKeySuffix(settingsKeySuffix);
    this.rows = rows;
    this.availableTypes = availableTypes;
    initAddMenu();
    rebuildRows();
  }

  public void setOnElementCreated(@NonNull Consumer<BoxElement> onElementCreated) {
    this.onElementCreated = onElementCreated;
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  // Rebuilt (via setAll, so re-running configure() on the same instance doesn't duplicate items) instead of
  // declared in FXML because each item's action needs to close over the specific element type it creates.
  private void initAddMenu() {
    addButton.getItems().setAll(availableTypes.stream()
        .map(type -> createAddMenuItem(displayNameFor(type), () -> newElement(type)))
        .toList());
  }

  private static BoxElement newElement(BoxElementType type) {
    return switch (type) {
      case SEARCH -> new SearchElement();
      case FILTER -> new FilterElement();
      case MULTI_SELECTION -> new MultiSelectionElement();
      case EXPAND_ALL_POPUP -> new ExpandAllPopupElement();
      default -> new ButtonElement();
    };
  }

  private MenuItem createAddMenuItem(String label, Supplier<BoxElement> factory) {
    MenuItem item = new MenuItem(label);
    item.setOnAction(event -> {
      BoxElement element = factory.get();
      onElementCreated.accept(element);
      rows.add(element);
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

    boolean isButton = element instanceof ButtonElement;
    OverviewButtonLike editable = element instanceof OverviewButtonLike configurable ? configurable : null;
    OverviewButtonLike button = isButton ? editable : null;

    Label eventLabel = new Label(isButton ? button.getEvent() : "");
    eventLabel.setId("subheaderSlotEvent-" + index);
    eventLabel.setMaxWidth(Double.MAX_VALUE);

    Label priorityLabel = new Label(isButton ? (Boolean.TRUE.equals(button.getPrimary()) ? "PRIMARY" : DEFAULT_PRIORITY) : "");
    priorityLabel.setId("subheaderSlotPriority-" + index);

    Label destructiveLabel = new Label(isButton && Boolean.TRUE.equals(button.getDestructive()) ? "✓" : "");
    destructiveLabel.setId("subheaderSlotDestructive-" + index);

    Label iconLabel = new Label(isButton ? button.getIconName() : "");
    iconLabel.setId("subheaderSlotIcon-" + index);

    if (editable != null) {
      for (Label cell : List.of(typeLabel, eventLabel, priorityLabel, destructiveLabel, iconLabel)) {
        makeClickableToEdit(cell, editable);
      }
    }

    rowsGrid.addRow(index + 1, typeLabel, eventLabel, priorityLabel, destructiveLabel, iconLabel, createActionsBox(element, editable, index, rowCount));
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
    return displayNameFor(element.getType());
  }

  private static String displayNameFor(BoxElementType type) {
    return switch (type) {
      case SEARCH -> StudioBundle.get("subheader_slot.type_search");
      case FILTER -> StudioBundle.get("subheader_slot.type_filter");
      case MULTI_SELECTION -> StudioBundle.get("subheader_slot.type_multi_selection");
      case EXPAND_ALL_POPUP -> StudioBundle.get("subheader_slot.type_expand_all_popup");
      default -> StudioBundle.get("subheader_slot.type_button");
    };
  }

  private HBox createActionsBox(BoxElement element, OverviewButtonLike editable, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_entry"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        rows.remove(element);
        rebuildRows();
        notifyChanged();
      }
    });

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(editable));
    editButton.setDisable(editable == null);

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
