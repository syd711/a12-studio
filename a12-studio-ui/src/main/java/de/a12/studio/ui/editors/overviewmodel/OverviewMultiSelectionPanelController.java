package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.ClearConfirmation;
import de.a12.studio.models.overviewmodel.Confirmation;
import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.overviewmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Edits an {@link OverviewModel}'s {@code content.configuration.multiSelection}: the collapse/counter/
 * selection-area options, the clear-selection confirmation, and one draggable, reorderable row per
 * {@link de.a12.studio.models.overviewmodel.Button} action, summarizing its Event, Priority, Destructive and
 * Icon. Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the
 * model-header pattern used by e.g. {@link OverviewSearchAndFiltersPanelController}. Clicking a row (or its Edit
 * button) opens {@link Dialogs#showMultiSelectionActionForEdit}, which is intentionally empty for now (no
 * fields yet) - the full action editor is a follow-up, matching {@link OverviewColumnsPanelController}'s
 * column dialog.
 */
public class OverviewMultiSelectionPanelController extends AbstractPropertyEditor implements Initializable {

  // Identifies a row-reorder drag; the dragboard content is the dragged action's current index into getActions().
  private static final DataFormat ACTION_INDEX = new DataFormat("application/x-a12-multi-selection-action-index");

  private static final List<String> COLLAPSE_OPTIONS = List.of("",
      MultiSelectionConfig.COLLAPSE_OPTION_COLLAPSIBLE_COLLAPSED, MultiSelectionConfig.COLLAPSE_OPTION_COLLAPSIBLE_EXPANDED,
      MultiSelectionConfig.COLLAPSE_OPTION_NON_COLLAPSIBLE);
  private static final List<String> COUNTER_OPTIONS = List.of("",
      MultiSelectionConfig.COUNTER_OPTION_SIMPLE, MultiSelectionConfig.COUNTER_OPTION_NONE);
  private static final List<String> SELECTION_AREA_OPTIONS = List.of("",
      MultiSelectionConfig.SELECTION_AREA_CHECKBOX, MultiSelectionConfig.SELECTION_AREA_CHECKBOX_AND_ROW);

  @FXML
  private ComboBox<String> collapseOptionField;
  @FXML
  private ComboBox<String> counterOptionField;
  @FXML
  private ComboBox<String> selectionAreaField;
  @FXML
  private CheckBox clearConfirmationField;
  @FXML
  private VBox clearConfirmationDetails;
  @FXML
  private LocalizedTextPanelController clearConfirmationTitleController;
  @FXML
  private LocalizedTextPanelController clearConfirmationMessageController;
  @FXML
  private HBox actionColumnHeaders;
  @FXML
  private VBox actionRows;
  @FXML
  private Label actionsEmptyLabel;

