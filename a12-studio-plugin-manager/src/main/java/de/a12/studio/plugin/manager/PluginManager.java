package de.a12.studio.plugin.manager;

import tools.jackson.databind.ObjectMapper;
import de.a12.studio.ui.util.StudioVersion;
import de.a12.studio.plugin.manager.PluginDescriptor.ExtensionPoint;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
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
 *       to its {@code "name"} field: {@link ICreateItemMenuEntry} for {@code "createMenu"},
 *       {@link IProjectSettingsPanelContribution} for {@code "projectSettingsPanel"},
 *       {@link IModelSaveInterceptor} for {@code "modelSave"},
 *       {@link IModelValidatorContribution} for {@code "modelValidator"},
 *       {@link INewModelNameInterceptor} for {@code "newModelName"},
 *       {@link IProjectOpenedListener} for {@code "projectOpened"}, and
 *       {@link IProjectToolbarButtonContribution} for {@code "projectToolbarButton"}, and
 *       {@link IFileDropHandler} for {@code "fileDrop"}.</li>
 * </ul>
 */
@Slf4j
public class PluginManager {

  /** Resource path of the descriptor inside each plugin JAR. */
  private static final String PLUGIN_JSON = "plugin.json";

  /** Extension point name for "New > Document" menu contributions. */
  public static final String EP_CREATE_MENU = "createMenu";

  /** Extension point name for project-settings panel contributions. */
  public static final String EP_PROJECT_SETTINGS_PANEL = "projectSettingsPanel";

  /** Extension point name for model-save interceptor contributions. */
  public static final String EP_MODEL_SAVE = "modelSave";

  /** Extension point name for model-validator contributions. */
  public static final String EP_MODEL_VALIDATOR = "modelValidator";

  /** Extension point name for new-model-name interceptor contributions. */
  public static final String EP_NEW_MODEL_NAME = "newModelName";

  /** Extension point name for project-opened listener contributions. */
  public static final String EP_PROJECT_OPENED = "projectOpened";

  /** Extension point name for project-tree-toolbar-button contributions. */
  public static final String EP_PROJECT_TOOLBAR_BUTTON = "projectToolbarButton";

  /** Extension point name for file-drop handler contributions. */
  public static final String EP_FILE_DROP = "fileDrop";

  /** Suffix appended to a downloaded update JAR while its predecessor is still locked by the running JVM. */
  private static final String PENDING_UPDATE_SUFFIX = ".pending";

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

    applyPendingUpdates();

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

