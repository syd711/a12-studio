package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.models.Locale;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.components.NewDocumentModelPanelController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Dialog for the "Move…" action in the Document Model tree. Lets the user move a group
 * to either an existing Document Model (appending it as a new root group there, and replacing it in
 * the source model with an Include pointing at the target) or a brand-new Document Model (which
 * gets the group as its sole root group, with the same include-replacement in the source).
 */
public class MoveGroupDialogController implements DialogController {

  /**
   * Sealed result type — exactly one variant is non-null after the dialog is confirmed.
   */
  public sealed interface MoveTarget permits MoveTarget.ExistingModel, MoveTarget.NewModel {

    /**
     * The user picked an existing Document Model as the target.
     */
    record ExistingModel(@NonNull DocumentModel documentModel) implements MoveTarget {
    }

    /**
     * The user wants a brand-new Document Model.
     */
    record NewModel(
        @NonNull String modelName,
        @NonNull ProjectItem folder,
        @NonNull List<Locale> locales,
        @NonNull List<String> roles
    ) implements MoveTarget {
    }
  }

  // -------------------------------------------------------------------------
  // FXML fields
  // -------------------------------------------------------------------------

  @FXML
  private RadioButton existingModelRadio;
  @FXML
  private RadioButton newModelRadio;
  @FXML
  private ToggleGroup modeGroup;

  @FXML
  private VBox existingModelSection;
  @FXML
  private ComboBox<DocumentModel> existingModelCombo;

  @FXML
  private VBox newModelSection;
  @FXML
  private NewDocumentModelPanelController newDocumentModelPanelController;

  @FXML
  private Button okButton;
  @FXML
  private Button cancelButton;

  @FXML
  private ErrorContainerController errorContainerController;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------

  private Stage stage;
  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);
  @Nullable
  private MoveTarget moveTarget;

  // -------------------------------------------------------------------------
  // Initialisation
  // -------------------------------------------------------------------------

  @FXML
  private void initialize() {
    existingModelCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(DocumentModel dm) {
        return dm == null ? "" : dm.getId();
      }

      @Override
      public DocumentModel fromString(String s) {
        return null;
      }
    });

    modeGroup.selectedToggleProperty().addListener((obs, old, val) -> {
      boolean useExisting = val == existingModelRadio;
      existingModelSection.setVisible(useExisting);
      existingModelSection.setManaged(useExisting);
      newModelSection.setVisible(!useExisting);
      newModelSection.setManaged(!useExisting);
      validate();
    });

    existingModelCombo.valueProperty().addListener((obs, old, val) -> validate());
    newDocumentModelPanelController.setOnChanged(this::validate);

    // Start with "existing model" selected
    existingModelRadio.setSelected(true);
  }

  /**
   * Populates the dialog's existing-model combo and seeds the new-model panel for the given source item.
   *
   * @param projectItem the source Document Model's project item (used to find sibling models)
   * @param groupName   the group being moved — used as the default name for a new model
   */
  public void init(@NonNull Stage stage, @NonNull ProjectItem projectItem, @NonNull String groupName) {
    this.stage = stage;

    // Populate existing document models (all other models in the project).
    List<DocumentModel> allDocumentModels = ProjectDocumentModels.getOtherDocumentModels(projectItem);
    allDocumentModels.sort(Comparator.comparing(DocumentModel::getId));
    existingModelCombo.getItems().setAll(allDocumentModels);

    // Seed new-model panel with a suggested name derived from the group
    newDocumentModelPanelController.init(projectItem.getParent(), groupName + "_DM");

    validate();
    stage.setUserData(this);
  }

  private void validate() {
    boolean useExisting = existingModelRadio.isSelected();
    if (useExisting) {
      errorContainerController.hide();
      okButton.setDisable(existingModelCombo.getValue() == null);
    }
    else {
      Optional<String> nameConventionError = newDocumentModelPanelController.getNameConventionError();
      Optional<String> suffixError = nameConventionError.isPresent() ? Optional.empty()
          : newDocumentModelPanelController.getSuffixError();
      nameConventionError.or(() -> suffixError).ifPresentOrElse(
          msg -> errorContainerController.show("ERROR", msg),
          errorContainerController::hide);
      okButton.setDisable(!newDocumentModelPanelController.isValid());
    }
  }

  // -------------------------------------------------------------------------
  // Actions
  // -------------------------------------------------------------------------

  @FXML
  private void onDialogSubmit() {
    if (existingModelRadio.isSelected()) {
      DocumentModel selected = existingModelCombo.getValue();
      if (selected == null) return;
      moveTarget = new MoveTarget.ExistingModel(selected);
    }
    else {
      if (!newDocumentModelPanelController.isValid()) return;
      moveTarget = new MoveTarget.NewModel(
          newDocumentModelPanelController.getModelName(),
          newDocumentModelPanelController.getFolder(),
          newDocumentModelPanelController.getLocales(),
          newDocumentModelPanelController.getRoles());
    }
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  // -------------------------------------------------------------------------
  // Result
  // -------------------------------------------------------------------------

  public Optional<MoveTarget> getMoveTarget() {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      return Optional.ofNullable(moveTarget);
    }
    return Optional.empty();
  }

  // -------------------------------------------------------------------------
  // Static factory
  // -------------------------------------------------------------------------

  /**
   * Opens the Move Group dialog and blocks until it is closed.
   *
   * @param owner       the owner stage
   * @param projectItem the source Document Model's project item
   * @param groupName   the name of the group being moved (used as default name for a new model)
   * @return the chosen {@link MoveTarget}, or empty if the dialog was cancelled
   */
  public static Optional<MoveTarget> show(
      @NonNull Stage owner,
      @NonNull ProjectItem projectItem,
      @NonNull String groupName) {

    FXMLLoader fxmlLoader = new FXMLLoader(
        MoveGroupDialogController.class.getResource("move-group-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("move-group-dialog", fxmlLoader, owner, StudioBundle.get("move_group_dialog.title"));
    MoveGroupDialogController controller = (MoveGroupDialogController) stage.getUserData();
    controller.init(stage, projectItem, groupName);

    WidgetFactory.installResizable(stage);
    stage.setMinWidth(750);
    stage.setMinHeight(750);

    stage.showAndWait();
    return controller.getMoveTarget();
  }
}
