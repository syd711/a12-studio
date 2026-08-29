package de.a12.studio.ui.editors.propertyeditors.dialogs;

import de.a12.studio.models.Label;
import de.a12.studio.models.overviewmodel.Confirmation;
import de.a12.studio.models.overviewmodel.OverviewButtonLike;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.overviewmodel.StylesPanelController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.PriorityPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Modal dialog for creating/editing a single {@link OverviewButtonLike} row (an overviewmodel {@link
 * de.a12.studio.models.overviewmodel.Button}/{@link de.a12.studio.models.overviewmodel.ButtonElement}), opened
 * via Add/Edit from {@link de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController}. SME's {@code
 * sme-sme-om-ba-docs.md} "Row Action Settings"/"Button Styling" sections: Event, Confirmation Title/Message
 * ({@link LocalizedTextPanelController}), Priority/Destructive ({@link PriorityPanelController}), Icon ({@link
 * IconPanelController}) plus a plain Hide Label checkbox, Label/Description ({@link LocalizedTextPanelController}
 * again) and Styles ({@link StylesPanelController})/Annotations ({@link AnnotationsPanelController}).
 * <p>
 * {@code button} starts out unattached (Add) or a JSON clone of the real row (Edit, see {@link
 * Dialogs#showEventButtonForEdit}) - the caller only splices it into its owning list once {@link #isConfirmed()}
 * is true, mirroring {@link de.a12.studio.ui.editors.formmodel.dialogs.FormButtonDialogController}.
 */
public class EventButtonDialogController implements DialogController {

  // "Select or enter the event...The default event is delete" (Row Action) / "The add event is selectable by
  // default" (Subheader/Footer Button) - see sme-sme-om-ba-docs.md. Both contexts share this one dialog, so both
  // documented defaults (plus the other common CRUD-style events actually seen in SME reference fixtures) are
  // offered; the field stays freely editable for any other event name.
  private static final List<String> EVENT_SUGGESTIONS = List.of("add", "edit", "delete", "copy");

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
  private Button okButton;

  // Shared by every embedded panel so their commits aren't persisted while the dialog is open - this dialog's
  // caller (EventButtonsPanelController) persists everything itself, in one go, once OK is pressed.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private OverviewButtonLike button;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  // Set while eventField's editor text is being repopulated from the model (see init()), so the listener below
  // doesn't mistake that programmatic change for a user edit - mirrors IconComboController.setValue().
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

  void init(@NonNull Stage stage, @NonNull OverviewButtonLike button) {
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

  OverviewButtonLike getButton() {
    return button;
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
