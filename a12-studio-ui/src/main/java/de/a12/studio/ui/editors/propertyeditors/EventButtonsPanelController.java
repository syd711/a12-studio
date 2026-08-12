package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.EventButtonLike;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
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
import java.util.function.Supplier;

/**
 * Edits a list of {@link EventButtonLike} rows (e.g. {@code content.rowActionGroup.actions} or a footer/
 * subheader box's button entries) as a compact, fully inline-editable table of Event/Priority/Destructive/Icon,
 * matching the SME reference's "Row Action"/"Major Buttons"/"Minor Buttons" tables. Reusable for any such list
 * by calling {@link #configure} with the list, a title, a settings-key suffix (so several instances of this
 * panel on the same editor don't collide on the same persisted expanded/collapsed state) and a factory for new
 * rows. Not tied to a single {@code de.a12.studio.models.documentmodel.Element}, so it follows the
 * model-header pattern ({@link #commitHeaderChange()}) rather than {@link #commitChange()}.
 */
public class EventButtonsPanelController extends AbstractPropertyEditor {

  private static final List<String> PRIORITIES = List.of("PRIMARY", "SECONDARY");
  private static final String DEFAULT_PRIORITY = "SECONDARY";

  @FXML
  private GridPane buttonsGrid;

  @FXML
  private Label emptyLabel;

  private List<EventButtonLike> rows;
  private Supplier<EventButtonLike> newRowFactory;

  /**
   * Binds this panel to {@code rows}. {@code rows} doesn't need to be declared as {@code List<EventButtonLike>}
   * itself (e.g. a footer box's {@code List<BoxElement>}, whose entries this panel exclusively populates via
   * {@code newRowFactory} and therefore knows are always the {@link EventButtonLike}-implementing subtype) -
   * the erased list reference is shared, so add/remove/move here mutate the caller's real list in place.
   */
  @SuppressWarnings("unchecked")
  public <T> void configure(@NonNull String title, @NonNull String settingsKeySuffix, @NonNull List<T> rows,
      @NonNull Supplier<T> newRowFactory) {
    setTitle(title);
    setSettingsKeySuffix(settingsKeySuffix);
    this.rows = (List<EventButtonLike>) rows;
    this.newRowFactory = () -> (EventButtonLike) newRowFactory.get();
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    rows.add(newRowFactory.get());
    rebuildRows();
    commitHeaderChange();
  }

  private void rebuildRows() {
    buttonsGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    boolean empty = rows.isEmpty();
    buttonsGrid.setVisible(!empty);
    buttonsGrid.setManaged(!empty);
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);

    for (int index = 0; index < rows.size(); index++) {
      addRow(rows.get(index), index, rows.size());
    }
  }

  private void addRow(EventButtonLike row, int index, int rowCount) {
    TextField eventField = new TextField();
    eventField.setId("eventButtonEvent-" + index);
    eventField.setMaxWidth(Double.MAX_VALUE);
    setFieldValue(eventField, row.getEvent());
    bindTextField(eventField, (el, value) -> row.setEvent(value.isEmpty() ? null : value));

    ComboBox<String> priorityField = new ComboBox<>();
    priorityField.setId("eventButtonPriority-" + index);
    priorityField.setMaxWidth(Double.MAX_VALUE);
    priorityField.getItems().setAll(PRIORITIES);
    setFieldValue(priorityField, Boolean.TRUE.equals(row.getPrimary()) ? "PRIMARY" : DEFAULT_PRIORITY);
    bindComboBox(priorityField, (el, value) -> row.setPrimary("PRIMARY".equals(value)));

    CheckBox destructiveField = new CheckBox();
    destructiveField.setId("eventButtonDestructive-" + index);
    setFieldValue(destructiveField, Boolean.TRUE.equals(row.getDestructive()));
    bindCheckBox(destructiveField, (el, value) -> row.setDestructive(value ? Boolean.TRUE : null));

    TextField iconField = new TextField();
    iconField.setId("eventButtonIcon-" + index);
    iconField.setMaxWidth(Double.MAX_VALUE);
    setFieldValue(iconField, row.getIconName());
    bindTextField(iconField, (el, value) -> row.setIconName(value.isEmpty() ? null : value));

    buttonsGrid.addRow(index + 1, eventField, priorityField, destructiveField, iconField, createActionsBox(row, index, rowCount));
  }

  private HBox createActionsBox(EventButtonLike row, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_button"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        rows.remove(row);
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
