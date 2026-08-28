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
 * Loads a {@link Project} from disk on a background thread, fires the project-open event on the
 * FX thread and waits for tabs to finish restoring before letting the progress dialog close.
 */
@Slf4j
class OpenProjectProgressModel extends ProgressModel<Void> {

  private final File file;
  private final Consumer<Project> onProjectLoaded;
  private final Runnable onFinalize;
  private boolean done = false;

  OpenProjectProgressModel(File file, Consumer<Project> onProjectLoaded, Runnable onFinalize) {
    super(StudioBundle.get("opening_project"));
    this.file = file;
    this.onProjectLoaded = onProjectLoaded;
    this.onFinalize = onFinalize;
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
    onProjectLoaded.accept(project);

    // Block this background thread until the project has actually finished opening on the FX thread:
    // both synchronous projectOpened listener dispatch (tree built, etc.) and TabPaneController's
    // asynchronous, pulse-yielding tab restoration (see TabPaneController.restoreNextTab - it defers each
    // tab's Scene Graph construction to its own Platform.runLater so this dialog's indeterminate animation
    // keeps rendering, rather than freezing solid for the whole restore). Only TabsRestoredEvent marks that
    // as actually done, so the progress dialog - which closes as soon as processNext() returns - doesn't
    // hide before the editor is actually shown.
    CountDownLatch projectOpenedLatch = new CountDownLatch(1);
    StudioEventListener tabsRestoredListener = new StudioEventListener() {
      @Override
      public void tabsRestored(@NonNull TabsRestoredEvent event) {
        StudioEventManager.getInstance().removeListener(this);
        projectOpenedLatch.countDown();
      }
    };
    StudioEventManager.getInstance().addListener(tabsRestoredListener);
    Platform.runLater(() -> {
      try {
        StudioEventManager.getInstance().fireProjectOpenEvent(project);
      }
      catch (Exception e) {
        log.error("Error dispatching project-open event: {}", e.getMessage(), e);
        StudioEventManager.getInstance().removeListener(tabsRestoredListener);
        projectOpenedLatch.countDown();
      }
    });
    try {
      projectOpenedLatch.await();
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public boolean hasNext() {
    return !done;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    onFinalize.run();
  }

}
