package de.a12.studio.commons.components;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class SearchFieldController {

  @FXML
  private TextField searchField;

  @FXML
  private Button resetSearchButton;

  @FXML
  private void initialize() {
    resetSearchButton.visibleProperty().bind(Bindings.isNotEmpty(searchField.textProperty()));
    resetSearchButton.managedProperty().bind(resetSearchButton.visibleProperty());
  }

  public void setOnSearch(@NonNull Consumer<String> onSearch) {
    searchField.textProperty().addListener((observable, oldValue, newValue) -> onSearch.accept(newValue));
  }

  public void setPromptText(String promptText) {
    searchField.setPromptText(promptText);
  }

  public String getText() {
    return searchField.getText();
  }

  public StringProperty textProperty() {
    return searchField.textProperty();
  }

  public void clear() {
    searchField.clear();
  }

  public void requestFocus() {
    searchField.requestFocus();
  }

  @FXML
  private void onResetSearch() {
    searchField.clear();
  }
}
