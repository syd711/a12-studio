package de.a12.studio.plugin.projectpackager;

import de.a12.studio.models.projects.Project;
import de.a12.studio.plugin.manager.IProjectSettingsPanelContribution;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class ProjectPackagerSettingsPanelContribution implements IProjectSettingsPanelContribution {

  @Override
  @NonNull
  public String getLabel() {
    return StudioBundle.get("project_packager.subtitle");
  }

  @Override
  public Node getGraphic() {
    return null;
  }

  @Override
  @NonNull
  public Node createPanel(@NonNull Project project) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("project-packager-panel.fxml"));
      loader.setClassLoader(getClass().getClassLoader());
      loader.setResources(StudioBundle.withFallback(ResourceBundle.getBundle(
          "de.a12.studio.plugin.projectpackager.messages",
          Locale.getDefault(),
          getClass().getClassLoader())));
      Parent root = loader.load();
      ProjectPackagerPanelController controller = loader.getController();
      controller.setProject(project);
      return root;
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not load project-packager-panel.fxml", e);
    }
  }
}
