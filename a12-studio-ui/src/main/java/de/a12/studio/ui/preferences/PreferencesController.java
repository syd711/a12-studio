package de.a12.studio.ui.preferences;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PreferencesController implements Initializable {

  private static final String AI_SETTINGS = "AI Settings";
  private static final String ANNOTATION_SETS = "Annotation Sets";

  @FXML
  private ListView<String> categoryList;

  @FXML
  private StackPane contentStack;

  private final Map<String, Parent> pages = new LinkedHashMap<>();

  private Runnable onCloseRequested;

  public void setOnCloseRequested(Runnable onCloseRequested) {
    this.onCloseRequested = onCloseRequested;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    categoryList.getItems().addAll(AI_SETTINGS, ANNOTATION_SETS);
    categoryList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        showPage(newValue);
      }
    });
    categoryList.getSelectionModel().selectFirst();
  }

  private void showPage(String category) {
    Parent page = pages.computeIfAbsent(category, this::loadPage);
    contentStack.getChildren().setAll(page);
  }

  private Parent loadPage(String category) {
    String fxml = AI_SETTINGS.equals(category) ? "ai-settings-panel.fxml" : "annotation-sets-panel.fxml";
    try {
      return FXMLLoader.load(getClass().getResource(fxml));
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not load preferences page '" + category + "'", e);
    }
  }

  @FXML
  private void onClose() {
    if (onCloseRequested != null) {
      onCloseRequested.run();
    }
  }
}
