package de.a12.studio.commons;

import de.a12.studio.commons.util.FileUtils;
import de.a12.studio.commons.util.OSUtil;
import de.a12.studio.commons.util.SystemCommandExecutor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Slf4j
public class Updater {

  public final static String BASE_URL = "https://github.com/syd711/a12-studio/releases/download/%s/";
  private final static String LATEST_RELEASE_URL = "https://github.com/syd711/a12-studio/releases/latest";
  public static String LATEST_VERSION = null;

  public final static String SERVER_ZIP = "A12-Studio-Server.zip";
  // Rough size estimate used only to render a download progress percentage; update once real release artifacts exist.
  public final static long SERVER_ZIP_SIZE = 150 * 1000 * 1000;

  public final static String UI_ZIP = "A12-Studio.zip";
  public final static String UI_JAR_ZIP = "a12-studio-ui-jar.zip";
  public final static long UI_ZIP_SIZE = 100 * 1000 * 1000;

  private final static String DOWNLOAD_SUFFIX = ".bak";

  public static boolean downloadUpdate(String versionSegment, String targetZip) {
    File out = new File(getWriteableBaseFolder(), targetZip);
    if (out.exists()) {
      out.delete();
    }
    String url = String.format(BASE_URL, versionSegment) + targetZip;
    download(url, out);
    return true;
  }

  public static int getDownloadProgress(String targetZip, long estimatedSize) {
    File tmp = new File(getWriteableBaseFolder(), targetZip + DOWNLOAD_SUFFIX);
    File zip = new File(getWriteableBaseFolder(), targetZip);
    if (zip.exists()) {
      return 100;
    }

    int percentage = (int) (tmp.length() * 100 / estimatedSize);
    if (percentage > 99) {
      percentage = 99;
    }

    log.info("{} download at {}%", tmp.getAbsolutePath(), percentage);
    return percentage;
  }

  public static void download(String downloadUrl, File target) {
    downloadAndOverwrite(downloadUrl, target, false);
  }

  public static void downloadAndOverwrite(String downloadUrl, File target, boolean overwrite) {
    try {
      log.info("Downloading {}", downloadUrl);
      URL url = URI.create(downloadUrl).toURL();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setReadTimeout(5000);
      connection.setUseCaches(false);
      connection.setRequestProperty("Cache-Control", "no-cache, no-store");
      connection.setRequestProperty("Pragma", "no-cache");
      BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
      File tmp = new File(getWriteableBaseFolder(), target.getName() + DOWNLOAD_SUFFIX);

      if (tmp.exists()) {
        tmp.delete();
      }
      FileOutputStream fileOutputStream = new FileOutputStream(tmp);
      byte[] dataBuffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      in.close();
      fileOutputStream.close();

      if (overwrite && target.exists() && !target.delete()) {
        log.error("Failed to overwrite target file \"{}\"", target.getAbsolutePath());
        return;
      }

      if (!FileUtils.checkedCopy(tmp, target)) {
        log.error("Failed to copy download temp file {} to {}", tmp.getAbsolutePath(), target.getAbsolutePath());
      }
      log.info("Download of {}/({}) finished", target.getAbsolutePath(), target.length());
      if (tmp.delete()) {
        log.info("Deleted downloaded temp file {}", tmp.getAbsolutePath());
      }
      else {
        log.info("Failed to deleted downloaded temp file {}", tmp.getAbsolutePath());
      }
    }
    catch (Exception e) {
      log.error("Updater Failed to execute download: {}", e.getMessage(), e);
    }
  }

  public static void download(String downloadUrl, File target, boolean synchronous) {
    if (synchronous) {
      download(downloadUrl, target);
    }
    else {
      new Thread(() -> download(downloadUrl, target)).start();
    }
  }

  public static boolean installServerUpdate() throws IOException {
    FileUtils.writeBatch("update-server.bat", loadTemplate("update-server.bat"));
    List<String> commands = Arrays.asList("cmd", "/c", "start", "update-server.bat");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(getWriteableBaseFolder());
    executor.executeCommandAsync();
    return true;
  }

