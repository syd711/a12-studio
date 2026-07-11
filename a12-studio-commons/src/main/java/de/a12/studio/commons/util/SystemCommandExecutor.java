package de.a12.studio.commons.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs an OS command via ProcessBuilder, draining stdout/stderr on background threads
 * so the child process never blocks on a full output buffer. Used by the self-updater
 * to launch the update scripts and restart the app.
 */
@Slf4j
public class SystemCommandExecutor {

  private final List<String> commandInformation;
  private final boolean prependCmd;
  private boolean enableLogging = false;
  private File dir;

  public SystemCommandExecutor(final List<String> commandInformation) {
    this(commandInformation, true);
  }

  public SystemCommandExecutor(final List<String> commandInformation, boolean prependCmd) {
    this.prependCmd = prependCmd;
    if (commandInformation == null) {
      throw new NullPointerException("The commandInformation is required.");
    }
    this.commandInformation = new ArrayList<>(commandInformation);
  }

  public void enableLogging(boolean b) {
    this.enableLogging = b;
  }

  public void setDir(File dir) {
    this.dir = dir;
  }

  public void executeCommandAsync() {
    Thread t = new Thread(() -> {
      try {
        execute();
      }
      catch (Exception e) {
        log.error("Failed to execute command {}: {}", String.join(" ", commandInformation), e.getMessage(), e);
      }
    });
    t.setName("Async Executor for " + String.join(" ", commandInformation));
    t.start();
  }

  public int executeCommand() throws IOException, InterruptedException {
    return execute();
  }

  private int execute() throws IOException, InterruptedException {
    if (prependCmd && !commandInformation.getFirst().equalsIgnoreCase("cmd.exe")) {
      commandInformation.addFirst("/c");
      commandInformation.addFirst("cmd.exe");
    }

    log.info("System Command: {}> {}", dir != null ? dir.getAbsolutePath() : "", String.join(" ", commandInformation));

    ProcessBuilder pb = new ProcessBuilder(commandInformation);
    if (dir != null) {
      pb.directory(dir);
    }

    Process process = pb.start();

    ThreadedStreamHandler inputStreamHandler = new ThreadedStreamHandler(String.join(" ", commandInformation), process.getInputStream());
    inputStreamHandler.enableLog(enableLogging);
    ThreadedStreamHandler errorStreamHandler = new ThreadedStreamHandler(String.join(" ", commandInformation), process.getErrorStream());
    errorStreamHandler.enableLog(enableLogging);

    inputStreamHandler.start();
    errorStreamHandler.start();

    int exitValue = process.waitFor();

    inputStreamHandler.stopThread();
    errorStreamHandler.stopThread();

    return exitValue;
  }
}
