package de.a12.studio.ui.util;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 *
 */
@Slf4j
public class FileUtils {

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