  public static boolean installClientUpdate(@Nullable String oldVersion, @Nullable String newVersion) throws IOException {
    if (OSUtil.isWindows()) {
      String cmds = loadTemplate("update-client-windows.bat");
      FileUtils.writeBatch("update-client.bat", cmds);
      log.info("Written temporary batch: {}", cmds);
      List<String> commands = Arrays.asList("cmd", "/c", "start", "update-client.bat");
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(getWriteableBaseFolder());
      executor.executeCommandAsync();
      new Thread(() -> {
        try {
          Thread.sleep(2000);
          System.exit(0);
        }
        catch (InterruptedException e) {
          //ignore
        }
      }).start();
    }
    else if (OSUtil.isLinux()) {
      try {
        String cmds = loadTemplate("update-client-linux.sh");
        File file = FileUtils.writeBatch("update-client.sh", cmds);
        log.info("Written temporary bash: {}", cmds);

        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(file.toPath(), perms);
        log.info("Applied execute permissions to : {}", file.getAbsolutePath());

        List<String> commands = List.of("./update-client.sh");
        SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
        executor.setDir(getWriteableBaseFolder());
        executor.enableLogging(true);
        executor.executeCommandAsync();
        new Thread(() -> {
          try {
            log.info("Exiting a12-studio");
            Thread.sleep(2000);
            System.exit(0);
          }
          catch (InterruptedException e) {
            //ignore
          }
        }).start();
      }
      catch (Exception e) {
        log.error("Failed to execute update: {}", e.getMessage(), e);
      }
    }
    else if (OSUtil.isMac()) {
      // For macOS we use our startup bash to perform the upgrade.
      try {
        MacOSUpdater.createUpdateScript();
        MacOSUpdater.updateAppVersion(oldVersion, newVersion);

        log.info("Exiting a12-studio to perform update...");
        MacOSUpdater.launchUpdateScript();
      }
      catch (Exception e) {
        log.error("Failed to execute update and restart: {}", e.getMessage(), e);
      }
    }
    return true;
  }

  public static void restartServer() {
    List<String> commands = List.of("A12-Studio-Server.exe");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(getWriteableBaseFolder());
    executor.executeCommandAsync();
  }

  public static String checkForUpdate() {
    try {
      URL obj = URI.create(LATEST_RELEASE_URL).toURL();
      HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
      conn.setInstanceFollowRedirects(true);
      HttpURLConnection.setFollowRedirects(true);
      conn.setReadTimeout(5000);
      conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
      conn.addRequestProperty("User-Agent", "Mozilla");
      conn.addRequestProperty("Referer", "google.com");

      int responseCode = conn.getResponseCode(); //DO NOT DELETE!!!!

      String s = conn.getURL().toString();
      String segment = s.substring(s.lastIndexOf("/") + 1);
      if (segment.equals("latest")) {
        // GitHub didn't redirect to a tag, meaning the repo has no releases yet.
        log.info("No releases found at {}", LATEST_RELEASE_URL);
        return null;
      }
      LATEST_VERSION = segment;
      return LATEST_VERSION;
    }
    catch (Exception e) {
      log.error("Update check failed: {}", e.getMessage());
    }
    return null;
  }

  public static boolean isLargerVersionThan(String versionA, String versionB) {
    if (versionA == null || versionB == null) {
      return false;
    }

    List<Integer> versionASegments = Arrays.stream(versionA.split("\\.")).map(Integer::parseInt).toList();
    List<Integer> versionBSegments = Arrays.stream(versionB.split("\\.")).map(Integer::parseInt).toList();

    for (int i = 0; i < versionBSegments.size(); i++) {
      if (versionASegments.get(i).intValue() == versionBSegments.get(i).intValue()) {
        continue;
      }

      return versionASegments.get(i) > versionBSegments.get(i);
    }

    return false;
  }

  public static String loadTemplate(String templateName) throws IOException {
    String resourcePath = "/de/a12/studio/commons/" + templateName;
    try (InputStream is = Updater.class.getResourceAsStream(resourcePath)) {
      if (is == null) {
        throw new IOException("Update template not found on classpath: " + resourcePath);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  public static File getWriteableBaseFolder() {
    if (!OSUtil.isMac()) {
      return new File("./");
    }
    else {
      log.info("Setting base path for Mac download to {}", System.getProperty("MAC_WRITE_PATH"));
      return new File(System.getProperty("MAC_WRITE_PATH"));
    }
  }
}
