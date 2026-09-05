package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Edits a single expression string via a {@link CodeArea} (RichTextFX), giving expression text a monospaced
 * editor instead of a plain {@link javafx.scene.control.TextArea}. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element} - the expression is read/written via a caller-supplied {@code
 * Supplier}/{@code Consumer} pair (see {@link #setCustom}), e.g. {@link
 * de.a12.studio.models.overviewmodel.Column#getExpression()} (used by {@link
 * de.a12.studio.ui.editors.overviewmodel.dialogs.OverviewColumnDialogController}) or {@link
 * de.a12.studio.models.formmodel.ExpressionText#getExpressionText()} (used by {@link
 * de.a12.studio.ui.editors.formmodel.dialogs.FormButtonDialogController} for a button's expression-typed label).
 */
public class RichtextEditorController extends AbstractPropertyEditor implements Initializable {

  // Quoted string literals in the a12 expression language, e.g. "* * *" in Invoice_OM.json's ExpressionColumn.
  private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");

  @FXML
  private StackPane editorContainer;

  private final CodeArea codeArea = new CodeArea();

  private Consumer<String> writer;

  // Optional; maps the current text to an error message (or null if valid), e.g. a query-language grammar
  // check. Left unset for plain expression fields with no dedicated grammar to validate against.
  private Function<String, String> validator;

  // Set while setCustom() is repopulating codeArea from the model, so the listener below doesn't mistake that
  // programmatic change for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    codeArea.getStyleClass().add("expression-code-area");
    codeArea.setWrapText(true);
    editorContainer.getChildren().add(new VirtualizedScrollPane<>(codeArea));

    codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      writer.accept(blankToNull(newValue));
      validate(newValue);
      commitChange();
    });
    codeArea.textProperty().addListener((observable, oldValue, newValue) ->
        codeArea.setStyleSpans(0, computeHighlighting(newValue)));
  }

  /**
   * Overrides this panel's title and expanded-state settings key, for a reuse other than the default
   * "%expression" (see {@link de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController#configureCustom}
   * for the same pattern).
   */
  public void configureCustom(@NonNull String fieldKey, @NonNull String title) {
    setTitle(title);
    setSettingsKeySuffix("." + fieldKey);
  }

  /** Shows or hides this whole panel, e.g. when it's an alternate editor for a field shown only for one of
   * several types the owner can switch between (see {@link
   * de.a12.studio.ui.editors.formmodel.dialogs.FormButtonDialogController}'s label Type combo). */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /**
   * Validates the text on every change (and once immediately in {@link #setCustom}), showing {@code validator}'s
   * message in this panel's error container when non-null. Must be called before {@link #setCustom} so the
   * initial value is validated too.
   */
  public void setValidator(@NonNull Function<String, String> validator) {
    this.validator = validator;
  }

  private static StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = STRING_PATTERN.matcher(text);
    StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
    int lastEnd = 0;
    while (matcher.find()) {
      builder.add(Collections.emptyList(), matcher.start() - lastEnd);
      builder.add(Collections.singleton("string"), matcher.end() - matcher.start());
      lastEnd = matcher.end();
    }
    builder.add(Collections.emptyList(), text.length() - lastEnd);
    return builder.create();
  }

  /**
   * Binds this panel directly to a caller-supplied expression string, read/written via {@code reader}/{@code
   * writer} (e.g. {@code column::getExpression}/{@code column::setExpression}). {@code reader} repopulates the
   * editor, including right away as this method runs, so it must be safe to call before the user has typed
   * anything.
   */
  public void setCustom(@NonNull Supplier<String> reader, @NonNull Consumer<String> writer) {
    this.writer = writer;
    updatingFromModel = true;
    try {
      String value = reader.get();
      codeArea.replaceText(value != null ? value : "");
    }
    finally {
      updatingFromModel = false;
    }
    validate(codeArea.getText());
  }

  private void validate(String text) {
    if (validator == null) {
      return;
    }
    String error = blankToNull(text) == null ? null : validator.apply(text);
    if (error != null) {
      showError("ERROR", error);
    } else {
      hideError();
    }
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