  /**
   * Replaces every previously installed plugin JAR with its downloaded update.
   *
   * <p>An update cannot overwrite its JAR while the old version is still loaded (its
   * {@link URLClassLoader} keeps the file open/locked), so {@link #getPendingUpdateFile(String)}
   * downloads it next to the old JAR with a {@value #PENDING_UPDATE_SUFFIX} suffix instead. On the
   * next startup, before any plugin is loaded, this method deletes the stale JAR (now unlocked,
   * since the previous JVM has exited) and renames the pending file into its place.
   */
  private void applyPendingUpdates() {
    File[] pendingFiles = pluginsDir.listFiles(f -> f.isFile() && f.getName().endsWith(PENDING_UPDATE_SUFFIX));
    if (pendingFiles == null) return;

    for (File pendingFile : pendingFiles) {
      String targetName = pendingFile.getName().substring(0,
          pendingFile.getName().length() - PENDING_UPDATE_SUFFIX.length());
      File target = new File(pluginsDir, targetName);
      try {
        Files.deleteIfExists(target.toPath());
        Files.move(pendingFile.toPath(), target.toPath());
        log.info("Applied pending update for plugin JAR: {}", targetName);
      }
      catch (IOException e) {
        log.warn("Failed to apply pending update for '{}': {}", targetName, e.getMessage(), e);
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

  /**
   * Returns all {@link IProjectSettingsPanelContribution} instances contributed by all loaded
   * plugins, in plugin load order.
   */
  @NonNull
  public List<IProjectSettingsPanelContribution> getProjectSettingsPanelContributions() {
    List<IProjectSettingsPanelContribution> result = new ArrayList<>();
    for (LoadedPlugin plugin : loadedPlugins) {
      result.addAll(plugin.getProjectSettingsPanelContributions());
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * Returns all {@link IModelSaveInterceptor} instances contributed by all loaded plugins, in
   * plugin load order.
   */
  @NonNull
  public List<IModelSaveInterceptor> getModelSaveInterceptors() {
    List<IModelSaveInterceptor> result = new ArrayList<>();
    for (LoadedPlugin plugin : loadedPlugins) {
      result.addAll(plugin.getModelSaveInterceptors());
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * Returns all {@link IModelValidatorContribution} instances contributed by all loaded plugins,
   * in plugin load order.
   */
  @NonNull
  public List<IModelValidatorContribution> getModelValidatorContributions() {
    List<IModelValidatorContribution> result = new ArrayList<>();
    for (LoadedPlugin plugin : loadedPlugins) {
      result.addAll(plugin.getModelValidatorContributions());
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * Returns all {@link INewModelNameInterceptor} instances contributed by all loaded plugins, in
   * plugin load order.
   */
  @NonNull
  public List<INewModelNameInterceptor> getNewModelNameInterceptors() {
    List<INewModelNameInterceptor> result = new ArrayList<>();
    for (LoadedPlugin plugin : loadedPlugins) {
      result.addAll(plugin.getNewModelNameInterceptors());
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * Returns all {@link IProjectOpenedListener} instances contributed by all loaded plugins, in
   * plugin load order.
   */
  @NonNull
  public List<IProjectOpenedListener> getProjectOpenedListeners() {
    List<IProjectOpenedListener> result = new ArrayList<>();
    for (LoadedPlugin plugin : loadedPlugins) {
      result.addAll(plugin.getProjectOpenedListeners());
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * Returns all {@link IProjectToolbarButtonContribution} instances contributed by all loaded
   * plugins, in plugin load order.
   */
  @NonNull
  public List<IProjectToolbarButtonContribution> getProjectToolbarButtonContributions() {
    List<IProjectToolbarButtonContribution> result = new ArrayList<>();
    for (LoadedPlugin plugin : loadedPlugins) {
      result.addAll(plugin.getProjectToolbarButtonContributions());
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * Returns all {@link IFileDropHandler} instances contributed by all loaded plugins,
   * in plugin load order.
   */
  @NonNull
  public List<IFileDropHandler> getFileDropHandlers() {
    List<IFileDropHandler> result = new ArrayList<>();
    for (LoadedPlugin plugin : loadedPlugins) {
      result.addAll(plugin.getFileDropHandlers());
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
    List<IProjectSettingsPanelContribution> projectSettingsPanelContributions = new ArrayList<>();
    List<IModelSaveInterceptor> modelSaveInterceptors = new ArrayList<>();
    List<IModelValidatorContribution> modelValidatorContributions = new ArrayList<>();
    List<INewModelNameInterceptor> newModelNameInterceptors = new ArrayList<>();
    List<IProjectOpenedListener> projectOpenedListeners = new ArrayList<>();
    List<IProjectToolbarButtonContribution> projectToolbarButtonContributions = new ArrayList<>();
    List<IFileDropHandler> fileDropHandlers = new ArrayList<>();
    for (ExtensionPoint ep : descriptor.getExtensionPoints()) {
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
      else if (EP_PROJECT_SETTINGS_PANEL.equals(ep.getName())) {
        if (instance instanceof IProjectSettingsPanelContribution entry) {
          projectSettingsPanelContributions.add(entry);
        }
        else {
          log.warn("Class '{}' in '{}' does not implement IProjectSettingsPanelContribution, skipping.",
              ep.getClassName(), jarFile.getName());
        }
      }
      else if (EP_MODEL_SAVE.equals(ep.getName())) {
        if (instance instanceof IModelSaveInterceptor entry) {
          modelSaveInterceptors.add(entry);
        }
        else {
          log.warn("Class '{}' in '{}' does not implement IModelSaveInterceptor, skipping.",
              ep.getClassName(), jarFile.getName());
        }
      }
      else if (EP_MODEL_VALIDATOR.equals(ep.getName())) {
        if (instance instanceof IModelValidatorContribution entry) {
          modelValidatorContributions.add(entry);
        }
        else {
          log.warn("Class '{}' in '{}' does not implement IModelValidatorContribution, skipping.",
              ep.getClassName(), jarFile.getName());
        }
      }
      else if (EP_NEW_MODEL_NAME.equals(ep.getName())) {
        if (instance instanceof INewModelNameInterceptor entry) {
          newModelNameInterceptors.add(entry);
        }
        else {
          log.warn("Class '{}' in '{}' does not implement INewModelNameInterceptor, skipping.",
              ep.getClassName(), jarFile.getName());
        }
      }
      else if (EP_PROJECT_OPENED.equals(ep.getName())) {
        if (instance instanceof IProjectOpenedListener entry) {
          projectOpenedListeners.add(entry);
        }
        else {
          log.warn("Class '{}' in '{}' does not implement IProjectOpenedListener, skipping.",
              ep.getClassName(), jarFile.getName());
        }
      }
      else if (EP_PROJECT_TOOLBAR_BUTTON.equals(ep.getName())) {
        if (instance instanceof IProjectToolbarButtonContribution entry) {
          projectToolbarButtonContributions.add(entry);
        }
        else {
          log.warn("Class '{}' in '{}' does not implement IProjectToolbarButtonContribution, skipping.",
              ep.getClassName(), jarFile.getName());
        }
      }
      else if (EP_FILE_DROP.equals(ep.getName())) {
        if (instance instanceof IFileDropHandler entry) {
          fileDropHandlers.add(entry);
        }
        else {
          log.warn("Class '{}' in '{}' does not implement IFileDropHandler, skipping.",
              ep.getClassName(), jarFile.getName());
        }
      }
      else {
        log.warn("Unknown extension point '{}' in '{}', skipping.", ep.getName(), jarFile.getName());
      }
    }

    return new LoadedPlugin(descriptor, pluginClassLoader, createMenuEntries, projectSettingsPanelContributions,
        modelSaveInterceptors, modelValidatorContributions, newModelNameInterceptors, projectOpenedListeners,
        projectToolbarButtonContributions, fileDropHandlers);
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
  private Object instantiate(@NonNull ExtensionPoint ep,
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


  // ---------------------------------------------------------------------------
  // Marketplace
  // ---------------------------------------------------------------------------

  private static final String MARKETPLACE_RESOURCE = "/marketplace.json";

  /**
   * Reads the bundled {@code marketplace.json} and returns all entries whose
   * {@code a12Version} prefix matches the running studio version.
   *
   * <p>Matching rule: the plugin's {@code a12Version} field (e.g. {@code "2606"}) must be
   * a prefix of {@link de.a12.studio.ui.util.StudioVersion#get()} (e.g. {@code "2606.06-ext0-0.0.1"}).
   * This allows minor / patch releases to stay compatible with plugins built for the
   * same major version.
   *
   * @return list of matching marketplace entries, or an empty list if the resource is missing
   */
  @NonNull
  public List<MarketplaceEntry> getMarketplaceEntries() {
    try (InputStream in = getClass().getResourceAsStream(MARKETPLACE_RESOURCE)) {
      if (in == null) {
        log.warn("marketplace.json not found on classpath");
        return List.of();
      }
      Marketplace marketplace = OBJECT_MAPPER.readValue(in, Marketplace.class);
      String studioVersion = StudioVersion.get();
      return marketplace.getPlugins().stream()
          .filter(e -> isCompatible(e.getA12Version(), studioVersion))
          .toList();
    }
    catch (Exception e) {
      log.warn("Failed to read marketplace.json: {}", e.getMessage(), e);
      return List.of();
    }
  }

  /**
   * Returns whether a plugin with the given {@code a12Version} requirement is compatible
   * with the running {@code studioVersion}.
   *
   * <p>The plugin's version is treated as a prefix: {@code "2606"} matches
   * {@code "2606.06-ext0-0.0.1"} and {@code "2606.99"} but not {@code "2607.0"}.
   */
  static boolean isCompatible(@NonNull String a12Version, @NonNull String studioVersion) {
    if (a12Version.isBlank() || studioVersion.isBlank() || "dev".equals(studioVersion)) {
      return true; // permissive in dev builds
    }
    return studioVersion.startsWith(a12Version);
  }

  /**
   * Returns {@code true} if a plugin with the given name is currently installed
   * (i.e. its JAR was successfully loaded from the {@code plugins/} directory).
   */
  public boolean isInstalled(@NonNull String pluginName) {
    return loadedPlugins.stream()
        .anyMatch(p -> pluginName.equals(p.getDescriptor().getName()));
  }

  /**
   * Returns the {@link LoadedPlugin} for the given name, or {@code null} if not installed.
   */
  @Nullable
  public LoadedPlugin getLoadedPlugin(@NonNull String pluginName) {
    return loadedPlugins.stream()
        .filter(p -> pluginName.equals(p.getDescriptor().getName()))
        .findFirst()
        .orElse(null);
  }

  /** Returns the {@code plugins/} directory (created on demand by {@link #scanPlugins()}). */
  @NonNull
  public File getPluginsDir() {
    return pluginsDir;
  }

  // ---------------------------------------------------------------------------
  // Updates
  // ---------------------------------------------------------------------------

  /**
   * Returns the JAR file the given plugin was actually loaded from, or {@code null} if it is
   * not currently installed/loaded.
   */
  @Nullable
  public File getInstalledJarFile(@NonNull String pluginName) {
    LoadedPlugin plugin = getLoadedPlugin(pluginName);
    if (plugin == null) return null;
    URL[] urls = plugin.getClassLoader().getURLs();
    if (urls.length == 0) return null;
    try {
      return new File(urls[0].toURI());
    }
    catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * Returns the staging file an update download should be written to for the given target JAR
   * file name. See {@link #applyPendingUpdates()} for why the update cannot be written directly
   * to the target file name while the studio is running.
   */
  @NonNull
  public File getPendingUpdateFile(@NonNull String jarFileName) {
    return new File(pluginsDir, jarFileName + PENDING_UPDATE_SUFFIX);
  }

  /**
   * Returns whether a newer version of the given (currently installed) plugin is available in
   * the marketplace.
   */
  public boolean isUpdateAvailable(@NonNull String pluginName, @Nullable String marketplaceVersion) {
    if (marketplaceVersion == null || marketplaceVersion.isBlank()) return false;
    LoadedPlugin plugin = getLoadedPlugin(pluginName);
    if (plugin == null) return false;
    String installedVersion = plugin.getDescriptor().getPluginVersion();
    if (installedVersion == null || installedVersion.isBlank()) return false;
    return compareVersions(marketplaceVersion, installedVersion) > 0;
  }

  /**
   * Compares two dot-separated numeric version strings (e.g. {@code "1.2.0"}), segment by
   * segment. Missing trailing segments are treated as {@code 0}; non-numeric segments are
   * treated as {@code 0} as well, so unparsable versions never look newer than a valid one.
   *
   * @return a negative number if {@code v1 < v2}, zero if equal, a positive number if {@code v1 > v2}
   */
  static int compareVersions(@NonNull String v1, @NonNull String v2) {
    String[] p1 = v1.split("\\.");
    String[] p2 = v2.split("\\.");
    int length = Math.max(p1.length, p2.length);
    for (int i = 0; i < length; i++) {
      int n1 = i < p1.length ? parseVersionSegment(p1[i]) : 0;
      int n2 = i < p2.length ? parseVersionSegment(p2[i]) : 0;
      if (n1 != n2) return Integer.compare(n1, n2);
    }
    return 0;
  }

  private static int parseVersionSegment(@NonNull String segment) {
    try {
      return Integer.parseInt(segment.trim());
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }
}
