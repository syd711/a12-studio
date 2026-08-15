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
 * subheader box's button entries) as a compact, fully inline-editable table of Event/Priority/Destructive/Icon,
 * matching the SME reference's "Row Action"/"Major Buttons"/"Minor Buttons" tables. Reusable for any such list
 * by calling {@link #configure} with the list, a title, a settings-key suffix (so several instances of this
 * panel on the same editor don't collide on the same persisted expanded/collapsed state) and a factory for new
 * rows. Not tied to a single {@code de.a12.studio.models.documentmodel.Element}, so it follows the
 * model-header pattern ({@link #commitHeaderChange()}) rather than {@link #commitChange()}.
 */
public class ToolbarButtonsPanelController extends AbstractPropertyEditor {

  private static final List<String> PRIORITIES = List.of("PRIMARY", "SECONDARY");
  private static final String DEFAULT_PRIORITY = "SECONDARY";

  // javafx.scene.input.DataFormat registers its mime type in a process-wide static registry and throws if the
  // same string is registered twice, with no way to unregister. settingsKeySuffix alone (e.g. ".rowAction") is
  // not unique across controller instances - it repeats every time an Overview Model editor is opened (new tab
  // or reopen) - so a counter is appended to keep each instance's format string globally unique for the life of
  // the process. EventButtonsPanelController keeps its own separate counter starting from the same 0 and is used
  // with some of the same suffixes (e.g. ".footerMajor"), so the class's own name is also mixed in below -
  // otherwise the two classes' counters can land on the same suffix+number and collide in the shared registry.
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
      indexFormat = new DataFormat("application/x-a12-toolbar-button-index" + settingsKeySuffix + "-" + INSTANCE_COUNTER.incrementAndGet());
    }
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    rows.add(newRowFactory.get());
    rebuildRows();
    commitHeaderChange();
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

    TextField eventField = new TextField();
    eventField.setId("eventButtonEvent-" + index);
    eventField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(eventField, Priority.ALWAYS);
    setFieldValue(eventField, row.getEvent());
    bindTextField(eventField, (el, value) -> row.setEvent(value.isEmpty() ? null : value));

    ComboBox<String> priorityField = new ComboBox<>();
    priorityField.setId("eventButtonPriority-" + index);
    priorityField.setPrefWidth(140.0);
    priorityField.getItems().setAll(PRIORITIES);
    setFieldValue(priorityField, Boolean.TRUE.equals(row.getPrimary()) ? "PRIMARY" : DEFAULT_PRIORITY);
    bindComboBox(priorityField, (el, value) -> row.setPrimary("PRIMARY".equals(value)));

    CheckBox destructiveField = new CheckBox();
    destructiveField.setId("eventButtonDestructive-" + index);
    destructiveField.setPrefWidth(110.0);
    setFieldValue(destructiveField, Boolean.TRUE.equals(row.getDestructive()));
    bindCheckBox(destructiveField, (el, value) -> row.setDestructive(value ? Boolean.TRUE : null));

    TextField iconField = new TextField();
    iconField.setId("eventButtonIcon-" + index);
    iconField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(iconField, Priority.ALWAYS);
    setFieldValue(iconField, row.getIconName());
    bindTextField(iconField, (el, value) -> row.setIconName(value.isEmpty() ? null : value));

    HBox rowBox = new HBox(10.0, dragHandle, eventField, priorityField, destructiveField, iconField, createActionsBox(row, index, rowCount));
    rowBox.setAlignment(Pos.CENTER_LEFT);
    rowBox.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(rowBox, dragHandle, indexFormat, index, this::moveRowViaDrag);
    return rowBox;
  }

  private void moveRowViaDrag(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(rows, fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitHeaderChange();
    }
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
