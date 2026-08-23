package de.a12.studio.ui;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.AdvancedSettings;
import de.a12.studio.ui.components.ProgressModel;
import de.a12.studio.ui.components.ProgressResultModel;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import javafx.application.Platform;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Loads a {@link Project} from disk on a background thread, fires the project-open event on the
 * FX thread and waits for it to finish dispatching before letting the progress dialog close.
 */
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
    autoDetectApplicationGroups(project);
    onProjectLoaded.accept(project);

    // Block this background thread until the project-open event has actually finished
    // dispatching on the FX thread (tree/tabs built), so the progress dialog - which closes
    // as soon as processNext() returns - doesn't hide before the editor is actually shown.
    CountDownLatch projectOpenedLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        StudioEventManager.getInstance().fireProjectOpenEvent(project);
      }
      finally {
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

  /**
   * Scans all models in the project tree for the "applicationGroup" annotation.
   * If found and the feature is not yet enabled in AdvancedSettings,
   * it is activated and persisted automatically.
   */
  private void autoDetectApplicationGroups(@NonNull Project project) {
    AdvancedSettings settings = project.getSettings().getAdvancedSettings();
    if (settings.isUseApplicationGroups()) {
      return;
    }
    if (hasApplicationGroupAnnotation(project.getRoot())) {
      settings.setUseApplicationGroups(true);
      settings.save();
    }
  }

  private boolean hasApplicationGroupAnnotation(@NonNull ProjectItem item) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        if (hasApplicationGroupAnnotation(child)) {
          return true;
        }
      }
    }
    else if (item.getModel() != null) {
      for (Annotation annotation : item.getModel().getAnnotations()) {
        if ("applicationGroup".equals(annotation.getName())) {
          return true;
        }
      }
    }
    return false;
  }
}
