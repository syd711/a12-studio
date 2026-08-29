package de.a12.studio.plugin.projectpackager;

import de.a12.studio.models.projects.Project;
import de.a12.studio.plugin.manager.IProjectToolbarButtonContribution;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.zip.ZipUtil;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProjectPackagerToolbarButtonContribution implements IProjectToolbarButtonContribution {

  @Override
  @NonNull
  public Node getGraphic() {
    return WidgetFactory.createIcon(Icons.ZIP);
  }

  @Override
  @NonNull
  public String getTooltip() {
    return StudioBundle.get("project_packager.toolbar_tooltip");
  }

  @Override
  public boolean isVisible(@NonNull Project project) {
    return ProjectPackagerSettings.load(project.getFolder()).isEnabled();
  }

  @Override
  public void execute(@NonNull Stage owner, @NonNull Project project) {
    ProjectPackagerSettings settings = ProjectPackagerSettings.load(project.getFolder());
    String targetFolderPath = settings.getTargetFolder();
    if (targetFolderPath == null || targetFolderPath.isBlank()) {
      WidgetFactory.showAlert(owner, StudioBundle.get("project_packager.no_target_folder_alert"));
      return;
    }

    File targetFolder = new File(targetFolderPath);
    if (!targetFolder.isDirectory()) {
      WidgetFactory.showAlert(owner, StudioBundle.get("project_packager.target_folder_missing_alert"));
      return;
    }

    String dateSuffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
    File zipFile = new File(targetFolder, project.getName() + "_" + dateSuffix + ".zip");
    try {
      ZipUtil.zipFolder(project.getFolder(), zipFile, (file, path) -> { });
      WidgetFactory.showInformation(owner, StudioBundle.get("project_packager.zipped_info", zipFile.getAbsolutePath()), null);
    }
    catch (IOException e) {
      WidgetFactory.showAlert(owner, StudioBundle.get("project_packager.zip_failed_alert", e.getMessage()));
    }
  }
}
