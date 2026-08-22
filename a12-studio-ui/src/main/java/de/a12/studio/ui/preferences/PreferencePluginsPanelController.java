package de.a12.studio.ui.preferences;

import de.a12.studio.plugin.manager.MarkdownRenderer;
import de.a12.studio.plugin.manager.Marketplace;
import de.a12.studio.plugin.manager.MarketplaceEntry;
import de.a12.studio.plugin.manager.PluginManager;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public class PreferencePluginsPanelController implements Initializable {

  // ---------------------------------------------------------------------------
  // FXML fields
  // ---------------------------------------------------------------------------

  @FXML private TextField searchField;
  @FXML private ListView<MarketplaceEntry> pluginListView;

  @FXML private VBox detailPane;
  @FXML private VBox emptyDetailPane;

  // Detail header
  @FXML private ImageView detailIcon;
  @FXML private Label detailName;
  @FXML private Label detailVersion;
  @FXML private Label detailAuthor;
  @FXML private Label detailLicense;
  @FXML private Button actionButton;

  // Description
  @FXML private WebView descriptionWebView;

  // Status bar
  @FXML private ProgressIndicator progressIndicator;
  @FXML private Label statusLabel;

  // ---------------------------------------------------------------------------
  // State
  // ---------------------------------------------------------------------------

  private ObservableList<MarketplaceEntry> allEntries;

  private MarketplaceEntry selectedEntry;

  // ---------------------------------------------------------------------------
  // Initialise
  // ---------------------------------------------------------------------------

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    statusLabel.setText("");
    progressIndicator.setVisible(false);
    showDetail(false);

    descriptionWebView.getEngine().setUserStyleSheetLocation(MarkdownRenderer.getStylesheetUrl());

    setupList();
    setupSearch();
    loadMarketplace();
  }

  // ---------------------------------------------------------------------------
  // List setup
  // ---------------------------------------------------------------------------

  private void setupList() {
    pluginListView.setCellFactory(lv -> new PluginListCell());
    pluginListView.getSelectionModel().selectedItemProperty().addListener(
        (obs, old, entry) -> onEntrySelected(entry));
  }

  private void setupSearch() {
    searchField.textProperty().addListener((obs, old, text) -> applyFilter(text));
  }

  private void applyFilter(String text) {
    if (allEntries == null) return;
    if (text == null || text.isBlank()) {
      pluginListView.setItems(allEntries);
    } else {
      String lower = text.toLowerCase();
      ObservableList<MarketplaceEntry> filtered = FXCollections.observableArrayList(
          allEntries.stream()
              .filter(e -> containsIgnoreCase(e.getName(), lower)
                  || containsIgnoreCase(e.getDescription(), lower)
                  || containsIgnoreCase(e.getAuthor(), lower))
              .toList());
      pluginListView.setItems(filtered);
    }
  }

  private static boolean containsIgnoreCase(@Nullable String haystack, String needle) {
    return haystack != null && haystack.toLowerCase().contains(needle);
  }

  // ---------------------------------------------------------------------------
  // Data loading
  // ---------------------------------------------------------------------------

  private void loadMarketplace() {
    List<MarketplaceEntry> entries = PluginManager.getInstance().getMarketplaceEntries();
    allEntries = FXCollections.observableArrayList(entries);
    pluginListView.setItems(allEntries);
    if (entries.isEmpty()) {
      statusLabel.setText(StudioBundle.get("plugins.no_compatible_plugins"));
    }
  }

  // ---------------------------------------------------------------------------
  // Selection handling
  // ---------------------------------------------------------------------------

  private void onEntrySelected(MarketplaceEntry entry) {
    selectedEntry = entry;
    if (entry == null) {
      showDetail(false);
      return;
    }
    showDetail(true);
    populateDetail(entry);
  }

  private void populateDetail(MarketplaceEntry entry) {
    // Icon
    Image img = decodeIcon(entry.getIcon());
    detailIcon.setImage(img);

    // Meta labels
    detailName.setText(entry.getName());
    detailVersion.setText("v" + nullSafe(entry.getPluginVersion()));
    detailAuthor.setText(nullSafe(entry.getAuthor(), StudioBundle.get("plugins.unknown_author")));
    detailLicense.setText(nullSafe(entry.getLicense(), StudioBundle.get("plugins.unknown_license")));

    // Install / Remove button
    refreshActionButton(entry);

    // Description as HTML rendered from Markdown
    String html = MarkdownRenderer.toHtml(entry.getDescription());
    descriptionWebView.getEngine().loadContent(html, "text/html");
  }

  private void refreshActionButton(MarketplaceEntry entry) {
    boolean installed = PluginManager.getInstance().isInstalled(entry.getName());
    if (installed) {
      actionButton.setText(StudioBundle.get("plugins.remove"));
      actionButton.getStyleClass().removeAll("primary-button", "secondary-button");
      actionButton.getStyleClass().add("secondary-button");
    } else {
      actionButton.setText(StudioBundle.get("plugins.install"));
      actionButton.getStyleClass().removeAll("primary-button", "secondary-button");
      actionButton.getStyleClass().add("primary-button");
    }
  }

  private void showDetail(boolean show) {
    detailPane.setVisible(show);
    detailPane.setManaged(show);
    emptyDetailPane.setVisible(!show);
    emptyDetailPane.setManaged(!show);
  }

  // ---------------------------------------------------------------------------
  // Action button handler
  // ---------------------------------------------------------------------------

  @FXML
  private void onActionButtonClicked() {
    if (selectedEntry == null) return;
    boolean installed = PluginManager.getInstance().isInstalled(selectedEntry.getName());
    if (installed) {
      onRemovePlugin(selectedEntry);
    } else {
      onInstallPlugin(selectedEntry);
    }
  }

  // ---------------------------------------------------------------------------
  // Install / remove logic
  // ---------------------------------------------------------------------------

  private void onInstallPlugin(@NonNull MarketplaceEntry entry) {
    if (entry.getDownloadUrl() == null || entry.getDownloadUrl().isBlank()) {
      WidgetFactory.showAlert(getStage(), StudioBundle.get("plugins.no_download_url"), entry.getName());
      return;
    }

    Optional<ButtonType> confirmation = WidgetFactory.showConfirmation(getStage(),
        StudioBundle.get("plugins.install_confirm", entry.getName()), null, null, StudioBundle.get("plugins.install"));
    if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
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
          pluginListView.refresh();
          if (selectedEntry != null) refreshActionButton(selectedEntry);
        });
      } catch (IOException ex) {
        log.error("Failed to download plugin '{}': {}", entry.getName(), ex.getMessage(), ex);
        Platform.runLater(() -> {
          setProgress(false, "");
          WidgetFactory.showAlert(getStage(), StudioBundle.get("plugins.download_failed"), ex.getMessage());
        });
      }
    }, "plugin-installer");
    thread.setDaemon(true);
    thread.start();
  }

  private void onRemovePlugin(@NonNull MarketplaceEntry entry) {
    File pluginsDir = PluginManager.getInstance().getPluginsDir();
    String fileName = deriveFileName(entry);
    File jarFile = new File(pluginsDir, fileName);
    if (!jarFile.exists()) {
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
      WidgetFactory.showAlert(getStage(), StudioBundle.get("plugins.jar_not_found"), entry.getName());
      return;
    }

    try {
      Files.delete(jarFile.toPath());
      setProgress(false, StudioBundle.get("plugins.remove_restart_required", entry.getName()));
      pluginListView.refresh();
      if (selectedEntry != null) refreshActionButton(selectedEntry);
    } catch (IOException ex) {
      log.error("Failed to delete plugin JAR '{}': {}", jarFile.getName(), ex.getMessage(), ex);
      WidgetFactory.showAlert(getStage(), StudioBundle.get("plugins.remove_failed"), ex.getMessage());
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
  private static String deriveFileName(@NonNull MarketplaceEntry entry) {
    String url = entry.getDownloadUrl();
    if (url != null && !url.isBlank() && url.contains("/")) {
      String lastSegment = url.substring(url.lastIndexOf('/') + 1);
      if (lastSegment.endsWith(".jar")) {
        return lastSegment;
      }
    }
    return entry.getName().replaceAll("[^A-Za-z0-9._-]", "_") + ".jar";
  }

  @Nullable
  private static Image decodeIcon(@Nullable String base64) {
    if (base64 == null || base64.isBlank()) return null;
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

  @Nullable
  private Stage getStage() {
    if (pluginListView.getScene() == null) return null;
    return (Stage) pluginListView.getScene().getWindow();
  }

  private static String nullSafe(@Nullable String s) {
    return s != null ? s : "";
  }

  private static String nullSafe(@Nullable String s, String fallback) {
    return (s != null && !s.isBlank()) ? s : fallback;
  }

  // ---------------------------------------------------------------------------
  // Custom list cell
  // ---------------------------------------------------------------------------

  private static class PluginListCell extends ListCell<MarketplaceEntry> {

    private final HBox root = new HBox(10);
    private final ImageView iconView = new ImageView();
    private final CheckBox installedCheck = new CheckBox();
    private final Label nameLabel = new Label();
    private final Label versionLabel = new Label();
    private final Label authorLabel = new Label();
    private final VBox textBox = new VBox(2);

    PluginListCell() {
      iconView.setFitWidth(36);
      iconView.setFitHeight(36);
      iconView.setPreserveRatio(true);

      installedCheck.setMouseTransparent(true);
      installedCheck.setFocusTraversable(false);

      nameLabel.getStyleClass().add("plugin-cell-name");
      versionLabel.getStyleClass().add("plugin-cell-meta");
      authorLabel.getStyleClass().add("plugin-cell-meta");

      HBox metaRow = new HBox(8, versionLabel, authorLabel);
      metaRow.setAlignment(Pos.CENTER_LEFT);

      textBox.getChildren().addAll(nameLabel, metaRow);
      HBox.setHgrow(textBox, Priority.ALWAYS);

      Region spacer = new Region();
      HBox.setHgrow(spacer, Priority.ALWAYS);

      root.setAlignment(Pos.CENTER_LEFT);
      root.getChildren().addAll(iconView, textBox, spacer, installedCheck);
      root.getStyleClass().add("plugin-list-cell");
    }

    @Override
    protected void updateItem(MarketplaceEntry entry, boolean empty) {
      super.updateItem(entry, empty);
      if (empty || entry == null) {
        setGraphic(null);
        return;
      }
      Image img = decodeIcon(entry.getIcon());
      iconView.setImage(img);
      nameLabel.setText(entry.getName());
      versionLabel.setText("v" + (entry.getPluginVersion() != null ? entry.getPluginVersion() : ""));
      authorLabel.setText(entry.getAuthor() != null ? entry.getAuthor() : "");
      installedCheck.setSelected(PluginManager.getInstance().isInstalled(entry.getName()));
      setGraphic(root);
    }

    @Nullable
    private static Image decodeIcon(@Nullable String base64) {
      if (base64 == null || base64.isBlank()) return null;
      try {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return new Image(new ByteArrayInputStream(bytes));
      } catch (Exception e) {
        return null;
      }
    }
  }
}
