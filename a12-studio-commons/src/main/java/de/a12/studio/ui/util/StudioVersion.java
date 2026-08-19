package de.a12.studio.ui.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Resolves the studio version from a build-generated {@code version.properties} resource.
 * Unlike the jar manifest's Implementation-Version attribute, this resource is populated by
 * the Gradle build script itself and is therefore also available when running from the IDE,
 * where classes are loaded from an exploded output directory rather than a packaged jar.
 */
@Slf4j
public final class StudioVersion {

  private static final String VERSION = loadVersion();

  private StudioVersion() {
  }

  public static String get() {
    return VERSION;
  }

  private static String loadVersion() {
    try (InputStream in = StudioVersion.class.getResourceAsStream("/version.properties")) {
      if (in != null) {
        Properties properties = new Properties();
        properties.load(in);
        String version = properties.getProperty("version");
        if (version != null && !version.isBlank()) {
          return version;
        }
      }
    }
    catch (IOException e) {
      log.warn("Failed to read version.properties: {}", e.getMessage());
    }
    return "dev";
  }
}
