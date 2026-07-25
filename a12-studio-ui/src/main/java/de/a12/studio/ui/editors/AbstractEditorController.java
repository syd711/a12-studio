package de.a12.studio.ui.editors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.SystemUtil;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;

@Slf4j
abstract public class AbstractEditorController implements StudioEventListener {

  protected ProjectItem projectItem;
  private BaseTableSettings baseTableSettings;

  /**
   * Injected via {@code fx:include} in every editor FXML. After {@link #load} is called
   * the component is wired to the current {@code projectItem}.
   */
  @FXML
  protected EditorFileToolbarButtons fileToolbarButtons;

  public void save() {
    projectItem.save();
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
    this.loadModel(projectItem.getModel());

    if (fileToolbarButtons != null) {
      fileToolbarButtons.setFileSupplier(() -> projectItem.getFile());
    }

    StudioEventManager.getInstance().addListener(this);
  }

  protected BaseTableSettings getBaseTableSettings() {
    if (baseTableSettings == null) {
      baseTableSettings = LocalUISettings.getTablePreference(getModelType().getValue());
    }
    return baseTableSettings;
  }

  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    this.save();
  }

  /**
   * Unregisters this editor once its tab is closed, so a closed editor stops receiving (and reacting to,
   * e.g. via {@link #modelSaved}) events fired for other tabs still open. {@code event.getItem()} is compared
   * by path (see {@link ProjectItem#equals}) rather than reference, since {@link #load} is always called with
   * whatever {@link ProjectItem} instance the caller happens to hold.
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
