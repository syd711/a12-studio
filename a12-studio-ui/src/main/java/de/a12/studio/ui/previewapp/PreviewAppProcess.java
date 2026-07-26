package de.a12.studio.ui.previewapp;

import de.a12.studio.models.projects.Project;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.OSUtil;
import de.a12.studio.ui.util.SystemUtil;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Launches and stops the real "Preview App" (the generated application preview shipped with the
 * A12 installation, at {@code <installation>/bin/preview-app-server}) for the currently open
 * project, replicating the process contract used by the standalone "A12 Preview App Control"
 * Electron tool (see {@link PreviewAppInstallation} and {@link PreviewAppWorkspaceBundler}).
 *
 * <p>Singleton: only one project can be previewed through the studio at a time.
 */
@Slf4j
public class PreviewAppProcess {

  public enum State {
    STOPPED, STARTING, RUNNING, STOPPING, FAILED
  }

  private static final String SUCCESS_MESSAGE = "Started PreviewAppServerApplicationKt in";

  private static final Map<String, String> ERROR_MESSAGES = Map.of(
      "already in use", "Port " + PreviewAppInstallation.SERVER_PORT + " is already in use. Free the port and try again.",
      "Error starting Tomcat", "Tomcat could not start due to an internal error. Check the Preview App log for details.",
      "Application run failed", "The Preview App failed to start. Check the Preview App log for details.");

  private static final PreviewAppProcess INSTANCE = new PreviewAppProcess();

  private final SimpleObjectProperty<State> state = new SimpleObjectProperty<>(State.STOPPED);

  private final ObservableList<String> logLines = FXCollections.observableArrayList();

  private volatile Process process;

  private volatile File dbTempDir;

  private volatile boolean shutdownHookRegistered;

  private PreviewAppProcess() {
  }

  public static PreviewAppProcess getInstance() {
    return INSTANCE;
  }

  public ReadOnlyObjectProperty<State> stateProperty() {
    return state;
  }

  public State getState() {
    return state.get();
  }

  public ObservableList<String> getLogLines() {
    return logLines;
  }

  public int getPort() {
    return PreviewAppInstallation.SERVER_PORT;
  }

  public synchronized void start(Project project) {
    if (state.get() == State.STARTING || state.get() == State.RUNNING) {
      return;
    }

    setState(State.STARTING);
    runOnFx(logLines::clear);

    Thread startThread = new Thread(() -> doStart(project), "Preview App Start");
    startThread.setDaemon(true);
    startThread.start();
  }

  public synchronized void stop() {
    Process currentProcess = this.process;
    if (currentProcess == null || !currentProcess.isAlive()) {
      setState(State.STOPPED);
      return;
    }

    setState(State.STOPPING);
    Thread stopThread = new Thread(this::stopBlocking, "Preview App Stop");
    stopThread.setDaemon(true);
    stopThread.start();
  }

  private void doStart(Project project) {
    try {
      PreviewAppInstallation installation = PreviewAppInstallation.resolve();
      File projectFolder = project.getFolder();
      PreviewAppWorkspaceBundler.bundle(projectFolder);

      File newDbTempDir = Files.createTempDirectory("a12-studio-preview-app-").toFile();

      String clientLocation = toFileUri(installation.getClientStaticDir());
      String resourcesLocation = toFileUri(new File(projectFolder, "resources"));

      ProcessBuilder processBuilder = new ProcessBuilder(
          installation.getJavaExecutable().getAbsolutePath(),
          "-Dspring.web.resources.static-locations=" + clientLocation + "," + resourcesLocation,
          "-jar", installation.getServerJar().getAbsolutePath());
      processBuilder.directory(installation.getServerJar().getParentFile());
      processBuilder.redirectErrorStream(true);
      processBuilder.environment().put("WORKSPACE_DIR", projectFolder.getAbsolutePath());
      processBuilder.environment().put("SERVER_ADDRESS", "127.0.0.1");
      processBuilder.environment().put("SERVER_PORT", String.valueOf(PreviewAppInstallation.SERVER_PORT));
      processBuilder.environment().put("DB_TEMP_DIR", newDbTempDir.getAbsolutePath());

      Process newProcess = processBuilder.start();
      this.process = newProcess;
      this.dbTempDir = newDbTempDir;
      registerShutdownHookOnce();

      appendLog("Starting Preview App from \"" + installation.getServerJar().getAbsolutePath() + "\".");
      readOutput(newProcess);
    }
    catch (Exception e) {
      log.error("Failed to launch Preview App: {}", e.getMessage(), e);
      setState(State.FAILED);
      String message = e.getMessage();
      runOnFx(() -> WidgetFactory.showAlert(Studio.stage, "Preview App failed to launch", message));
    }
  }

