package de.a12.studio.ui.preferences;

import de.a12.studio.plugin.manager.Marketplace;
import de.a12.studio.plugin.manager.PluginManager;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class PreferencePluginsPanelController implements Initializable {

  // ---------------------------------------------------------------------------
  // FXML
  // ---------------------------------------------------------------------------

  @FXML private TableView<Marketplace.MarketplaceEntry> pluginsTable;
  @FXML private TableColumn<Marketplace.MarketplaceEntry, Marketplace.MarketplaceEntry> iconCol;
  @FXML private TableColumn<Marketplace.MarketplaceEntry, String> nameCol;
  @FXML private TableColumn<Marketplace.MarketplaceEntry, String> descriptionCol;
  @FXML private TableColumn<Marketplace.MarketplaceEntry, String> versionCol;
  @FXML private TableColumn<Marketplace.MarketplaceEntry, Marketplace.MarketplaceEntry> statusCol;
  @FXML private Label statusLabel;
  @FXML private ProgressIndicator progressIndicator;

  // ---------------------------------------------------------------------------
  // Initialise
  // ---------------------------------------------------------------------------

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    setupColumns();
    statusLabel.setText("");
    progressIndicator.setVisible(false);
    loadMarketplace();
  }

  // ---------------------------------------------------------------------------
  // Column setup
  // ---------------------------------------------------------------------------

  private void setupColumns() {
    // Icon column
    iconCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
    iconCol.setCellFactory(col -> new TableCell<>() {
      private final ImageView imageView = new ImageView();
      {
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);
        imageView.setPreserveRatio(true);
        setAlignment(Pos.CENTER);
      }
      @Override
      protected void updateItem(Marketplace.MarketplaceEntry entry, boolean empty) {
        super.updateItem(entry, empty);
        if (empty || entry == null) {
          setGraphic(null);
        } else {
          Image img = decodeIcon(entry.getIcon());
          imageView.setImage(img);
          setGraphic(img != null ? imageView : null);
        }
      }
    });

    // Name column
    nameCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getName()));

    // Description column
    descriptionCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getDescription()));
    descriptionCol.setCellFactory(col -> new TableCell<>() {
      private final Label label = new Label();
      {
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.getStyleClass().add("field-description");
      }
      @Override
      protected void updateItem(String text, boolean empty) {
        super.updateItem(text, empty);
        if (empty || text == null) {
          setGraphic(null);
        } else {
          label.setText(text);
          setGraphic(label);
        }
      }
    });

    // Version column
    versionCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getPluginVersion()));

    // Status/action column – shows "Installed" checkbox (disabled, read-only) + Install/Remove button
    statusCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
    statusCol.setCellFactory(col -> new TableCell<>() {
      private final CheckBox installedCheck = new CheckBox();
      private final Button actionBtn = new Button();
      private final VBox box = new VBox(4, installedCheck, actionBtn);
      {
        box.setAlignment(Pos.CENTER);
        installedCheck.setMouseTransparent(true);
        installedCheck.setFocusTraversable(false);
        installedCheck.setText(StudioBundle.get("plugins.installed"));
        actionBtn.setMinWidth(120);
      }
      @Override
      protected void updateItem(Marketplace.MarketplaceEntry entry, boolean empty) {
        super.updateItem(entry, empty);
        if (empty || entry == null) {
          setGraphic(null);
          return;
        }
        boolean installed = PluginManager.getInstance().isInstalled(entry.getName());
        installedCheck.setSelected(installed);
        if (installed) {
          actionBtn.setText(StudioBundle.get("plugins.remove"));
          actionBtn.getStyleClass().removeAll("primary-button", "secondary-button");
          actionBtn.getStyleClass().add("secondary-button");
          actionBtn.setOnAction(e -> onRemovePlugin(entry, this));
        } else {
          actionBtn.setText(StudioBundle.get("plugins.install"));
          actionBtn.getStyleClass().removeAll("primary-button", "secondary-button");
          actionBtn.getStyleClass().add("primary-button");
          actionBtn.setOnAction(e -> onInstallPlugin(entry, this));
        }
        setGraphic(box);
      }
    });
  }

  // ---------------------------------------------------------------------------
  // Data loading
  // ---------------------------------------------------------------------------

  private void loadMarketplace() {
    List<Marketplace.MarketplaceEntry> entries = PluginManager.getInstance().getMarketplaceEntries();
    pluginsTable.setItems(FXCollections.observableArrayList(entries));
    if (entries.isEmpty()) {
      statusLabel.setText(StudioBundle.get("plugins.no_compatible_plugins"));
    }
  }

  // ---------------------------------------------------------------------------
  // Install / remove actions
  // ---------------------------------------------------------------------------

  private void onInstallPlugin(Marketplace.@NonNull MarketplaceEntry entry,
                               @NonNull TableCell<Marketplace.MarketplaceEntry, Marketplace.MarketplaceEntry> cell) {
    if (entry.getDownloadUrl() == null || entry.getDownloadUrl().isBlank()) {
      WidgetFactory.showAlert(getStage(cell), StudioBundle.get("plugins.no_download_url"), entry.getName());
      return;
    }

    setProgress(true, StudioBundle.get("plugins.downloading", entry.getName()));

    Thread thread = new Thread(() -> {
      try {
        File pluginsDir = PluginManager.getInstance().getPluginsDir();
        pluginsDir.mkdirs();
        String fileName = deriveFileName(entry);
        File dest = new File(pluginsDir, fileName);
        downloadFile(entry.getDownloadUrl(), dest);
        Platform.runLater(() -> {
          setProgress(false, StudioBundle.get("plugins.install_restart_required", entry.getName()));
          pluginsTable.refresh();
        });
      } catch (IOException ex) {
        log.error("Failed to download plugin '{}': {}", entry.getName(), ex.getMessage(), ex);
        Platform.runLater(() -> {
          setProgress(false, "");
          WidgetFactory.showAlert(getStage(cell),
              StudioBundle.get("plugins.download_failed"), ex.getMessage());
        });
      }
    }, "plugin-installer");
    thread.setDaemon(true);
    thread.start();
  }

  private void onRemovePlugin(Marketplace.@NonNull MarketplaceEntry entry,
                              @NonNull TableCell<Marketplace.MarketplaceEntry, Marketplace.MarketplaceEntry> cell) {
    File pluginsDir = PluginManager.getInstance().getPluginsDir();
    String fileName = deriveFileName(entry);
    File jarFile = new File(pluginsDir, fileName);
    if (!jarFile.exists()) {
      // Try to find by scanning the plugins dir for any JAR matching the plugin name
      File[] jars = pluginsDir.listFiles(f -> f.getName().endsWith(".jar"));
      if (jars != null) {
        for (File jar : jars) {
          try (var jf = new java.util.jar.JarFile(jar)) {
            var pjEntry = jf.getJarEntry("plugin.json");
            if (pjEntry != null) {
              try (var in = jf.getInputStream(pjEntry)) {
                var node = new tools.jackson.databind.ObjectMapper().readTree(in);
                if (entry.getName().equals(node.path("name").asText())) {
                  jarFile = jar;
                  break;
                }
              }
            }
          } catch (Exception ignored) { }
        }
      }
    }

    if (!jarFile.exists()) {
      WidgetFactory.showAlert(getStage(cell), StudioBundle.get("plugins.jar_not_found"), entry.getName());
      return;
    }

    try {
      Files.delete(jarFile.toPath());
      setProgress(false, StudioBundle.get("plugins.remove_restart_required", entry.getName()));
      pluginsTable.refresh();
    } catch (IOException ex) {
      log.error("Failed to delete plugin JAR '{}': {}", jarFile.getName(), ex.getMessage(), ex);
      WidgetFactory.showAlert(getStage(cell), StudioBundle.get("plugins.remove_failed"), ex.getMessage());
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static void downloadFile(@NonNull String urlString, @NonNull File dest) throws IOException {
    URL url = URI.create(urlString).toURL();
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setConnectTimeout(15_000);
    connection.setReadTimeout(60_000);
    connection.setRequestProperty("User-Agent", "a12-studio-plugin-manager");
    int status = connection.getResponseCode();
    if (status != HttpURLConnection.HTTP_OK) {
      throw new IOException("Server returned HTTP " + status + " for: " + urlString);
    }
    try (InputStream in = connection.getInputStream();
         FileOutputStream out = new FileOutputStream(dest)) {
      in.transferTo(out);
    }
  }

  @NonNull
  private static String deriveFileName(Marketplace.@NonNull MarketplaceEntry entry) {
    // Derive a filename from the download URL or fall back to sanitized plugin name.
    String url = entry.getDownloadUrl();
    if (url != null && !url.isBlank() && url.contains("/")) {
      String lastSegment = url.substring(url.lastIndexOf('/') + 1);
      if (lastSegment.endsWith(".jar")) {
        return lastSegment;
      }
    }
    return entry.getName().replaceAll("[^A-Za-z0-9._-]", "_") + ".jar";
  }

  private static javafx.scene.image.Image decodeIcon(String base64) {
    if (base64 == null || base64.isBlank()) {
      return null;
    }
    try {
      byte[] bytes = Base64.getDecoder().decode(base64);
      return new Image(new ByteArrayInputStream(bytes));
    } catch (Exception e) {
      return null;
    }
  }

  private void setProgress(boolean active, @NonNull String message) {
    progressIndicator.setVisible(active);
    statusLabel.setText(message);
  }

  private Stage getStage(@NonNull TableCell<?, ?> cell) {
    return (Stage) cell.getTableView().getScene().getWindow();
  }
}
