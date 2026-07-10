package de.a12.studio.commons;

import de.a12.studio.commons.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class MacOSUpdater {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String UPDATE_CLIENT_SCRIPT_NAME = "update-client.sh";

  public static void createUpdateScript() {
    try {
      LOG.info("Creating update script: " + UPDATE_CLIENT_SCRIPT_NAME);
      String macWritePath = System.getProperty("MAC_WRITE_PATH");
      String script = Updater.loadTemplate("update-client-macos.sh")
          .replace("{{MAC_WRITE_PATH}}", macWritePath)
          .replace("{{MAC_JAR_PATH}}", System.getProperty("MAC_JAR_PATH"))
          .replace("{{MAC_APP_PATH}}", System.getProperty("MAC_APP_PATH"));
      createScript(UPDATE_CLIENT_SCRIPT_NAME, script);
    }
    catch (Exception e) {
      LOG.error("Failed to create update script: {}", e.getMessage(), e);
    }
  }

  public static void launchUpdateScript() throws Exception {
    LOG.info("Launching update script:" + UPDATE_CLIENT_SCRIPT_NAME);

    String basePath = System.getProperty("MAC_WRITE_PATH");
    ProcessBuilder processBuilder = new ProcessBuilder(basePath + UPDATE_CLIENT_SCRIPT_NAME);
    LOG.info("Using macOS base path: {}", basePath);
    processBuilder.directory(new File(basePath));

    Process process = processBuilder.start();
    LOG.info("Starting upgrade process...");

    boolean isRunning = false;
    long startTime = System.currentTimeMillis();
    long maxWaitTime = 5000;

    while (System.currentTimeMillis() - startTime < maxWaitTime) {
      if (process.isAlive()) {
        isRunning = true;
        break;
      }
      Thread.sleep(100);
    }

    if (isRunning) {
      LOG.info("Upgrade process is running.");
    }
    else {
      LOG.warn("Upgrade process did not start successfully within the time limit.");
    }
  }

  private static void createScript(String name, String body) throws IOException {
    try {
      LOG.info("Writing script " + name);
      File file = FileUtils.writeBatch(name, body);

      Set<PosixFilePermission> perms = new HashSet<>();
      perms.add(PosixFilePermission.OWNER_READ);
      perms.add(PosixFilePermission.OWNER_WRITE);
      perms.add(PosixFilePermission.OWNER_EXECUTE);
      Files.setPosixFilePermissions(file.toPath(), perms);
      LOG.info("Applied execute permissions to: " + file.getAbsolutePath());
    }
    catch (Exception e) {
      LOG.error("Failed to create script file: {}", e.getMessage(), e);
    }
  }

  public static void updateAppVersion(String appVersion, String newVersion) throws IOException {
    String cfgFilePath = System.getProperty("MAC_JAR_PATH") + "/A12-Studio.cfg";
    String pListFilePath = System.getProperty("MAC_JAR_PATH") + "/../Info.plist";
    try {
      replaceTextInFile(cfgFilePath, appVersion, newVersion);
      LOG.info("Mac Updater: Incremented app version from " + appVersion + " to " + newVersion + " in " + cfgFilePath);
      replaceTextInFile(pListFilePath, appVersion, newVersion);
      LOG.info("Mac Updater: Incremented app version from " + appVersion + " to " + newVersion + " in " + pListFilePath);
    }
    catch (IOException e) {
      LOG.error("Failed to increment mac app version: {}", e.getMessage(), e);
    }
  }

  private static void replaceTextInFile(String path, String oldText, String newText) throws IOException {
    try {
      String fileContent = Files.readString(Paths.get(path));
      fileContent = fileContent.replaceAll(oldText, newText);
      Files.writeString(Paths.get(path), fileContent);
      LOG.info("Replaced text in file: " + oldText + " to " + newText + " in " + path);
    }
    catch (IOException e) {
      LOG.error("Error replacing text in file: {}", e.getMessage(), e);
    }
  }
}
