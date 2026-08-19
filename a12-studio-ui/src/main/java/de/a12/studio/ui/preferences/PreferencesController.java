package de.a12.studio.ui.preferences;

import de.a12.studio.ui.events.PreferencesOpenRequestedEvent;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PreferencesController implements Initializable {

  @FXML
  private Button appGeneralSettingsBtn;

  @FXML
  private Button generalSettingsBtn;

  @FXML
  private Button advancedSettingsBtn;

  @FXML
  private Button aiSettingsBtn;

  @FXML
  private Button annotationSetsBtn;

  @FXML
  private Button modelVersionBtn;

  @FXML
  private Button a12InstallationBtn;

  @FXML
  private Button previewSettingsBtn;

  @FXML
  private Button shortcutsBtn;

  @FXML
  private Button pluginsBtn;

  @FXML
  private StackPane contentStack;

  @FXML
  private VBox projectSettingsNav;

  private final Map<String, Parent> pages = new HashMap<>();

  private Button selectedButton;

  private Runnable onCloseRequested;

  public void setOnCloseRequested(Runnable onCloseRequested) {
    this.onCloseRequested = onCloseRequested;
  }

  public void setProjectOpen(boolean projectOpen) {
    projectSettingsNav.setVisible(projectOpen);
    projectSettingsNav.setManaged(projectOpen);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    showPage(appGeneralSettingsBtn, "preference-app-general.fxml");
  }

  public void showSection(PreferencesOpenRequestedEvent.@NonNull Section section) {
    switch (section) {
      case ANNOTATION_SETS -> onAnnotationSets();
      case GENERAL_SETTINGS -> onGeneralSettings();
      case A12_INSTALLATION -> onA12Installation();
      default -> onGeneralSettings();
    }
  }

  @FXML
  private void onAppGeneralSettings() {
    showPage(appGeneralSettingsBtn, "preference-app-general.fxml");
  }

  @FXML
  private void onGeneralSettings() {
    showPage(generalSettingsBtn, "preference-general.fxml");
  }

  @FXML
  private void onAdvancedSettings() {
    showPage(advancedSettingsBtn, "preference-advanced.fxml");
  }

  @FXML
  private void onAiSettings() {
    showPage(aiSettingsBtn, "preference-ai.fxml");
  }

  @FXML
  private void onAnnotationSets() {
    showPage(annotationSetsBtn, "preference-annotation-sets.fxml");
  }

  @FXML
  private void onModelVersion() {
    showPage(modelVersionBtn, "preference-model-version.fxml");
  }

  @FXML
  private void onA12Installation() {
    showPage(a12InstallationBtn, "preference-a12-installation.fxml");
  }

  @FXML
  private void onPreviewSettings() {
    showPage(previewSettingsBtn, "preference-preview.fxml");
  }

  @FXML
  private void onShortcuts() {
    showPage(shortcutsBtn, "preference-shortcuts.fxml");
  }

  @FXML
  private void onPlugins() {
    showPage(pluginsBtn, "preference-plugins.fxml");
  }

  private void showPage(Button button, String fxml) {
    if (selectedButton != null) {
      selectedButton.getStyleClass().remove("preference-button-selected");
    }
    button.getStyleClass().add("preference-button-selected");
    selectedButton = button;

    Parent page = pages.computeIfAbsent(fxml, this::loadPage);
    contentStack.getChildren().setAll(page);
  }

  private Parent loadPage(String fxml) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
      loader.setResources(StudioBundle.getBundle());
      return loader.load();
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not load preferences page '" + fxml + "'", e);
    }
  }

  @FXML
  private void onClose() {
    if (onCloseRequested != null) {
      onCloseRequested.run();
    }
  }
}
