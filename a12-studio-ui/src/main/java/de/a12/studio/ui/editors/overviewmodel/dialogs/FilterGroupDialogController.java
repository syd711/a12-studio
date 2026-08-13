package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.FilterGroup;
import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.overviewmodel.OverviewElementOptions;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Add/edit dialog for a single {@link FilterGroup}, opened from {@link
 * de.a12.studio.ui.editors.overviewmodel.CustomFilterConfigurationPanelController} by clicking a row or its
 * Edit button, or by the Add button/Add menu's generate options (see {@link Dialogs#showFilterGroupForAdd}/
 * {@link Dialogs#showFilterGroupForEdit}). Its Filter Items list opens {@link FilterItemDialogController} per
 * item, mirroring how {@link OverviewColumnDialogController} embeds simpler property editors directly.
 */
public class FilterGroupDialogController implements DialogController {

  @FXML
  private TextField nameField;
  @FXML
  private LocalizedTextPanelController labelController;
  @FXML
  private IconPanelController iconController;
  @FXML
  private CheckBox collapsedField;
  @FXML
  private HBox filterItemColumnHeaders;
  @FXML
  private VBox filterItemRows;
  @FXML
  private Label filterItemsEmptyLabel;

  // Shared by the embedded label/icon panels so their commits aren't persisted while this dialog is open: this
  // dialog persists everything itself, in one go, once OK is pressed.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private FilterGroup group;

  private FilterGroupSnapshot snapshot;

  private ElementIndex documentModelIndex;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits.
  private boolean updatingFromModel;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    labelController.configureCustom("label", "LABEL");
    labelController.setSaveMode(saveMode);
    iconController.setSaveMode(saveMode);

    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        group.setName(blankToNull(newValue));
      }
    });
    collapsedField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        group.setCollapsed(newValue ? Boolean.TRUE : null);
      }
    });
  }

  void init(Stage stage, ElementIndex documentModelIndex, @NonNull FilterGroup group) {
    this.stage = stage;
    this.group = group;
    this.documentModelIndex = documentModelIndex;
    this.snapshot = new FilterGroupSnapshot(group);

    updatingFromModel = true;
    try {
      nameField.setText(group.getName());
      collapsedField.setSelected(Boolean.TRUE.equals(group.getCollapsed()));
    }
    finally {
      updatingFromModel = false;
    }

    labelController.setCustom(group::getLabel);
    iconController.setCustom(group::getIcon, group::setIcon);

    rebuildFilterItemRows();
  }

  /** Unregisters the embedded panels once this dialog is closed - see {@link Dialogs#showFilterGroup}, which
   * calls this from the stage's {@code onHidden} handler. */
  void destroy() {
    labelController.destroy();
    iconController.destroy();
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
  private void onAddFilterItem() {
    FilterItem item = new FilterItem();
    item.setId("filter-item-" + shortId());
    if (Dialogs.showFilterItemForAdd(stage, documentModelIndex, item)) {
      group.getFilterItems().add(item);
      rebuildFilterItemRows();
    }
  }

  private void rebuildFilterItemRows() {
    filterItemRows.getChildren().clear();

    List<FilterItem> items = group.getFilterItems();
    boolean empty = items.isEmpty();
    filterItemColumnHeaders.setVisible(!empty);
    filterItemColumnHeaders.setManaged(!empty);
    filterItemsEmptyLabel.setVisible(empty);
    filterItemsEmptyLabel.setManaged(empty);

    for (int index = 0; index < items.size(); index++) {
      filterItemRows.getChildren().add(createFilterItemRow(items.get(index), index, items.size()));
    }
  }

  private HBox createFilterItemRow(FilterItem item, int index, int rowCount) {
    Label summaryLabel = new Label(filterItemSummary(item));
    summaryLabel.setId("filterItemSummary-" + index);
    summaryLabel.setMaxWidth(Double.MAX_VALUE);
    summaryLabel.setCursor(Cursor.HAND);
    summaryLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(item);
      }
    });
    HBox.setHgrow(summaryLabel, Priority.ALWAYS);

    HBox actionsBox = createActionsBox(item, index, rowCount);

    HBox row = new HBox(10.0, summaryLabel, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    return row;
  }

  private String filterItemSummary(FilterItem item) {
    String label = item.getLabel().stream()
        .map(de.a12.studio.models.Label::getText)
        .filter(text -> text != null && !text.isBlank())
        .findFirst()
        .orElse(null);
    if (label != null) {
      return label;
    }
    if (item.getFieldRef() != null && item.getFieldRef().getFieldId() != null) {
      return OverviewElementOptions.displayPath(documentModelIndex, item.getFieldRef().getFieldId());
    }
    return "";
  }

  private void openEditDialog(FilterItem item) {
    if (Dialogs.showFilterItemForEdit(stage, documentModelIndex, item)) {
      rebuildFilterItemRows();
    }
  }

  private HBox createActionsBox(FilterItem item, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveFilterItem);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(item));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> confirmResult = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_filter_item"), null, null, StudioBundle.get("delete"));
      if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
        group.getFilterItems().remove(item);
        rebuildFilterItemRows();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveFilterItem(int fromIndex, int toIndex) {
    Collections.swap(group.getFilterItems(), fromIndex, toIndex);
    rebuildFilterItemRows();
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }
}
