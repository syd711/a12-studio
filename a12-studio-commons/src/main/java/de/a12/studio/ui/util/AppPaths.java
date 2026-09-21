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

  /** System property holding the macOS base folder, set by the packaged app's launcher. */
  public static final String MAC_WRITE_PATH = "MAC_WRITE_PATH";

  /** Fallback location used on macOS when {@link #MAC_WRITE_PATH} is not set (e.g. IDE/gradle runs). */
  private static final String MAC_FALLBACK_PATH = "Library/Application Support/A12-Studio";

  private AppPaths() {}

  /**
   * Returns the writeable base folder for configuration and download files.
   * <ul>
   *   <li>On macOS: the directory pointed to by the {@code MAC_WRITE_PATH} system property, or
   *       {@code ~/Library/Application Support/A12-Studio} when that property is not set.</li>
   *   <li>On all other platforms: the current working directory ({@code ./}).</li>
   * </ul>
   */
  public static File getWriteableBaseFolder() {
    if (!OSUtil.isMac()) {
      return new File("./");
    }

    String macWritePath = System.getProperty(MAC_WRITE_PATH);
    if (macWritePath == null || macWritePath.isBlank()) {
      File fallback = new File(System.getProperty("user.home"), MAC_FALLBACK_PATH);
      if (!fallback.exists() && !fallback.mkdirs()) {
        log.error("Failed to create Mac base path {}", fallback.getAbsolutePath());
      }
      // Publish the resolved folder so the other MAC_WRITE_PATH consumers (e.g. MacOSUpdater and
      // the update-client-macos.sh template) agree on it. They append names directly, hence the
      // trailing separator.
      macWritePath = fallback.getAbsolutePath() + File.separator;
      System.setProperty(MAC_WRITE_PATH, macWritePath);
      log.warn("MAC_WRITE_PATH was not set, falling back to base path {}", macWritePath);
      return fallback;
    }

    log.info("Setting base path for Mac to {}", macWritePath);
    return new File(macWritePath);
  }
}
