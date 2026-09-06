package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.ConditionLanguage;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Edits {@link ModelConfig#getDecimalSeparator()} and {@link ModelConfig#getConditionLanguage()} - both
 * previously unexposed anywhere in a12-studio-ui (confirmed by grep before this panel was added). Not bound to
 * a single {@link de.a12.studio.models.documentmodel.Element} (both live on the model's own {@link ModelConfig}),
 * same as {@link TimezonePanelController} - {@code setElement} is never called, only {@link #setModel}.
 */
public class ModelConfigPanelController extends AbstractPropertyEditor implements Initializable {

  // Every fixture in this repo uses "." - offered as a closed choice (period/comma) rather than free text,
  // since a decimal separator genuinely only has two sensible values.
  private static final List<String> DECIMAL_SEPARATORS = List.of(".", ",");

  // Every fixture in this repo uses "en_US"; "de_DE" is the documented alternate locale format (see the Hint
  // List example in documentation/2606-06-doc/data_services-dataservices-documentation-src.md). Offered as
  // presets but editable, since the full valid set isn't confirmable from the documentation available here.
  private static final List<String> CONDITION_LANGUAGES = List.of("en_US", "de_DE");

  @FXML
  private ComboBox<String> decimalSeparatorCombo;

  @FXML
  private ComboBox<String> conditionLanguageCombo;

  private DocumentModel model;

  // Set while setModel() is repopulating the combo boxes from the model, so the valueProperty listeners below
  // don't mistake that programmatic change for a user edit and write it straight back - same guard as
  // TimezonePanelController.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    decimalSeparatorCombo.getItems().setAll(DECIMAL_SEPARATORS);
    decimalSeparatorCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        applyChange(config -> config.setDecimalSeparator(newValue == null || newValue.isEmpty() ? null : newValue));
      }
    });

    conditionLanguageCombo.setEditable(true);
    conditionLanguageCombo.getItems().setAll(CONDITION_LANGUAGES);
    conditionLanguageCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        applyChange(config -> config.setConditionLanguage(newValue == null || newValue.isBlank() ? null : newConditionLanguage(newValue)));
      }
    });
  }

  /** Hides this panel entirely for model types that have no {@link ModelConfig} concept (only a
   * {@link DocumentModel} is ever passed to {@link #setModel}). */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull DocumentModel model) {
    this.model = model;
    ModelConfig modelConfig = getModelConfig(model);
    updatingFromModel = true;
    try {
      decimalSeparatorCombo.setValue(modelConfig != null ? modelConfig.getDecimalSeparator() : null);
      conditionLanguageCombo.setValue(modelConfig != null && modelConfig.getConditionLanguage() != null
          ? modelConfig.getConditionLanguage().getCode() : null);
    } finally {
      updatingFromModel = false;
    }
  }

  private void applyChange(Consumer<ModelConfig> mutator) {
    if (model == null) {
      return;
    }
    ModelConfig modelConfig = getModelConfig(model);
    if (modelConfig == null) {
      return;
    }
    mutator.accept(modelConfig);
    commitChange();
  }

  private static ConditionLanguage newConditionLanguage(String code) {
    ConditionLanguage conditionLanguage = new ConditionLanguage();
    conditionLanguage.setCode(code);
    return conditionLanguage;
  }

  private static ModelConfig getModelConfig(DocumentModel model) {
    DocumentModelContent content = model.getContent();
    return content != null ? content.getModelConfig() : null;
  }
}
