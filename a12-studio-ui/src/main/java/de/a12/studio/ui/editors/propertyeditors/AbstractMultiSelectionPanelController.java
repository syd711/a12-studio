package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.ClearConfirmation;
import de.a12.studio.models.overviewmodel.Confirmation;
import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.overviewmodel.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

/**
 * Edits a {@link MultiSelectionConfig}: the collapse/counter/selection-area options, the clear-selection
 * confirmation, and one draggable, reorderable row per {@link Button} action, summarizing its Event, Priority,
 * Destructive and Icon. Shared by the Overview Model's and the Tree Model's Multi-Selection panels, which only
 * differ in where the configuration lives (the abstract hooks below) and in the fields the Tree adds on top
 * (see {@link #populateExtraFields}). Not bound to a single {@link de.a12.studio.models.documentmodel.Element},
 * so it follows the model-header pattern.
 * <p>
 * Clicking a row (or its Edit button) opens {@link Dialogs#showMultiSelectionActionForEdit}. Enabling the
 * feature seeds SME's defaults (collapsed and collapsible, simple counter, checkbox and row, confirmation on).
 */
public abstract class AbstractMultiSelectionPanelController extends AbstractPropertyEditor implements Initializable {

  // Identifies a row-reorder drag; the dragboard content is the dragged action's current index into getActions().
  private static final DataFormat ACTION_INDEX = new DataFormat("application/x-a12-multi-selection-action-index");

  // The empty entry stands for "unset"; the enabled listener replaces it by the first real entry.
  private static final List<String> COLLAPSE_OPTIONS = List.of("",
      MultiSelectionConfig.COLLAPSE_OPTION_COLLAPSIBLE_COLLAPSED, MultiSelectionConfig.COLLAPSE_OPTION_COLLAPSIBLE_EXPANDED,
      MultiSelectionConfig.COLLAPSE_OPTION_NON_COLLAPSIBLE);
  private static final List<String> COUNTER_OPTIONS = List.of("",
      MultiSelectionConfig.COUNTER_OPTION_SIMPLE, MultiSelectionConfig.COUNTER_OPTION_NONE);
  private static final List<String> SELECTION_AREA_OPTIONS = List.of("",
      MultiSelectionConfig.SELECTION_AREA_CHECKBOX, MultiSelectionConfig.SELECTION_AREA_CHECKBOX_AND_ROW);

  @FXML
  private CheckBox multiSelectionEnabledField;
  @FXML
  private VBox multiSelectionContent;
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
  // Optional: only panels whose FXML shows the "i" icons next to these fields declare them.
  @FXML
  private Label selectionAreaInfoIcon;
  @FXML
  private Label clearConfirmationInfoIcon;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  protected boolean updatingFromModel;

  /** Whether a model has been handed to this panel yet; edits made before that are ignored. */
  protected abstract boolean isModelLoaded();

  /** The configuration being edited, or {@code null} while Multi-Selection is disabled. */
  protected abstract MultiSelectionConfig getMultiSelection();

  /** Attaches a new, empty configuration to the model (creating its parent configuration if needed) and returns it. */
  protected abstract MultiSelectionConfig createMultiSelection();

  /** Detaches the configuration from the model, i.e. disables Multi-Selection. */
  protected abstract void removeMultiSelection();

  /** Called after the enabled checkbox was toggled and the change committed. */
  protected void onEnabledChanged() {
  }

  /** Called for a freshly added action before it is stored, e.g. to give it an id. */
  protected void onActionCreated(@NonNull Button button) {
  }

