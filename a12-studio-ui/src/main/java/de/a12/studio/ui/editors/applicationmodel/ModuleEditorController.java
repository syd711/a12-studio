package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.propertyeditors.ActivityPanelController;
import de.a12.studio.ui.editors.propertyeditors.ChildMenuPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.ModuleRolesPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Debouncer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Inline editor for a single {@link Module}, meant to be displayed in an editor container (e.g. the
 * application model editor's {@code editorContainer}) once a module is selected. Currently edits the module's
 * required name plus its menu's name, label, activity descriptor, roles and child menus; further module
 * details (flows) are expected to be added to this editor later.
 */
public class ModuleEditorController implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  @FXML
  private TextField nameField;

  @FXML
  private TextField menuNameField;

  @FXML
  private LocalizedTextPanelController labelController;

  @FXML
  private ActivityPanelController activityController;

  @FXML
  private ModuleRolesPanelController rolesController;

  @FXML
  private ChildMenuPanelController childMenuController;

  private final Debouncer debouncer = new Debouncer();

  private Module module;

  // Set while nameField's value is being repopulated from the model, so that programmatic update isn't
  // mistaken for a user edit and doesn't trigger a save.
  private boolean updatingFromModel;

  // Notified when the close button is pressed, e.g. by ApplicationModelEditorController to remove this panel
  // from its editorContainer. Set via setOnCloseRequested once this panel is loaded from FXML.
  private Runnable onCloseRequested;

  public void setOnCloseRequested(@NonNull Runnable onCloseRequested) {
    this.onCloseRequested = onCloseRequested;
  }

  @FXML
  private void onClose() {
    if (onCloseRequested != null) {
      onCloseRequested.run();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    labelController.configureModuleMenuLabel();
    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || module == null) {
        return;
      }
      module.setName(newValue);
      debouncer.debounce("module-name", this::commitNameChange, COMMIT_DEBOUNCE_MS, true);
    });
    menuNameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || module == null) {
        return;
      }
      module.getOrCreateMenu().setName(newValue);
      debouncer.debounce("menu-name", this::commitNameChange, COMMIT_DEBOUNCE_MS, true);
    });
  }

  public void setModule(@NonNull Module module) {
    this.module = module;
    updatingFromModel = true;
    try {
      nameField.setText(module.getName());
      Menu menu = module.getMenu();
      menuNameField.setText(menu != null && menu.getName() != null ? menu.getName() : "");
    } finally {
      updatingFromModel = false;
    }
    labelController.setModule(module);
    activityController.setModule(module);
    rolesController.setModule(module);
    childMenuController.setModule(module);
  }

  public void destroy() {
    labelController.destroy();
  }

  private void commitNameChange() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    projectItem.save();
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
  }
}