  private void readOutput(Process runningProcess) {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(runningProcess.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        handleLogLine(line);
      }
    }
    catch (IOException e) {
      log.debug("Preview App output stream closed: {}", e.getMessage());
    }
    handleProcessExit();
  }

  private void handleLogLine(String line) {
    appendLog(line);

    if (line.contains(SUCCESS_MESSAGE)) {
      setState(State.RUNNING);
      SystemUtil.openUrl("http://localhost:" + PreviewAppInstallation.SERVER_PORT);
      return;
    }

    if (state.get() != State.RUNNING) {
      String error = matchError(line);
      if (error != null) {
        setState(State.FAILED);
        runOnFx(() -> WidgetFactory.showAlert(Studio.stage, "Preview App failed to start", error));
      }
    }
  }

  private void handleProcessExit() {
    State current = state.get();
    if (current == State.STARTING || current == State.RUNNING) {
      log.warn("Preview App process exited unexpectedly.");
      cleanupTempDir();
      process = null;
      setState(State.STOPPED);
    }
  }

  private void stopBlocking() {
    Process currentProcess = this.process;
    if (currentProcess == null) {
      setState(State.STOPPED);
      return;
    }

    try {
      requestGracefulShutdown();
      if (!currentProcess.waitFor(15, TimeUnit.SECONDS)) {
        forceKill(currentProcess);
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    finally {
      cleanupTempDir();
      process = null;
      setState(State.STOPPED);
    }
  }

  private void requestGracefulShutdown() {
    try {
      URI uri = URI.create("http://localhost:" + PreviewAppInstallation.SERVER_PORT + "/actuator/shutdown");
      HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
      connection.setRequestMethod("POST");
      connection.setConnectTimeout(2000);
      connection.setReadTimeout(2000);
      connection.getResponseCode();
      connection.disconnect();
    }
    catch (IOException e) {
      log.debug("Graceful shutdown request failed, will force-stop if needed: {}", e.getMessage());
    }
  }

  private void forceKill(Process runningProcess) {
    runningProcess.descendants().forEach(ProcessHandle::destroyForcibly);
    runningProcess.destroyForcibly();
  }

  private void cleanupTempDir() {
    File currentDbTempDir = this.dbTempDir;
    if (currentDbTempDir == null) {
      return;
    }
    try {
      org.apache.commons.io.FileUtils.deleteDirectory(currentDbTempDir);
    }
    catch (IOException e) {
      log.warn("Failed to delete Preview App temp directory \"{}\": {}", currentDbTempDir.getAbsolutePath(), e.getMessage());
    }
    this.dbTempDir = null;
  }

  private void registerShutdownHookOnce() {
    if (shutdownHookRegistered) {
      return;
    }
    shutdownHookRegistered = true;

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      Process currentProcess = this.process;
      if (currentProcess == null || !currentProcess.isAlive()) {
        return;
      }
      requestGracefulShutdown();
      try {
        currentProcess.waitFor(5, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      if (currentProcess.isAlive()) {
        forceKill(currentProcess);
      }
    }, "Preview App Shutdown Hook"));
  }

  private static String matchError(String line) {
    for (Map.Entry<String, String> entry : ERROR_MESSAGES.entrySet()) {
      if (line.contains(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  private static String toFileUri(File file) {
    return (OSUtil.isWindows() ? "file:/" : "file://") + file.getAbsolutePath();
  }

  private void appendLog(String line) {
    runOnFx(() -> logLines.add(line));
  }

  private void setState(State newState) {
    runOnFx(() -> state.set(newState));
  }

  private static void runOnFx(Runnable action) {
    if (Platform.isFxApplicationThread()) {
      action.run();
    }
    else {
      Platform.runLater(action);
    }
  }
}
