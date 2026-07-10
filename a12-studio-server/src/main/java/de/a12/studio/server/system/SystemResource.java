package de.a12.studio.server.system;

import de.a12.studio.commons.Updater;
import de.a12.studio.server.A12StudioServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * Exposes the self-update flow (check/download/install) over REST so a12-studio-ui
 * can trigger a server update remotely.
 */
@RestController
@RequestMapping("/api/system")
public class SystemResource {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @GetMapping("/version")
  public String version() {
    String version = A12StudioServer.class.getPackage().getImplementationVersion();
    return version != null ? version : "dev";
  }

  @GetMapping("/update-check")
  public UpdateCheckResult updateCheck() {
    String latestVersion = Updater.checkForUpdate();
    String currentVersion = version();
    boolean updateAvailable = Updater.isLargerVersionThan(latestVersion, currentVersion);
    return new UpdateCheckResult(currentVersion, latestVersion, updateAvailable);
  }

  @GetMapping("/update/{version}/download/start")
  public boolean downloadUpdate(@PathVariable("version") String version) {
    new Thread(() -> {
      Thread.currentThread().setName("Server Update Downloader");
      Updater.downloadUpdate(version, Updater.SERVER_ZIP);
    }).start();
    return true;
  }

  @GetMapping("/update/download/status")
  public int updateDownloadStatus() {
    return Updater.getDownloadProgress(Updater.SERVER_ZIP, Updater.SERVER_ZIP_SIZE);
  }

  @PostMapping("/update/install")
  public boolean installUpdate() throws IOException {
    LOG.info("Installing server update and restarting.");
    Updater.installServerUpdate();
    new Thread(() -> {
      try {
        Thread.sleep(2000);
        System.exit(0);
      }
      catch (InterruptedException e) {
        //ignore
      }
    }).start();
    return true;
  }

  public record UpdateCheckResult(String currentVersion, String latestVersion, boolean updateAvailable) {
  }
}
