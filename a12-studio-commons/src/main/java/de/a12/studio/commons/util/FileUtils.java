package de.a12.studio.commons.util;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;

/**
 * File helpers used by the self-updater. Trimmed port of vpin-studio's
 * de.mephisto.vpin.restclient.util.FileUtils to just the pieces Updater needs.
 */
public class FileUtils {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static boolean checkedCopy(@NonNull File source, @NonNull File target) {
    try {
      if (!target.exists() || source.length() != target.length()) {
        if (target.exists() && !target.delete()) {
          LOG.error("Failed to delete target file {} of checked copy {}", target.getAbsolutePath(), source.getAbsolutePath());
          return false;
        }
        org.apache.commons.io.FileUtils.copyFile(source, target);
        LOG.info("Copied {}/({}) to {}", source.getAbsolutePath(), source.length(), target.getAbsolutePath());
        return true;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to execute checked copy: {}", e.getMessage(), e);
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
      LOG.error("Failed to delete existing script file {}", path.getAbsolutePath());
    }
    Files.write(path.toPath(), content.getBytes());
    LOG.info("Written script file {}", path.getAbsolutePath());
    return path;
  }
}
