package de.a12.studio.ui.util;


import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.ui.Studio;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import static de.a12.studio.commons.util.OSUtil.isLinux;
import static de.a12.studio.commons.util.OSUtil.isMac;
import static de.a12.studio.commons.util.OSUtil.isWindows;

@Slf4j
public class SystemUtil {

  public static long getMemorySize(String value) {
    long size = 8 * (int) ((((value.length()) * 2) + 45) / 8);
    return size;
  }


  public static boolean editFile(@Nullable File file) {
    if (file != null && file.exists()) {
      String osName = System.getProperty("os.name");
      if (osName.contains("Windows")) {
//        Studio.hostServices.showDocument(file.getAbsolutePath());
        try {
          new ProcessBuilder("cmd", "/c", "start", "", file.getAbsolutePath()).start();
        }
        catch (IOException e) {
          log.error("Open failed: {}", e.getMessage());
        }
      }
      else if (osName.toLowerCase().contains("mac")) {
        try {
          Runtime.getRuntime().exec(new String[]{"/usr/bin/open", "-t", file.getAbsolutePath()});
        }
        catch (IOException e) {
          log.error("Error opening browser: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Error opening browser: " + e.getMessage());
        }
      }
      else if (osName.toLowerCase().contains("nux")) {
        try {
          Runtime.getRuntime().exec(new String[]{"xdg-open", file.getAbsolutePath()});
        }
        catch (IOException e) {
          log.error("Error opening browser: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Error opening browser: " + e.getMessage());
        }
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to determine operating system for name \"" + osName + "\".");
      }
    }
    return false;
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
      log.error("Failed to open system file: " + e.getMessage(), e);
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
      log.error("Failed to open folder: " + e.getMessage(), e);
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
   * Opens a new terminal window running the Claude Code console in the given directory.
   *
   * @param folder The project directory to start the Claude console in.
   */
  public static void openClaudeConsole(File folder) {
    if (folder == null || !folder.exists()) {
      return;
    }

    try {
      openClaudeConsoleWithOS(folder);
    }
    catch (IOException e) {
      log.error("Failed to open Claude console: " + e.getMessage(), e);
    }
  }

  private static void openClaudeConsoleWithOS(File folder) throws IOException {
    if (isWindows()) {
      new ProcessBuilder("cmd.exe", "/c", "start", "", "cmd.exe", "/k", "claude")
          .directory(folder)
          .start();
    }
    else if (isMac()) {
      String innerCommand = "cd '" + folder.getAbsolutePath() + "' && claude";
      String appleScriptCommand = innerCommand.replace("\\", "\\\\").replace("\"", "\\\"");
      new ProcessBuilder("osascript", "-e",
          "tell application \"Terminal\" to do script \"" + appleScriptCommand + "\"")
          .start();
    }
    else if (isLinux()) {
      new ProcessBuilder("x-terminal-emulator", "-e", "bash", "-c",
          "cd '" + folder.getAbsolutePath() + "' && claude; exec bash")
          .start();
    }
    else {
      throw new UnsupportedOperationException("Unsupported operating system: " + System.getProperty("os.name"));
    }
  }

  /**
   * Opens the given URL in the system's default web browser.
   *
   * @param url The URL to open.
   */
  public static void openUrl(String url) {
    try {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI.create(url));
      }
      else if (isWindows()) {
        new ProcessBuilder("cmd.exe", "/c", "start", "", url).start();
      }
      else if (isMac()) {
        new ProcessBuilder("open", url).start();
      }
      else if (isLinux()) {
        new ProcessBuilder("xdg-open", url).start();
      }
    }
    catch (IOException e) {
      log.error("Failed to open URL: " + e.getMessage(), e);
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
