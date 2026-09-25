package de.a12.studio.ui.previewapp;

import java.io.File;

/**
 * Resolves the pieces of the installed Simple Model Editor ({@code <installation>/bin/simple-model-editor/<version>})
 * that the Form Engine preview borrows: the Spring Boot backend {@code sme.jar} (Document Model expansion and
 * validation-code generation, see {@link SmeBackend}) and the compiled client bundle in {@code static}, which
 * renders a Form Model with the real Form Engine when loaded in "preview window" mode (see {@code
 * de.a12.studio.ui.preview.PreviewServer}).
 *
 * <p>Both are read from the user's own A12 installation at runtime, like {@link PreviewAppInstallation} does for the
 * Preview App, rather than being copied into a12-studio.
 */
public class SmeInstallation {

  private final File javaExecutable;
  private final File backendJar;
  private final File staticDir;

  private SmeInstallation(File javaExecutable, File backendJar, File staticDir) {
    this.javaExecutable = javaExecutable;
    this.backendJar = backendJar;
    this.staticDir = staticDir;
  }

  public File getJavaExecutable() {
    return javaExecutable;
  }

  public File getBackendJar() {
    return backendJar;
  }

  /** The client bundle folder ({@code index.html}, the webpack chunks, fonts, images). */
  public File getStaticDir() {
    return staticDir;
  }

  public static SmeInstallation resolve() throws PreviewAppException {
    File bin = PreviewAppInstallation.resolveBinFolder();
    File javaExecutable = PreviewAppInstallation.findJavaExecutable(bin);

    File smeHome = new File(bin, "simple-model-editor");
    File backendJar = PreviewAppInstallation.findLatestVersionEntry(smeHome, versionDir -> new File(versionDir, "sme.jar"))
        .orElseThrow(() -> new PreviewAppException(
            "Could not find the Simple Model Editor (\"sme.jar\") under \"" + smeHome.getAbsolutePath() + "\"."));
    File staticDir = PreviewAppInstallation.findLatestVersionEntry(smeHome, versionDir -> new File(versionDir, "static"))
        .filter(dir -> new File(dir, "index.html").isFile())
        .orElseThrow(() -> new PreviewAppException(
            "Could not find the Simple Model Editor client (\"static/index.html\") under \"" + smeHome.getAbsolutePath() + "\"."));

    return new SmeInstallation(javaExecutable, backendJar, staticDir);
  }
}
