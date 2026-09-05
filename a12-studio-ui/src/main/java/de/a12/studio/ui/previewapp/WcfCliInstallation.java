package de.a12.studio.ui.previewapp;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Resolves the {@code wcf-cli/} folder that {@code stageWcfCli} (see {@code a12-studio-ui/build.gradle})
 * stages next to {@code A12-Studio.exe}/{@code a12-studio-ui.jar} - a flat set of loose jars for the
 * WCF-&gt;RMC model conversion tool ({@code com.mgmtp.a12.dataservices.wcf.WcfCli}), used as a {@code -cp}
 * for a subprocess (see {@link ModelConversionService}).
 *
 * <p>Unlike {@link PreviewAppInstallation}, which resolves the external, user-configured "A12
 * installation folder", this resolves an asset a12-studio ships with itself - so it looks next to
 * a12-studio's own running jar, not at a configured path.
 */
public final class WcfCliInstallation {

  private WcfCliInstallation() {
  }

  public static File resolve() throws PreviewAppException {
    File installDir = resolveOwnInstallDir();

    File wcfCliDir = new File("./Output/A12-Studio/", "wcf-cli");
    if (!wcfCliDir.exists()) {
      wcfCliDir = new File(installDir, "wcf-cli");
    }
    if (!wcfCliDir.isDirectory() || wcfCliDir.listFiles((dir, name) -> name.endsWith(".jar")).length == 0) {
      throw new PreviewAppException(
          "Could not find the WCF-CLI model conversion jars under \"" + wcfCliDir.getAbsolutePath()
              + "\". Run the \"stageWcfCli\" Gradle task (or reinstall A12 Studio) to provision them.");
    }
    return wcfCliDir;
  }

  private static File resolveOwnInstallDir() throws PreviewAppException {
    try {
      File location = new File(WcfCliInstallation.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      // Packaged: location is .../A12-Studio/a12-studio-ui.jar -> parent is the install dir.
      // Dev/IDE: location is an exploded classes dir with no jar sibling - resolve() will fail
      // with a clear message rather than silently returning a nonsensical directory.
      return location.isFile() ? location.getParentFile() : location;
    }
    catch (URISyntaxException | NullPointerException e) {
      throw new PreviewAppException("Could not determine A12 Studio's own install location.", e);
    }
  }
}
