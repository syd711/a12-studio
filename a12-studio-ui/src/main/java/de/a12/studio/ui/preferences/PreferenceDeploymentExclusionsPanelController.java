package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.GeneralSettings;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.preferences.dialogs.DeploymentExclusionsDialogController;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Preferences panel for managing the deployment exclusion list stored in
 * {@link GeneralSettings#getDeploymentExclusions()}.
 *
 * <p>Displays the current exclusion paths in a read-only {@link ListView} and provides
 * an "Edit Exclusions" button that opens {@link DeploymentExclusionsDialogController} to
 * let the user pick which model files should be excluded from deployment.
 */
public class PreferenceDeploymentExclusionsPanelController implements Initializable {

  @FXML
  private ListView<String> exclusionsList;

  private ProjectRootSettings rootSettings;
  private GeneralSettings general;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Project project = Studio.getCurrentProject();
    if (project == null) {
      return;
    }

    rootSettings = project.getSettings().getProjectRootSettings();
    general = rootSettings.getGeneral();

    exclusionsList.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(String path, boolean empty) {
        super.updateItem(path, empty);
        setText(empty || path == null ? null : path);
      }
    });

    exclusionsList.setPlaceholder(
        WidgetFactory.createDefaultLabel(StudioBundle.get("there_are_no_entries_yet")));

    refreshList();
  }

  @FXML
  private void onEditExclusions() {
    Project project = Studio.getCurrentProject();
    if (project == null) {
      return;
    }

    Optional<List<String>> result = DeploymentExclusionsDialogController.show(
        Studio.stage,
        project.getRoot(),
        general.getDeploymentExclusions());

    result.ifPresent(paths -> {
      general.setDeploymentExclusions(paths);
      rootSettings.save();
      refreshList();
    });
  }

  private void refreshList() {
    List<String> exclusions = general.getDeploymentExclusions();
    exclusionsList.getItems().setAll(exclusions);
  }
}
