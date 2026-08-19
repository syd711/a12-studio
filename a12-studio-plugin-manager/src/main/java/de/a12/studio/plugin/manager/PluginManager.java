package de.a12.studio.plugin.manager;

import tools.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scans the {@code plugins/} directory next to the application root for JAR files,
 * reads their bundled {@code plugin.json} descriptors, instantiates all registered
 * extension-point classes, and makes them available to the rest of the application.
 *
 * <h2>Plugin directory</h2>
 * <p>The directory is resolved relative to the application's working directory.
 * It is created on demand if it does not exist. If scanning fails for an individual
 * JAR, that plugin is skipped with a warning and loading continues for the rest.
 *
 * <h2>Plugin JAR requirements</h2>
 * <ul>
 *   <li>Must contain a {@code plugin.json} at the root of the JAR.</li>
 *   <li>Each extension point class must have a public no-arg constructor.</li>
 *   <li>Each extension point class must implement the interface that corresponds
 *       to its {@code "name"} field (currently {@link ICreateItemMenuEntry} for
 *       {@code "createMenu"}).</li>
 * </ul>
 */
@Slf4j
public class PluginManager {

  /** Resource path of the descriptor inside each plugin JAR. */
  private static final String PLUGIN_JSON = "plugin.json";

  /** Extension point name for "New > Document" menu contributions. */
  public static final String EP_CREATE_MENU = "createMenu";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /** The {@code plugins/} directory (may not exist yet). */
  private final File pluginsDir;

  /** All successfully loaded plugins, in the order they were discovered. */
  @Getter
  private final List<LoadedPlugin> loadedPlugins = new ArrayList<>();

  // ---------------------------------------------------------------------------
  // Singleton
  // ---------------------------------------------------------------------------

  private static PluginManager instance;

  /** Returns the application-wide singleton; call {@link #initialize(File)} first. */
  @NonNull
  public static PluginManager getInstance() {
    if (instance == null) {
      throw new IllegalStateException("PluginManager has not been initialized. Call initialize() first.");
    }
    return instance;
  }

  /**
   * Initialises the singleton with the given application root directory and immediately
   * scans the {@code plugins/} sub-directory.
   *
   * @param appRoot the directory that contains (or will contain) the {@code plugins/} folder
   */
  public static void initialize(@NonNull File appRoot) {
    instance = new PluginManager(new File(appRoot, "plugins"));
    instance.scanPlugins();
  }

  // ---------------------------------------------------------------------------
  // Construction & scanning
  // ---------------------------------------------------------------------------

  PluginManager(@NonNull File pluginsDir) {
    this.pluginsDir = pluginsDir;
  }

