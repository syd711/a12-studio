package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.Label;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.Confirmation;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.overviewmodel.StylesPanelController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Add/edit dialog for a single context-menu action ({@link Button}), opened from {@link
 * ContextMenuGroupDialogController} (via {@link Dialogs#showContextMenuActionForAdd}/{@link
 * Dialogs#showContextMenuActionForEdit}). Mirrors {@link
 * de.a12.studio.ui.editors.propertyeditors.dialogs.EventButtonDialogController} (Event, Confirmation Title/
 * Message, Icon, Label/Description, Styles, Annotations) but - per {@link
 * de.a12.studio.models.overviewmodel.ActionGroup}'s class doc, matching the SME reference's Context Menu
 * documentation - deliberately omits Priority, Destructive and Hide Label, none of which apply to a menu item.
 * <p>
 * The action is mutated live and {@link #onDialogCancel} restores a {@link ContextMenuActionSnapshot} taken in
 * {@link #init}, mirroring {@link FilterItemDialogController} - the owning {@link ContextMenuGroupDialogController}
 * only adds a brand-new action to its group once this dialog is confirmed (see {@link Dialogs#showContextMenuActionForAdd}).
 */
public class ContextMenuActionDialogController implements DialogController {

  private static final List<String> EVENT_SUGGESTIONS = List.of("add", "edit", "delete", "copy");

  @FXML
  private ComboBox<String> eventField;

  @FXML
  private LocalizedTextPanelController confirmationTitleController;

  @FXML
  private LocalizedTextPanelController confirmationMessageController;

  @FXML
  private IconPanelController iconController;

  @FXML
  private LocalizedTextPanelController labelController;

  @FXML
  private LocalizedTextPanelController descriptionController;

  @FXML
  private StylesPanelController stylesController;

  @FXML
  private AnnotationsPanelController annotationsController;

  @FXML
  private javafx.scene.control.Button okButton;

  // Shared by every embedded panel so their commits aren't persisted while this dialog is open - the owning
  // ContextMenuGroupDialogController persists everything itself, in one go, once its own OK is pressed.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private Button action;

  private ContextMenuActionSnapshot snapshot;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  // Set while eventField's editor text is being repopulated from the model, so the listener below doesn't
  // mistake that programmatic change for a user edit.
  private boolean updatingFromModel;

  @FXML
  private void initialize() {
    confirmationTitleController.setSaveMode(saveMode);
    confirmationMessageController.setSaveMode(saveMode);
    iconController.setSaveMode(saveMode);
    labelController.setSaveMode(saveMode);
    descriptionController.setSaveMode(saveMode);
    stylesController.setSaveMode(saveMode);
    annotationsController.setSaveMode(saveMode);

    confirmationTitleController.configureCustom("confirmationTitle", StudioBundle.get("confirmation_title"));
    confirmationMessageController.configureCustom("confirmationMessage", StudioBundle.get("confirmation_message"));
    labelController.configureCustom("label", StudioBundle.get("label"));
    descriptionController.configureCustom("description", StudioBundle.get("description"));
    annotationsController.hideAnnotationDatasetsButton();

    eventField.setEditable(true);
    eventField.getItems().setAll(EVENT_SUGGESTIONS);
    okButton.disableProperty().bind(eventField.getEditor().textProperty().isEmpty());
    eventField.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      action.setEvent(newValue == null || newValue.isBlank() ? null : newValue);
    });
  }

  void init(@NonNull Stage stage, @NonNull Button action) {
    this.stage = stage;
    this.action = action;
    this.snapshot = new ContextMenuActionSnapshot(action);

    updatingFromModel = true;
    try {
      eventField.getEditor().setText(action.getEvent());
    }
    finally {
      updatingFromModel = false;
    }

    confirmationTitleController.setCustom(this::currentConfirmationTitle, this::writeConfirmationTitle);
    confirmationMessageController.setCustom(this::currentConfirmationMessage, this::writeConfirmationMessage);
    iconController.setCustom(action::getIcon, action::setIcon);
    labelController.setCustom(action::getLabel);
    descriptionController.setCustom(action::getDescription);
    stylesController.setCustom(action::getStyles);
    annotationsController.setCustom(action::getAnnotations);
  }

  /** Unregisters the embedded panels once this dialog is closed - see {@link Dialogs}. */
  void destroy() {
    confirmationTitleController.destroy();
    confirmationMessageController.destroy();
    iconController.destroy();
    labelController.destroy();
    descriptionController.destroy();
    stylesController.destroy();
    annotationsController.destroy();
  }

  @Override
  public void onDialogCancel() {
    snapshot.restore();
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  private List<Label> currentConfirmationTitle() {
    Confirmation confirmation = action.getConfirmation();
    return confirmation != null ? confirmation.getTitle() : List.of();
  }

  private List<Label> writeConfirmationTitle() {
    return action.getOrCreateConfirmation().getTitle();
  }

  private List<Label> currentConfirmationMessage() {
    Confirmation confirmation = action.getConfirmation();
    return confirmation != null ? confirmation.getMessage() : List.of();
  }

  private List<Label> writeConfirmationMessage() {
    return action.getOrCreateConfirmation().getMessage();
  }
}
