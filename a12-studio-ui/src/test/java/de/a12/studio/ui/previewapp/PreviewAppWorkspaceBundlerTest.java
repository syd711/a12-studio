package de.a12.studio.ui.previewapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the hand-rolled USTAR writer by round-tripping through the platform's own {@code tar}
 * binary (present on Windows 10+, macOS and Linux) rather than re-implementing a tar reader.
 */
class PreviewAppWorkspaceBundlerTest {

  @Test
  void bundle_excludesBundledFolderAndRoundTripsContentThroughSystemTar(@TempDir File workspace, @TempDir File extractDir)
      throws IOException, InterruptedException {
    File topLevelFile = new File(workspace, "settings.json");
    Files.writeString(topLevelFile.toPath(), "{\"version\":\"1.0.0\"}");

    File modelsDir = new File(workspace, "models");
    modelsDir.mkdirs();
    File nestedFile = new File(modelsDir, "Person_DM.json");
    Files.writeString(nestedFile.toPath(), "{\"id\":\"Person_DM\"}");

    File dotFolder = new File(workspace, ".studio");
    dotFolder.mkdirs();
    File dotFolderFile = new File(dotFolder, "annotation-settings.json");
    Files.writeString(dotFolderFile.toPath(), "{}");

    // Pre-existing stale "bundled" folder from an earlier run - must never end up in the archive.
    File staleBundled = new File(workspace, "bundled");
    staleBundled.mkdirs();
    Files.writeString(new File(staleBundled, "seed.tar.gz").toPath(), "stale");

    File seedFile = PreviewAppWorkspaceBundler.bundle(workspace);

    assertTrue(seedFile.exists());
    assertEquals(new File(workspace, "bundled/seed.tar.gz"), seedFile);

    Process tar = new ProcessBuilder("tar", "xzf", seedFile.getAbsolutePath(), "-C", extractDir.getAbsolutePath())
        .redirectErrorStream(true)
        .start();
    String tarOutput = new String(tar.getInputStream().readAllBytes());
    assertEquals(0, tar.waitFor(), "tar extraction failed: " + tarOutput);

    assertEquals("{\"version\":\"1.0.0\"}", Files.readString(new File(extractDir, "settings.json").toPath()));
    assertEquals("{\"id\":\"Person_DM\"}", Files.readString(new File(extractDir, "models/Person_DM.json").toPath()));
    assertEquals("{}", Files.readString(new File(extractDir, ".studio/annotation-settings.json").toPath()));
    assertFalse(new File(extractDir, "bundled").exists(), "the 'bundled' folder itself must be excluded from the archive");
  }
}
