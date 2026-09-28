package de.a12.studio.ui.versioncontrol;

import de.a12.studio.ui.components.ProgressModel;
import de.a12.studio.ui.components.ProgressResultModel;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jspecify.annotations.NonNull;

/**
 * Runs a single git commit/revert call on the {@link de.a12.studio.ui.components.ProgressDialog}'s
 * background thread, showing an indeterminate progress dialog while it's in flight. These are local
 * repository operations with no cancellation hook in JGit, so unlike {@link
 * de.a12.studio.ui.preferences.PluginDownloadProgressModel} this model is not cancelable - a mid-flight
 * cancel would only close the dialog while the operation kept running to completion underneath it.
 */
class GitOperationProgressModel extends ProgressModel<Void> {

  @FunctionalInterface
  interface Operation {
    void run() throws GitAPIException;
  }

  private final Operation operation;
  private boolean done = false;
  private GitAPIException error;

  GitOperationProgressModel(@NonNull String title, @NonNull Operation operation) {
    super(title);
    this.operation = operation;
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
  public void processNext(ProgressResultModel progressResultModel, Void next) throws GitAPIException {
    try {
      operation.run();
    }
    catch (GitAPIException e) {
      error = e;
      throw e;
    }
  }

  @Override
  public boolean hasNext() {
    return !done;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    if (error != null) {
      progressResultModel.addError();
    }
    else {
      progressResultModel.addProcessed();
    }
  }
}
