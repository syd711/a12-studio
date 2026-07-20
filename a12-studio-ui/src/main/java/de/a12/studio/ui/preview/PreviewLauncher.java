package de.a12.studio.ui.preview;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.PreviewSettings;
import de.a12.studio.ui.util.SystemUtil;
import org.jspecify.annotations.NonNull;

/**
 * Entry point for the Preview toolbar button: ensures the embedded {@link PreviewServer} is running, registers
 * the clicked editor's model under its id, and opens the preview URL in the browser configured via {@link
 * PreviewSettings.BrowserType}.
 */
public class PreviewLauncher {

  private PreviewLauncher() {
  }

  public static void openPreview(@NonNull ProjectItem projectItem) {
    PreviewServer server = PreviewServer.getOrStart();
    String modelId = projectItem.getModel().getId();
    server.register(modelId, projectItem);

    String url = "http://localhost:" + server.getPort() + "/preview/" + modelId;
    SystemUtil.openUrl(url, PreviewSettings.load().getBrowserType());
  }
}
