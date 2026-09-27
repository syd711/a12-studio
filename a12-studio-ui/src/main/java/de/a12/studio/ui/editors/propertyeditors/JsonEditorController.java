package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
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
 * Edits a single JSON string via a {@link CodeArea} (RichTextFX), giving JSON text a monospaced editor with
 * syntax highlighting instead of a plain {@link javafx.scene.control.TextArea}. Same read/write/debounce/error
 * shape as {@link RuleEditorController}, but highlights JSON's own lexical elements (keys, string values,
 * numbers, {@code true}/{@code false}/{@code null}) instead of the a12 expression language, and always
 * validates the text as JSON (via {@link JsonSettings#objectMapper}) rather than leaving validation entirely
 * to a caller-supplied {@link #setValidator}.
 */
public class JsonEditorController extends AbstractPropertyEditor implements Initializable {

  // Matches both keys and values - which one a given match is gets decided by isKey() below, based on whether
  // it's followed by a colon.
  private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");

  private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?");

  private static final Pattern LITERAL_PATTERN = Pattern.compile("\\b(true|false|null)\\b");

  // Coalesces the writer.accept()/validate()/commitChange() triggered by every keystroke into one, fired this
  // long after the user stops typing - see RuleEditorController#SAVE_DEBOUNCE for why.
  private static final Duration SAVE_DEBOUNCE = Duration.millis(100);

  @FXML
  private StackPane editorContainer;

  private final CodeArea codeArea = new CodeArea();

  private final PauseTransition saveDebounce = new PauseTransition(SAVE_DEBOUNCE);

  private Consumer<String> writer;

  // Optional; checked only once the text has already parsed as valid JSON, for a caller-specific constraint
  // on top of plain well-formedness (e.g. "must be a JSON object"). Left unset for callers with no such
  // constraint.
  private Function<String, String> validator;

  // Set while setCustom() is repopulating codeArea from the model, so the listener below doesn't mistake that
  // programmatic change for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

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

  /** Writes the current text, validates it and persists the change - see {@link RuleEditorController#commitEdit()}. */
  private void commitEdit() {
    String text = codeArea.getText();
    writer.accept(blankToNull(text));
    validate(text);
    commitChange();
  }

  /** Runs a still-pending debounced {@link #commitEdit()} immediately - see {@link RuleEditorController#flushPendingEdit()}. */
  private void flushPendingEdit() {
    if (saveDebounce.getStatus() == Animation.Status.RUNNING) {
      saveDebounce.stop();
      commitEdit();
    }
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

  /**
   * Whether this editor currently has keyboard focus, so a caller that repopulates this panel from elsewhere
   * (e.g. another panel editing the same underlying data) can skip doing so while the user is in the middle
   * of typing here.
   */
  public boolean isFocused() {
    return codeArea.isFocused();
  }

  /** Flushes any still-debounced edit so the last keystrokes aren't lost, once this panel is torn down. */
  @Override
  public void destroy() {
    super.destroy();
    flushPendingEdit();
  }

  /**
   * Checks an additional caller-specific constraint on top of this editor's always-on JSON well-formedness
   * check, e.g. "must be a JSON object" - see {@link #validator}. Only invoked once the text has already
   * parsed successfully; a syntax error always wins.
   */
  public void setValidator(@NonNull Function<String, String> validator) {
    this.validator = validator;
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

  /**
   * Binds this panel directly to a caller-supplied JSON string, read/written via {@code reader}/{@code writer}
   * - see {@link RuleEditorController#setCustom} for the same pattern.
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

  private void validate(String text) {
    String trimmed = blankToNull(text);
    if (trimmed == null) {
      hideError();
      return;
    }
    try {
      JsonSettings.objectMapper.readTree(trimmed);
    }
    catch (RuntimeException e) {
      showError("ERROR", StudioBundle.get("json_editor.invalid_json", e.getMessage()));
      return;
    }
    String error = validator == null ? null : validator.apply(text);
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
