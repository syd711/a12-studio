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

  /** All {@link IProjectSettingsPanelContribution} instances contributed by this plugin. */
  private final List<IProjectSettingsPanelContribution> projectSettingsPanelContributions;

  /** All {@link IModelSaveInterceptor} instances contributed by this plugin. */
  private final List<IModelSaveInterceptor> modelSaveInterceptors;

  /** All {@link IModelValidatorContribution} instances contributed by this plugin. */
  private final List<IModelValidatorContribution> modelValidatorContributions;

  /** All {@link INewModelNameInterceptor} instances contributed by this plugin. */
  private final List<INewModelNameInterceptor> newModelNameInterceptors;

  /** All {@link IProjectOpenedListener} instances contributed by this plugin. */
  private final List<IProjectOpenedListener> projectOpenedListeners;

  /** All {@link IProjectToolbarButtonContribution} instances contributed by this plugin. */
  private final List<IProjectToolbarButtonContribution> projectToolbarButtonContributions;

  /** All {@link IFileDropHandler} instances contributed by this plugin. */
  private final List<IFileDropHandler> fileDropHandlers;

  LoadedPlugin(@NonNull PluginDescriptor descriptor,
               @NonNull URLClassLoader classLoader,
               @NonNull List<ICreateItemMenuEntry> createMenuEntries,
               @NonNull List<IProjectSettingsPanelContribution> projectSettingsPanelContributions,
               @NonNull List<IModelSaveInterceptor> modelSaveInterceptors,
               @NonNull List<IModelValidatorContribution> modelValidatorContributions,
               @NonNull List<INewModelNameInterceptor> newModelNameInterceptors,
               @NonNull List<IProjectOpenedListener> projectOpenedListeners,
               @NonNull List<IProjectToolbarButtonContribution> projectToolbarButtonContributions,
               @NonNull List<IFileDropHandler> fileDropHandlers) {
    this.descriptor = descriptor;
    this.classLoader = classLoader;
    this.createMenuEntries = createMenuEntries;
    this.projectSettingsPanelContributions = projectSettingsPanelContributions;
    this.modelSaveInterceptors = modelSaveInterceptors;
    this.modelValidatorContributions = modelValidatorContributions;
    this.newModelNameInterceptors = newModelNameInterceptors;
    this.projectOpenedListeners = projectOpenedListeners;
    this.projectToolbarButtonContributions = projectToolbarButtonContributions;
    this.fileDropHandlers = fileDropHandlers;
  }
}
