package de.a12.studio.ui.tabs;

import de.a12.studio.ui.components.ProgressModel;
import de.a12.studio.ui.components.ProgressResultModel;
import de.a12.studio.ui.util.StudioBundle;
import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;

/**
 * Shows a progress dialog while a tab's editor content is built - see
 * {@link TabPaneController#loadTabContentWithProgress} - since {@link de.a12.studio.ui.EditorFactory#create}
 * (a full FXML + controller {@code load()}) can be noticeably slow for a large model. {@code buildContent}
 * must run on the FX Application Thread (same reasoning as {@link TabPaneController#restoreNextTab}), so it
 * can't run as this model's own {@code processNext} - that runs on the progress dialog's background service
 * thread. Instead, like {@link de.a12.studio.ui.OpenProjectProgressModel}/{@link
 * de.a12.studio.ui.RestoreTabsProgressModel}, it is queued via {@link Platform#runLater} before the dialog's
 * background thread blocks on a latch, so the FX thread gets a pulse to actually render the dialog before
 * {@code buildContent} - queued first - runs inside the dialog's own nested event loop.
 */
class LoadTabProgressModel extends ProgressModel<Void> {

  private final Runnable buildContent;
  private boolean done = false;

  LoadTabProgressModel(Runnable buildContent) {
    super(StudioBundle.get("loading_editor"));
    this.buildContent = buildContent;
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
    CountDownLatch contentBuiltLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        buildContent.run();
      }
      finally {
        contentBuiltLatch.countDown();
      }
    });
    try {
      contentBuiltLatch.await();
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
