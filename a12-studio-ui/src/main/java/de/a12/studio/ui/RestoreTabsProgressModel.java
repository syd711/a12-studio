package de.a12.studio.ui;

import de.a12.studio.ui.components.ProgressModel;
import de.a12.studio.ui.components.ProgressResultModel;
import de.a12.studio.ui.util.StudioBundle;

import java.util.concurrent.CountDownLatch;

/**
 * Waits for previously-open tabs to finish restoring (see
 * {@link de.a12.studio.ui.tabs.TabPaneController#projectOpened}) behind its own progress dialog,
 * separate from {@link OpenProjectProgressModel}'s "loading the project" dialog. The latch is
 * created and its counting-down listener registered by {@link OpenProjectProgressModel} before the
 * project-open event fires - see that class's javadoc for why - so this model only ever has to wait
 * on it, never register anything itself.
 */
class RestoreTabsProgressModel extends ProgressModel<Void> {

  private final CountDownLatch tabsRestoredLatch;
  private final Runnable onFinalize;
  private boolean done = false;

  RestoreTabsProgressModel(CountDownLatch tabsRestoredLatch, Runnable onFinalize) {
    super(StudioBundle.get("restoring_tabs"));
    this.tabsRestoredLatch = tabsRestoredLatch;
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
    try {
      tabsRestoredLatch.await();
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
