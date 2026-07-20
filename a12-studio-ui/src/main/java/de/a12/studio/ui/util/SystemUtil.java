package de.a12.studio.ui.util;


import de.a12.studio.models.projects.settings.PreviewSettings;
import de.a12.studio.ui.Studio;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static de.a12.studio.ui.util.OSUtil.isLinux;
import static de.a12.studio.ui.util.OSUtil.isMac;
import static de.a12.studio.ui.util.OSUtil.isWindows;

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
    openClaudeConsole(folder, "claude");
  }

  /**
   * Opens a new terminal window running the Claude Code console in the given directory.
   *
   * @param folder The project directory to start the Claude console in.
   * @param claudeCommand The command (or absolute path to the executable) used to launch Claude.
   */
  public static void openClaudeConsole(File folder, String claudeCommand) {
    if (folder == null || !folder.exists()) {
      return;
    }

    try {
      openClaudeConsoleWithOS(folder, claudeCommand);
    }
    catch (IOException e) {
      log.error("Failed to open Claude console: " + e.getMessage(), e);
    }
  }

  private static void openClaudeConsoleWithOS(File folder, String claudeCommand) throws IOException {
    if (isWindows()) {
      new ProcessBuilder("cmd.exe", "/c", "start", "", "cmd.exe", "/k", claudeCommand)
          .directory(folder)
          .start();
    }
    else if (isMac()) {
      String innerCommand = "cd '" + folder.getAbsolutePath() + "' && '" + claudeCommand + "'";
      String appleScriptCommand = innerCommand.replace("\\", "\\\\").replace("\"", "\\\"");
      new ProcessBuilder("osascript", "-e",
          "tell application \"Terminal\" to do script \"" + appleScriptCommand + "\"")
          .start();
    }
    else if (isLinux()) {
      new ProcessBuilder("x-terminal-emulator", "-e", "bash", "-c",
          "cd '" + folder.getAbsolutePath() + "' && '" + claudeCommand + "'; exec bash")
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
   * Opens the given URL in the browser selected by {@code browserType}, falling back to {@link
   * #openUrl(String)}'s system-default behavior for {@code SYSTEM_DEFAULT} or if the requested browser's
   * executable can't be launched (e.g. not installed).
   *
   * @param url The URL to open.
   * @param browserType Which browser to launch the URL in.
   */
  public static void openUrl(String url, PreviewSettings.BrowserType browserType) {
    if (browserType == PreviewSettings.BrowserType.SYSTEM_DEFAULT) {
      openUrl(url);
      return;
    }

    try {
      new ProcessBuilder(browserCommand(browserType, url)).start();
    }
    catch (IOException e) {
      log.warn("Failed to launch {}, falling back to the system default browser: {}", browserType, e.getMessage());
      openUrl(url);
    }
  }

  /**
   * Windows resolves common browser names ("chrome", "firefox", "msedge") through the registry's App Paths
   * entries when routed through {@code cmd /c start}, same as the plain {@code start "" <url>} call in {@link
   * #openUrl(String)} - a bare {@link ProcessBuilder} call wouldn't find them without their install directory
   * on PATH, which isn't the default for these otherwise.
   */
  private static List<String> browserCommand(PreviewSettings.BrowserType browserType, String url) {
    if (isWindows()) {
      String executable = switch (browserType) {
        case CHROME -> "chrome";
        case FIREFOX -> "firefox";
        case EDGE -> "msedge";
        case SYSTEM_DEFAULT -> throw new IllegalStateException("handled by the caller");
      };
      return List.of("cmd.exe", "/c", "start", "", executable, url);
    }
    if (isMac()) {
      String appName = switch (browserType) {
        case CHROME -> "Google Chrome";
        case FIREFOX -> "Firefox";
        case EDGE -> "Microsoft Edge";
        case SYSTEM_DEFAULT -> throw new IllegalStateException("handled by the caller");
      };
      return List.of("open", "-a", appName, url);
    }
    String executable = switch (browserType) {
      case CHROME -> "google-chrome";
      case FIREFOX -> "firefox";
      case EDGE -> "microsoft-edge";
      case SYSTEM_DEFAULT -> throw new IllegalStateException("handled by the caller");
    };
    return List.of(executable, url);
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
