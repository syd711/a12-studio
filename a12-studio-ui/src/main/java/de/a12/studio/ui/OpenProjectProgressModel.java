package de.a12.studio.ui;

import de.a12.studio.models.projects.Project;
import de.a12.studio.plugin.manager.IProjectOpenedListener;
import de.a12.studio.plugin.manager.PluginManager;
import de.a12.studio.ui.components.ProgressModel;
import de.a12.studio.ui.components.ProgressResultModel;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.events.TabsRestoredEvent;
import de.a12.studio.ui.util.StudioBundle;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Loads a {@link Project} from disk on a background thread and fires the project-open event on the
 * FX thread. Shown in its own progress dialog, separate from {@link RestoreTabsProgressModel}, so
 * "loading the project" and "restoring previously open tabs" each get their own progress bar instead
 * of one dialog silently covering both phases.
 * <p>
 * Registers the listener {@link RestoreTabsProgressModel} waits on for {@link TabsRestoredEvent}
 * itself, before firing the project-open event - tab restoration (see
 * {@link de.a12.studio.ui.tabs.TabPaneController#projectOpened}) can start synchronously as part of
 * that same event dispatch and may even finish restoring (e.g. a project with no open tabs) before
 * the caller gets around to creating the second progress dialog. Handing over an already-registered
 * {@link CountDownLatch} means that race is harmless: a completed restore just counts the latch down
 * early, and {@link RestoreTabsProgressModel}'s {@code await()} returns immediately instead of
 * hanging.
 */
@Slf4j
class OpenProjectProgressModel extends ProgressModel<Void> {

  private final File file;
  private final Consumer<Project> onProjectLoaded;
  private final CountDownLatch tabsRestoredLatch;
  private boolean done = false;
  private String incompatibleModelError;

  OpenProjectProgressModel(File file, Consumer<Project> onProjectLoaded, CountDownLatch tabsRestoredLatch) {
    super(StudioBundle.get("opening_project"));
    this.file = file;
    this.onProjectLoaded = onProjectLoaded;
    this.tabsRestoredLatch = tabsRestoredLatch;
  }

  /**
   * {@code false} if the project failed to load (e.g. an incompatible model version was found), in
   * which case no project-open event was fired and {@link #getError()} describes why.
   */
  boolean isSuccessful() {
    return incompatibleModelError == null;
  }

  String getError() {
    return incompatibleModelError;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public boolean isCancelable() {
    return false;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public Void getNext() {
    done = true;
    return null;
  }

  @Override
  public String nextToString(Void next) {
    return null;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, Void next) {
    Project project = new Project();
    project.load(file);
    for (IProjectOpenedListener listener : PluginManager.getInstance().getProjectOpenedListeners()) {
      listener.onProjectOpened(project);
    }

    // Checked here, before anything is notified that the project opened, so a failed check cancels
    // the open outright - no listener ever sees ProjectOpenedEvent for this project, so none of them
    // have state to unwind. The caller shows the resulting error only after this dialog has closed.
    incompatibleModelError = Studio.checkModelVersions(project);
    if (incompatibleModelError != null) {
      return;
    }

    // Registered before the event fires (see class javadoc) so a same-pulse restore can't finish
    // before something is listening for it.
    StudioEventListener tabsRestoredListener = new StudioEventListener() {
      @Override
      public void tabsRestored(@NonNull TabsRestoredEvent event) {
        StudioEventManager.getInstance().removeListener(this);
        tabsRestoredLatch.countDown();
      }
    };
    StudioEventManager.getInstance().addListener(tabsRestoredListener);

    CountDownLatch eventDispatchedLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        onProjectLoaded.accept(project);
        StudioEventManager.getInstance().fireProjectOpenEvent(project);
      }
      catch (Exception e) {
        log.error("Error dispatching project-open event: {}", e.getMessage(), e);
      }
      finally {
        eventDispatchedLatch.countDown();
      }
    });
    try {
      eventDispatchedLatch.await();
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public boolean hasNext() {
    return !done;
  }

}
