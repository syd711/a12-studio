package de.a12.studio.ui.previewapp;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Runs the vendor's {@code WcfCli} tool ({@code com.mgmtp.a12.dataservices.wcf.WcfCli}, staged by
 * {@code stageWcfCli}/resolved via {@link WcfCliInstallation}) to convert raw "WCF"-format source
 * Document Model JSON (as authored/saved by a12-studio - no {@code __meta} metadata group) into
 * "RMC" runtime models with {@code __meta} injected, mirroring the {@code convertModels} Gradle
 * task in the reference a12 project template.
 *
 * <p>Without this step, a real a12 kernel server (e.g. the Preview App server) fails to compute
 * documents against these models with {@code IllegalArgumentException: The set of fields does not
 * contain </__meta>.} - the kernel-generated validator unconditionally expects that group.
 */
@Slf4j
public final class ModelConversionService {

  private static final String WCF_CLI_MAIN_CLASS = "com.mgmtp.a12.dataservices.wcf.WcfCli";

  private static final String CONVERSION_JAR_PREFIX = "conversion-";

  private ModelConversionService() {
  }

  /**
   * Converts every model under {@code sourceModelsDir} into {@code outputDir}, returning the
   * directory that actually holds the converted model JSON files (a subdirectory of {@code
   * outputDir} - {@code <outputDir>/data/models}, per WcfCli's own output layout).
   */
  public static File convert(File javaExecutable, File wcfCliDir, File sourceModelsDir, File outputDir,
      Consumer<String> logSink) throws IOException, PreviewAppException {
    return convert(javaExecutable, wcfCliDir, sourceModelsDir, outputDir, WCF_CLI_MAIN_CLASS, logSink);
  }

  // Package-private: lets tests point at a fixture main class instead of the real WcfCli, so the
  // subprocess/classpath/error-handling logic here can be verified without the real vendor jars.
  static File convert(File javaExecutable, File wcfCliDir, File sourceModelsDir, File outputDir, String mainClass,
      Consumer<String> logSink) throws IOException, PreviewAppException {
    log.info("Starting model conversion (WcfCli): source=\"{}\", output=\"{}\", wcfCliDir=\"{}\"",
        sourceModelsDir.getAbsolutePath(), outputDir.getAbsolutePath(), wcfCliDir.getAbsolutePath());

    File conversionJar = findConversionJar(wcfCliDir);
    String classpath = buildClasspath(wcfCliDir);
    log.debug("Using conversion jar \"{}\" with classpath: {}", conversionJar.getAbsolutePath(), classpath);

    ProcessBuilder processBuilder = new ProcessBuilder(
        javaExecutable.getAbsolutePath(),
        "-cp", classpath,
        mainClass,
        sourceModelsDir.getAbsolutePath(),
        outputDir.getAbsolutePath(),
        "-c", conversionJar.getAbsolutePath());
    processBuilder.redirectErrorStream(true);

    Process process = processBuilder.start();
    List<String> output = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.add(line);
        log.debug("[WcfCli] {}", line);
        logSink.accept(line);
      }
    }

    int exitCode = waitFor(process);
    if (exitCode != 0) {
      log.warn("Model conversion (WcfCli) failed with exit code {}", exitCode);
      throw new PreviewAppException("Model conversion (WcfCli) failed with exit code " + exitCode + ":\n"
          + String.join("\n", tail(output, 20)));
    }

    File convertedModelsDir = new File(outputDir, "data/models");
    if (!convertedModelsDir.isDirectory()) {
      throw new PreviewAppException(
          "Model conversion (WcfCli) reported success but produced no \"" + convertedModelsDir.getAbsolutePath()
              + "\" directory.");
    }
    log.info("Model conversion (WcfCli) succeeded, converted models available at \"{}\"",
        convertedModelsDir.getAbsolutePath());
    return convertedModelsDir;
  }

  private static File findConversionJar(File wcfCliDir) throws PreviewAppException {
    File[] matches = wcfCliDir.listFiles((dir, name) -> name.startsWith(CONVERSION_JAR_PREFIX) && name.endsWith(".jar"));
    if (matches == null || matches.length == 0) {
      throw new PreviewAppException(
          "Could not find the RMC conversion jar (expected a \"" + CONVERSION_JAR_PREFIX + "*.jar\") under \""
              + wcfCliDir.getAbsolutePath() + "\".");
    }
    return matches[0];
  }

  private static String buildClasspath(File wcfCliDir) throws PreviewAppException {
    File[] jars = wcfCliDir.listFiles((dir, name) -> name.endsWith(".jar"));
    if (jars == null || jars.length == 0) {
      throw new PreviewAppException("No jars found under \"" + wcfCliDir.getAbsolutePath() + "\".");
    }
    StringBuilder classpath = new StringBuilder();
    for (File jar : jars) {
      if (classpath.length() > 0) {
        classpath.append(File.pathSeparatorChar);
      }
      classpath.append(jar.getAbsolutePath());
    }
    return classpath.toString();
  }

  private static int waitFor(Process process) throws IOException {
    try {
      return process.waitFor();
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while waiting for model conversion to finish.", e);
    }
  }

  private static List<String> tail(List<String> lines, int maxLines) {
    return lines.size() <= maxLines ? lines : lines.subList(lines.size() - maxLines, lines.size());
  }
}
