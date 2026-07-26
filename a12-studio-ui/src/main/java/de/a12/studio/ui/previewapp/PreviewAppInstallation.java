package de.a12.studio.ui.previewapp;

import de.a12.studio.models.projects.settings.A12Settings;
import de.a12.studio.ui.util.OSUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

/**
 * Resolves the java executable, preview-app-server jar and preview-app-client static folder from
 * the configured A12 installation folder, e.g.:
 * <pre>
 * {@code <installation>/bin/java/<version>/bin/java.exe}
 * {@code <installation>/bin/preview-app-server/<version>/previewapp-server-<version>.jar}
 * {@code <installation>/bin/preview-app-client/<version>/app}
 * </pre>
 * This mirrors the layout that the "A12 Preview App Control" Electron tool (shipped alongside)
 * consumes, discovered independently rather than by reading that tool's own settings.json, so it
 * keeps working even if that tool is absent.
 */
public class PreviewAppInstallation {

  public static final int SERVER_PORT = 8082;

  private final File javaExecutable;
  private final File serverJar;
  private final File clientStaticDir;

  private PreviewAppInstallation(File javaExecutable, File serverJar, File clientStaticDir) {
    this.javaExecutable = javaExecutable;
    this.serverJar = serverJar;
    this.clientStaticDir = clientStaticDir;
  }

  public File getJavaExecutable() {
    return javaExecutable;
  }

  public File getServerJar() {
    return serverJar;
  }

  public File getClientStaticDir() {
    return clientStaticDir;
  }

  public static PreviewAppInstallation resolve() throws PreviewAppException {
    String installationPath = A12Settings.load().getInstallationPath();
    if (installationPath == null || installationPath.isEmpty()) {
      throw new PreviewAppException("No A12 installation folder is configured. Set it in Preferences first.");
    }

    File installationFolder = new File(installationPath);
    if (!A12Settings.isValidInstallationFolder(installationFolder)) {
      throw new PreviewAppException("\"" + installationPath + "\" is not a valid A12 installation folder.");
    }

    File bin = new File(installationFolder, "bin");

    File javaHome = new File(bin, "java");
    File javaExecutable = findLatestVersionEntry(javaHome,
        versionDir -> new File(versionDir, OSUtil.isWindows() ? "bin/java.exe" : "bin/java"))
        .orElseThrow(() -> new PreviewAppException(
            "Could not find a bundled Java runtime under \"" + javaHome.getAbsolutePath() + "\"."));

    File serverHome = new File(bin, "preview-app-server");
    File serverVersionDir = findLatestVersionEntry(serverHome, Function.identity())
        .orElseThrow(() -> new PreviewAppException(
            "Could not find \"preview-app-server\" under \"" + serverHome.getAbsolutePath() + "\"."));
    File serverJar = findSingleJar(serverVersionDir)
        .orElseThrow(() -> new PreviewAppException(
            "Could not find the preview-app-server jar in \"" + serverVersionDir.getAbsolutePath() + "\"."));

    File clientHome = new File(bin, "preview-app-client");
    File clientStaticDir = findLatestVersionEntry(clientHome, versionDir -> new File(versionDir, "app"))
        .orElseThrow(() -> new PreviewAppException(
            "Could not find \"preview-app-client\" under \"" + clientHome.getAbsolutePath() + "\"."));

    return new PreviewAppInstallation(javaExecutable, serverJar, clientStaticDir);
  }

  /**
   * Lists the version subfolders of {@code parent} (e.g. "202606.0.2"), tries the newest first (by
   * name, descending) and returns the first one for which {@code mapper} produces an existing file.
   */
  private static Optional<File> findLatestVersionEntry(File parent, Function<File, File> mapper) {
    File[] versionDirs = parent.listFiles(File::isDirectory);
    if (versionDirs == null || versionDirs.length == 0) {
      return Optional.empty();
    }

    return Arrays.stream(versionDirs)
        .sorted(Comparator.comparing(File::getName).reversed())
        .map(mapper)
        .filter(File::exists)
        .findFirst();
  }

  private static Optional<File> findSingleJar(File dir) {
    File[] jars = dir.listFiles((d, name) -> name.endsWith(".jar"));
    if (jars == null || jars.length == 0) {
      return Optional.empty();
    }
    return Optional.of(jars[0]);
  }
}
