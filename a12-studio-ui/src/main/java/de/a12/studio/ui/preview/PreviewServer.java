package de.a12.studio.ui.preview;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.a12.studio.dataservices.preview.ApplicationModelPreviewService;
import de.a12.studio.dataservices.preview.FormModelPreviewService;
import de.a12.studio.dataservices.preview.PreviewApplicationDto;
import de.a12.studio.dataservices.preview.PreviewSceneDto;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.PreviewAppSettings;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeInstallation;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Embedded HTTP server backing the Application Model and Form Model wireframe previews: one shared static
 * HTML/JS shell ({@code preview.html} - it renders whichever of the two generic shapes its data response
 * carries, see the file's {@code render()}) polls a JSON data endpoint on {@link
 * PreviewAppSettings#getAutoRefreshDelayMillis()}, reading straight from the live in-memory {@link
 * ApplicationModel}/{@link FormModel} held by the editor's {@link ProjectItem} (edits made via property
 * editors are already reflected there, so no explicit change notification is needed - see {@code
 * PreviewLauncher}).
 *
 * <p>Not thread-safe against concurrent edits from the JavaFX application thread while a request is being
 * served (the model classes are plain, unsynchronized POJOs); acceptable for a preview polling a handful of
 * times a second, and consistent with this being a lightweight v1 wireframe rather than a production service.
 *
 * <p>The same server also hosts the Form Engine preview, which renders with the real Form Engine instead of the
 * wireframe: {@code /sme/} serves the Simple Model Editor's client bundle from the user's A12 installation (its
 * {@code index.html} gets {@code form-engine-bootstrap.js} injected, which turns the page into SME's "preview
 * window"), and {@code /fe/{session}/data} feeds it the models of a {@link FormEnginePreviewSession}.
 */
@Slf4j
public class PreviewServer {

  private static final Pattern SHELL_PATH = Pattern.compile("^/preview/([^/]+)$");

  private static final Pattern DATA_PATH = Pattern.compile("^/preview/([^/]+)/data$");

  private static final Pattern FORM_ENGINE_DATA_PATH = Pattern.compile("^/fe/([^/]+)/data$");

  private static final String SME_CONTEXT = "/sme/";

  private static final String JSON = "application/json; charset=utf-8";

  private static PreviewServer instance;

  private final HttpServer httpServer;

  private final Map<String, ProjectItem> registeredModels = new ConcurrentHashMap<>();

  private final Map<String, FormEnginePreviewSession> formEngineSessions = new ConcurrentHashMap<>();

  private final String bootstrapTemplate;

  private final ApplicationModelPreviewService applicationPreviewService = new ApplicationModelPreviewService();

  private final FormModelPreviewService formPreviewService = new FormModelPreviewService();

  private final String shellTemplate;

  private PreviewServer() throws IOException {
    shellTemplate = new String(
        PreviewServer.class.getResourceAsStream("preview.html").readAllBytes(), StandardCharsets.UTF_8);

    bootstrapTemplate = new String(
        PreviewServer.class.getResourceAsStream("form-engine-bootstrap.js").readAllBytes(), StandardCharsets.UTF_8);

    httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    // Loading the Form Engine page means many parallel asset requests, and a data request can take seconds while the
    // Simple Model Editor backend starts - the default single-threaded executor would serialize all of it.
    httpServer.setExecutor(Executors.newCachedThreadPool(runnable -> {
      Thread thread = new Thread(runnable, "Preview Server");
      thread.setDaemon(true);
      return thread;
    }));
    httpServer.createContext("/preview/", this::handle);
    httpServer.createContext("/fe/", this::handleFormEngineData);
    httpServer.createContext(SME_CONTEXT, this::handleSmeClient);
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

  /**
   * Makes {@code session} reachable for the Form Engine page at {@link #getFormEnginePreviewUrl}. Re-registering
   * an id replaces the session.
   */
  public void registerFormEngineSession(String sessionId, FormEnginePreviewSession session) {
    formEngineSessions.put(sessionId, session);
  }

  public String getFormEnginePreviewUrl(String sessionId) {
    return "http://localhost:" + getPort() + SME_CONTEXT + "index.html?session="
        + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
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

    Project project = Studio.getCurrentProject();
    PreviewAppSettings settings = project != null
        ? project.getSettings().getProjectRootSettings().getPreviewApp()
        : new PreviewAppSettings();
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

  private void handleFormEngineData(HttpExchange exchange) throws IOException {
    try {
      Matcher matcher = FORM_ENGINE_DATA_PATH.matcher(exchange.getRequestURI().getPath());
      FormEnginePreviewSession session = matcher.matches() ? formEngineSessions.get(matcher.group(1)) : null;
      if (session == null) {
        sendResponse(exchange, 404, JSON, errorJson("No preview session is registered for this page."));
        return;
      }

      Map<String, String> query = parseQuery(exchange.getRequestURI());
      FormEnginePreviewSession.Snapshot snapshot = session.snapshot(query.get("fm"), query.get("dm"));

      ObjectNode json = JsonSettings.objectMapper.createObjectNode();
      json.put("title", snapshot.title());
      json.put("formModelRevision", snapshot.formModelRevision());
      json.put("documentModelRevision", snapshot.documentModelRevision());
      if (snapshot.formModel() != null) {
        json.put("formModel", snapshot.formModel());
      }
      if (snapshot.documentModel() != null) {
        json.put("documentModel", snapshot.documentModel());
      }
      if (snapshot.validationCode() != null) {
        json.put("validationCode", snapshot.validationCode());
      }
      sendResponse(exchange, 200, JSON, JsonSettings.objectMapper.writeValueAsString(json));
    }
    catch (PreviewAppException e) {
      log.warn("Form Engine preview data unavailable: {}", e.getMessage());
      sendResponse(exchange, 500, JSON, errorJson(e.getMessage()));
    }
    catch (Exception e) {
      log.error("Failed to handle Form Engine preview request '{}': {}", exchange.getRequestURI(), e.getMessage(), e);
      sendResponse(exchange, 500, JSON, errorJson("Internal error: " + e.getMessage()));
    }
  }

  private static String errorJson(String message) {
    ObjectNode json = JsonSettings.objectMapper.createObjectNode();
    json.put("error", message);
    return JsonSettings.objectMapper.writeValueAsString(json);
  }

  /**
   * Serves the Simple Model Editor's client bundle (looked up in the A12 installation on every request, so an
   * updated installation is picked up); {@code index.html} is served with the bootstrap script injected.
   */
  private void handleSmeClient(HttpExchange exchange) throws IOException {
    try {
      Path staticDir = SmeInstallation.resolve().getStaticDir().toPath().toAbsolutePath().normalize();
      String relative = exchange.getRequestURI().getPath().substring(SME_CONTEXT.length());
      Path file = staticDir.resolve(relative.isEmpty() ? "index.html" : relative).normalize();
      if (!file.startsWith(staticDir) || !Files.isRegularFile(file)) {
        sendResponse(exchange, 404, "text/plain", "Not found");
        return;
      }

      if (file.equals(staticDir.resolve("index.html"))) {
        sendResponse(exchange, 200, "text/html; charset=utf-8", injectBootstrap(Files.readString(file, StandardCharsets.UTF_8)));
        return;
      }
      exchange.getResponseHeaders().add("Content-Type", contentType(file));
      exchange.sendResponseHeaders(200, Files.size(file));
      try (OutputStream out = exchange.getResponseBody()) {
        Files.copy(file, out);
      }
    }
    catch (PreviewAppException e) {
      sendResponse(exchange, 503, "text/plain; charset=utf-8",
          "The Form Engine preview needs the Simple Model Editor of the A12 installation: " + e.getMessage());
    }
    catch (Exception e) {
      log.error("Failed to serve Simple Model Editor client file '{}': {}", exchange.getRequestURI(), e.getMessage(), e);
      sendResponse(exchange, 500, "text/plain", "Internal error: " + e.getMessage());
    }
  }

  private String injectBootstrap(String indexHtml) {
    Project project = Studio.getCurrentProject();
    PreviewAppSettings settings = project != null
        ? project.getSettings().getProjectRootSettings().getPreviewApp()
        : new PreviewAppSettings();
    String bootstrap = bootstrapTemplate
        .replace("__AUTO_REFRESH_ENABLED__", String.valueOf(settings.isAutoRefreshEnabled()))
        .replace("__AUTO_REFRESH_DELAY_MILLIS__", String.valueOf(settings.getAutoRefreshDelayMillis()));
    return indexHtml.replaceFirst("(?i)<head>", Matcher.quoteReplacement("<head><script>" + bootstrap + "</script>"));
  }

  private static String contentType(Path file) {
    String name = file.getFileName().toString().toLowerCase();
    return switch (name.substring(name.lastIndexOf('.') + 1)) {
      case "html" -> "text/html; charset=utf-8";
      case "js", "mjs" -> "text/javascript; charset=utf-8";
      case "css" -> "text/css; charset=utf-8";
      case "json", "map" -> "application/json; charset=utf-8";
      case "png" -> "image/png";
      case "jpg", "jpeg" -> "image/jpeg";
      case "gif" -> "image/gif";
      case "svg" -> "image/svg+xml";
      case "ico" -> "image/x-icon";
      case "ttf" -> "font/ttf";
      case "otf" -> "font/otf";
      case "woff" -> "font/woff";
      case "woff2" -> "font/woff2";
      case "txt", "md" -> "text/plain; charset=utf-8";
      default -> "application/octet-stream";
    };
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
