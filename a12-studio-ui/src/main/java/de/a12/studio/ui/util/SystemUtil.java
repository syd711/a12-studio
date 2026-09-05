package de.a12.studio.ui.util;


import de.a12.studio.models.projects.settings.PreviewAppSettings;
import de.a12.studio.ui.Studio;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    if (file == null || !file.exists()) {
      return false;
    }
    try {
      if (isWindows()) {
        // "start" with an empty title routes through the registry to the default editor
        new ProcessBuilder("cmd.exe", "/c", "start", "", file.getAbsolutePath()).start();
      }
      else if (isMac()) {
        // "-t" forces TextEdit (or the default text editor) instead of the default app for the file type
        new ProcessBuilder("/usr/bin/open", "-t", file.getAbsolutePath()).start();
      }
      else if (isLinux()) {
        // xdg-open delegates to the desktop environment's default handler (gedit, kate, etc.)
        new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Error",
            "Failed to determine operating system for name \"" + System.getProperty("os.name") + "\".");
      }
    }
    catch (IOException e) {
      log.error("Failed to edit file: {}", e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open file for editing: " + e.getMessage());
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
      new ProcessBuilder("open", absolutePath).start();
    }
    else if (isLinux()) {
      // xdg-open delegates to the desktop environment's file manager (Nautilus, Dolphin, Thunar, etc.)
      new ProcessBuilder("xdg-open", absolutePath).start();
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
  public static void openUrl(String url, PreviewAppSettings.BrowserType browserType) {
    if (browserType == PreviewAppSettings.BrowserType.SYSTEM_DEFAULT) {
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
   *
   * <p>Each command also passes the browser's own new-window flag (e.g. Chromium's {@code --new-window},
   * Firefox's {@code -new-window}) so the preview opens as its own window rather than a new tab of whatever
   * window is already frontmost - matching {@link de.a12.studio.ui.util.browsers.Browser#openUrl}.
   */
  private static List<String> browserCommand(PreviewAppSettings.BrowserType browserType, String url) {
    String newWindowFlag = browserType == PreviewAppSettings.BrowserType.FIREFOX ? "-new-window" : "--new-window";

    if (isWindows()) {
      String executable = switch (browserType) {
        case CHROME -> "chrome";
        case FIREFOX -> "firefox";
        case EDGE -> "msedge";
        case SYSTEM_DEFAULT -> throw new IllegalStateException("handled by the caller");
      };
      return List.of("cmd.exe", "/c", "start", "", executable, newWindowFlag, url);
    }
    if (isMac()) {
      String appName = switch (browserType) {
        case CHROME -> "Google Chrome";
        case FIREFOX -> "Firefox";
        case EDGE -> "Microsoft Edge";
        case SYSTEM_DEFAULT -> throw new IllegalStateException("handled by the caller");
      };
      return List.of("open", "-a", appName, "--args", newWindowFlag, url);
    }
    String executable = switch (browserType) {
      case CHROME -> "google-chrome";
      case FIREFOX -> "firefox";
      case EDGE -> "microsoft-edge";
      case SYSTEM_DEFAULT -> throw new IllegalStateException("handled by the caller");
    };
    return List.of(executable, newWindowFlag, url);
  }

  /**
   * Opens the given URL in a borderless kiosk window (no toolbar, address bar or window chrome) using
   * whichever browser is currently registered as the OS default, falling back to {@link #openUrl(String)}'s
   * normal-window behavior if that browser can't be identified or doesn't support a kiosk mode.
   *
   * @param url The URL to open.
   */
  public static void openUrlInKioskWindow(String url) {
    try {
      if (isWindows()) {
        openUrlInKioskWindowOnWindows(url);
      }
      else {
        log.warn("Kiosk-mode browser launch isn't implemented for this OS; opening \"{}\" in the default browser window instead.", url);
        openUrl(url);
      }
    }
    catch (IOException e) {
      log.error("Failed to open URL in a kiosk window: " + e.getMessage(), e);
      openUrl(url);
    }
  }

  /**
   * Resolves the default browser via the same {@code UserChoice} registry entry Windows itself consults for
   * {@code http} links, then relaunches it with that browser's kiosk-mode flag. Chromium-based browsers
   * (Chrome, Edge, Brave) and Firefox all support this; browsers that don't (e.g. Safari - not applicable on
   * Windows - or Internet Explorer) fall back to {@link #openUrl(String)}.
   */
  private static void openUrlInKioskWindowOnWindows(String url) throws IOException {
    String progId = queryRegistryValue(
        "HKCU\\Software\\Microsoft\\Windows\\Shell\\Associations\\UrlAssociations\\http\\UserChoice", "ProgId");
    String kioskFlag = kioskFlagForProgId(progId);
    String executable = progId != null ? queryDefaultCommandExecutable(progId) : null;

    if (executable == null || kioskFlag == null) {
      log.warn("Could not resolve a kiosk-capable default browser (ProgId \"{}\"); opening \"{}\" in the default browser window instead.", progId, url);
      openUrl(url);
      return;
    }

    new ProcessBuilder(executable, kioskFlag, url).start();
  }

  private static String kioskFlagForProgId(String progId) {
    if (progId == null) {
      return null;
    }
    String normalized = progId.toLowerCase();
    if (normalized.startsWith("chromehtml") || normalized.startsWith("msedgehtm") || normalized.contains("brave") || normalized.contains("chromium")) {
      return "--kiosk";
    }
    if (normalized.startsWith("firefoxurl")) {
      return "-kiosk";
    }
    return null;
  }

  private static String queryRegistryValue(String keyPath, String valueName) throws IOException {
    String output = runAndCaptureOutput(List.of("reg", "query", keyPath, "/v", valueName));
    for (String line : output.split("\\R")) {
      line = line.trim();
      if (line.startsWith(valueName)) {
        String[] parts = line.split("\\s+", 3);
        if (parts.length == 3) {
          return parts[2].trim();
        }
      }
    }
    return null;
  }

  private static String queryDefaultCommandExecutable(String progId) throws IOException {
    String output = runAndCaptureOutput(List.of("reg", "query", "HKCR\\" + progId + "\\shell\\open\\command", "/ve"));
    for (String line : output.split("\\R")) {
      line = line.trim();
      if (!line.startsWith("(Default)")) {
        continue;
      }
      int firstQuote = line.indexOf('"');
      int secondQuote = firstQuote >= 0 ? line.indexOf('"', firstQuote + 1) : -1;
      if (firstQuote >= 0 && secondQuote > firstQuote) {
        return line.substring(firstQuote + 1, secondQuote);
      }
    }
    return null;
  }

  private static String runAndCaptureOutput(List<String> command) throws IOException {
    Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      StringBuilder output = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append('\n');
      }
      process.waitFor();
      return output.toString();
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while running: " + String.join(" ", command), e);
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
      // /select highlights the file inside Explorer
      new ProcessBuilder("explorer.exe", "/select,", absolutePath).start();
    }
    else if (isMac()) {
      // -R reveals the file in Finder
      new ProcessBuilder("open", "-R", absolutePath).start();
    }
    else if (isLinux()) {
      // xdg-open on a file opens it with the default application for its MIME type;
      // most desktop file managers (Nautilus, Dolphin, Thunar) handle this correctly.
      new ProcessBuilder("xdg-open", absolutePath).start();
    }
    else {
      throw new UnsupportedOperationException("Unsupported operating system: " + System.getProperty("os.name"));
    }
  }

}
