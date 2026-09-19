package de.a12.studio.ui.editors.formmodel.dialogs;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.ButtonStyling;
import de.a12.studio.models.formmodel.LocalizedText;
import de.a12.studio.models.formmodel.RowAction;
import de.a12.studio.models.formmodel.Style;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.formmodel.ButtonVisualSettingsPanelController;
import de.a12.studio.ui.editors.formmodel.RowActionFunctionsPanelController;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Edit dialog for a single {@link RowAction} of a Repeat's {@code rowActionGroup}, opened from {@link
 * de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.RepeatRowActionsPanelController}. SME's {@code
 * I_SectionRowAction-form.json}, which deliberately mirrors the Button dialog ({@link
 * FormButtonDialogController}): Button Functions ({@link RowActionFunctionsPanelController}), Confirmation Title
 * and Message ({@link LocalizedTextPanelController} each), Visual Settings ({@link
 * ButtonVisualSettingsPanelController}), Label ({@link LocalizedTextTypePanelController}), Description ({@link
 * LocalizedTextPanelController}), Styles ({@link StylesPanelController}) and Annotations ({@link
 * AnnotationsPanelController}).
 * <p>
 * Edits a working copy handed in by {@link Dialogs#showRowActionForEdit}, so Cancel leaves the real, attached
 * action untouched. Nothing here is persisted: the caller replaces the original with {@link #getRowAction()} and
 * saves once OK was pressed.
 */
public class RowActionDialogController implements DialogController {

  @FXML
  private RowActionFunctionsPanelController functionsController;
  @FXML
  private LocalizedTextPanelController confirmationTitleController;
  @FXML
  private LocalizedTextPanelController confirmationMessageController;
  @FXML
  private ButtonVisualSettingsPanelController visualController;
  @FXML
  private LocalizedTextTypePanelController labelController;
  @FXML
  private LocalizedTextPanelController descriptionController;
  @FXML
  private StylesPanelController stylesController;
  @FXML
  private AnnotationsPanelController annotationsController;

  @FXML
  private javafx.scene.control.Button okButton;
  @FXML
  private javafx.scene.control.Button cancelButton;

  // Shared by every embedded panel so their commits aren't persisted while the dialog is open.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private RowAction rowAction;

  private boolean confirmed;

  @FXML
  private void initialize() {
    functionsController.setSaveMode(saveMode);
    confirmationTitleController.setSaveMode(saveMode);
    confirmationMessageController.setSaveMode(saveMode);
    visualController.setSaveMode(saveMode);
    labelController.setSaveMode(saveMode);
    descriptionController.setSaveMode(saveMode);
    stylesController.setSaveMode(saveMode);
    annotationsController.setSaveMode(saveMode);

    confirmationTitleController.configureCustom("rowActionConfirmationTitle", StudioBundle.get("confirmation_title"));
    confirmationMessageController.configureCustom("rowActionConfirmationMessage", StudioBundle.get("confirmation_message"));
    labelController.configureCustom("rowActionLabel", StudioBundle.get("label"));
    descriptionController.configureCustom("rowActionDescription", StudioBundle.get("description"));
    annotationsController.hideAnnotationDatasetsButton();

    functionsController.eventProperty().addListener((observable, oldValue, newValue) -> validate());
  }

  void init(@NonNull Stage stage, @Nullable ElementIndex elementIndex, @NonNull RowAction rowAction) {
    this.stage = stage;
    this.rowAction = rowAction;

    functionsController.setRowAction(rowAction);

    confirmationTitleController.setCustom(() -> texts(rowAction.getConfirmationDialogTitle()),
        () -> writeTexts(rowAction::getConfirmationDialogTitle, rowAction::setConfirmationDialogTitle));
    confirmationMessageController.setCustom(() -> texts(rowAction.getConfirmation()),
        () -> writeTexts(rowAction::getConfirmation, rowAction::setConfirmation));

    visualController.setStyling(rowAction::getButtonStyling, this::getOrCreateButtonStyling);

    labelController.setCustom(this::currentLabel, value -> getOrCreateButtonStyling().setLabel(value));
    labelController.setFieldSuggestionSource(elementIndex);

    descriptionController.setCustom(
        () -> texts(rowAction.getButtonStyling() != null ? rowAction.getButtonStyling().getDescription() : null),
        () -> writeTexts(() -> getOrCreateButtonStyling().getDescription(), value -> getOrCreateButtonStyling().setDescription(value)));

    stylesController.setCustom(
        () -> rowAction.getButtonStyling() != null ? rowAction.getButtonStyling().getStyle() : List.<Style>of(),
        () -> getOrCreateButtonStyling().getStyle());

    annotationsController.setCustom(rowAction::getAnnotations);

    validate();
  }

  /** Unregisters the embedded panels once this dialog is closed - see {@link Dialogs#showRowActionForEdit}. */
  void destroy() {
    functionsController.destroy();
    confirmationTitleController.destroy();
    confirmationMessageController.destroy();
    visualController.destroy();
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
    dropEmptyContainers();
    confirmed = true;
    stage.close();
  }

  boolean isConfirmed() {
    return confirmed;
  }

  RowAction getRowAction() {
    return rowAction;
  }

  private void validate() {
    String event = functionsController.eventProperty().get();
    okButton.setDisable(event == null || event.isBlank());
  }

  /**
   * The panels only ever create {@code buttonStyling}/{@code confirmation*} lazily on an actual edit, but a
   * user can type something and delete it again. Drops whatever ended up empty so the saved file doesn't gain
   * an empty {@code "buttonStyling": {}} (or a confirmation without any text) that the source never had.
   */
  private void dropEmptyContainers() {
    ButtonStyling styling = rowAction.getButtonStyling();
    if (styling != null) {
      if (styling.getDescription() != null && styling.getDescription().getText().isEmpty()) {
        styling.setDescription(null);
      }
      if (styling.isBlank()) {
        rowAction.setButtonStyling(null);
      }
    }
    if (rowAction.getConfirmation() != null && rowAction.getConfirmation().getText().isEmpty()) {
      rowAction.setConfirmation(null);
    }
    if (rowAction.getConfirmationDialogTitle() != null && rowAction.getConfirmationDialogTitle().getText().isEmpty()) {
      rowAction.setConfirmationDialogTitle(null);
    }
  }

  private ButtonStyling getOrCreateButtonStyling() {
    if (rowAction.getButtonStyling() == null) {
      rowAction.setButtonStyling(new ButtonStyling());
    }
    return rowAction.getButtonStyling();
  }

  private LocalizedText currentLabel() {
    ButtonStyling styling = rowAction.getButtonStyling();
    return styling != null ? styling.getLabel() : null;
  }

  private static List<Label> texts(@Nullable TextContainer container) {
    return container != null ? container.getText() : List.of();
  }

  private static List<Label> writeTexts(Supplier<TextContainer> getter, Consumer<TextContainer> setter) {
    TextContainer container = getter.get();
    if (container == null) {
      container = new TextContainer();
      setter.accept(container);
    }
    return container.getText();
  }
}
