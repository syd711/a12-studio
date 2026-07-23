package de.a12.studio.ui.preferences;

import de.a12.studio.ui.events.PreferencesOpenRequestedEvent;
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
  private Button generalSettingsBtn;

  @FXML
  private Button aiSettingsBtn;

  @FXML
  private Button annotationSetsBtn;

  @FXML
  private Button previewSettingsBtn;

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
    showPage(aiSettingsBtn, "general-settings.fxml");
  }

  public void showSection(PreferencesOpenRequestedEvent.@NonNull Section section) {
    switch (section) {
      case ANNOTATION_SETS -> onAnnotationSets();
      case GENERAL_SETTINGS -> onGeneralSettings();
      default -> onAiSettings();
    }
  }

  @FXML
  private void onGeneralSettings() {
    showPage(generalSettingsBtn, "general-settings.fxml");
  }

  @FXML
  private void onAiSettings() {
    showPage(aiSettingsBtn, "ai-settings.fxml");
  }

  @FXML
  private void onAnnotationSets() {
    showPage(annotationSetsBtn, "annotation-sets.fxml");
  }

  @FXML
  private void onPreviewSettings() {
    showPage(previewSettingsBtn, "preview-settings.fxml");
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
      return FXMLLoader.load(getClass().getResource(fxml));
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
