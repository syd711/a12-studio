package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Edits {@link ModelConfig#getSupportedCharacters()}. Not bound to a single {@link Element}
 * (supported characters live on the model's {@link ModelConfig}), so {@link #setElement} is never called and
 * only {@link #setModel} is used.
 */
public class SupportedCharactersPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private static final String INVALID_JSON_MESSAGE = "Please enter a valid JSON array of strings, e.g. [\"A\", \"B\"].";

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private TextField supportedCharactersField;

  private A12Model<?> model;

  // Set while setModel() is repopulating the field from the model, so the valueProperty listener below
  // does not mistake that programmatic change for a user edit and write it straight back.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    supportedCharactersField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        commitCharactersChange(newValue);
      }
    });
  }

  /**
   * Hides this panel entirely for model types that have no supported-characters concept, e.g. an
   * {@code ApplicationModel} (only {@link ModelConfig#getSupportedCharacters()} backs this panel).
   */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull A12Model<?> model) {
    this.model = model;
    ModelConfig modelConfig = getModelConfig(model);
    updatingFromModel = true;
    try {
      supportedCharactersField.setText(modelConfig != null ? toText(modelConfig.getSupportedCharacters()) : "");
    } finally {
      updatingFromModel = false;
    }
    hideError();
  }

  private void commitCharactersChange(String text) {
    if (model == null) {
      return;
    }

    ModelConfig modelConfig = getModelConfig(model);
    if (modelConfig == null) {
      return;
    }

    List<String> parsed = parseText(text);
    if (parsed == null) {
      showError("ERROR", INVALID_JSON_MESSAGE);
      return;
    }
    hideError();
    modelConfig.setSupportedCharacters(parsed);

    debouncer.debounce(getClass().getSimpleName(), this::commitChange, COMMIT_DEBOUNCE_MS, true);
  }

  private static String toText(List<String> supportedCharacters) {
    if (supportedCharacters == null || supportedCharacters.isEmpty()) {
      return "[]";
    }
    return supportedCharacters.stream()
        .map(character -> JsonSettings.objectMapper.writeValueAsString(character))
        .collect(Collectors.joining(", ", "[", "]"));
  }

  private static List<String> parseText(String text) {
    if (text == null || text.isBlank()) {
      return new ArrayList<>();
    }
    try {
      List<?> raw = JsonSettings.objectMapper.readValue(text, List.class);
      List<String> result = new ArrayList<>();
      for (Object entry : raw) {
        if (!(entry instanceof String value)) {
          return null;
        }
        result.add(value);
      }
      return result;
    } catch (RuntimeException e) {
      return null;
    }
  }
}
