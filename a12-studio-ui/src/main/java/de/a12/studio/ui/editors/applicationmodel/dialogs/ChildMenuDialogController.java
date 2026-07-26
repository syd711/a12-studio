package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.ActivityPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.ModuleRolesPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.FileUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Add/edit dialog for a single child {@link Menu} entry of {@link
 * de.a12.studio.ui.editors.propertyeditors.ChildMenuPanelController}, i.e. a {@link Menu} nested under {@link
 * Menu#getChildren()}. Edits the same fields as a {@link Module}'s own menu (name, activity descriptor, label,
 * roles), reusing those panels via a throwaway {@link Module} wrapper whose menu is the {@link Menu} being
 * edited/added, since those panels are written against {@link Module#getOrCreateMenu()}.
 * <p>
 * Unlike that inline module editor, this is a modal Add/Edit dialog: for an edit, the embedded panels mutate
 * the real, already-attached {@link Menu} live (so a {@link MenuSnapshot} taken before showing the dialog can
 * undo it on Cancel); for an add, they mutate a new, not-yet-attached {@link Menu} that the caller only
 * attaches to the parent's children list once this dialog resolves with {@link ButtonType#OK} (see {@link
 * Dialogs#showChildMenuForAdd}), so Cancel needs no undo there.
 */
public class ChildMenuDialogController implements DialogController {

  @FXML
  private TextField nameField;

  @FXML
  private ActivityPanelController activityController;

  @FXML
  private LocalizedTextPanelController labelController;

  @FXML
  private ModuleRolesPanelController rolesController;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  // Shared by the embedded panels so their commits aren't persisted while the dialog is open: an edit is
  // persisted directly by onDialogSubmit below once OK is pressed, and an add is only persisted by the caller
  // once the new menu is attached to its parent's children list.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private Module workingModule;

  // Non-null only when editing an existing, already-attached menu, so onDialogCancel can undo in-place edits;
  // null for a new menu that's never attached until OK, which needs no undo.
  private MenuSnapshot snapshot;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    labelController.configureModuleMenuLabel();
    activityController.setSaveMode(saveMode);
    labelController.setSaveMode(saveMode);
    rolesController.setSaveMode(saveMode);
    okButton.disableProperty().bind(nameField.textProperty().map(name -> !FileUtils.isValidWindowsFilename(name)));
    nameField.requestFocus();
  }

  @Override
  public void onDialogCancel() {
    if (snapshot != null) {
      snapshot.restore();
    }
    stage.close();
  }

  /**
   * Unregisters {@code labelController} (the only embedded panel that self-registers with {@link
   * StudioEventManager}) once this dialog is closed, regardless of how it was closed (OK, Cancel, or the
   * window's own close button) — see {@link Dialogs#show}, which calls this from the stage's {@code onHidden}
   * handler.
   */
  void destroy() {
    labelController.destroy();
  }

  @FXML
  private void onDialogSubmit() {
    Menu menu = workingModule.getOrCreateMenu();
    menu.setName(nameField.getText().trim());
    result = Optional.of(ButtonType.OK);

    if (snapshot != null) {
      ProjectItem projectItem = Studio.getSelectedProjectItem();
      if (projectItem != null) {
        projectItem.save();
        StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
      }
    }
    stage.close();
  }

  void init(Stage stage, Menu menu, MenuSnapshot snapshot) {
    this.stage = stage;
    this.snapshot = snapshot;

    Module workingModule = new Module();
    workingModule.setMenu(menu);
    this.workingModule = workingModule;

    nameField.setText(menu.getName());
    activityController.setModule(workingModule);
    labelController.setModule(workingModule);
    rolesController.setModule(workingModule);
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }
}
