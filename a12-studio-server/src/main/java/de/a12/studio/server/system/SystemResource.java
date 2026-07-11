package de.a12.studio.server.system;

import de.a12.studio.commons.Updater;
import de.a12.studio.server.A12StudioServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Exposes the self-update flow (check/download/install) over REST so a12-studio-ui
 * can trigger a server update remotely.
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
public class SystemResource {

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
    log.info("Installing server update and restarting.");
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
