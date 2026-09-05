package de.a12.studio.ui.previewapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Stands in for the real {@code com.mgmtp.a12.dataservices.wcf.WcfCli} tool in {@link
 * ModelConversionServiceTest}, so that test exercises {@link ModelConversionService}'s subprocess
 * invocation, classpath assembly and error handling without depending on the real vendor jars.
 * Mirrors the real tool's positional-argument contract: {@code SOURCE_DIR OUTPUT_DIR -c
 * CONVERSION_JAR}. Writes a marker file under {@code OUTPUT_DIR/data/models} recording the
 * conversion jar path it was given, and exits non-zero if {@code SOURCE_DIR/FAIL} exists.
 */
public final class FakeWcfCliMain {

  private FakeWcfCliMain() {
  }

  public static void main(String[] args) throws IOException {
    File sourceModelsDir = new File(args[0]);
    File outputDir = new File(args[1]);
    String conversionJarPath = args[3];

    System.out.println("Fake WcfCli converting " + sourceModelsDir.getAbsolutePath());

    if (new File(sourceModelsDir, "FAIL").isFile()) {
      System.err.println("Fake WcfCli: simulated failure");
      System.exit(1);
    }

    File convertedModelsDir = new File(outputDir, "data/models");
    convertedModelsDir.mkdirs();
    Files.writeString(new File(convertedModelsDir, "converted.txt").toPath(), "conversionJar=" + conversionJarPath);
  }
}
