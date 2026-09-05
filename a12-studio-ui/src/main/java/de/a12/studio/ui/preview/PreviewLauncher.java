package de.a12.studio.ui.preview;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.PreviewAppSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.SystemUtil;
import de.a12.studio.ui.util.browsers.Browser;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * Entry point for the Preview toolbar buttons: ensures the embedded {@link PreviewServer} is running, registers
 * the clicked editor's model under its id, and opens the preview URL.
 */
public class PreviewLauncher {

  private PreviewLauncher() {
  }

  /**
   * Used by the Application Model editor's Preview button: opens the preview URL in the browser configured via
   * {@link PreviewAppSettings.BrowserType}.
   */
  public static void openPreview(@NonNull ProjectItem projectItem) {
    openPreview(projectItem, url -> SystemUtil.openUrl(url, getPreviewAppSettings().getBrowserType()));
  }

  private static PreviewAppSettings getPreviewAppSettings() {
    Project project = Studio.getCurrentProject();
    return project != null
        ? project.getSettings().getProjectRootSettings().getPreviewApp()
        : new PreviewAppSettings();
  }

  /**
   * Used by the Form Model editor's Preview button: opens the preview URL via {@link Browser}.
   */
  public static void openPreviewInBrowser(@NonNull ProjectItem projectItem) {
    openPreview(projectItem, Browser.getInstance()::openUrl);
  }

  private static void openPreview(@NonNull ProjectItem projectItem, @NonNull Consumer<String> opener) {
    PreviewServer server = PreviewServer.getOrStart();
    String modelId = projectItem.getModel().getId();
    server.register(modelId, projectItem);

    String url = "http://localhost:" + server.getPort() + "/preview/" + modelId;
    opener.accept(url);
  }
}
