package de.a12.studio.ui.previewapp;

import de.a12.studio.models.util.JsonSettings;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The installed Simple Model Editor's Spring Boot backend ({@code sme.jar}, see {@link SmeInstallation}), started
 * lazily on a free local port and used as a black box for the three things the Form Engine preview needs from the
 * A12 kernel that a12-studio does not implement itself: expanding a Document Model (includes, imported type
 * definitions), generating the JavaScript validation code the Form Engine runs in the browser, and reducing a
 * Document Model to a selection of its elements (ad hoc testing). The endpoints and payloads are the ones SME's own
 * client calls ({@code client/src/modules/commonDocumentModel/api/backendClient}). The request shapes are those of the
 * installed backend (13.0.2), which is what counts - the SME source checkout can be at another revision (its
 * combination expansion takes different fields, for one).
 *
 * <p>Singleton; the process is stopped with the application ({@link #stop()}, plus a shutdown hook).
 */
@Slf4j
public final class SmeBackend {

  private static final SmeBackend INSTANCE = new SmeBackend();

  private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(60);
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(120);

  private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

  private Process process;
  private int port;
  private boolean shutdownHookRegistered;

  private SmeBackend() {
  }

  public static SmeBackend getInstance() {
    return INSTANCE;
  }

  /**
   * The outcome of {@link #generateAdHocTestInput}.
   *
   * @param documentModel  the reduced Document Model, serialized
   * @param validationCode the JavaScript validation code for it
   */
  public record AdHocTestInput(String documentModel, String validationCode) {
  }

  /**
   * Expands the Document Model {@code documentModelId}: {@code documentModels} must hold it together with every
   * model it includes or imports type definitions from, each as its serialized JSON. Returns the expanded model
   * (with the {@code __meta} group the kernel's validator expects).
   */
  public JsonNode expand(@NonNull String documentModelId, @NonNull List<String> documentModels) throws PreviewAppException {
    ObjectNode request = JsonSettings.objectMapper.createObjectNode();
    request.put("documentModelId", documentModelId);
    putModels(request, "documentModels", documentModels);

    JsonNode response = post("/api/document-model/expand", request);
    JsonNode error = response.get("error");
    if (error != null && !error.isNull()) {
      throw new PreviewAppException(error.asString());
    }
    JsonNode expanded = response.get("documentModel");
    if (expanded == null || expanded.isNull()) {
      throw new PreviewAppException("The Simple Model Editor backend did not return an expanded Document Model.");
    }
    return expanded;
  }

  /**
   * Expands a Combination Model into the Document Model it stands for (the one a Form Model bound to the
   * combination is rendered against). {@code referencedModels} are the Document, Selection and other Combination
   * Models it references, each as its serialized JSON.
   */
  public JsonNode expandCombination(@NonNull String combinationModel, @NonNull List<String> referencedModels) throws PreviewAppException {
    ObjectNode request = JsonSettings.objectMapper.createObjectNode();
    request.set("combinationModel", JsonSettings.objectMapper.readTree(combinationModel));
    putModels(request, "referencedModels", referencedModels);

    JsonNode response = post("/api/combination-model/expand", request);
    JsonNode expanded = response.get("documentModel");
    if (expanded == null || expanded.isNull()) {
      StringBuilder message = new StringBuilder("The Combination Model could not be expanded.");
      JsonNode errors = response.get("errors");
      if (errors != null && errors.isArray()) {
        for (JsonNode error : errors) {
          message.append(' ').append(error.path("message").asString(""));
        }
      }
      throw new PreviewAppException(message.toString());
    }
    return expanded;
  }

  private static void putModels(ObjectNode request, String field, List<String> models) {
    ArrayNode array = request.putArray(field);
    for (String model : models) {
      array.add(JsonSettings.objectMapper.readTree(model));
    }
  }

  /** Generates the browser-side (JavaScript) validation code for an expanded Document Model. */
  public String generateValidationCode(@NonNull JsonNode expandedDocumentModel) throws PreviewAppException {
    ObjectNode request = JsonSettings.objectMapper.createObjectNode();
    request.set("documentModel", expandedDocumentModel);

    JsonNode code = post("/api/document-model/generate-validation-code", request).get("validationCode");
    if (code == null || code.isNull()) {
      throw new PreviewAppException("The validation code could not be generated - the Document Model contains errors.");
    }
    return code.asString();
  }

  /**
   * Reduces an expanded Document Model to {@code selectedElements} plus the {@code partiallySelectedElements}
   * (their ancestors, kept only as containers) and generates the validation code for the result.
   */
  public AdHocTestInput generateAdHocTestInput(@NonNull JsonNode expandedDocumentModel,
      @NonNull Collection<String> selectedElements, @NonNull Collection<String> partiallySelectedElements) throws PreviewAppException {
    ObjectNode request = JsonSettings.objectMapper.createObjectNode();
    request.set("documentModel", expandedDocumentModel);
    ArrayNode selected = request.putArray("selectedElements");
    selectedElements.forEach(selected::add);
    ArrayNode partial = request.putArray("partiallySelectedElements");
    partiallySelectedElements.forEach(partial::add);

    JsonNode response = post("/api/document-model/generate-ad-hoc-test-input", request);
    return new AdHocTestInput(response.get("documentModel").asString(), response.get("validationCode").asString());
  }

  private JsonNode post(String path, JsonNode body) throws PreviewAppException {
    int backendPort = ensureStarted();
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + backendPort + path))
          .timeout(REQUEST_TIMEOUT)
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(JsonSettings.objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
          .build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() != 200) {
        throw new PreviewAppException("The Simple Model Editor backend rejected " + path + " (HTTP " + response.statusCode()
            + "). The Document Model probably contains errors.");
      }
      return JsonSettings.objectMapper.readTree(response.body());
    }
    catch (IOException e) {
      throw new PreviewAppException("The Simple Model Editor backend could not be reached: " + e.getMessage());
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new PreviewAppException("Interrupted while waiting for the Simple Model Editor backend.");
    }
  }

  /** Starts the backend if it is not running yet and returns its port. Blocks until it accepts requests. */
  private synchronized int ensureStarted() throws PreviewAppException {
    if (process != null && process.isAlive()) {
      return port;
    }

    SmeInstallation installation = SmeInstallation.resolve();
    int freePort = findFreePort();
    ProcessBuilder processBuilder = new ProcessBuilder(
        installation.getJavaExecutable().getAbsolutePath(),
        "-jar", installation.getBackendJar().getAbsolutePath(),
        "--server.port=" + freePort,
        "--server.address=127.0.0.1")
        .directory(installation.getBackendJar().getParentFile())
        .redirectErrorStream(true);

    log.info("Starting the Simple Model Editor backend on port {}", freePort);
    Process started;
    try {
      started = processBuilder.start();
    }
    catch (IOException e) {
      throw new PreviewAppException("The Simple Model Editor backend could not be started: " + e.getMessage());
    }
    drainOutput(started);
    registerShutdownHook();

    long deadline = System.nanoTime() + STARTUP_TIMEOUT.toNanos();
    while (System.nanoTime() < deadline) {
      if (!started.isAlive()) {
        throw new PreviewAppException("The Simple Model Editor backend exited during startup (exit code " + started.exitValue() + ").");
      }
      if (isResponding(freePort)) {
        process = started;
        port = freePort;
        log.info("The Simple Model Editor backend is ready on port {}", freePort);
        return port;
      }
      try {
        Thread.sleep(200);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        started.destroyForcibly();
        throw new PreviewAppException("Interrupted while starting the Simple Model Editor backend.");
      }
    }
    stopProcess(started);
    throw new PreviewAppException("The Simple Model Editor backend did not become ready within " + STARTUP_TIMEOUT.toSeconds() + " seconds.");
  }

  private boolean isResponding(int candidatePort) {
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + candidatePort + "/"))
          .timeout(Duration.ofSeconds(2)).GET().build();
      return httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() < 500;
    }
    catch (IOException e) {
      return false;
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private static int findFreePort() throws PreviewAppException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
    catch (IOException e) {
      throw new PreviewAppException("No free local port available for the Simple Model Editor backend: " + e.getMessage());
    }
  }

  // The backend's output has to be consumed, or its pipe fills up and it blocks.
  private static void drainOutput(Process started) {
    Thread drain = new Thread(() -> {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(started.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          log.debug("[sme-backend] {}", line);
        }
      }
      catch (IOException e) {
        log.debug("Simple Model Editor backend output closed: {}", e.getMessage());
      }
    }, "SME Backend Output");
    drain.setDaemon(true);
    drain.start();
  }

  private void registerShutdownHook() {
    if (!shutdownHookRegistered) {
      shutdownHookRegistered = true;
      Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "SME Backend Shutdown"));
    }
  }

  public synchronized void stop() {
    if (process != null) {
      stopProcess(process);
      process = null;
    }
  }

  private static void stopProcess(Process toStop) {
    toStop.descendants().forEach(ProcessHandle::destroyForcibly);
    toStop.destroyForcibly();
    try {
      toStop.waitFor(5, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
