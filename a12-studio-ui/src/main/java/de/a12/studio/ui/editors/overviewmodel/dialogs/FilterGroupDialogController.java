package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.FilterGroup;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Add/edit dialog for a single {@link FilterGroup}, opened from {@link
 * de.a12.studio.ui.editors.overviewmodel.CustomFilterConfigurationPanelController} by clicking a row or its
 * Edit button, or by the Add button/Add menu's generate options (see {@link Dialogs#showFilterGroupForAdd}/
 * {@link Dialogs#showFilterGroupForEdit}). Its Filter Items list is the embedded {@link
 * FilterItemsPanelController}, which opens {@link FilterItemDialogController} per item, mirroring how {@link
 * OverviewColumnDialogController} embeds simpler property editors directly.
 */
public class FilterGroupDialogController implements DialogController {

  @FXML
  private TextField nameField;
  @FXML
  private TextField groupIdField;
  @FXML
  private LocalizedTextPanelController labelController;
  @FXML
  private IconPanelController iconController;
  @FXML
  private CheckBox collapsedField;
  @FXML
  private FilterItemsPanelController filterItemsController;

  // Shared by the embedded label/icon panels so their commits aren't persisted while this dialog is open: this
  // dialog persists everything itself, in one go, once OK is pressed.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private FilterGroup group;

  private FilterGroupSnapshot snapshot;

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
    this.snapshot = new FilterGroupSnapshot(group);

    updatingFromModel = true;
    try {
      nameField.setText(group.getName());
      groupIdField.setText(group.getId());
      collapsedField.setSelected(Boolean.TRUE.equals(group.getCollapsed()));
    }
    finally {
      updatingFromModel = false;
    }

    labelController.setCustom(group::getLabel);
    iconController.setCustom(group::getIcon, group::setIcon);

    filterItemsController.init(stage, documentModelIndex, group);
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
  private void onCopyGroupId() {
    ClipboardContent content = new ClipboardContent();
    content.putString(groupIdField.getText());
    Clipboard.getSystemClipboard().setContent(content);
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