  /** Called while the fields are repopulated from the model (inside the {@link #updatingFromModel} guard); {@code config} is null when disabled. */
  protected void populateExtraFields(MultiSelectionConfig config) {
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    if (selectionAreaInfoIcon != null) {
      WidgetFactory.createHelpIcon(selectionAreaInfoIcon, StudioBundle.get("multi_selection_panel.selection_area_info"));
    }
    if (clearConfirmationInfoIcon != null) {
      WidgetFactory.createHelpIcon(clearConfirmationInfoIcon, StudioBundle.get("multi_selection_panel.clear_confirmation_info"));
    }

    multiSelectionContent.visibleProperty().bind(multiSelectionEnabledField.selectedProperty());
    multiSelectionContent.managedProperty().bind(multiSelectionContent.visibleProperty());
    multiSelectionEnabledField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || !isModelLoaded()) {
        return;
      }
      if (newValue) {
        applyDefaults(ensureMultiSelection());
      }
      else {
        removeMultiSelection();
      }
      commitHeaderChange();
      onEnabledChanged();
    });

    setupOptionField(collapseOptionField, COLLAPSE_OPTIONS, "multi_selection_panel.collapse_option.", MultiSelectionConfig::setCollapseOption);
    setupOptionField(counterOptionField, COUNTER_OPTIONS, "multi_selection_panel.counter_option.", MultiSelectionConfig::setCounterOption);
    setupOptionField(selectionAreaField, SELECTION_AREA_OPTIONS, "multi_selection_panel.selection_area.", MultiSelectionConfig::setSelectionArea);

    clearConfirmationField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || !isModelLoaded()) {
        return;
      }
      MultiSelectionConfig config = ensureMultiSelection();
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

  private void setupOptionField(ComboBox<String> field, List<String> options, String bundleKeyPrefix,
                                BiConsumer<MultiSelectionConfig, String> setter) {
    field.getItems().setAll(options);
    field.setConverter(new StringConverter<>() {
      @Override
      public String toString(String option) {
        return option == null || option.isEmpty() ? "" : StudioBundle.get(bundleKeyPrefix + option);
      }

      @Override
      public String fromString(String label) {
        return null;
      }
    });
    field.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || !isModelLoaded()) {
        return;
      }
      setter.accept(ensureMultiSelection(), newValue == null || newValue.isBlank() ? null : newValue);
      commitHeaderChange();
    });
  }

  /** Repopulates every field (and the action rows) from the model. Call after the model has been handed over. */
  protected final void loadFromModel() {
    updatingFromModel = true;
    try {
      MultiSelectionConfig config = getMultiSelection();
      multiSelectionEnabledField.setSelected(config != null);
      collapseOptionField.setValue(config != null ? orEmpty(config.getCollapseOption()) : "");
      counterOptionField.setValue(config != null ? orEmpty(config.getCounterOption()) : "");
      selectionAreaField.setValue(config != null ? orEmpty(config.getSelectionArea()) : "");
      ClearConfirmation clearConfirmation = config != null ? config.getClearConfirmation() : null;
      boolean confirmationEnabled = clearConfirmation != null && Boolean.TRUE.equals(clearConfirmation.getEnabled());
      clearConfirmationField.setSelected(confirmationEnabled);
      bindConfirmationControllers(confirmationEnabled ? ensureConfirmationDetails(clearConfirmation) : new Confirmation());
      populateExtraFields(config);
      rebuildActionRows();
    }
    finally {
      updatingFromModel = false;
    }
  }

  protected final MultiSelectionConfig ensureMultiSelection() {
    MultiSelectionConfig config = getMultiSelection();
    return config != null ? config : createMultiSelection();
  }

  /** Seeds SME's defaults for whatever the freshly enabled configuration doesn't have yet. */
  private void applyDefaults(MultiSelectionConfig config) {
    updatingFromModel = true;
    try {
      if (config.getCollapseOption() == null || config.getCollapseOption().isBlank()) {
        config.setCollapseOption(COLLAPSE_OPTIONS.get(1));
        collapseOptionField.setValue(COLLAPSE_OPTIONS.get(1));
      }
      if (config.getCounterOption() == null || config.getCounterOption().isBlank()) {
        config.setCounterOption(COUNTER_OPTIONS.get(1));
        counterOptionField.setValue(COUNTER_OPTIONS.get(1));
      }
      if (config.getSelectionArea() == null || config.getSelectionArea().isBlank()) {
        config.setSelectionArea(SELECTION_AREA_OPTIONS.get(2));
        selectionAreaField.setValue(SELECTION_AREA_OPTIONS.get(2));
      }
      if (config.getClearConfirmation() == null) {
        ClearConfirmation confirmation = new ClearConfirmation();
        confirmation.setEnabled(true);
        config.setClearConfirmation(confirmation);
        clearConfirmationField.setSelected(true);
        bindConfirmationControllers(ensureConfirmationDetails(confirmation));
      }
    }
    finally {
      updatingFromModel = false;
    }
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

  private List<Button> getActions() {
    return ensureMultiSelection().getButtons();
  }

  @FXML
  private void onAddAction() {
    Dialogs.showMultiSelectionActionForAdd(Studio.stage).ifPresent(button -> {
      onActionCreated(button);
      getActions().add(button);
      rebuildActionRows();
      commitHeaderChange();
    });
  }

  private void rebuildActionRows() {
    actionRows.getChildren().clear();

    MultiSelectionConfig config = getMultiSelection();
    List<Button> actions = config != null ? config.getButtons() : List.of();
    boolean empty = actions.isEmpty();
    actionColumnHeaders.setVisible(!empty);
    actionColumnHeaders.setManaged(!empty);
    actionsEmptyLabel.setVisible(empty);
    actionsEmptyLabel.setManaged(empty);

    for (int index = 0; index < actions.size(); index++) {
      actionRows.getChildren().add(createRow(actions.get(index), index, actions.size()));
    }
  }

  private HBox createRow(Button button, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label eventLabel = createRowLabel(orEmpty(button.getEvent()), "multiSelectionActionEvent-" + index, 160.0, button);
    Label priorityLabel = createRowLabel(StudioBundle.get(Boolean.TRUE.equals(button.getPrimary())
        ? "multi_selection_panel.priority_primary" : "multi_selection_panel.priority_secondary"), "multiSelectionActionPriority-" + index, 90.0, button);
    Label destructiveLabel = createRowLabel(StudioBundle.get(Boolean.TRUE.equals(button.getDestructive()) ? "yes" : "no"), "multiSelectionActionDestructive-" + index, 90.0, button);
    Label iconLabel = createRowLabel(button.getIcon() != null ? orEmpty(button.getIcon().getName()) : "", "multiSelectionActionIcon-" + index, 120.0, button);

    HBox row = new HBox(10.0, dragHandle, eventLabel, priorityLabel, destructiveLabel, iconLabel, createActionsBox(button, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, ACTION_INDEX, index, this::moveAction);
    return row;
  }

  private Label createRowLabel(String text, String id, double width, Button button) {
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

  private void openEditDialog(Button button) {
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

  private HBox createActionsBox(Button button, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    javafx.scene.control.Button editButton = RowFactory.createActionButton(Icons.PENCIL, StudioBundle.get("edit_action_title"), () -> openEditDialog(button));

    javafx.scene.control.Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_action"), null, null, StudioBundle.get("delete"));
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
