package de.a12.studio.ui.editors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.SystemUtil;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
abstract public class AbstractEditorController implements StudioEventListener {

  protected ProjectItem projectItem;
  private BaseTableSettings baseTableSettings;

  /**
   * Set while fields are being repopulated from the model, so that programmatic updates aren't
   * mistaken for user edits and don't trigger a save.
   */
  protected boolean updatingFromModel;

  /**
   * Injected via {@code fx:include} in every editor FXML. After {@link #load} is called
   * the component is wired to the current {@code projectItem}.
   */
  @FXML
  protected EditorFileToolbarButtonsController fileToolbarButtonsController;

  /**
   * Injected via {@code fx:include} in every editor FXML that shows the settings button.
   * May be null for editors without a settings button.
   */
  @FXML
  protected EditorSettingsToolbarButtonController settingsToolbarButtonController;

  protected void updateSettingsErrorBadge() {
    if (settingsToolbarButtonController == null) {
      return;
    }
    settingsToolbarButtonController.updateErrorBadge();
  }

  public void save() {
    projectItem.save();
  }

  /**
   * Wires a {@link CheckBox} directly to a model setter: on every user toggle (guarded by {@link
   * #updatingFromModel} so repopulating the control from {@link #loadModel} doesn't re-trigger a save),
   * calls {@code onChange} with the new value and saves. The generic binding point for the many plain
   * checkboxes used by flat, non-panel-based editors (e.g. {@code OverviewModelEditorController}). Not
   * named {@code commitChange} to avoid colliding with the several existing editor controllers that
   * already declare their own private method of that name.
   */
  protected void bindCheckBox(@NonNull CheckBox checkBox, @NonNull Consumer<Boolean> onChange) {
    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      onChange.accept(newValue);
      projectItem.save();
      StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
    });
  }

  @FXML
  public void onFileOpen(ActionEvent e) {
    File file = projectItem.getFile();
    SystemUtil.openFile(file);
  }

  @FXML
  public void onFileEdit(ActionEvent e) {
    File file = projectItem.getFile();
    SystemUtil.editFile(file);
  }

  public void load(@NonNull ProjectItem projectItem) {
    this.projectItem = projectItem;

    if (fileToolbarButtonsController != null) {
      fileToolbarButtonsController.setFileSupplier(() -> projectItem.getFile());
    }

    if (settingsToolbarButtonController != null) {
      settingsToolbarButtonController.setIssuesSupplier(() -> projectItem != null && projectItem.getModel() != null
          ? Studio.getValidationService().getSettingsIssueMessages(projectItem.getModel())
          : List.of());
    }

    this.loadModel(projectItem.getModel());

    StudioEventManager.getInstance().addListener(this);
  }

  protected BaseTableSettings getBaseTableSettings() {
    if (baseTableSettings == null) {
      baseTableSettings = LocalUISettings.getTablePreference(getModelType().getValue());
    }
    return baseTableSettings;
  }

  /**
   * Unregisters this editor once its tab is closed, so a closed editor stops receiving (and reacting to,
   * e.g. via {@code modelSaved} overrides such as {@link
   * de.a12.studio.ui.editors.typedefinitionmodel.TypeDefintionModelEditorController#modelSaved}) events fired
   * for other tabs still open. {@code event.getItem()} is compared by path (see {@link ProjectItem#equals})
   * rather than reference, since {@link #load} is always called with whatever {@link ProjectItem} instance the
   * caller happens to hold.
   */
  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    if (event.getItem().equals(projectItem)) {
      StudioEventManager.getInstance().removeListener(this);
    }
  }

  @NonNull
  abstract public ModelType getModelType();

  abstract public void loadModel(@NonNull A12Model<?> model);
}
