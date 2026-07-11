package de.a12.studio.commons.util.localsettings;

import de.a12.studio.commons.Updater;
import de.a12.studio.commons.util.PropertiesStore;
import javafx.scene.shape.Rectangle;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class LocalUISettings {

  public static final String LAST_FOLDER_SELECTION = "lastFolderSelection";

  public static final String RECENT_PROJECTS = "recentProjects";

  private static PropertiesStore store;

  private static List<LocalSettingsChangeListener> listeners;

  private static final Map<String, Object> jsonSettingsCache = new HashMap<>();
  private static File propertiesFile;

  static {
    initialize();
  }

  private static void initialize() {
    File basePath = Updater.getWriteableBaseFolder();
    propertiesFile = new File(basePath, "config/settings.properties");
    propertiesFile.getParentFile().mkdirs();
    store = PropertiesStore.create(propertiesFile);

    listeners = new ArrayList<>();
  }

  public static <T> T getTablePreference(Class<?> clazz) {
    try {
      String clazzName = clazz.getSimpleName();
      if (!jsonSettingsCache.containsKey(clazzName)) {
        BaseTableSettings baseTableSettings = LocalJsonSettings.load(clazz.getSimpleName(), BaseTableSettings.class);
        jsonSettingsCache.put(clazzName, baseTableSettings);
      }
      return (T) jsonSettingsCache.get(clazzName);
    }
    catch (Exception e) {
      log.error("Failed to read preferences: {}", e.getMessage(), e);
    }
    return null;
  }

  public static void addListener(LocalSettingsChangeListener listener) {
    listeners.add(listener);
  }

  public static void saveProperty(@NonNull String key, @Nullable String value) {
    store.set(key, value);
    for (LocalSettingsChangeListener listener : listeners) {
      listener.localSettingsChanged(key, value);
    }
  }

  public static void saveJsonProperty(@NonNull String key, @Nullable Object value) {
    try {
      String json = LocalJsonSettings.objectMapper.writeValueAsString(value);
      saveProperty(key, json);
    }
    catch (Exception e) {
      log.warn("Failed to write json preference for {}: {}", key, e.getMessage(), e);
    }
  }

  public static <T> T getJsonProperty(@NonNull String key, @NonNull Class<T> clazz, @Nullable T defaultValue) {
    try {
      String value = getString(key);
      if (!StringUtils.isEmpty(value)) {
        return (T) LocalJsonSettings.objectMapper.readValue(value, clazz);
      }
    }
    catch (Exception e) {
      log.warn("Failed to read json preference for {}: {}", key, e.getMessage(), e);
    }
    return defaultValue;
  }

  @Nullable
  public static String getProperties(@NonNull String key) {
    if (store.containsKey(key)) {
      return store.get(key);
    }
    return null;
  }

  public static boolean getBoolean(@NonNull String key) {
    if (store.containsKey(key)) {
      String s = store.get(key);
      return Boolean.valueOf(s);
    }
    return false;
  }

  public static String getString(@NonNull String key) {
    if (store.containsKey(key)) {
      return store.get(key);
    }
    return null;
  }

  public static void saveLastFolderLocation(@Nullable File file) {
    if (file != null) {
      if (file.isFile()) {
        file = file.getParentFile();
      }
      store.set(LAST_FOLDER_SELECTION, file.getAbsolutePath());
    }
  }

  @Nullable
  public static File getLastFolderSelection() {
    if (store.containsKey(LAST_FOLDER_SELECTION)) {
      return new File(store.get(LAST_FOLDER_SELECTION));
    }
    return null;
  }

  public static void saveProject(@NonNull File file) {
    String path = file.getAbsolutePath();
    List<String> recentProjects = getRecentProjects();
    recentProjects.remove(path);
    recentProjects.add(0, path);
    saveJsonProperty(RECENT_PROJECTS, recentProjects);
    saveLastFolderLocation(file);
  }

  @NonNull
  public static List<String> getRecentProjects() {
    List<String> recentProjects = getJsonProperty(RECENT_PROJECTS, List.class, null);
    if (recentProjects == null) {
      return new ArrayList<>();
    }
    return recentProjects;
  }

  public static void saveLocation(int x, int y, int width, int height) {
    if (y >= 0) {
      store.set("x", x);
      store.set("y", y);
      store.set("width", width);
      store.set("height", height);
      log.info("Saved window position to store.");
    }
  }

  public static void saveLocation(String id, int x, int y, int width, int height) {
    if (y >= 0 && id != null) {
      store.set(id + ".x", x);
      store.set(id + ".y", y);
      store.set(id + ".width", width);
      store.set(id + ".height", height);
      log.info("Saved window position to store for " + id);
    }
  }

  public static Rectangle getPosition() {
    Rectangle rectangle = new Rectangle();
    rectangle.setX(store.getInt("x", -1));
    rectangle.setY(store.getInt("y", -1));
    rectangle.setWidth(store.getInt("width", -1));
    rectangle.setHeight(store.getInt("height", -1));
    return rectangle;
  }

  public static Rectangle getPosition(String id) {
    if (store.containsKey(id + ".x")) {
      Rectangle rectangle = new Rectangle();
      rectangle.setX(store.getInt(id + ".x", -1));
      rectangle.setY(store.getInt(id + ".y", -1));
      rectangle.setWidth(store.getInt(id + ".width", -1));
      rectangle.setHeight(store.getInt(id + ".height", -1));
      return rectangle;
    }

    return null;
  }

  public static void setModal(String stateId, boolean modal) {
    store.set(stateId + "_modality", String.valueOf(modal));
  }

  public static boolean isModal(String stateId) {
    String key = stateId + "_modality";
    if (store.containsKey(key)) {
      return store.getBoolean(key);
    }
    return true;
  }

  public static boolean isMaximizeable(String stateId) {
    if (stateId == null) {
      return true;
    }

    return !stateId.equalsIgnoreCase("dialog-table-data");
  }

  public static void reset() {
    store.getProperties().clear();
    if (!propertiesFile.delete()) {
      log.error("Reset failed.");
    }
    else {
      log.info("Deleted {}", propertiesFile.getAbsolutePath());
    }
  }
}
