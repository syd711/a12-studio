package de.a12.studio.ui;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.FileAppender;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.StudioVersion;
import de.a12.studio.ui.util.SystemUtil;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * System tray icon with a small context menu (open the log file, close the client). Silently does
 * nothing on platforms without system tray support.
 */
@Slf4j
public class StudioTray {

  private static @Nullable TrayIcon trayIcon;

  private StudioTray() {
  }

  /**
   * Adds the tray icon; safe to call more than once and from any thread.
   */
  public static synchronized void install() {
    if (trayIcon != null) {
      return;
    }
    if (!SystemTray.isSupported()) {
      log.info("System tray is not supported on this platform, skipping tray menu.");
      return;
    }

    try {
      PopupMenu menu = new PopupMenu();

      MenuItem openLog = new MenuItem(StudioBundle.get("studio_tray.open_log"));
      openLog.addActionListener(e -> openLogFile());
      menu.add(openLog);

      menu.addSeparator();

      MenuItem close = new MenuItem(StudioBundle.get("studio_tray.close"));
      close.addActionListener(e -> Platform.runLater(Platform::exit));
      menu.add(close);

      TrayIcon icon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Studio.class.getResource("logo-180.png")),
          "A12 Studio - " + StudioVersion.get(), menu);
      icon.setImageAutoSize(true);
      SystemTray.getSystemTray().add(icon);
      trayIcon = icon;
    }
    catch (AWTException | RuntimeException e) {
      log.warn("Failed to install tray menu: {}", e.getMessage(), e);
    }
  }

  /**
   * Removes the tray icon. Required on shutdown, otherwise the AWT event thread keeps the JVM alive.
   */
  public static synchronized void uninstall() {
    if (trayIcon != null) {
      SystemTray.getSystemTray().remove(trayIcon);
      trayIcon = null;
    }
  }

  /**
   * Prefers {@link Desktop#open} (ShellExecute "open"), which honors the user's per-extension choice;
   * {@link SystemUtil#editFile} goes through {@code cmd /c start}, which exits 1 without opening
   * anything for {@code .log} when no system-wide association is registered.
   */
  private static void openLogFile() {
    File logFile = getLogFile();
    if (logFile == null || !logFile.exists()) {
      log.warn("Log file to open does not exist: {}", logFile);
      return;
    }

    try {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
        Desktop.getDesktop().open(logFile);
        return;
      }
    }
    catch (IOException | RuntimeException e) {
      log.warn("Failed to open log file {} with the default application: {}", logFile, e.getMessage());
    }
    Platform.runLater(() -> SystemUtil.editFile(logFile));
  }

  /**
   * The file the running logback configuration writes to, so this stays correct however
   * {@code OSLOG_PATH} was set.
   */
  private static @Nullable File getLogFile() {
    if (LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) instanceof Logger root) {
      for (Iterator<ch.qos.logback.core.Appender<ch.qos.logback.classic.spi.ILoggingEvent>> it = root.iteratorForAppenders();
           it.hasNext(); ) {
        if (it.next() instanceof FileAppender<?> fileAppender && fileAppender.getFile() != null) {
          return new File(fileAppender.getFile()).getAbsoluteFile();
        }
      }
    }
    log.warn("No log file appender configured.");
    return null;
  }
}
