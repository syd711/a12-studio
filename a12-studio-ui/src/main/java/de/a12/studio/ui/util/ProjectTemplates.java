package de.a12.studio.ui.util;

import de.a12.studio.ui.util.zip.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Resolves the bundled project templates (zip archives under the {@code project-templates}
 * classpath resource folder, see {@code a12-studio-ui/src/main/resources/project-templates}) and
 * installs them into a new project folder.
 */
@Slf4j
public class ProjectTemplates {

  private static final String RESOURCE_FOLDER = "project-templates";

  private ProjectTemplates() {
  }

  /**
   * Returns the base names (without the {@code .zip} extension) of the bundled project templates,
   * sorted alphabetically. Works both when running exploded (IDE/gradle run) and when packaged into
   * the shaded application jar.
   */
  public static List<String> listTemplateNames() {
    List<String> names = new ArrayList<>();
    try {
      URL url = ProjectTemplates.class.getClassLoader().getResource(RESOURCE_FOLDER);
      if (url == null) {
        log.error("Project template resource folder \"{}\" not found on the classpath.", RESOURCE_FOLDER);
        return names;
      }

      if ("file".equals(url.getProtocol())) {
        File dir = new File(url.toURI());
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".zip"));
        if (files != null) {
          for (File file : files) {
            names.add(FilenameUtils.getBaseName(file.getName()));
          }
        }
      }
      else if ("jar".equals(url.getProtocol())) {
        JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        try (JarFile jarFile = jarConnection.getJarFile()) {
          String prefix = RESOURCE_FOLDER + "/";
          Enumeration<JarEntry> entries = jarFile.entries();
          while (entries.hasMoreElements()) {
            String entryName = entries.nextElement().getName();
            if (entryName.startsWith(prefix) && entryName.toLowerCase().endsWith(".zip")) {
              names.add(FilenameUtils.getBaseName(entryName));
            }
          }
        }
      }
    }
    catch (Exception e) {
      log.error("Failed to list project templates: {}", e.getMessage(), e);
    }
    Collections.sort(names);
    return names;
  }

  /**
   * Extracts the given template's zip archive into {@code targetFolder}. Template archives may wrap
   * their content in one or more nested top-level folders (e.g. {@code basic/...} or even
   * {@code advanced/advanced/...}); that wrapping is stripped so the archive's actual content ends up
   * directly inside {@code targetFolder}.
   *
   * @return {@code true} if the template was installed successfully.
   */
  public static boolean install(String templateName, File targetFolder) {
    String resourcePath = RESOURCE_FOLDER + "/" + templateName + ".zip";
    File tempZip = null;
    File tempExtractDir = null;
    try {
      tempZip = File.createTempFile("a12-project-template-", ".zip");
      try (InputStream in = ProjectTemplates.class.getClassLoader().getResourceAsStream(resourcePath)) {
        if (in == null) {
          log.error("Project template resource \"{}\" not found on the classpath.", resourcePath);
          return false;
        }
        Files.copy(in, tempZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }

      tempExtractDir = Files.createTempDirectory("a12-project-template-extract-").toFile();
      if (!ZipUtil.unzip(tempZip, tempExtractDir, null)) {
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
      if (tempZip != null) {
        org.apache.commons.io.FileUtils.deleteQuietly(tempZip);
      }
      if (tempExtractDir != null) {
        org.apache.commons.io.FileUtils.deleteQuietly(tempExtractDir);
      }
    }
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
