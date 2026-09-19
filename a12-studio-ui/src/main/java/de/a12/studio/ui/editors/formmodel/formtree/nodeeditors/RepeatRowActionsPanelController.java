package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.RowAction;
import de.a12.studio.models.formmodel.RowActionGroup;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.formmodel.dialogs.Dialogs;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Row Actions" property editor for a selected {@link AbstractRepeat} node: custom row action buttons
 * ({@link RowActionGroup}, distinct from {@link AbstractRepeat#getDefaultRowAction()}, which only configures
 * the built-in row-click behavior). Previously {@code rowActionGroup} had no editor at all - the data model
 * didn't even have it (only the single-action {@code defaultRowAction}, itself unwired in the UI).
 * <p>
 * {@code event} and {@code scope} can be edited inline in the table; everything else a row action carries -
 * {@code buttonStyling} (label/description/icon/priority/destructive/hide-label/styles), {@code confirmation}/
 * {@code confirmationDialogTitle} and {@code annotations} - is edited through the Edit dialog ({@link
 * de.a12.studio.ui.editors.formmodel.dialogs.RowActionDialogController}), opened via the Edit button or by
 * double-clicking a row.
 */
public class RepeatRowActionsPanelController implements Initializable {

  private static final List<String> SCOPE_VALUES =
      List.of("ALWAYS", "DISABLED_IN_EDIT_MODE", "DISABLED_IN_READONLY_MODE", "HIDDEN_IN_EDIT_MODE", "HIDDEN_IN_READONLY_MODE");

  @FXML
  private TitledPane root;
  @FXML
  private TableView<RowAction> actionsTable;
  @FXML
  private TableColumn<RowAction, String> eventColumn;
  @FXML
  private TableColumn<RowAction, String> scopeColumn;
  @FXML
  private Button addButton;
  @FXML
  private Button removeButton;
  @FXML
  private Button editButton;

  private AbstractRepeat repeat;
  private @Nullable ElementIndex elementIndex;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AbstractPropertyEditor.persistExpandedState(root, getClass());

    actionsTable.setEditable(true);
    actionsTable.setItems(FXCollections.observableArrayList());

    eventColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEvent()));
    eventColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    eventColumn.setOnEditCommit(event -> {
      event.getRowValue().setEvent(event.getNewValue());
      commitChange();
    });

    scopeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getScope()));
    scopeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(SCOPE_VALUES)));
    scopeColumn.setOnEditCommit(event -> {
      event.getRowValue().setScope(event.getNewValue());
      commitChange();
    });

    addButton.setOnAction(e -> {
      RowAction action = new RowAction();
      action.setEvent("event_row_action");
      action.setScope("ALWAYS");
      getOrCreate().getAction().add(action);
      actionsTable.getItems().add(action);
      commitChange();
    });

    editButton.disableProperty().bind(actionsTable.getSelectionModel().selectedItemProperty().isNull());
    editButton.setOnAction(e -> editSelected());
    actionsTable.setRowFactory(table -> {
      javafx.scene.control.TableRow<RowAction> row = new javafx.scene.control.TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
          editSelected();
        }
      });
      return row;
    });

    removeButton.setOnAction(e -> {
      RowAction selected = actionsTable.getSelectionModel().getSelectedItem();
      if (selected == null) {
        return;
      }
      if (repeat.getRowActionGroup() != null) {
        repeat.getRowActionGroup().getAction().remove(selected);
      }
      actionsTable.getItems().remove(selected);
      commitChange();
    });
  }

  public void setRepeat(@NonNull AbstractRepeat repeat, @Nullable ElementIndex elementIndex) {
    this.elementIndex = elementIndex;
    this.repeat = repeat;
    ObservableList<RowAction> items = FXCollections.observableArrayList();
    if (repeat.getRowActionGroup() != null) {
      items.addAll(repeat.getRowActionGroup().getAction());
    }
    actionsTable.setItems(items);
  }

  private void editSelected() {
    RowAction selected = actionsTable.getSelectionModel().getSelectedItem();
    if (selected == null || repeat.getRowActionGroup() == null) {
      return;
    }
    List<RowAction> actions = repeat.getRowActionGroup().getAction();
    int index = actions.indexOf(selected);
    if (index < 0) {
      return;
    }
    Dialogs.showRowActionForEdit(Studio.stage, elementIndex, selected).ifPresent(edited -> {
      actions.set(index, edited);
      actionsTable.getItems().set(index, edited);
      actionsTable.getSelectionModel().select(index);
      commitChange();
    });
  }

  private RowActionGroup getOrCreate() {
    if (repeat.getRowActionGroup() == null) {
      repeat.setRowActionGroup(new RowActionGroup());
    }
    return repeat.getRowActionGroup();
  }

  private void commitChange() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }
}
