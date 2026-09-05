package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.overviewmodel.ActionGroup;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Add/edit dialog for a single {@link ActionGroup} of an {@link OverviewModel}'s {@code content.contextMenu},
 * opened from {@link de.a12.studio.ui.editors.overviewmodel.ContextMenuPanelController} by clicking a row or its
 * Edit button (see {@link Dialogs#showContextMenuGroupForAdd}/{@link Dialogs#showContextMenuGroupForEdit}). Edits
 * the real {@link ActionGroup} live, so a {@link ContextMenuGroupSnapshot} taken before showing the dialog can
 * undo those edits on Cancel, mirroring {@link FilterGroupDialogController}. The Name field, multilingual Title
 * grid and Actions grid reproduce exactly what {@link
 * de.a12.studio.ui.editors.overviewmodel.ContextMenuPanelController} used to show inline before it became a
 * row-based editor.
 */
public class ContextMenuGroupDialogController implements DialogController {

  @FXML
  private TextField nameField;
  @FXML
  private GridPane titleGrid;
  @FXML
  private GridPane actionsGrid;
  @FXML
  private javafx.scene.control.Label actionsEmptyLabel;

  private Stage stage;

  private OverviewModel model;

  private ActionGroup group;

  private ContextMenuGroupSnapshot snapshot;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits.
  private boolean updatingFromModel;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        group.setName(newValue == null || newValue.isBlank() ? null : newValue);
      }
    });
  }

  void init(Stage stage, @NonNull OverviewModel model, @NonNull ActionGroup group) {
    this.stage = stage;
    this.model = model;
    this.group = group;
    this.snapshot = new ContextMenuGroupSnapshot(group);

    updatingFromModel = true;
    try {
      nameField.setText(group.getName() != null ? group.getName() : "");
    }
    finally {
      updatingFromModel = false;
    }

    rebuildLocaleGrid(titleGrid, group.getTitle(), (code, text) -> setLabelText(group.getTitle(), code, text));
    rebuildActionsGrid();
  }

  @Override
  public void onDialogCancel() {
    snapshot.restore();
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null) {
      projectItem.save();
      StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
    }
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  @FXML
  private void onAddAction() {
    Dialogs.showContextMenuActionForAdd(stage).ifPresent(action -> {
      group.getActions().add(action);
      rebuildActionsGrid();
    });
  }

  private void rebuildActionsGrid() {
    actionsGrid.getChildren().clear();
    List<Button> actions = group.getActions();
    boolean empty = actions.isEmpty();
    actionsGrid.setVisible(!empty);
    actionsGrid.setManaged(!empty);
    actionsEmptyLabel.setVisible(empty);
    actionsEmptyLabel.setManaged(empty);

    int row = 0;
    for (Button action : List.copyOf(actions)) {
      javafx.scene.control.Label eventLabel = new javafx.scene.control.Label(action.getEvent() != null ? action.getEvent() : "");
      eventLabel.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(eventLabel, Priority.ALWAYS);
      makeClickableToEdit(eventLabel, action);

      javafx.scene.control.Label iconLabel = new javafx.scene.control.Label(action.getIconName() != null ? action.getIconName() : "");
      iconLabel.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(iconLabel, Priority.ALWAYS);
      makeClickableToEdit(iconLabel, action);

      HBox actionsBox = createActionsBox(action);

      actionsGrid.addRow(row++, eventLabel, iconLabel, actionsBox);
    }
  }

  private void makeClickableToEdit(javafx.scene.control.Label label, Button action) {
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openActionEditDialog(action);
      }
    });
  }

  private void openActionEditDialog(Button action) {
    Dialogs.showContextMenuActionForEdit(stage, action).ifPresent(edited -> {
      group.getActions().set(group.getActions().indexOf(action), edited);
      rebuildActionsGrid();
    });
  }

  private HBox createActionsBox(Button action) {
    javafx.scene.control.Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openActionEditDialog(action));

    javafx.scene.control.Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("remove_action"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_action"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        group.getActions().remove(action);
        rebuildActionsGrid();
      }
    });

    HBox actionsBox = new HBox(4.0, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void rebuildLocaleGrid(GridPane grid, List<Label> labels, BiConsumer<String, String> onTextChange) {
    grid.getChildren().clear();
    int row = 0;
    for (Locale locale : model.getLocales()) {
      String code = locale.getCode();
      javafx.scene.control.Label localeLabel = new javafx.scene.control.Label(code);
      localeLabel.getStyleClass().add("field-label");

      TextField textField = new TextField(labelText(labels, code));
      textField.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(textField, Priority.ALWAYS);
      textField.textProperty().addListener((observable, oldValue, newValue) -> onTextChange.accept(code, newValue));

      grid.addRow(row++, localeLabel, textField);
    }
  }

  private static String labelText(List<Label> labels, String locale) {
    return labels.stream()
        .filter(label -> locale.equals(label.getLocale()))
        .map(Label::getText)
        .filter(text -> text != null)
        .findFirst()
        .orElse("");
  }

  private static void setLabelText(List<Label> labels, String locale, String text) {
    Label existing = labels.stream()
        .filter(label -> locale.equals(label.getLocale()))
        .findFirst()
        .orElse(null);
    if (existing == null) {
      existing = new Label();
      existing.setLocale(locale);
      labels.add(existing);
    }
    existing.setText(text == null || text.isBlank() ? null : text);
  }
}
