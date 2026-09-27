package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.models.treemodel.TreeNodeActionGroup;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.treemodel.TreeActionContext;
import de.a12.studio.ui.editors.treemodel.TreeNodeActionsPanelController;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * Add/edit dialog for one {@link TreeNodeActionGroup} of a node type's context menu, opened from {@link
 * de.a12.studio.ui.editors.treemodel.TreeNodeContextMenuPanelController}: the group's Name (required), its
 * multilingual Title, its Type (an "add" group holds insert actions only) and its Actions, edited with the same
 * panel as a node's row actions ({@link TreeNodeActionsPanelController}). The group is a working copy (see
 * {@link Dialogs#showContextMenuGroupForEdit}) - the caller only splices it in once {@link #isConfirmed()}.
 */
public class TreeNodeContextMenuGroupDialogController implements DialogController {

  // Absent type = a plain group; SME's only other value is "add".
  private static final String PLAIN = "";

  @FXML
  private TextField nameField;

  @FXML
  private ComboBox<String> typeField;

  @FXML
  private LocalizedTextPanelController titleController;

  @FXML
  private TreeNodeActionsPanelController actionsController;

  @FXML
  private Button okButton;

  // Shared by the embedded panels so their commits aren't persisted while the dialog is open.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private TreeNodeActionGroup group;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  // Set while the fields are being repopulated from the group, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @FXML
  private void initialize() {
    titleController.setSaveMode(saveMode);
    actionsController.setSaveMode(saveMode);
    titleController.configureCustom("groupTitle", StudioBundle.get("tree_node_context_menu_group.title"));

    okButton.disableProperty().bind(nameField.textProperty().isEmpty());
    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        group.setName(newValue == null || newValue.isBlank() ? null : newValue);
      }
    });

    typeField.getItems().setAll(Arrays.asList(PLAIN, TreeNodeActionGroup.TYPE_ADD));
    typeField.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null ? null : StudioBundle.get("tree_node_context_menu_group.type_" + (value.isEmpty() ? "plain" : value));
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    typeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || newValue == null) {
        return;
      }
      if (TreeNodeActionGroup.TYPE_ADD.equals(newValue) && !confirmDroppingEventActions()) {
        updatingFromModel = true;
        try {
          typeField.setValue(oldValue);
        }
        finally {
          updatingFromModel = false;
        }
        return;
      }
      group.setType(newValue.isEmpty() ? null : newValue);
    });
  }

  void init(@NonNull Stage stage, @NonNull ProjectItem projectItem, @NonNull TreeNodeActionGroup group, @NonNull TreeActionContext context) {
    this.stage = stage;
    this.group = group;

    updatingFromModel = true;
    try {
      nameField.setText(group.getName() != null ? group.getName() : "");
      typeField.setValue(group.getType() != null ? group.getType() : PLAIN);
    }
    finally {
      updatingFromModel = false;
    }

    titleController.setCustom(group::getTitle);
    actionsController.configure(StudioBundle.get("actions"), ".contextMenuGroupActions", projectItem, group::getActions,
        () -> TreeNodeActionGroup.TYPE_ADD.equals(group.getType()), TreeNodeAction::new, () -> context);
  }

  /** Unregisters the embedded panels once this dialog is closed - see {@link Dialogs}. */
  void destroy() {
    titleController.destroy();
    actionsController.destroy();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  /**
   * An "add" group holds insert actions only, so switching to it removes the group's event actions. Asks first when
   * there are any; returns whether the switch may go ahead.
   */
  private boolean confirmDroppingEventActions() {
    long eventActions = group.getActions().stream().filter(action -> !action.isInsert()).count();
    if (eventActions == 0) {
      return true;
    }
    Optional<ButtonType> answer = WidgetFactory.showConfirmation(stage, StudioBundle.get("tree_node_context_menu_group.confirm_add_type", eventActions),
        null, null, StudioBundle.get("ok"));
    if (answer.isEmpty() || answer.get() != ButtonType.OK) {
      return false;
    }
    group.getActions().removeIf(action -> !action.isInsert());
    actionsController.refresh();
    return true;
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  TreeNodeActionGroup getGroup() {
    return group;
  }
}
