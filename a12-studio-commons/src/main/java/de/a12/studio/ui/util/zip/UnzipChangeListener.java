package de.a12.studio.ui.util.zip;

public interface UnzipChangeListener {

  boolean unzipping(String name, int index, int total);

  void onError(String error);
}
