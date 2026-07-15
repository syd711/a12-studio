package de.a12.studio.commons.util.zip;

public interface UnzipChangeListener {

  boolean unzipping(String name, int index, int total);

  void onError(String error);
}
