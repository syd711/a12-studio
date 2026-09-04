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
import de.a12.studio.ui.editors.propertyeditors.PriorityPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Add/edit dialog for a single multi-selection {@link Button} action, opened from {@link
 * de.a12.studio.ui.editors.overviewmodel.OverviewMultiSelectionPanelController} by clicking a row or its
 * Edit button, or by the Add button. Per {@code sme-sme-om-ba-docs.md} ("Actions for Multi-Selection"), this
 * configuration "is similar to the Subheader or Footer button's configuration", so the fields mirror {@link
 * de.a12.studio.ui.editors.propertyeditors.dialogs.EventButtonDialogController} (used for those contexts)
 * almost verbatim: Event, Confirmation Title/Message, Priority/Destructive, Icon, Hide Label, Label,
 * Description, Styles and Annotations.
 * <p>
 * Unlike {@code EventButtonDialogController}, the caller ({@code OverviewMultiSelectionPanelController})
 * edits the real, attached {@link Button} directly rather than a working copy, so Cancel does not revert
 * in-progress edits here - it only discards them for a not-yet-added row (see {@code
 * Dialogs#showMultiSelectionActionForAdd}).
 */
public class MultiSelectionActionDialogController implements DialogController {

  // "The delete_selected event, which removes all multi-selected rows, is selectable from the dropdown" -
  // sme-sme-om-ba-docs.md "Actions for Multi-Selection". Unlike the shared Subheader/Footer/Row Action event
  // field, Multi-Selection actions only ever document this one suggested event; the field stays freely
  // editable for any other event name.
  private static final List<String> EVENT_SUGGESTIONS = List.of("delete_selected");

  @FXML
  private ComboBox<String> eventField;

  @FXML
  private LocalizedTextPanelController confirmationTitleController;

  @FXML
  private LocalizedTextPanelController confirmationMessageController;

  @FXML
  private PriorityPanelController priorityController;

  @FXML
  private IconPanelController iconController;

  @FXML
  private CheckBox hideLabelField;

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

  // Shared by every embedded panel so their commits aren't persisted while the dialog is open - the caller
  // (OverviewMultiSelectionPanelController) persists everything itself, in one go, once OK is pressed.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private Button button;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  // Set while eventField's editor text is being repopulated from the model (see initDialog()), so the
  // listener below doesn't mistake that programmatic change for a user edit - mirrors EventButtonDialogController.
  private boolean updatingFromModel;

  @FXML
  private void initialize() {
    confirmationTitleController.setSaveMode(saveMode);
    confirmationMessageController.setSaveMode(saveMode);
    priorityController.setSaveMode(saveMode);
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
      button.setEvent(newValue == null || newValue.isBlank() ? null : newValue);
    });
  }

  public void initDialog(Stage stage, @NonNull Button button) {
    this.stage = stage;
    this.button = button;

    updatingFromModel = true;
    try {
      eventField.getEditor().setText(button.getEvent());
    }
    finally {
      updatingFromModel = false;
    }

    hideLabelField.setSelected(Boolean.TRUE.equals(button.getLabelHidden()));
    hideLabelField.selectedProperty().addListener((observable, oldValue, newValue) ->
        button.setLabelHidden(newValue ? Boolean.TRUE : null));

    confirmationTitleController.setCustom(this::currentConfirmationTitle, this::writeConfirmationTitle);
    confirmationMessageController.setCustom(this::currentConfirmationMessage, this::writeConfirmationMessage);
    priorityController.setButton(button);
    iconController.setCustom(button::getIcon, button::setIcon);
    labelController.setCustom(button::getLabel);
    descriptionController.setCustom(button::getDescription);
    stylesController.setCustom(button::getStyles);
    annotationsController.setCustom(button::getAnnotations);
  }

  /** Unregisters the embedded panels once this dialog is closed - see {@link Dialogs}. */
  void destroy() {
    confirmationTitleController.destroy();
    confirmationMessageController.destroy();
    priorityController.destroy();
    iconController.destroy();
    labelController.destroy();
    descriptionController.destroy();
    stylesController.destroy();
    annotationsController.destroy();
  }

  @Override
  public void onDialogCancel() {
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
    Confirmation confirmation = button.getConfirmation();
    return confirmation != null ? confirmation.getTitle() : List.of();
  }

  private List<Label> writeConfirmationTitle() {
    return button.getOrCreateConfirmation().getTitle();
  }

  private List<Label> currentConfirmationMessage() {
    Confirmation confirmation = button.getConfirmation();
    return confirmation != null ? confirmation.getMessage() : List.of();
  }

  private List<Label> writeConfirmationMessage() {
    return button.getOrCreateConfirmation().getMessage();
  }
}
