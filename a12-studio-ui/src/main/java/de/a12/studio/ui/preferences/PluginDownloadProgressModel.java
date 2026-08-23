package de.a12.studio.ui.preferences;

import de.a12.studio.ui.components.ProgressModel;
import de.a12.studio.ui.components.ProgressResultModel;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Downloads a plugin JAR from a marketplace entry's download URL to the plugins directory,
 * showing an indeterminate {@link de.a12.studio.ui.components.ProgressDialog} while the
 * transfer is in progress.
 */
class PluginDownloadProgressModel extends ProgressModel<Void> {

  private final String downloadUrl;
  private final File dest;

  private volatile HttpURLConnection connection;
  private boolean done = false;
  private IOException error;

  PluginDownloadProgressModel(@NonNull String title, @NonNull String downloadUrl, @NonNull File dest) {
    super(title);
    this.downloadUrl = downloadUrl;
    this.dest = dest;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public boolean isCancelable() {
    return true;
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
  public void processNext(ProgressResultModel progressResultModel, Void next) throws IOException {
    try {
      HttpURLConnection conn = (HttpURLConnection) URI.create(downloadUrl).toURL().openConnection();
      connection = conn;
      conn.setConnectTimeout(15_000);
      conn.setReadTimeout(60_000);
      conn.setRequestProperty("User-Agent", "a12-studio-plugin-manager");
      int status = conn.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        throw new IOException("Server returned HTTP " + status + " for: " + downloadUrl);
      }
      try (InputStream in = conn.getInputStream();
           FileOutputStream out = new FileOutputStream(dest)) {
        in.transferTo(out);
      }
    }
    catch (IOException e) {
      error = e;
      throw e;
    }
    finally {
      connection = null;
    }
  }

  @Override
  public boolean hasNext() {
    return !done;
  }

  @Override
  public void cancel() {
    HttpURLConnection conn = connection;
    if (conn != null) {
      conn.disconnect();
    }
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    if (error != null) {
      progressResultModel.addError();
    }
    else if (!progressResultModel.isCancelled()) {
      progressResultModel.addProcessed();
    }
  }
}
