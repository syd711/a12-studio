package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
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
 * required name plus its menu's label and roles; further module details (menu name, activity descriptor,
 * child menus, flows) are expected to be added to this editor later.
 */
public class ModuleEditorController implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  @FXML
  private TextField nameField;

  @FXML
  private LocalizedTextPanelController labelController;

  @FXML
  private ModuleRolesPanelController rolesController;

  private final Debouncer debouncer = new Debouncer();

  private Module module;

  // Set while nameField's value is being repopulated from the model, so that programmatic update isn't
  // mistaken for a user edit and doesn't trigger a save.
  private boolean updatingFromModel;

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
  }

  public void setModule(@NonNull Module module) {
    this.module = module;
    updatingFromModel = true;
    try {
      nameField.setText(module.getName());
    } finally {
      updatingFromModel = false;
    }
    labelController.setModule(module);
    rolesController.setModule(module);
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
