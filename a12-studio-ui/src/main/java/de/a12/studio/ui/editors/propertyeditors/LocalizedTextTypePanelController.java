package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.ExpressionText;
import de.a12.studio.models.formmodel.LocalizedText;
import de.a12.studio.models.formmodel.LocalizedTextType;
import de.a12.studio.models.formmodel.MultilingualText;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Edits a single {@link LocalizedText} value that is either per-locale text ({@link MultilingualText}, the
 * default) or an {@link ExpressionText}, switched via a Type combo box: {@link #textController} edits the
 * per-locale case, {@link #expressionController} the expression case, and only the one matching the current
 * type is shown at a time. Not bound to a single {@link de.a12.studio.models.documentmodel.Element} - the
 * value is read/written via a caller-supplied {@code Supplier}/{@code Consumer} pair (see {@link #setCustom}),
 * e.g. {@link de.a12.studio.models.formmodel.ButtonStyling#getLabel()}/{@code setLabel} (a button's label, via
 * {@link de.a12.studio.ui.editors.formmodel.dialogs.FormButtonDialogController}), {@link
 * de.a12.studio.models.formmodel.Row#getTitle()}/{@code setTitle} and {@link
 * de.a12.studio.models.formmodel.Screen#getTitle()}/{@code setTitle} (form tree node editors), and {@link
 * de.a12.studio.models.formmodel.FormModelContent#getSubtitle()}/{@code setSubtitle} (via {@link
 * de.a12.studio.ui.editors.formmodel.modelsettings.SubtitlePanelController}).
 */
public class LocalizedTextTypePanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> typeCombo;

  @FXML
  private LocalizedTextPanelController textController;

  @FXML
  private RichtextEditorController expressionController;

  private Supplier<LocalizedText> reader;

  private Consumer<LocalizedText> writer;

  // Set while typeCombo is being repopulated from the model (setCustom(), on every rebind), so that
  // programmatic set doesn't get mistaken for a user edit and eagerly create a value the user never touched.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    typeCombo.setItems(FXCollections.observableArrayList(
        LocalizedTextType.MULTILINGUAL.getValue(), LocalizedTextType.EXPRESSION.getValue()));
    typeCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return LocalizedTextType.EXPRESSION.getValue().equals(value) ? "Expression" : "Text";
      }

      @Override
      public String fromString(String displayName) {
        return "Expression".equals(displayName) ? LocalizedTextType.EXPRESSION.getValue() : LocalizedTextType.MULTILINGUAL.getValue();
      }
    });
    typeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        applyType(newValue);
      }
    });
  }

  /**
   * Overrides this panel's title and expanded-state settings key, and clears the nested {@link
   * #textController}/{@link #expressionController}'s own titles (this panel's own title already names the
   * field) - mirrors {@link LocalizedTextPanelController#configureCustom}.
   */
  public void configureCustom(@NonNull String fieldKey, @NonNull String title) {
    setTitle(title);
    setSettingsKeySuffix("." + fieldKey);
    textController.configureCustom(fieldKey, "");
    expressionController.configureCustom(fieldKey, "");
  }

  @Override
  public void setSaveMode(@NonNull PropertyEditorSaveMode saveMode) {
    super.setSaveMode(saveMode);
    textController.setSaveMode(saveMode);
    expressionController.setSaveMode(saveMode);
  }

  @Override
  public void destroy() {
    super.destroy();
    textController.destroy();
    expressionController.destroy();
  }

  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /**
   * Binds this panel to a {@link LocalizedText} value, read/written via {@code reader}/{@code writer} (e.g.
   * {@code buttonStyling::getLabel}/{@code buttonStyling::setLabel}). {@code reader} must be safe to call
   * before the user has typed anything - it repopulates the fields, including right away as this method runs
   * - mirroring {@link LocalizedTextPanelController#setCustom(Supplier, Supplier)}; {@code writer} is only
   * ever invoked once the user actually switches the type or edits a field, so it's the one allowed to lazily
   * materialize a parent object the read path found absent.
   */
  public void setCustom(@NonNull Supplier<LocalizedText> reader, @NonNull Consumer<LocalizedText> writer) {
    this.reader = reader;
    this.writer = writer;

    boolean expression = reader.get() instanceof ExpressionText;
    updatingFromModel = true;
    try {
      typeCombo.setValue(expression ? LocalizedTextType.EXPRESSION.getValue() : LocalizedTextType.MULTILINGUAL.getValue());
    }
    finally {
      updatingFromModel = false;
    }
    textController.setCustom(this::currentTexts, this::writeTexts);
    expressionController.setCustom(this::currentExpression, this::writeExpression);
    updateVisibility(expression);
  }

  private void applyType(String value) {
    boolean expression = LocalizedTextType.EXPRESSION.getValue().equals(value);
    if (expression) {
      if (!(reader.get() instanceof ExpressionText)) {
        writer.accept(new ExpressionText());
      }
    }
    else {
      getOrCreateMultilingualText();
    }
    updateVisibility(expression);
  }

  private void updateVisibility(boolean expression) {
    textController.setVisible(!expression);
    expressionController.setVisible(expression);
  }

  private List<Label> currentTexts() {
    LocalizedText value = reader.get();
    if (value instanceof MultilingualText multilingualText && multilingualText.getMultilingualText() != null) {
      return multilingualText.getMultilingualText().getText();
    }
    return List.of();
  }

  private List<Label> writeTexts() {
    return getOrCreateMultilingualText().getMultilingualText().getText();
  }

  private String currentExpression() {
    LocalizedText value = reader.get();
    return value instanceof ExpressionText expressionText ? expressionText.getExpressionText() : null;
  }

  private void writeExpression(String value) {
    LocalizedText current = reader.get();
    ExpressionText expressionText;
    if (current instanceof ExpressionText existing) {
      expressionText = existing;
    }
    else {
      expressionText = new ExpressionText();
      writer.accept(expressionText);
    }
    expressionText.setExpressionText(value);
  }

  private MultilingualText getOrCreateMultilingualText() {
    LocalizedText current = reader.get();
    MultilingualText multilingualText;
    if (current instanceof MultilingualText existing) {
      multilingualText = existing;
    }
    else {
      multilingualText = new MultilingualText();
      writer.accept(multilingualText);
    }
    if (multilingualText.getMultilingualText() == null) {
      multilingualText.setMultilingualText(new TextContainer());
    }
    return multilingualText;
  }
}
