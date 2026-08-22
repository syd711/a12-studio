package de.a12.studio.ui.util.localsettings;

import de.a12.studio.ui.util.AppPaths;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
abstract public class LocalJsonSettings {

  public final static ObjectMapper objectMapper;

  /**
   * Settings files are small and written from UI event handlers (e.g. a SplitPane divider-position listener
   * firing during an animated collapse/expand), so {@link #save()} keeps the actual disk write off the caller's
   * thread - otherwise a blocking write lands on the JavaFX Application Thread mid-animation and shows up as a
   * frame hitch. A single-threaded executor keeps writes to the same file in submission order.
   */
  private final static ExecutorService saveExecutor =
      Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "local-json-settings-writer");
        thread.setDaemon(true);
        return thread;
      });

  File settingsFile;

  static {
    objectMapper = JsonMapper.builder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
        .build();
  }


  public static <T extends LocalJsonSettings> T load(String configName, Class configClazz) {
    FileInputStream in = null;
    try {
      File dbFile = getConfigFile(configName);
      T t = null;
      if (dbFile.exists()) {
        in = new FileInputStream(dbFile);
        t = (T) objectMapper.readValue(in, configClazz);
      }
      else {
        t = (T) configClazz.getDeclaredConstructor().newInstance();
      }

      t.settingsFile = dbFile;
      return t;
    }
    catch (Exception e) {
      log.error("Failed to json: " + e.getMessage());
    }
    finally {
      try {
        if (in != null) {
          in.close();
        }
      }
      catch (IOException e) {
        //ignore
      }
    }
    return null;
  }

  private static File getConfigFile(String configName) {
    String settingsFileName = configName + ".json";
    File basePath = AppPaths.getWriteableBaseFolder();
    File parent = new File(basePath, "config/");
    if (!parent.exists()) {
      parent.mkdirs();
    }

    return new File(parent, settingsFileName);
  }

  public void save() {
    try {
      byte[] json = objectMapper.writeValueAsBytes(this);
      saveExecutor.submit(() -> {
        try {
          Files.write(settingsFile.toPath(), json);
        }
        catch (IOException e) {
          log.error("Failed to write {}: {}", settingsFile.getName(), e.getMessage(), e);
        }
      });
    }
    catch (Exception e) {
      log.error("Failed to write {}: {}", settingsFile.getName(), e.getMessage(), e);
    }
  }
}
