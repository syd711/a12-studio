package de.a12.studio.ui.updater;

import de.a12.studio.ui.util.StudioVersion;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Checks for, downloads and installs a12-studio-ui updates. The UI client is the only
 * executable this app self-updates, so this runs entirely in-process against {@link Updater} -
 * no server/REST involvement needed.
 */
@Slf4j
public class UpdaterService {

  public @Nullable String checkForNewerVersion() {
    String currentVersion = StudioVersion.get();
    String latestVersion = Updater.checkForUpdate();
    if (isNewer(latestVersion, currentVersion)) {
      return latestVersion;
    }
    return null;
  }

  public void downloadUpdate(String version) {
    Updater.downloadUpdate(version, Updater.STUDIO_ZIP);
  }

  public int getDownloadProgress() {
    return Updater.getDownloadProgress(Updater.STUDIO_ZIP, Updater.STUDIO_ZIP_SIZE);
  }

  public void installUpdate(String oldVersion, String newVersion) throws IOException {
    Updater.installClientUpdate(oldVersion, newVersion);
  }

  private static boolean isNewer(String candidate, String current) {
    try {
      return Updater.isLargerVersionThan(candidate, current);
    }
    catch (Exception e) {
      log.warn("Failed to compare versions \"{}\" and \"{}\": {}", candidate, current, e.getMessage());
      return false;
    }
  }
}
