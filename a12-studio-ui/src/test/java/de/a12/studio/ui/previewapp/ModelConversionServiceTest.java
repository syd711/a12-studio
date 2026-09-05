package de.a12.studio.ui.previewapp;

import de.a12.studio.ui.util.OSUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises {@link ModelConversionService}'s subprocess invocation, classpath assembly and error
 * handling against {@link FakeWcfCliMain} (a fixture standing in for the real vendor tool), so
 * these tests need neither network access nor the real {@code dataservices-wcf-cli}/{@code
 * rmc-conversion} jars.
 */
class ModelConversionServiceTest {

  private static final String FAKE_MAIN_CLASS = FakeWcfCliMain.class.getName();

  @Test
  void convertsUsingFakeWcfCliAndReturnsConvertedModelsDir(@TempDir File wcfCliDir, @TempDir File sourceModelsDir,
      @TempDir File outputDir) throws Exception {
    writeFakeWcfCliJar(wcfCliDir);
    File conversionJar = writeDummyConversionJar(wcfCliDir);

    List<String> logLines = new ArrayList<>();
    File convertedModelsDir = ModelConversionService.convert(
        javaExecutable(), wcfCliDir, sourceModelsDir, outputDir, FAKE_MAIN_CLASS, logLines::add);

    assertEquals(new File(outputDir, "data/models"), convertedModelsDir);
    File marker = new File(convertedModelsDir, "converted.txt");
    assertTrue(marker.isFile());
    assertEquals("conversionJar=" + conversionJar.getAbsolutePath(), Files.readString(marker.toPath()));
    assertTrue(logLines.stream().anyMatch(line -> line.contains("Fake WcfCli converting")));
  }

  @Test
  void throwsWhenNoConversionJarIsPresent(@TempDir File wcfCliDir, @TempDir File sourceModelsDir,
      @TempDir File outputDir) throws Exception {
    writeFakeWcfCliJar(wcfCliDir);
    // No "conversion-*.jar" written - only the fake WcfCli jar itself.

    PreviewAppException exception = assertThrows(PreviewAppException.class, () -> ModelConversionService.convert(
        javaExecutable(), wcfCliDir, sourceModelsDir, outputDir, FAKE_MAIN_CLASS, line -> { }));
    assertTrue(exception.getMessage().contains("conversion jar"));
  }

  @Test
  void throwsOnNonZeroExitCode(@TempDir File wcfCliDir, @TempDir File sourceModelsDir, @TempDir File outputDir)
      throws Exception {
    writeFakeWcfCliJar(wcfCliDir);
    writeDummyConversionJar(wcfCliDir);
    assertTrue(new File(sourceModelsDir, "FAIL").createNewFile());

    PreviewAppException exception = assertThrows(PreviewAppException.class, () -> ModelConversionService.convert(
        javaExecutable(), wcfCliDir, sourceModelsDir, outputDir, FAKE_MAIN_CLASS, line -> { }));
    assertTrue(exception.getMessage().contains("exit code"));
  }

  private static File javaExecutable() {
    return new File(System.getProperty("java.home"), "bin/java" + (OSUtil.isWindows() ? ".exe" : ""));
  }

  private static void writeFakeWcfCliJar(File wcfCliDir) throws IOException {
    String resourcePath = FAKE_MAIN_CLASS.replace('.', '/') + ".class";
    byte[] classBytes;
    try (InputStream in = FakeWcfCliMain.class.getClassLoader().getResourceAsStream(resourcePath)) {
      classBytes = in.readAllBytes();
    }
    try (JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(new File(wcfCliDir, "fake-wcf-cli.jar")))) {
      jarOut.putNextEntry(new JarEntry(resourcePath));
      jarOut.write(classBytes);
      jarOut.closeEntry();
    }
  }

  private static File writeDummyConversionJar(File wcfCliDir) throws IOException {
    File jarFile = new File(wcfCliDir, "conversion-0.0.0.jar");
    try (JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jarFile))) {
      // Empty marker jar: ModelConversionService only needs its path, never loads classes from it.
    }
    return jarFile;
  }
}
