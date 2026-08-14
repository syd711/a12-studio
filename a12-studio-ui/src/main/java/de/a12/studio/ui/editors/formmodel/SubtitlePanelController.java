package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.ExpressionText;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.LocalizedText;
import de.a12.studio.models.formmodel.LocalizedTextType;
import de.a12.studio.models.formmodel.MultilingualText;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Edits {@link FormModelContent#getSubtitle()} (SME's {@code FormModelFrame-form.json} {@code section-093cb},
 * "Subtitle"): a {@link LocalizedText} that is either per-locale text ({@link MultilingualText}, the default)
 * or an {@link ExpressionText}. The nested {@link LocalizedTextPanelController} only edits the per-locale
 * case; switching the Type combo box to "Expression" hides it, mirroring {@link
 * GeneralSettingsPanelController}'s Amount Suffix type toggle.
 */
public class SubtitlePanelController extends AbstractPropertyEditor implements Initializable {

  private static final Map<String, String> TYPE_LABELS = new LinkedHashMap<>();
  static {
    TYPE_LABELS.put(LocalizedTextType.MULTILINGUAL.getValue(), "Text");
    TYPE_LABELS.put(LocalizedTextType.EXPRESSION.getValue(), "Expression");
  }

  @FXML
  private ComboBox<String> typeCombo;

  @FXML
  private LocalizedTextPanelController subtitleTextController;

  private FormModel model;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    subtitleTextController.configureCustom("subtitle", "");

    typeCombo.getItems().addAll(TYPE_LABELS.keySet());
    typeCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null ? "" : TYPE_LABELS.getOrDefault(value, value);
      }

      @Override
      public String fromString(String displayName) {
        return TYPE_LABELS.entrySet().stream()
            .filter(entry -> entry.getValue().equals(displayName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(LocalizedTextType.MULTILINGUAL.getValue());
      }
    });

    bindComboBox(typeCombo, (element, value) -> applyType(value));
  }

  @Override
  public void setSaveMode(@NonNull PropertyEditorSaveMode saveMode) {
    super.setSaveMode(saveMode);
    subtitleTextController.setSaveMode(saveMode);
  }

  /** Hides this panel entirely for model types other than {@link FormModel}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull FormModel model) {
    this.model = model;
    boolean expression = getContent().getSubtitle() instanceof ExpressionText;
    setFieldValue(typeCombo,
        expression ? LocalizedTextType.EXPRESSION.getValue() : LocalizedTextType.MULTILINGUAL.getValue());
    subtitleTextController.setCustom(this::currentSubtitleTexts, this::writeSubtitleTexts);
    updateVisibility(expression);
  }

  private void applyType(String value) {
    boolean expression = LocalizedTextType.EXPRESSION.getValue().equals(value);
    if (expression) {
      if (!(getContent().getSubtitle() instanceof ExpressionText)) {
        getContent().setSubtitle(new ExpressionText());
      }
    } else {
      getOrCreateMultilingualText();
    }
    updateVisibility(expression);
  }

  private void updateVisibility(boolean expression) {
    subtitleTextController.setVisible(!expression);
  }

  private List<Label> currentSubtitleTexts() {
    LocalizedText subtitle = getContent().getSubtitle();
    if (subtitle instanceof MultilingualText multilingualText && multilingualText.getMultilingualText() != null) {
      return multilingualText.getMultilingualText().getText();
    }
    return List.of();
  }

  private List<Label> writeSubtitleTexts() {
    return getOrCreateMultilingualText().getMultilingualText().getText();
  }

  private MultilingualText getOrCreateMultilingualText() {
    LocalizedText subtitle = getContent().getSubtitle();
    MultilingualText multilingualText;
    if (subtitle instanceof MultilingualText existing) {
      multilingualText = existing;
    } else {
      multilingualText = new MultilingualText();
      getContent().setSubtitle(multilingualText);
    }
    if (multilingualText.getMultilingualText() == null) {
      multilingualText.setMultilingualText(new TextContainer());
    }
    return multilingualText;
  }

  private FormModelContent getContent() {
    FormModelContent content = model.getContent();
    if (content == null) {
      content = new FormModelContent();
      model.setContent(content);
    }
    return content;
  }
}
