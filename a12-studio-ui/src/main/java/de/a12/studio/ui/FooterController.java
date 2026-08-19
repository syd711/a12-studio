package de.a12.studio.ui;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.A12Settings;
import de.a12.studio.models.projects.settings.AiSettings;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.events.ProjectClosedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.SettingsChangedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.events.TabSelectionChangedEvent;
import de.a12.studio.ui.previewapp.PreviewAppLogWindow;
import de.a12.studio.ui.previewapp.PreviewAppProcess;
import de.a12.studio.ui.previewapp.PreviewAppStatusMonitor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class FooterController implements Initializable, StudioEventListener {

  private static final DateTimeFormatter MODIFIED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  // Only sampled to keep detection cheap on large files; a wrong encoding is rare in this many bytes.
  private static final int ENCODING_SAMPLE_SIZE = 64 * 1024;

  private static final String STATUS_BUBBLE_RUNNING = "status-running";
  private static final String STATUS_BUBBLE_STARTING = "status-starting";
  private static final String STATUS_BUBBLE_STOPPED = "status-stopped";

  @FXML
  private Label filePathLabel;

  @FXML
  private Label fileSizeLabel;

  @FXML
  private Label fileModifiedLabel;

  @FXML
  private Label fileEncodingLabel;

  @FXML
  private HBox previewAppStatus;

  @FXML
  private Label previewAppStatusLabel;

  @FXML
  private FontIcon previewAppStatusIcon;

  private ProjectItem currentItem;

  private boolean projectOpen;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);
    showItem(null);

    refreshPreviewAppStatusVisibility();
    PreviewAppStatusMonitor monitor = PreviewAppStatusMonitor.getInstance();
    monitor.start();
    updatePreviewAppStatus(monitor.getStatus());
    monitor.statusProperty().addListener((observable, oldStatus, newStatus) -> updatePreviewAppStatus(newStatus));
  }

  @Override
  public void settingsChanged(@NonNull SettingsChangedEvent event) {
    if (event.getSettings().getSettingsType().equals(AiSettings.SettingsType.A12_INSTALLATION)) {
      refreshPreviewAppStatusVisibility();
    }
  }

  private void refreshPreviewAppStatusVisibility() {
    String installationPath = A12Settings.load().getInstallationPath();
    boolean visible = projectOpen && installationPath != null
        && A12Settings.isValidInstallationFolder(new File(installationPath));
    previewAppStatus.setVisible(visible);
    previewAppStatus.setManaged(visible);
  }

  private void updatePreviewAppStatus(PreviewAppStatusMonitor.Status status) {
    previewAppStatusIcon.getStyleClass().removeAll(STATUS_BUBBLE_RUNNING, STATUS_BUBBLE_STARTING, STATUS_BUBBLE_STOPPED);

    switch (status) {
      case RUNNING -> {
        previewAppStatusIcon.getStyleClass().add(STATUS_BUBBLE_RUNNING);
        previewAppStatusLabel.setText("Preview App running on port " + PreviewAppProcess.getInstance().getPort());
      }
      case STARTING -> {
        previewAppStatusIcon.getStyleClass().add(STATUS_BUBBLE_STARTING);
        previewAppStatusLabel.setText("Preview App starting…");
      }
      case STOPPED -> {
        previewAppStatusIcon.getStyleClass().add(STATUS_BUBBLE_STOPPED);
        previewAppStatusLabel.setText("Preview App stopped");
      }
    }
  }

  @FXML
  private void onOpenPreviewAppLog() {
    PreviewAppLogWindow.show(Studio.stage);
  }

  @Override
  public void tabSelectionChanged(@NonNull TabSelectionChangedEvent event) {
    showItem(event.getItem());
  }

  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    if (currentItem != null && currentItem.getPath().equals(event.getItem().getPath())) {
      showItem(currentItem);
    }
  }

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    projectOpen = true;
    refreshPreviewAppStatusVisibility();
  }

  @Override
  public void projectClosed(@NonNull ProjectClosedEvent event) {
    projectOpen = false;
    refreshPreviewAppStatusVisibility();
    showItem(null);
  }

  private void showItem(ProjectItem item) {
    this.currentItem = item;

    if (item == null || item.isFolder() || !item.getFile().exists()) {
      filePathLabel.setText("");
      fileSizeLabel.setText("");
      fileModifiedLabel.setText("");
      fileEncodingLabel.setText("");
      return;
    }

    File file = item.getFile();
    filePathLabel.setText(file.getAbsolutePath());
    fileSizeLabel.setText(FileUtils.byteCountToDisplaySize(file.length()));
    fileModifiedLabel.setText(MODIFIED_FORMAT.format(Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault())));
    fileEncodingLabel.setText(detectEncoding(file));
  }

  private String detectEncoding(@NonNull File file) {
    byte[] bytes;
    try (SeekableByteChannel channel = Files.newByteChannel(file.toPath(), StandardOpenOption.READ)) {
      ByteBuffer buffer = ByteBuffer.allocate((int) Math.min(file.length(), ENCODING_SAMPLE_SIZE));
      channel.read(buffer);
      buffer.flip();
      bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
    }
    catch (IOException e) {
      return "Unknown";
    }

    if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
      return "UTF-8 (BOM)";
    }
    if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xFE) {
      return "UTF-16LE";
    }
    if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xFF) == 0xFF) {
      return "UTF-16BE";
    }

    try {
      StandardCharsets.UTF_8.newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(bytes));
      return "UTF-8";
    }
    catch (CharacterCodingException e) {
      return "Other";
    }
  }
}