  /**
   * Scans {@link #pluginsDir} for JAR files, parses each {@code plugin.json},
   * and instantiates extension points.
   */
  void scanPlugins() {
    if (!pluginsDir.exists()) {
      log.info("Plugin directory does not exist yet, creating: {}", pluginsDir.getAbsolutePath());
      if (!pluginsDir.mkdirs()) {
        log.warn("Could not create plugin directory: {}", pluginsDir.getAbsolutePath());
        return;
      }
    }

    File[] jars = pluginsDir.listFiles(f -> f.isFile() && f.getName().endsWith(".jar"));
    if (jars == null || jars.length == 0) {
      log.info("No plugin JARs found in: {}", pluginsDir.getAbsolutePath());
      return;
    }

    log.info("Scanning {} plugin JAR(s) in: {}", jars.length, pluginsDir.getAbsolutePath());
    for (File jar : jars) {
      try {
        LoadedPlugin plugin = loadPlugin(jar);
        if (plugin != null) {
          loadedPlugins.add(plugin);
          log.info("Loaded plugin '{}' v{} from {}",
              plugin.getDescriptor().getName(),
              plugin.getDescriptor().getPluginVersion(),
              jar.getName());
        }
      }
      catch (Exception e) {
        log.warn("Failed to load plugin from '{}': {}", jar.getName(), e.getMessage(), e);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Convenience accessors
  // ---------------------------------------------------------------------------

  /**
   * Returns all {@link ICreateItemMenuEntry} instances contributed by all loaded plugins,
   * in plugin load order.
   */
  @NonNull
  public List<ICreateItemMenuEntry> getCreateMenuEntries() {
    List<ICreateItemMenuEntry> result = new ArrayList<>();
    for (LoadedPlugin plugin : loadedPlugins) {
      result.addAll(plugin.getCreateMenuEntries());
    }
    return Collections.unmodifiableList(result);
  }

  // ---------------------------------------------------------------------------
  // Internal loading logic
  // ---------------------------------------------------------------------------

  private LoadedPlugin loadPlugin(@NonNull File jarFile) throws Exception {
    // 1. Read plugin.json from the JAR without opening a class loader yet.
    PluginDescriptor descriptor = readDescriptor(jarFile);
    if (descriptor == null) {
      log.warn("No plugin.json found in '{}', skipping.", jarFile.getName());
      return null;
    }

    if (descriptor.getExtensionPoints().isEmpty()) {
      log.info("Plugin '{}' defines no extension points, skipping.", jarFile.getName());
      return null;
    }

    // 2. Open a URLClassLoader with the plugin JAR on top of the system class loader.
    //    The system class loader already contains a12-studio-commons (and therefore the
    //    extension-point interfaces), so instanceof checks work correctly.
    URL jarUrl = jarFile.toURI().toURL();
    URLClassLoader pluginClassLoader = new URLClassLoader(
        new URL[]{jarUrl},
        Thread.currentThread().getContextClassLoader());

    // 3. Instantiate each registered extension point.
    List<ICreateItemMenuEntry> createMenuEntries = new ArrayList<>();
    for (PluginDescriptor.ExtensionPoint ep : descriptor.getExtensionPoints()) {
      Object instance = instantiate(ep, pluginClassLoader, jarFile.getName());
      if (instance == null) {
        continue;
      }
      if (EP_CREATE_MENU.equals(ep.getName())) {
        if (instance instanceof ICreateItemMenuEntry entry) {
          createMenuEntries.add(entry);
        }
        else {
          log.warn("Class '{}' in '{}' does not implement ICreateItemMenuEntry, skipping.",
              ep.getClassName(), jarFile.getName());
        }
      }
      else {
        log.warn("Unknown extension point '{}' in '{}', skipping.", ep.getName(), jarFile.getName());
      }
    }

    return new LoadedPlugin(descriptor, pluginClassLoader, createMenuEntries);
  }

  /** Reads and parses the {@code plugin.json} from inside the given JAR, or returns {@code null}. */
  private PluginDescriptor readDescriptor(@NonNull File jarFile) throws IOException {
    try (JarFile jar = new JarFile(jarFile)) {
      JarEntry entry = jar.getJarEntry(PLUGIN_JSON);
      if (entry == null) {
        return null;
      }
      try (InputStream in = jar.getInputStream(entry)) {
        return OBJECT_MAPPER.readValue(in, PluginDescriptor.class);
      }
    }
  }

  /** Loads and instantiates the extension-point class, returning {@code null} on any error. */
  private Object instantiate(@NonNull PluginDescriptor.ExtensionPoint ep,
                             @NonNull URLClassLoader classLoader,
                             @NonNull String jarName) {
    try {
      Class<?> clazz = classLoader.loadClass(ep.getClassName());
      return clazz.getDeclaredConstructor().newInstance();
    }
    catch (ClassNotFoundException e) {
      log.warn("Extension point class '{}' not found in '{}': {}",
          ep.getClassName(), jarName, e.getMessage());
    }
    catch (Exception e) {
      log.warn("Failed to instantiate '{}' from '{}': {}",
          ep.getClassName(), jarName, e.getMessage(), e);
    }
    return null;
  }
}
