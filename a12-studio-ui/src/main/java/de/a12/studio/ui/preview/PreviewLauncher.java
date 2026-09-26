package de.a12.studio.ui.preview;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.PreviewAppSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.SystemUtil;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeInstallation;
import lombok.extern.slf4j.Slf4j;
import javafx.geometry.Rectangle2D;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
   * FormModelPreviewSession}) in a browser window of its own without toolbar, address bar and bookmarks bar, sized to two
   * thirds of the Studio stage (see {@link SystemUtil#openUrlInAppWindow}). Without the
   * Simple Model Editor in the A12 installation that rendering is not possible; the wireframe preview opens instead.
   */
  public static void openFormPreview(@NonNull ProjectItem projectItem) {
    if (!isFormEnginePreviewAvailable()) {
      openPreview(projectItem);
      return;
    }
    openFormEnginePreview("form-" + projectItem.getModel().getId(), new FormModelPreviewSession(projectItem), true);
  }

  /**
   * Used by the Document Model editor's "Ad Hoc Testing" action: renders a Form Model generated for {@code
   * selectedElementIds} of the Document Model (all of it if empty) with the real Form Engine, see {@link
   * AdHocTestPreviewSession}. It opens in a browser window of its own without toolbar and address bar, see {@link
   * SystemUtil#openUrlInAppWindow}.
   */
  public static void openAdHocTest(@NonNull ProjectItem documentModelItem, @NonNull Set<String> selectedElementIds) {
    openFormEnginePreview("adhoc-" + documentModelItem.getModel().getId(),
        new AdHocTestPreviewSession(documentModelItem, selectedElementIds), true);
  }

  /**
   * Used by the Content Model editor's embedded preview: registers a {@link ContentModelPreviewSession} for the
   * Content Model, which the page at the returned URL renders with the real Content Engine. The page is meant to be
   * shown in a {@code WebView}, but works in any browser.
   *
   * @throws PreviewAppException if the A12 installation has no Simple Model Editor, whose client renders the page
   */
  public static String registerContentPreview(@NonNull ProjectItem projectItem) throws PreviewAppException {
    SmeInstallation.resolve();
    PreviewServer server = PreviewServer.getOrStart();
    String sessionId = "content-" + projectItem.getModel().getId();
    server.registerContentSession(sessionId, new ContentModelPreviewSession(projectItem));
    return server.getContentPreviewUrl(sessionId);
  }

  /**
   * Used by the Content Model editor's "open in browser" button: opens the same preview page the embedded {@code
   * WebView} shows in the browser configured via {@link PreviewAppSettings.BrowserType}.
   *
   * @throws PreviewAppException if the A12 installation has no Simple Model Editor, whose client renders the page
   */
  public static void openContentPreview(@NonNull ProjectItem projectItem) throws PreviewAppException {
    SystemUtil.openUrl(registerContentPreview(projectItem), getPreviewAppSettings().getBrowserType());
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

  private static void openFormEnginePreview(String sessionId, FormEnginePreviewSession session, boolean appWindow) {
    PreviewServer server = PreviewServer.getOrStart();
    server.registerFormEngineSession(sessionId, session);
    String url = server.getFormEnginePreviewUrl(sessionId);
    PreviewAppSettings.BrowserType browserType = getPreviewAppSettings().getBrowserType();
    if (appWindow) {
      SystemUtil.openUrlInAppWindow(url, browserType, previewWindowBounds());
    }
    else {
      SystemUtil.openUrl(url, browserType);
    }
  }

  /** Two thirds of the Studio stage's size, centered on it; {@code null} while there is no stage. */
  private static @Nullable Rectangle2D previewWindowBounds() {
    Stage stage = Studio.stage;
    if (stage == null) {
      return null;
    }
    double width = stage.getWidth() * 2 / 3;
    double height = stage.getHeight() * 2 / 3;
    return new Rectangle2D(stage.getX() + (stage.getWidth() - width) / 2, stage.getY() + (stage.getHeight() - height) / 2,
        width, height);
  }

  private static void openPreview(@NonNull ProjectItem projectItem, @NonNull Consumer<String> opener) {
    PreviewServer server = PreviewServer.getOrStart();
    String modelId = projectItem.getModel().getId();
    server.register(modelId, projectItem);

    String url = "http://localhost:" + server.getPort() + "/preview/" + modelId;
    opener.accept(url);
  }
}
