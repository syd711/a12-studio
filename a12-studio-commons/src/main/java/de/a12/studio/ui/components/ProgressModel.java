package de.a12.studio.ui.components;

public abstract class ProgressModel<T> {

  private String title;

  public ProgressModel(String title) {
    this.title = title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public boolean isShowSummary() {
    return true;
  }

  public boolean isIndeterminate() {
    return false;
  }

  public boolean isCancelable() {
    return true;
  }

  public boolean isShowSteps() {
    return true;
  }

  public abstract int getMax();

  public abstract T getNext();

  public abstract String nextToString(T t);

  public abstract void processNext(ProgressResultModel progressResultModel, T next) throws Exception;

  public abstract boolean hasNext();

  public void cancel() {

  }

  public void finalizeModel(ProgressResultModel progressResultModel) {

  }
}
