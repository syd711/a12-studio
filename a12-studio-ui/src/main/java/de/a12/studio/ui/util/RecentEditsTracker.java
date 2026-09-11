package de.a12.studio.ui.util;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.UISettings;
import de.a12.studio.ui.events.ModelDeletedEvent;
import de.a12.studio.ui.events.ModelRenamedEvent;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.events.ProjectClosedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * List of the most recently edited files, most recent first, capped at {@link #MAX_ENTRIES}.
 * Backs the "Recent Files" palette (Ctrl+E). Persisted to the user's {@link UISettings}
 * ("ui-settings.json") whenever a model is saved, so the list survives across sessions. Reloaded
 * on {@link #projectOpened}, dropping any persisted paths that no longer exist on disk.
 */
public class RecentEditsTracker implements StudioEventListener {

  public static final int MAX_ENTRIES = UISettings.MAX_RECENT_FILES;

  private static final RecentEditsTracker INSTANCE = new RecentEditsTracker();

  private final List<ProjectItem> recent = new ArrayList<>();

  private Project project;

  private RecentEditsTracker() {
  }

  public static RecentEditsTracker getInstance() {
    return INSTANCE;
  }

  public List<ProjectItem> getRecentlyEdited() {
    return new ArrayList<>(recent);
  }

  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    ProjectItem item = event.getItem();
    recent.remove(item);
    recent.add(0, item);
    while (recent.size() > MAX_ENTRIES) {
      recent.remove(recent.size() - 1);
    }

    if (project != null) {
      UISettings uiSettings = project.getSettings().getUISettings();
      uiSettings.addRecentFile(item.getPath());
      uiSettings.save();
    }
  }

  @Override
  public void modelDeleted(@NonNull ModelDeletedEvent event) {
    recent.remove(event.getItem());

    if (project != null) {
      UISettings uiSettings = project.getSettings().getUISettings();
      uiSettings.removeRecentFile(event.getItem().getPath());
      uiSettings.save();
    }
  }

  @Override
  public void modelRenamed(@NonNull ModelRenamedEvent event) {
    recent.removeIf(item -> item.getPath().equals(event.getOldPath()));

    if (project != null) {
      UISettings uiSettings = project.getSettings().getUISettings();
      uiSettings.removeRecentFile(event.getOldPath());
      uiSettings.save();
    }
  }

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    project = event.getProject();
    recent.clear();

    UISettings uiSettings = project.getSettings().getUISettings();
    List<String> persisted = uiSettings.getRecentFiles();
    List<String> existing = new ArrayList<>();
    for (String path : persisted) {
      if (!new File(path).exists()) {
        continue;
      }
      ProjectItem item = project.getRoot().findByPath(path);
      if (item != null) {
        recent.add(item);
        existing.add(path);
      }
    }

    if (existing.size() != persisted.size()) {
      uiSettings.setRecentFiles(existing);
      uiSettings.save();
    }
  }

  @Override
  public void projectClosed(@NonNull ProjectClosedEvent event) {
    recent.clear();
    project = null;
  }
}
