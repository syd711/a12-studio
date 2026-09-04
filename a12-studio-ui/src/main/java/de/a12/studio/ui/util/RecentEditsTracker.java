package de.a12.studio.ui.util;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.ModelDeletedEvent;
import de.a12.studio.ui.events.ModelRenamedEvent;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.events.ProjectClosedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * In-memory (never persisted to disk) list of the most recently edited files, most recent first,
 * capped at {@link #MAX_ENTRIES}. Backs the "Recent Files" palette (Ctrl+E). Updated whenever a
 * model is saved and reset whenever the project changes, so it always reflects the current
 * session's edit history rather than anything saved to settings.
 */
public class RecentEditsTracker implements StudioEventListener {

  public static final int MAX_ENTRIES = 15;

  private static final RecentEditsTracker INSTANCE = new RecentEditsTracker();

  private final LinkedList<ProjectItem> recent = new LinkedList<>();

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
    recent.addFirst(item);
    while (recent.size() > MAX_ENTRIES) {
      recent.removeLast();
    }
  }

  @Override
  public void modelDeleted(@NonNull ModelDeletedEvent event) {
    recent.remove(event.getItem());
  }

  @Override
  public void modelRenamed(@NonNull ModelRenamedEvent event) {
    recent.removeIf(item -> item.getPath().equals(event.getOldPath()));
  }

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    recent.clear();
  }

  @Override
  public void projectClosed(@NonNull ProjectClosedEvent event) {
    recent.clear();
  }
}
