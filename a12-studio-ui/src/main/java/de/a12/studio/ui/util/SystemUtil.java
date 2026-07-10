package de.a12.studio.ui.util;


import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.ui.Studio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static de.a12.studio.commons.util.OSUtil.isMac;
import static de.a12.studio.commons.util.OSUtil.isWindows;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


  public static long getMemorySize(String value) {
    long size = 8 * (int) ((((value.length()) * 2) + 45) / 8);
    return size;
  }


  public static void openFolder(File folder) {
    openFolder(folder, null);
  }

  public static void openFile(File file) {
    if (file == null) {
      return;
    }

    File folder = file.getParentFile();
    try {
      if (file.exists()) {
        openFileWithOS(file.getAbsolutePath());
      }
      else if (folder != null && folder.exists()) {
        openFolder(folder);
      }
    }
    catch (IOException e) {
      LOG.error("Failed to open system file: " + e.getMessage(), e);
    }
  }

  public static void openFolder(File folder, File fallback) {
    if (folder == null) {
      return;
    }

    while (!folder.exists()) {
      folder = folder.getParentFile();
    }

    if (!folder.exists() && (fallback != null && !fallback.exists())) {
      WidgetFactory.showAlert(Studio.stage, "Error", "The local folder \"" + folder.getAbsolutePath() + "\" does not exist.");
      return;
    }

    try {
      if (folder.exists()) {
        openFolderWithOS(folder.getAbsolutePath());
      }
      else if (fallback != null && fallback.exists()) {
        openFolderWithOS(fallback.getAbsolutePath());
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Error", "The local folder \"" + folder.getAbsolutePath() + "\" does not exist.");
      }
    }
    catch (IOException e) {
      LOG.error("Failed to open folder: " + e.getMessage(), e);
    }
  }

  /**
   * Opens the folder specified by the absolute path using the operating system's
   * file explorer.
   *
   * @param absolutePath The absolute path of the folder to open typically from getAbsolutePath().
   * @throws IOException                   If an I/O error occurs.
   * @throws UnsupportedOperationException If the operating system is not supported.
   */
  private static void openFolderWithOS(String absolutePath) throws IOException {
    if (isWindows()) {
      if (absolutePath.startsWith("\\\\")) {
        // UNC paths require routing through cmd /c start; explorer.exe \\server\share silently fails
        new ProcessBuilder("cmd.exe", "/c", "start", "explorer.exe", absolutePath).start();
      }
      else {
        new ProcessBuilder("explorer.exe", absolutePath).start();
      }
    }
    else if (isMac()) {
      new ProcessBuilder("open", absolutePath).start();  // macOS command
    }
    else {
      throw new UnsupportedOperationException("Unsupported operating system: " + System.getProperty("os.name"));
    }
  }

  /**
   * Opens the file specified by the absolute path using the operating system's
   * file explorer, selecting the file if possible.
   *
   * @param absolutePath The absolute path of the file to open typically from getAbsolutePath().
   * @throws IOException                   If an I/O error occurs.
   * @throws UnsupportedOperationException If the operating system is not supported.
   */
  private static void openFileWithOS(String absolutePath) throws IOException {
    if (isWindows()) {
      new ProcessBuilder("explorer.exe", "/select,", absolutePath).start();
    }
    else if (isMac()) {
      new ProcessBuilder("open", "-R", absolutePath).start();
    }
    else {
      throw new UnsupportedOperationException("Unsupported operating system: " + System.getProperty("os.name"));
    }
  }

}
