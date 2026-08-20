package de.a12.studio.ui.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Provides application-level path resolution that is safe to use from
 * {@code a12-studio-commons} without depending on the higher-level
 * {@code Updater} class (which lives in {@code a12-studio-ui}).
 */
@Slf4j
public final class AppPaths {

  private AppPaths() {}

  /**
   * Returns the writeable base folder for configuration and download files.
   * <ul>
   *   <li>On macOS: the directory pointed to by the {@code MAC_WRITE_PATH} system property.</li>
   *   <li>On all other platforms: the current working directory ({@code ./}).</li>
   * </ul>
   */
  public static File getWriteableBaseFolder() {
    if (!OSUtil.isMac()) {
      return new File("./");
    }
    else {
      log.info("Setting base path for Mac to {}", System.getProperty("MAC_WRITE_PATH"));
      return new File(System.getProperty("MAC_WRITE_PATH"));
    }
  }
}
