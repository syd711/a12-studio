package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.EventButtonLike;
import de.a12.studio.models.overviewmodel.OverviewButtonLike;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Edits a list of {@link EventButtonLike} rows (e.g. {@code content.rowActionGroup.actions} or a footer/
 * subheader box's button entries) as a compact table of Event/Priority/Destructive/Icon, matching the SME
 * reference's "Row Action"/"Major Buttons"/"Minor Buttons" tables. Reusable for any such list by calling
 * {@link #configure} with the list, a title, a settings-key suffix (so several instances of this panel on the
 * same editor don't collide on the same persisted expanded/collapsed state) and a factory for new rows. Rows
 * only summarize each button; the fields are edited in a dedicated dialog (see {@link
 * Dialogs#showEventButtonForAdd}/{@link Dialogs#showEventButtonForEdit}), opened via Add/Edit or a single click
 * on a row - every row here is actually an {@link OverviewButtonLike} (row/newRowFactory are only declared more
 * loosely so a footer/subheader box's {@code List<BoxElement>} can be passed directly). Not tied to a single {@code
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern ({@link
 * #commitHeaderChange()}) rather than {@link #commitChange()}.
 */
public class EventButtonsPanelController extends AbstractPropertyEditor {

  // javafx.scene.input.DataFormat registers its mime type in a process-wide static registry and throws if the
  // same string is registered twice, with no way to unregister. settingsKeySuffix alone (e.g. ".rowAction") is
  // not unique across controller instances - it repeats every time an Overview Model editor is opened (new tab
  // or reopen) - so a counter is appended to keep each instance's format string globally unique for the life of
  // the process. ToolbarButtonsPanelController keeps its own separate counter starting from the same 0 and is
  // used with some of the same suffixes (e.g. ".footerMajor"), so the mime type below is prefixed with this
  // class's own name ("event-button") rather than something the two classes could share.
  private static final AtomicLong INSTANCE_COUNTER = new AtomicLong();

  @FXML
  private HBox buttonsHeader;

  @FXML
  private VBox buttonsList;

  @FXML
  private Label emptyLabel;

  private List<EventButtonLike> rows;
  private Supplier<EventButtonLike> newRowFactory;

  // Identifies a row-reorder drag; unique per panel instance (this class is instantiated several times in the
  // same window, e.g. subheader/footer major/minor buttons), so drags from a sibling instance's row list are
  // rejected rather than accepted into this one. Created once in configure(), keyed off settingsKeySuffix.
  private DataFormat indexFormat;

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
    if (indexFormat == null) {
      indexFormat = new DataFormat("application/x-a12-event-button-index" + settingsKeySuffix + "-" + INSTANCE_COUNTER.incrementAndGet());
    }
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    Dialogs.showEventButtonForAdd(Studio.stage, () -> (OverviewButtonLike) newRowFactory.get()).ifPresent(row -> {
      rows.add(row);
      rebuildRows();
      commitHeaderChange();
    });
  }

  private void openEditDialog(EventButtonLike row) {
    Dialogs.showEventButtonForEdit(Studio.stage, (OverviewButtonLike) row).ifPresent(edited -> {
      rows.set(rows.indexOf(row), edited);
      rebuildRows();
      commitHeaderChange();
    });
  }

  private void rebuildRows() {
    buttonsList.getChildren().clear();

    boolean empty = rows.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
    buttonsHeader.setVisible(!empty);
    buttonsHeader.setManaged(!empty);

    for (int index = 0; index < rows.size(); index++) {
      buttonsList.getChildren().add(createRow(rows.get(index), index, rows.size()));
    }
  }

  private HBox createRow(EventButtonLike row, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label eventLabel = new Label(row.getEvent());
    eventLabel.setId("eventButtonEvent-" + index);
    eventLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(eventLabel, Priority.ALWAYS);
    makeClickableToEdit(eventLabel, row);

    Label priorityLabel = new Label(Boolean.TRUE.equals(row.getPrimary()) ? "PRIMARY" : "SECONDARY");
    priorityLabel.setId("eventButtonPriority-" + index);
    priorityLabel.setPrefWidth(140.0);
    makeClickableToEdit(priorityLabel, row);

    Label destructiveLabel = new Label(Boolean.TRUE.equals(row.getDestructive()) ? "✓" : "");
    destructiveLabel.setId("eventButtonDestructive-" + index);
    destructiveLabel.setPrefWidth(110.0);
    makeClickableToEdit(destructiveLabel, row);

    Label iconLabel = new Label(row.getIconName());
    iconLabel.setId("eventButtonIcon-" + index);
    iconLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(iconLabel, Priority.ALWAYS);
    makeClickableToEdit(iconLabel, row);

    HBox rowBox = new HBox(10.0, dragHandle, eventLabel, priorityLabel, destructiveLabel, iconLabel, createActionsBox(row, index, rowCount));
    rowBox.setAlignment(Pos.CENTER_LEFT);
    rowBox.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(rowBox, dragHandle, indexFormat, index, this::moveRowViaDrag);
    return rowBox;
  }

  private void makeClickableToEdit(Label label, EventButtonLike row) {
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(row);
      }
    });
  }

  private void moveRowViaDrag(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(rows, fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitHeaderChange();
    }
  }

  private HBox createActionsBox(EventButtonLike row, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(row));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_button"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        rows.remove(row);
        rebuildRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(rows, fromIndex, toIndex);
    rebuildRows();
    commitHeaderChange();
  }
}
