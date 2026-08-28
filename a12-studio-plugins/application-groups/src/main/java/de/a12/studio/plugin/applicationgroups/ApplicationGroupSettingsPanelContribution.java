package de.a12.studio.plugin.applicationgroups;

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

public class ApplicationGroupSettingsPanelContribution implements IProjectSettingsPanelContribution {

  @Override
  @NonNull
  public String getLabel() {
    return StudioBundle.get("application_groups.subtitle");
  }

  @Override
  public Node getGraphic() {
    return null;
  }

  @Override
  @NonNull
  public Node createPanel(@NonNull Project project) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("application-groups-panel.fxml"));
      loader.setClassLoader(getClass().getClassLoader());
      loader.setResources(StudioBundle.withFallback(ResourceBundle.getBundle(
          "de.a12.studio.plugin.applicationgroups.messages",
          Locale.getDefault(),
          getClass().getClassLoader())));
      Parent root = loader.load();
      ApplicationGroupsPanelController controller = loader.getController();
      controller.setProject(project);
      return root;
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not load application-groups-panel.fxml", e);
    }
  }
}