  private OverviewModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    collapseOptionField.getItems().setAll(COLLAPSE_OPTIONS);
    collapseOptionField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureMultiSelectionConfig().setCollapseOption(newValue == null || newValue.isBlank() ? null : newValue);
      commitHeaderChange();
    });

    counterOptionField.getItems().setAll(COUNTER_OPTIONS);
    counterOptionField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureMultiSelectionConfig().setCounterOption(newValue == null || newValue.isBlank() ? null : newValue);
      commitHeaderChange();
    });

    selectionAreaField.getItems().setAll(SELECTION_AREA_OPTIONS);
    selectionAreaField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureMultiSelectionConfig().setSelectionArea(newValue == null || newValue.isBlank() ? null : newValue);
      commitHeaderChange();
    });

    clearConfirmationField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      MultiSelectionConfig config = ensureMultiSelectionConfig();
      if (newValue) {
        ClearConfirmation confirmation = config.getClearConfirmation();
        if (confirmation == null) {
          confirmation = new ClearConfirmation();
          config.setClearConfirmation(confirmation);
        }
        confirmation.setEnabled(true);
        bindConfirmationControllers(ensureConfirmationDetails(confirmation));
      }
      else {
        config.setClearConfirmation(null);
        bindConfirmationControllers(new Confirmation());
      }
      commitHeaderChange();
    });

    clearConfirmationTitleController.configureConfirmationTitle();
    clearConfirmationMessageController.configureConfirmationMessage();
    clearConfirmationDetails.visibleProperty().bind(clearConfirmationField.selectedProperty());
    clearConfirmationDetails.managedProperty().bind(clearConfirmationDetails.visibleProperty());
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      MultiSelectionConfig config = currentMultiSelectionConfig();
      collapseOptionField.setValue(config != null ? orEmpty(config.getCollapseOption()) : "");
      counterOptionField.setValue(config != null ? orEmpty(config.getCounterOption()) : "");
      selectionAreaField.setValue(config != null ? orEmpty(config.getSelectionArea()) : "");
      ClearConfirmation clearConfirmation = config != null ? config.getClearConfirmation() : null;
      boolean confirmationEnabled = clearConfirmation != null && Boolean.TRUE.equals(clearConfirmation.getEnabled());
      clearConfirmationField.setSelected(confirmationEnabled);
      bindConfirmationControllers(confirmationEnabled ? ensureConfirmationDetails(clearConfirmation) : new Confirmation());
      rebuildActionRows();
    }
    finally {
      updatingFromModel = false;
    }
  }

  private MultiSelectionConfig currentMultiSelectionConfig() {
    return model.getContent().getConfiguration() != null ? model.getContent().getConfiguration().getMultiSelection() : null;
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private MultiSelectionConfig ensureMultiSelectionConfig() {
    OverviewConfiguration configuration = ensureConfiguration();
    if (configuration.getMultiSelection() == null) {
      configuration.setMultiSelection(new MultiSelectionConfig());
    }
    return configuration.getMultiSelection();
  }

  private Confirmation ensureConfirmationDetails(ClearConfirmation clearConfirmation) {
    if (clearConfirmation.getConfirmation() == null) {
      clearConfirmation.setConfirmation(new Confirmation());
    }
    return clearConfirmation.getConfirmation();
  }

  private void bindConfirmationControllers(Confirmation confirmation) {
    clearConfirmationTitleController.setConfirmation(confirmation);
    clearConfirmationMessageController.setConfirmation(confirmation);
  }

  private List<de.a12.studio.models.overviewmodel.Button> getActions() {
    return ensureMultiSelectionConfig().getButtons();
  }

  @FXML
  private void onAddAction() {
    Dialogs.showMultiSelectionActionForAdd(Studio.stage).ifPresent(button -> {
      getActions().add(button);
      rebuildActionRows();
      commitHeaderChange();
    });
  }

  private void rebuildActionRows() {
    actionRows.getChildren().clear();

    MultiSelectionConfig config = currentMultiSelectionConfig();
    List<de.a12.studio.models.overviewmodel.Button> actions = config != null ? config.getButtons() : List.of();
    boolean empty = actions.isEmpty();
    actionColumnHeaders.setVisible(!empty);
    actionColumnHeaders.setManaged(!empty);
    actionsEmptyLabel.setVisible(empty);
    actionsEmptyLabel.setManaged(empty);

    for (int index = 0; index < actions.size(); index++) {
      actionRows.getChildren().add(createRow(actions.get(index), index, actions.size()));
    }
  }

  private HBox createRow(de.a12.studio.models.overviewmodel.Button button, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label eventLabel = createRowLabel(orEmpty(button.getEvent()), "multiSelectionActionEvent-" + index, 160.0, button);
    Label priorityLabel = createRowLabel("", "multiSelectionActionPriority-" + index, 90.0, button);
    Label destructiveLabel = createRowLabel(Boolean.TRUE.equals(button.getDestructive()) ? "Yes" : "No", "multiSelectionActionDestructive-" + index, 90.0, button);
    Label iconLabel = createRowLabel(button.getIcon() != null ? orEmpty(button.getIcon().getName()) : "", "multiSelectionActionIcon-" + index, 120.0, button);

    HBox row = new HBox(10.0, dragHandle, eventLabel, priorityLabel, destructiveLabel, iconLabel, createActionsBox(button, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, ACTION_INDEX, index, this::moveAction);
    return row;
  }

  private Label createRowLabel(String text, String id, double width, de.a12.studio.models.overviewmodel.Button button) {
    Label label = new Label(text);
    label.setId(id);
    label.setPrefWidth(width);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(button);
      }
    });
    return label;
  }

  private void openEditDialog(de.a12.studio.models.overviewmodel.Button button) {
    if (Dialogs.showMultiSelectionActionForEdit(Studio.stage, button)) {
      rebuildActionRows();
      commitHeaderChange();
    }
  }

  private void moveAction(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getActions(), fromIndex, insertBeforeIndex)) {
      rebuildActionRows();
      commitHeaderChange();
    }
  }

  private HBox createActionsBox(de.a12.studio.models.overviewmodel.Button button, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(button));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_action"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getActions().remove(button);
        rebuildActionRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getActions(), fromIndex, toIndex);
    rebuildActionRows();
    commitHeaderChange();
  }

  private static String orEmpty(String value) {
    return value != null ? value : "";
  }
}
