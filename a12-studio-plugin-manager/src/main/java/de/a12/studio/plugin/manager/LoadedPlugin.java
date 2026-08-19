package de.a12.studio.plugin.manager;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.net.URLClassLoader;
import java.util.List;

/**
 * Represents a plugin JAR that has been successfully loaded at runtime.
 *
 * <p>Holds the {@link PluginDescriptor} parsed from {@code plugin.json},
 * the {@link URLClassLoader} that owns the JAR's classes, and the
 * instantiated extension-point objects.
 */
@Getter
public class LoadedPlugin {

  /** Metadata parsed from the plugin's {@code plugin.json}. */
  private final PluginDescriptor descriptor;

  /** Class loader for this plugin's JAR (kept open for reflection). */
  private final URLClassLoader classLoader;

  /** All {@link ICreateItemMenuEntry} instances contributed by this plugin. */
  private final List<ICreateItemMenuEntry> createMenuEntries;

  LoadedPlugin(@NonNull PluginDescriptor descriptor,
               @NonNull URLClassLoader classLoader,
               @NonNull List<ICreateItemMenuEntry> createMenuEntries) {
    this.descriptor = descriptor;
    this.classLoader = classLoader;
    this.createMenuEntries = createMenuEntries;
  }
}
