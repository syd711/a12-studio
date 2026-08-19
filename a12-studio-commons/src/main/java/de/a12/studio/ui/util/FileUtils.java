package de.a12.studio.ui.util;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 */
@Slf4j
public class FileUtils {

  private static final Pattern VALID_WINDOWS_FILENAME = Pattern.compile("^[^\\s\\\\/:*?\"<>|\\x00-\\x1F]+$");

  private static final Set<String> RESERVED_WINDOWS_NAMES = Set.of(
      "CON", "PRN", "AUX", "NUL",
      "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
      "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9");

  /**
   * Checks whether the given name is a valid Windows filename without whitespaces, i.e. it does not contain
   * any character reserved by Windows, is not a reserved device name, does not end with a dot and is not blank.
   */
  public static boolean isValidWindowsFilename(String name) {
    if (name == null || name.isBlank()) {
      return false;
    }
    if (!VALID_WINDOWS_FILENAME.matcher(name).matches()) {
      return false;
    }
    if (name.endsWith(".")) {
      return false;
    }
    String baseName = name.contains(".") ? name.substring(0, name.indexOf('.')) : name;
    return !RESERVED_WINDOWS_NAMES.contains(baseName.toUpperCase());
  }

  public static boolean checkedCopy(@NonNull File source, @NonNull File target) {
    try {
      if (!target.exists() || source.length() != target.length()) {
        if (target.exists() && !target.delete()) {
          log.error("Failed to delete target file {} of checked copy {}", target.getAbsolutePath(), source.getAbsolutePath());
          return false;
        }
        org.apache.commons.io.FileUtils.copyFile(source, target);
        log.info("Copied {}/({}) to {}", source.getAbsolutePath(), source.length(), target.getAbsolutePath());
        return true;
      }
    }
    catch (Exception e) {
      log.error("Failed to execute checked copy: {}", e.getMessage(), e);
    }
    return true;
  }

  public static File writeBatch(String name, String content) throws IOException {
    File path;
    if (!OSUtil.isMac()) {
      path = new File("./" + name);
    }
    else {
      path = new File(System.getProperty("MAC_WRITE_PATH") + name);
    }

    if (path.exists() && !path.delete()) {
      log.error("Failed to delete existing script file {}", path.getAbsolutePath());
    }
    Files.write(path.toPath(), content.getBytes());
    log.info("Written script file {}", path.getAbsolutePath());
    return path;
  }
}
