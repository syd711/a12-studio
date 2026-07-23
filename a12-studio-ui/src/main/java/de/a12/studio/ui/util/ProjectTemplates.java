package de.a12.studio.ui.util;

import de.a12.studio.ui.updater.Updater;
import de.a12.studio.ui.util.zip.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resolves the bundled project templates (zip archives in the {@code resources/project-templates}
 * folder next to the running application, see {@link Updater#getWriteableBaseFolder()}) and installs
 * them into a new project folder.
 */
@Slf4j
public class ProjectTemplates {

  private static final String TEMPLATES_FOLDER = "resources/project-templates";

  private ProjectTemplates() {
  }

  /**
   * Returns the base names (without the {@code .zip} extension) of the bundled project templates,
   * sorted alphabetically.
   */
  public static List<String> listTemplateNames() {
    List<String> names = new ArrayList<>();
    File[] files = templatesFolder().listFiles((dir, name) -> name.toLowerCase().endsWith(".zip"));
    if (files != null) {
      for (File file : files) {
        names.add(FilenameUtils.getBaseName(file.getName()));
      }
    }
    else {
      log.error("Project templates folder \"{}\" not found.", templatesFolder().getAbsolutePath());
    }
    Collections.sort(names);
    return names;
  }

  /**
   * Extracts the given template's zip archive into {@code targetFolder}. Template archives may wrap
   * their content in one or more nested top-level folders; that wrapping is stripped so the archive's
   * actual content ends up directly inside {@code targetFolder}.
   *
   * @return {@code true} if the template was installed successfully.
   */
  public static boolean install(String templateName, File targetFolder) {
    File templateZip = new File(templatesFolder(), templateName + ".zip");
    if (!templateZip.exists()) {
      log.error("Project template archive \"{}\" not found.", templateZip.getAbsolutePath());
      return false;
    }

    File tempExtractDir = null;
    try {
      tempExtractDir = Files.createTempDirectory("a12-project-template-extract-").toFile();
      if (!ZipUtil.unzip(templateZip, tempExtractDir, null)) {
        return false;
      }

      File contentRoot = resolveContentRoot(tempExtractDir);
      org.apache.commons.io.FileUtils.copyDirectory(contentRoot, targetFolder);
      return true;
    }
    catch (IOException e) {
      log.error("Failed to install project template \"{}\": {}", templateName, e.getMessage(), e);
      return false;
    }
    finally {
      if (tempExtractDir != null) {
        org.apache.commons.io.FileUtils.deleteQuietly(tempExtractDir);
      }
    }
  }

  private static File templatesFolder() {
    return new File(Updater.getWriteableBaseFolder(), TEMPLATES_FOLDER);
  }

  /**
   * Descends through directories that contain nothing but a single subdirectory, so the returned
   * folder is the first level that actually holds the template's real content.
   */
  private static File resolveContentRoot(File dir) {
    File[] children = dir.listFiles();
    if (children != null && children.length == 1 && children[0].isDirectory()) {
      return resolveContentRoot(children[0]);
    }
    return dir;
  }
}
