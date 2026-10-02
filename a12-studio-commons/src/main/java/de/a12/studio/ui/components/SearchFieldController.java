package de.a12.studio.ui.components;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class SearchFieldController {

  @FXML
  private TextField searchField;

  @FXML
  private Button resetSearchButton;

  private static final KeyCombination SEARCH_KEYS = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);

  @FXML
  private void initialize() {
    resetSearchButton.visibleProperty().bind(Bindings.isNotEmpty(searchField.textProperty()));
    resetSearchButton.managedProperty().bind(resetSearchButton.visibleProperty());
    setPromptText(searchField.getPromptText());
  }

  /** Focuses the search field when Ctrl+F is pressed anywhere inside the given node (e.g. the tree or table it filters). */
  public void installShortcut(@NonNull Node scope) {
    scope.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (SEARCH_KEYS.match(event)) {
        searchField.requestFocus();
        event.consume();
      }
    });
  }

  public void setOnSearch(@NonNull Consumer<String> onSearch) {
    searchField.textProperty().addListener((observable, oldValue, newValue) -> onSearch.accept(newValue));
  }

  public void setPromptText(String promptText) {
    searchField.setPromptText(promptText == null ? null : promptText + " (" + SEARCH_KEYS.getDisplayText() + ")");
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
