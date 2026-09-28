package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.util.StudioBundle;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chrome-less JSON editor: a {@link CodeArea} (RichTextFX) with JSON syntax highlighting, debounced writes and
 * JSON validation, but no {@link javafx.scene.control.TitledPane} and no error container of its own. The current
 * validation error is exposed via {@link #errorProperty()} so the embedding panel shows it in its own error
 * container. {@link JsonEditorController} wraps this in the standard property-editor TitledPane for callers that
 * want a standalone panel; panels that already provide their own TitledPane (e.g. the Content Model editor's raw
 * props panel) include {@code json-code-editor.fxml} directly.
 */
public class JsonCodeEditorController implements Initializable {

  // Matches both keys and values - which one a given match is gets decided by isKey() below, based on whether
  // it's followed by a colon.
  private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");

  private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?");

  private static final Pattern LITERAL_PATTERN = Pattern.compile("\\b(true|false|null)\\b");

  // Coalesces the writer.accept()/validate()/onCommit triggered by every keystroke into one, fired this long
  // after the user stops typing - see RuleEditorController#SAVE_DEBOUNCE for why.
  private static final Duration SAVE_DEBOUNCE = Duration.millis(100);

  @FXML
  private StackPane editorContainer;

  private final CodeArea codeArea = new CodeArea();

  private final PauseTransition saveDebounce = new PauseTransition(SAVE_DEBOUNCE);

  private final ReadOnlyStringWrapper error = new ReadOnlyStringWrapper();

  private Consumer<String> writer;

  // Optional; run after each written edit, e.g. to persist it. Callers that persist on their own (via the
  // writer) leave it unset.
  private Runnable onCommit = () -> {
  };

  // Optional; checked only once the text has already parsed as valid JSON, for a caller-specific constraint
  // on top of plain well-formedness (e.g. "must be a JSON object").
  private Function<String, String> validator;

  // Set while setCustom() is repopulating codeArea from the model, so the listener below doesn't mistake that
  // programmatic change for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    codeArea.getStyleClass().add("json-code-area");
    codeArea.setWrapText(true);
    codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
    editorContainer.getChildren().add(new VirtualizedScrollPane<>(codeArea));

    saveDebounce.setOnFinished(event -> commitEdit());
    codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      saveDebounce.playFromStart();
    });
    codeArea.textProperty().addListener((observable, oldValue, newValue) ->
        codeArea.setStyleSpans(0, computeHighlighting(newValue)));
  }

  /** The current validation error message, or {@code null} while the text is valid (or blank). */
  public ReadOnlyStringProperty errorProperty() {
    return error.getReadOnlyProperty();
  }

  public void setOnCommit(@NonNull Runnable onCommit) {
    this.onCommit = onCommit;
  }

  /**
   * Checks an additional caller-specific constraint on top of the always-on JSON well-formedness check, e.g.
   * "must be a JSON object". Only invoked once the text has already parsed successfully; a syntax error always
   * wins.
   */
  public void setValidator(@NonNull Function<String, String> validator) {
    this.validator = validator;
  }

  /**
   * Whether this editor currently has keyboard focus, so a caller that repopulates it from elsewhere can skip
   * doing so while the user is in the middle of typing here.
   */
  public boolean isFocused() {
    return codeArea.isFocused();
  }

  /**
   * Binds this editor to a caller-supplied JSON string, read/written via {@code reader}/{@code writer} - see
   * {@link RuleEditorController#setCustom} for the same pattern.
   */
  public void setCustom(@NonNull Supplier<String> reader, @NonNull Consumer<String> writer) {
    // Flushed against the *old* writer/text, before it's replaced below - otherwise a debounce left pending
    // from a previous binding would fire later against the new writer with stale, no-longer-relevant text.
    flushPendingEdit();
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

  /** Flushes any still-debounced edit so the last keystrokes aren't lost, once the embedding panel is torn down. */
  public void destroy() {
    flushPendingEdit();
  }

  /** Writes the current text, validates it and notifies {@link #onCommit} - see {@link RuleEditorController#commitEdit()}. */
  private void commitEdit() {
    String text = codeArea.getText();
    writer.accept(blankToNull(text));
    validate(text);
    onCommit.run();
  }

  /** Runs a still-pending debounced {@link #commitEdit()} immediately - see {@link RuleEditorController#flushPendingEdit()}. */
  private void flushPendingEdit() {
    if (saveDebounce.getStatus() == Animation.Status.RUNNING) {
      saveDebounce.stop();
      commitEdit();
    }
  }

  private void validate(String text) {
    String trimmed = blankToNull(text);
    if (trimmed == null) {
      error.set(null);
      return;
    }
    try {
      JsonSettings.objectMapper.readTree(trimmed);
    }
    catch (RuntimeException e) {
      error.set(StudioBundle.get("json_editor.invalid_json", e.getMessage()));
      return;
    }
    error.set(validator == null ? null : validator.apply(text));
  }

  private StyleSpans<Collection<String>> computeHighlighting(String text) {
    List<HighlightSpan> spans = new ArrayList<>();
    Matcher stringMatcher = STRING_PATTERN.matcher(text);
    while (stringMatcher.find()) {
      String styleClass = isKey(text, stringMatcher.end()) ? "key" : "string";
      spans.add(new HighlightSpan(stringMatcher.start(), stringMatcher.end(), styleClass));
    }
    addNonOverlapping(spans, NUMBER_PATTERN.matcher(text), "number");
    addNonOverlapping(spans, LITERAL_PATTERN.matcher(text), "literal");
    spans.sort(Comparator.comparingInt(HighlightSpan::start));

    StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
    int lastEnd = 0;
    for (HighlightSpan span : spans) {
      builder.add(Collections.emptyList(), span.start() - lastEnd);
      builder.add(Collections.singleton(span.styleClass()), span.end() - span.start());
      lastEnd = span.end();
    }
    builder.add(Collections.emptyList(), text.length() - lastEnd);
    return builder.create();
  }

  // A number/literal can't legally overlap a string, but guard anyway so two spans covering the same
  // characters never reach StyleSpansBuilder#add, which requires strictly increasing offsets.
  private static void addNonOverlapping(List<HighlightSpan> spans, Matcher matcher, String styleClass) {
    while (matcher.find()) {
      if (spans.stream().noneMatch(span -> span.overlaps(matcher.start(), matcher.end()))) {
        spans.add(new HighlightSpan(matcher.start(), matcher.end(), styleClass));
      }
    }
  }

  /** Whether the string ending at {@code index} is an object key, i.e. followed by a colon (skipping whitespace). */
  private static boolean isKey(String text, int index) {
    int i = index;
    while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
      i++;
    }
    return i < text.length() && text.charAt(i) == ':';
  }

  private record HighlightSpan(int start, int end, String styleClass) {
    boolean overlaps(int otherStart, int otherEnd) {
      return start < otherEnd && otherStart < end;
    }
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
