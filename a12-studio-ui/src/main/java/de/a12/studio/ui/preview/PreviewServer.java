package de.a12.studio.ui.preview;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.a12.studio.dataservices.preview.ApplicationModelPreviewService;
import de.a12.studio.dataservices.preview.FormModelPreviewService;
import de.a12.studio.dataservices.preview.PreviewApplicationDto;
import de.a12.studio.dataservices.preview.PreviewSceneDto;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.PreviewSettings;
import de.a12.studio.models.util.JsonSettings;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Embedded HTTP server backing the Application Model and Form Model wireframe previews: one shared static
 * HTML/JS shell ({@code preview.html} - it renders whichever of the two generic shapes its data response
 * carries, see the file's {@code render()}) polls a JSON data endpoint on {@link
 * PreviewSettings#getAutoRefreshDelayMillis()}, reading straight from the live in-memory {@link
 * ApplicationModel}/{@link FormModel} held by the editor's {@link ProjectItem} (edits made via property
 * editors are already reflected there, so no explicit change notification is needed - see {@code
 * PreviewLauncher}).
 *
 * <p>Not thread-safe against concurrent edits from the JavaFX application thread while a request is being
 * served (the model classes are plain, unsynchronized POJOs); acceptable for a preview polling a handful of
 * times a second, and consistent with this being a lightweight v1 wireframe rather than a production service.
 */
@Slf4j
public class PreviewServer {

  private static final Pattern SHELL_PATH = Pattern.compile("^/preview/([^/]+)$");

  private static final Pattern DATA_PATH = Pattern.compile("^/preview/([^/]+)/data$");

  private static PreviewServer instance;

  private final HttpServer httpServer;

  private final Map<String, ProjectItem> registeredModels = new ConcurrentHashMap<>();

  private final ApplicationModelPreviewService applicationPreviewService = new ApplicationModelPreviewService();

  private final FormModelPreviewService formPreviewService = new FormModelPreviewService();

  private final String shellTemplate;

  private PreviewServer() throws IOException {
    shellTemplate = new String(
        PreviewServer.class.getResourceAsStream("preview.html").readAllBytes(), StandardCharsets.UTF_8);

    httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    httpServer.createContext("/preview/", this::handle);
    httpServer.start();
  }

  /**
   * Starts the server on first use (lazily, on the first Preview click) and returns the running singleton.
   */
  public static synchronized PreviewServer getOrStart() {
    if (instance == null) {
      try {
        instance = new PreviewServer();
      }
      catch (IOException e) {
        throw new RuntimeException("Failed to start the preview server", e);
      }
    }
    return instance;
  }

  public static synchronized void stopIfRunning() {
    if (instance != null) {
      instance.httpServer.stop(0);
      instance = null;
    }
  }

  public int getPort() {
    return httpServer.getAddress().getPort();
  }

  /**
   * Makes {@code projectItem} reachable at {@code /preview/{modelId}}, where {@code modelId} is the model's
   * own id (i.e. its filename without ".json"). Re-registering the same id simply replaces the mapping, e.g.
   * when Preview is clicked again after switching projects.
   */
  public void register(String modelId, ProjectItem projectItem) {
    registeredModels.put(modelId, projectItem);
  }

  private void handle(HttpExchange exchange) throws IOException {
    try {
      String path = exchange.getRequestURI().getPath();

      Matcher dataMatcher = DATA_PATH.matcher(path);
      if (dataMatcher.matches()) {
        handleData(exchange, dataMatcher.group(1));
        return;
      }

      Matcher shellMatcher = SHELL_PATH.matcher(path);
      if (shellMatcher.matches()) {
        handleShell(exchange, shellMatcher.group(1));
        return;
      }

      sendResponse(exchange, 404, "text/plain", "Not found");
    }
    catch (Exception e) {
      log.error("Failed to handle preview request '{}': {}", exchange.getRequestURI(), e.getMessage(), e);
      sendResponse(exchange, 500, "text/plain", "Internal error: " + e.getMessage());
    }
  }

  private void handleShell(HttpExchange exchange, String modelId) throws IOException {
    ProjectItem projectItem = registeredModels.get(modelId);
    if (projectItem == null) {
      sendResponse(exchange, 404, "text/plain", "No preview registered for '" + modelId + "'");
      return;
    }

    PreviewSettings settings = PreviewSettings.load();
    String html = shellTemplate
        .replace("__MODEL_ID__", modelId)
        .replace("__AUTO_REFRESH_ENABLED__", String.valueOf(settings.isAutoRefreshEnabled()))
        .replace("__AUTO_REFRESH_DELAY_MILLIS__", String.valueOf(settings.getAutoRefreshDelayMillis()));
    sendResponse(exchange, 200, "text/html; charset=utf-8", html);
  }

  private void handleData(HttpExchange exchange, String modelId) throws IOException {
    ProjectItem projectItem = registeredModels.get(modelId);
    if (projectItem == null) {
      sendResponse(exchange, 404, "text/plain", "No preview registered for '" + modelId + "'");
      return;
    }

    if (projectItem.getModel() instanceof FormModel) {
      handleFormData(exchange, projectItem);
      return;
    }
    if (projectItem.getModel() instanceof ApplicationModel) {
      handleApplicationData(exchange, projectItem);
      return;
    }
    sendResponse(exchange, 404, "text/plain", "No preview available for model type of '" + modelId + "'");
  }

  private void handleApplicationData(HttpExchange exchange, ProjectItem projectItem) throws IOException {
    Map<String, String> query = parseQuery(exchange.getRequestURI());
    String moduleName = query.get("module");
    String sceneName = query.get("scene");

    PreviewApplicationDto application = applicationPreviewService.buildPreview(projectItem);
    PreviewSceneDto scene = null;
    if (moduleName != null && sceneName != null) {
      try {
        scene = applicationPreviewService.resolveScene(projectItem, moduleName, sceneName);
      }
      catch (IllegalArgumentException e) {
        log.debug("Ignoring unresolved module/scene '{}/{}': {}", moduleName, sceneName, e.getMessage());
      }
    }

    String json = JsonSettings.objectMapper.writeValueAsString(new PreviewDataResponse(application, scene));
    sendResponse(exchange, 200, "application/json; charset=utf-8", json);
  }

  private void handleFormData(HttpExchange exchange, ProjectItem projectItem) throws IOException {
    String json = JsonSettings.objectMapper.writeValueAsString(
        new FormPreviewDataResponse(formPreviewService.buildPreview(projectItem)));
    sendResponse(exchange, 200, "application/json; charset=utf-8", json);
  }

  private static Map<String, String> parseQuery(URI uri) {
    String query = uri.getRawQuery();
    if (query == null || query.isBlank()) {
      return Map.of();
    }

    Map<String, String> result = new ConcurrentHashMap<>();
    for (String pair : query.split("&")) {
      int separator = pair.indexOf('=');
      if (separator > 0) {
        String key = URLDecoder.decode(pair.substring(0, separator), StandardCharsets.UTF_8);
        String value = URLDecoder.decode(pair.substring(separator + 1), StandardCharsets.UTF_8);
        result.put(key, value);
      }
    }
    return result;
  }

  private static void sendResponse(HttpExchange exchange, int status, String contentType, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", contentType);
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(bytes);
    }
  }
}
