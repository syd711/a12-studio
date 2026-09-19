package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.RootController;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.StudioBundle;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Helpers for tests that load real FXML and drive its controllers on the JavaFX thread, without the
 * application running. {@link #startToolkit()} reports whether a JavaFX toolkit could be started at all, so a
 * test can skip itself on a headless build machine.
 */
public final class FxTestSupport {

  private static Boolean toolkitAvailable;

  private FxTestSupport() {
  }

  /** A loaded FXML's root controller plus the loader that produced it. */
  public record Loaded<T>(T controller, Object root) {
  }

  // Property editors ask Studio for the currently selected project item while initializing (to key their
  // persisted expanded state) and some (annotations) for the current project; with no application running
  // there is neither, so an empty project and no selection stand in.
  private static class NoSelectionRootController extends RootController {
    @Override
    public ProjectItem getSelectedProjectItem() {
      return selectedProjectItem;
    }
  }

  private static volatile ProjectItem selectedProjectItem;

  /** Makes {@code item} the "currently selected project item" panels see through {@code Studio}; null for none. */
  public static void selectProjectItem(ProjectItem item) {
    selectedProjectItem = item;
  }

  public static synchronized boolean startToolkit() throws Exception {
    if (toolkitAvailable != null) {
      return toolkitAvailable;
    }
    Field rootController = Studio.class.getDeclaredField("rootController");
    rootController.setAccessible(true);
    rootController.set(null, new NoSelectionRootController());
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, new Project());

    CountDownLatch started = new CountDownLatch(1);
    try {
      Platform.startup(started::countDown);
    }
    catch (IllegalStateException alreadyStarted) {
      started.countDown();
    }
    catch (Throwable noDisplay) {
      toolkitAvailable = false;
      return false;
    }
    toolkitAvailable = started.await(10, TimeUnit.SECONDS);
    return toolkitAvailable;
  }

  /** Runs {@code action} on the JavaFX thread and returns its result, rethrowing whatever it threw. */
  public static <T> T onFx(Callable<T> action) throws Exception {
    AtomicReference<T> result = new AtomicReference<>();
    AtomicReference<Throwable> failure = new AtomicReference<>();
    CountDownLatch done = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        result.set(action.call());
      }
      catch (Throwable t) {
        failure.set(t);
      }
      finally {
        done.countDown();
      }
    });
    if (!done.await(30, TimeUnit.SECONDS)) {
      throw new AssertionError("JavaFX thread did not finish in time");
    }
    if (failure.get() != null) {
      throw new AssertionError("Failed on the JavaFX thread: " + failure.get(), failure.get());
    }
    return result.get();
  }

  public static void onFx(Runnable action) throws Exception {
    onFx(() -> {
      action.run();
      return null;
    });
  }

  /** Loads {@code /de/a12/studio/ui/...} FXML with the application's resource bundle, on the JavaFX thread. */
  public static <T> Loaded<T> load(String absoluteResourcePath) throws Exception {
    URL location = FxTestSupport.class.getResource(absoluteResourcePath);
    if (location == null) {
      throw new AssertionError("Missing FXML " + absoluteResourcePath);
    }
    return onFx(() -> {
      FXMLLoader loader = new FXMLLoader(location, StudioBundle.getBundle());
      Object root = loader.load();
      @SuppressWarnings("unchecked")
      T controller = (T) loader.getController();
      return new Loaded<>(controller, root);
    });
  }

  /** Reads a (typically private) field, e.g. an {@code @FXML} control of a controller. */
  @SuppressWarnings("unchecked")
  public static <T> T field(Object target, String name) throws Exception {
    for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
      try {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(target);
      }
      catch (NoSuchFieldException notHere) {
        // keep looking in the superclass
      }
    }
    throw new NoSuchFieldException(name + " on " + target.getClass());
  }
}
