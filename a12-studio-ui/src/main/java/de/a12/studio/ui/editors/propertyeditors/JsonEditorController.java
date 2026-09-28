package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Standalone JSON property panel: wraps the chrome-less {@link JsonCodeEditorController} in the standard
 * property-editor TitledPane, shows its validation errors in this panel's error container and persists every
 * edit via {@link #commitChange()}. Panels that already have their own TitledPane should include
 * {@code json-code-editor.fxml} directly instead of nesting this one.
 */
public class JsonEditorController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private JsonCodeEditorController codeEditorController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    codeEditorController.setOnCommit(this::commitChange);
    codeEditorController.errorProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        hideError();
      } else {
        showError("ERROR", newValue);
      }
    });
  }

  /**
   * Overrides this panel's title and expanded-state settings key, for a reuse other than the default "%json"
   * (see {@link RuleEditorController#configureCustom} for the same pattern).
   */
  public void configureCustom(@NonNull String fieldKey, @NonNull String title) {
    setTitle(title);
    setSettingsKeySuffix("." + fieldKey);
  }

  /** Shows or hides this whole panel - see {@link RuleEditorController#setVisible}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /** See {@link JsonCodeEditorController#isFocused()}. */
  public boolean isFocused() {
    return codeEditorController.isFocused();
  }

  /** See {@link JsonCodeEditorController#setValidator}. */
  public void setValidator(@NonNull Function<String, String> validator) {
    codeEditorController.setValidator(validator);
  }

  /** See {@link JsonCodeEditorController#setCustom}. */
  public void setCustom(@NonNull Supplier<String> reader, @NonNull Consumer<String> writer) {
    codeEditorController.setCustom(reader, writer);
  }

  @Override
  public void destroy() {
    super.destroy();
    codeEditorController.destroy();
  }
}
