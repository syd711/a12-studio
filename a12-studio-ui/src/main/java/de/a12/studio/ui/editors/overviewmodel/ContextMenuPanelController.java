package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.overviewmodel.ActionGroup;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.ContextMenu;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Edits an {@link OverviewModel}'s {@code content.contextMenu}: named {@link ActionGroup}s, each holding
 * multilingual title text and a list of {@link Button} actions, matching the SME reference's Context Menu
 * section - "similar to adding Row Actions, but without Priority, Destructive, and Hide Label". Not bound to a
 * single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern
 * ({@link #commitHeaderChange()}) used by e.g. {@link OverviewSortingPanelController}.
 */
public class ContextMenuPanelController extends AbstractPropertyEditor {

  @FXML
  private ListView<ActionGroup> groupsList;
  @FXML
  private javafx.scene.layout.VBox detailBox;
  @FXML
  private TextField groupNameField;
  @FXML
  private GridPane groupTitleGrid;
  @FXML
  private GridPane actionsGrid;
  @FXML
  private javafx.scene.control.Label actionsEmptyLabel;

  private OverviewModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits and don't trigger a save.
  private boolean updatingFromModel;

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;

    groupsList.setCellFactory(list -> new ListCell<>() {
      @Override
      protected void updateItem(ActionGroup group, boolean empty) {
        super.updateItem(group, empty);
        setText(empty || group == null ? null : describeGroup(group));
      }
    });
    groupsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showGroup(newValue));

    groupNameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      ActionGroup group = groupsList.getSelectionModel().getSelectedItem();
      if (group == null) {
        return;
      }
      group.setName(newValue == null || newValue.isBlank() ? null : newValue);
      groupsList.refresh();
      commitHeaderChange();
    });

    refreshGroupsList();
    showGroup(null);
  }

  @FXML
  public void onAddGroup() {
    ContextMenu contextMenu = ensureContextMenu();
    ActionGroup group = new ActionGroup();
    group.setName("group-" + shortId());
    contextMenu.getGroups().add(group);
    refreshGroupsList();
    groupsList.getSelectionModel().select(group);
    commitHeaderChange();
  }

  @FXML
  public void onRemoveGroup() {
    ActionGroup group = groupsList.getSelectionModel().getSelectedItem();
    if (group == null) {
      return;
    }
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_context_menu_group"), null, null, "Delete");
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }
    ContextMenu contextMenu = model.getContent().getContextMenu();
    if (contextMenu != null) {
      contextMenu.getGroups().remove(group);
    }
    refreshGroupsList();
    commitHeaderChange();
  }

  @FXML
  public void onAddAction() {
    ActionGroup group = groupsList.getSelectionModel().getSelectedItem();
    if (group == null) {
      return;
    }
    group.getActions().add(new Button());
    rebuildActionsGrid(group);
    groupsList.refresh();
    commitHeaderChange();
  }

  private void refreshGroupsList() {
    ActionGroup selected = groupsList.getSelectionModel().getSelectedItem();
    ContextMenu contextMenu = model.getContent().getContextMenu();
    List<ActionGroup> groups = contextMenu != null ? contextMenu.getGroups() : List.of();
    groupsList.getItems().setAll(groups);
    if (selected != null && groups.contains(selected)) {
      groupsList.getSelectionModel().select(selected);
    }
  }

  private void showGroup(ActionGroup group) {
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      boolean present = group != null;
      detailBox.setVisible(present);
      detailBox.setManaged(present);
      if (!present) {
        return;
      }
      groupNameField.setText(group.getName() != null ? group.getName() : "");
      rebuildLocaleGrid(groupTitleGrid, group.getTitle(), (code, text) -> setLabelText(group.getTitle(), code, text));
      rebuildActionsGrid(group);
    }
    finally {
      updatingFromModel = wasUpdating;
    }
  }

  private void rebuildActionsGrid(ActionGroup group) {
    actionsGrid.getChildren().clear();
    List<Button> actions = group.getActions();
    boolean empty = actions.isEmpty();
    actionsGrid.setVisible(!empty);
    actionsGrid.setManaged(!empty);
    actionsEmptyLabel.setVisible(empty);
    actionsEmptyLabel.setManaged(empty);

    int row = 0;
    for (Button action : List.copyOf(actions)) {
      TextField eventField = new TextField();
      eventField.setPromptText("Event");
      eventField.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(eventField, Priority.ALWAYS);
      setFieldValue(eventField, action.getEvent());
      eventField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (updatingFromModel) {
          return;
        }
        action.setEvent(newValue.isEmpty() ? null : newValue);
        commitHeaderChange();
      });

      TextField iconField = new TextField();
      iconField.setPromptText("Icon");
      iconField.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(iconField, Priority.ALWAYS);
      setFieldValue(iconField, action.getIconName());
      iconField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (updatingFromModel) {
          return;
        }
        action.setIconName(newValue.isEmpty() ? null : newValue);
        commitHeaderChange();
      });

      javafx.scene.control.Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Remove Action", () -> {
        actions.remove(action);
        rebuildActionsGrid(group);
        groupsList.refresh();
        commitHeaderChange();
      });

      actionsGrid.addRow(row++, eventField, iconField, deleteButton);
    }
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
      textField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (updatingFromModel) {
          return;
        }
        onTextChange.accept(code, newValue);
        commitHeaderChange();
      });

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

  private ContextMenu ensureContextMenu() {
    if (model.getContent().getContextMenu() == null) {
      model.getContent().setContextMenu(new ContextMenu());
    }
    return model.getContent().getContextMenu();
  }

  private static String describeGroup(ActionGroup group) {
    String name = group.getName() != null ? group.getName() : "(new group)";
    String actions = group.getActions().stream()
        .map(Button::getEvent)
        .filter(event -> event != null && !event.isBlank())
        .collect(Collectors.joining(", "));
    return actions.isEmpty() ? name : name + " (" + actions + ")";
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }
}
