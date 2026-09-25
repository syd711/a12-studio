package de.a12.studio.ui.preview;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.PreviewAppSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.SystemUtil;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeInstallation;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Entry point for the Preview toolbar buttons: ensures the embedded {@link PreviewServer} is running, registers
 * the clicked editor's model under its id, and opens the preview URL.
 */
@Slf4j
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
   * Used by the Form Model editor's Preview button: renders the Form Model with the real Form Engine (see {@link
   * FormModelPreviewSession}) in the browser configured via {@link PreviewAppSettings.BrowserType}. Without the
   * Simple Model Editor in the A12 installation that rendering is not possible; the wireframe preview opens instead.
   */
  public static void openFormPreview(@NonNull ProjectItem projectItem) {
    if (!isFormEnginePreviewAvailable()) {
      openPreview(projectItem);
      return;
    }
    openFormEnginePreview("form-" + projectItem.getModel().getId(), new FormModelPreviewSession(projectItem));
  }

  /**
   * Used by the Document Model editor's "Ad Hoc Testing" action: renders a Form Model generated for {@code
   * selectedElementIds} of the Document Model (all of it if empty) with the real Form Engine, see {@link
   * AdHocTestPreviewSession}.
   */
  public static void openAdHocTest(@NonNull ProjectItem documentModelItem, @NonNull Set<String> selectedElementIds) {
    openFormEnginePreview("adhoc-" + documentModelItem.getModel().getId(),
        new AdHocTestPreviewSession(documentModelItem, selectedElementIds));
  }

  private static boolean isFormEnginePreviewAvailable() {
    try {
      SmeInstallation.resolve();
      return true;
    }
    catch (PreviewAppException e) {
      log.warn("Real Form Engine preview unavailable, using the wireframe preview: {}", e.getMessage());
      return false;
    }
  }

  private static void openFormEnginePreview(String sessionId, FormEnginePreviewSession session) {
    PreviewServer server = PreviewServer.getOrStart();
    server.registerFormEngineSession(sessionId, session);
    SystemUtil.openUrl(server.getFormEnginePreviewUrl(sessionId), getPreviewAppSettings().getBrowserType());
  }

  private static void openPreview(@NonNull ProjectItem projectItem, @NonNull Consumer<String> opener) {
    PreviewServer server = PreviewServer.getOrStart();
    String modelId = projectItem.getModel().getId();
    server.register(modelId, projectItem);

    String url = "http://localhost:" + server.getPort() + "/preview/" + modelId;
    opener.accept(url);
  }
}
