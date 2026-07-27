package de.a12.studio.ui.previewapp;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Tracks the footer's traffic-light status for the Preview App by combining
 * {@link PreviewAppProcess}'s own lifecycle state (which only reflects what this JVM launched) with an
 * independent HTTP ping, so a server that hangs or dies without the launched process noticing it is still
 * reflected here. Runs the ping on its own thread rather than piggy-backing on {@link PreviewAppProcess}'s
 * state changes, since a timed-out socket must not stall the FX thread.
 */
@Slf4j
public class PreviewAppStatusMonitor {

  public enum Status {
    RUNNING, STARTING, STOPPED
  }

  private static final Duration PING_INTERVAL = Duration.ofSeconds(5);
  private static final Duration PING_TIMEOUT = Duration.ofSeconds(5);

  private static final PreviewAppStatusMonitor INSTANCE = new PreviewAppStatusMonitor();

  private final SimpleObjectProperty<Status> status = new SimpleObjectProperty<>(Status.STOPPED);
  private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(PING_TIMEOUT).build();

  private volatile boolean started;
  private volatile boolean reachable;

  private PreviewAppStatusMonitor() {
  }

  public static PreviewAppStatusMonitor getInstance() {
    return INSTANCE;
  }

  public ReadOnlyObjectProperty<Status> statusProperty() {
    return status;
  }

  public Status getStatus() {
    return status.get();
  }

  public synchronized void start() {
    if (started) {
      return;
    }
    started = true;

    PreviewAppProcess.getInstance().stateProperty().addListener((observable, oldState, newState) -> {
      if (newState == PreviewAppProcess.State.RUNNING) {
        // Confirms reachability right away instead of leaving the bubble on the previous
        // (possibly stale) reading until the next scheduled tick, up to PING_INTERVAL later.
        Thread immediatePing = new Thread(this::pingAndRecompute, "Preview App Ping (immediate)");
        immediatePing.setDaemon(true);
        immediatePing.start();
      }
      else {
        recomputeStatus();
      }
    });
    recomputeStatus();

    Thread pingThread = new Thread(this::runPingLoop, "Preview App Ping");
    pingThread.setDaemon(true);
    pingThread.start();
  }

  private void runPingLoop() {
    while (true) {
      try {
        Thread.sleep(PING_INTERVAL.toMillis());
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
      pingAndRecompute();
    }
  }

  private void pingAndRecompute() {
    reachable = ping();
    recomputeStatus();
  }

  private boolean ping() {
    if (PreviewAppProcess.getInstance().getState() != PreviewAppProcess.State.RUNNING) {
      return false;
    }

    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("http://localhost:" + PreviewAppProcess.getInstance().getPort() + "/"))
          .timeout(PING_TIMEOUT)
          .GET()
          .build();
      HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
      return response.statusCode() < 500;
    }
    catch (IOException e) {
      log.debug("Preview App ping failed: {}", e.getMessage());
      return false;
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private void recomputeStatus() {
    PreviewAppProcess.State processState = PreviewAppProcess.getInstance().getState();
    Status newStatus = switch (processState) {
      case STARTING, STOPPING -> Status.STARTING;
      case RUNNING -> reachable ? Status.RUNNING : Status.STOPPED;
      case STOPPED, FAILED -> Status.STOPPED;
    };
    setStatus(newStatus);
  }

  private void setStatus(Status newStatus) {
    if (Platform.isFxApplicationThread()) {
      status.set(newStatus);
    }
    else {
      Platform.runLater(() -> status.set(newStatus));
    }
  }
}
