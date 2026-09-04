package de.a12.studio.ui.editors.formmodel.dialogs;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.ButtonStyling;
import de.a12.studio.models.formmodel.ButtonType;
import de.a12.studio.models.formmodel.EventButton;
import de.a12.studio.models.formmodel.LocalizedText;
import de.a12.studio.models.formmodel.MultilingualText;
import de.a12.studio.models.formmodel.NavigationButton;
import de.a12.studio.models.formmodel.Style;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.formmodel.ButtonFunctionsPanelController;
import de.a12.studio.ui.editors.formmodel.ButtonGeneralPanelController;
import de.a12.studio.ui.editors.formmodel.ButtonVisualSettingsPanelController;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Add dialog for a single {@link Button} on one of a Form Model's Subheader/Footer Major/Minor button lists,
 * opened from {@link de.a12.studio.ui.editors.propertyeditors.ToolbarButtonsPanelController}'s Add button (see
 * {@link Dialogs#showButtonForAdd}). SME's {@code I_Button-form.json}: General Settings ({@link
 * ButtonGeneralPanelController}), Button Functions ({@link ButtonFunctionsPanelController}), Visual Settings
 * ({@link ButtonVisualSettingsPanelController}), Label ({@link LocalizedTextTypePanelController}), Description
 * ({@link LocalizedTextPanelController}), Styles ({@link StylesPanelController}) and Annotations ({@link
 * AnnotationsPanelController}).
 * <p>
 * {@code button} starts out unattached (not yet added to its owning list - the caller only does that once
 * {@link #isConfirmed()} is true), so unlike {@link
 * de.a12.studio.ui.editors.overviewmodel.dialogs.OverviewColumnDialogController} there's nothing to restore on
 * Cancel. Switching the Type combo (Event/Navigation) can't be applied in place - {@link EventButton} and
 * {@link NavigationButton} are distinct classes - so {@link #onTypeChanged} replaces {@link #button} wholesale
 * (carrying over the fields the two types share) and re-binds every embedded panel to the new instance.
 */
public class FormButtonDialogController implements DialogController {

  @FXML
  private ButtonGeneralPanelController generalController;
  @FXML
  private ButtonFunctionsPanelController functionsController;
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

  // Shared by every embedded panel so their commits aren't persisted while the dialog is open: this dialog
  // persists everything itself, in one go, once OK is pressed - matches OverviewColumnDialogController.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private Button button;

  private List<String> screenIds = List.of();

  private Optional<javafx.scene.control.ButtonType> result = Optional.of(javafx.scene.control.ButtonType.CANCEL);

  @FXML
  private void initialize() {
    generalController.setSaveMode(saveMode);
    functionsController.setSaveMode(saveMode);
    visualController.setSaveMode(saveMode);
    labelController.setSaveMode(saveMode);
    descriptionController.setSaveMode(saveMode);
    stylesController.setSaveMode(saveMode);
    annotationsController.setSaveMode(saveMode);

    labelController.configureCustom("label", StudioBundle.get("label"));
    descriptionController.configureCustom("description", StudioBundle.get("description"));
    annotationsController.hideAnnotationDatasetsButton();

    functionsController.setOnTypeChanged(this::onTypeChanged);

    generalController.nameProperty().addListener((observable, oldValue, newValue) -> validate());
  }

  void init(@NonNull Stage stage, @NonNull List<String> screenIds, @NonNull Button button) {
    this.stage = stage;
    this.screenIds = screenIds;
    this.button = button;

    functionsController.setScreenIds(screenIds);
    bindPanels();
    validate();
  }

  /** Unregisters the embedded panels once this dialog is closed - see {@link Dialogs#showButtonForAdd}. */
  void destroy() {
    generalController.destroy();
    functionsController.destroy();
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
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null) {
      projectItem.save();
      StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
    }
    result = Optional.of(javafx.scene.control.ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK;
  }

  /** The button currently being edited - may be a different instance than the one passed to {@link #init}
   * if {@link #onTypeChanged} swapped it for a different {@link Button} subtype since. */
  Button getButton() {
    return button;
  }

  private void onTypeChanged(ButtonType newType) {
    this.button = createButtonOfType(newType, button);
    bindPanels();
  }

  private static Button createButtonOfType(ButtonType type, Button existing) {
    Button created = type == ButtonType.NAVIGATION ? new NavigationButton() : new EventButton();
    created.setId(existing.getId());
    created.setName(existing.getName());
    created.setButtonStyling(existing.getButtonStyling());
    created.setValidation(existing.getValidation());
    created.setScope(existing.getScope());
    created.setAnnotations(existing.getAnnotations());
    return created;
  }

  private void bindPanels() {
    generalController.setButton(button);
    functionsController.setButton(button);
    visualController.setButton(button);

    labelController.setCustom(this::currentLabel, this::writeLabel);

    descriptionController.setCustom(this::currentDescriptionTexts, this::writeDescriptionTexts);

    stylesController.setCustom(this::currentStyles, this::writeStyles);
    annotationsController.setCustom(button::getAnnotations);

    validate();
  }

  private void validate() {
    String name = generalController.nameProperty().get();
    okButton.setDisable(name == null || name.isBlank());
  }

  private ButtonStyling getButtonStyling() {
    return button.getButtonStyling();
  }

  private LocalizedText currentLabel() {
    ButtonStyling styling = getButtonStyling();
    return styling != null ? styling.getLabel() : null;
  }

  private void writeLabel(LocalizedText value) {
    button.getOrCreateButtonStyling().setLabel(value);
  }

  private List<Label> currentDescriptionTexts() {
    ButtonStyling styling = getButtonStyling();
    LocalizedText description = styling != null ? styling.getDescription() : null;
    if (description instanceof MultilingualText multilingualText && multilingualText.getMultilingualText() != null) {
      return multilingualText.getMultilingualText().getText();
    }
    return List.of();
  }

  private List<Label> writeDescriptionTexts() {
    ButtonStyling styling = button.getOrCreateButtonStyling();
    MultilingualText multilingualText;
    if (styling.getDescription() instanceof MultilingualText existing) {
      multilingualText = existing;
    }
    else {
      multilingualText = new MultilingualText();
      styling.setDescription(multilingualText);
    }
    if (multilingualText.getMultilingualText() == null) {
      multilingualText.setMultilingualText(new TextContainer());
    }
    return multilingualText.getMultilingualText().getText();
  }

  private List<Style> currentStyles() {
    ButtonStyling styling = getButtonStyling();
    return styling != null ? styling.getStyle() : List.of();
  }

  private List<Style> writeStyles() {
    return button.getOrCreateButtonStyling().getStyle();
  }
}
