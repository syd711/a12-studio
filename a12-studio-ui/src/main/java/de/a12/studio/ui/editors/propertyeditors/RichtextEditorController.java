package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.jspecify.annotations.NonNull;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

  private static final double COMPLETION_LIST_PREF_HEIGHT = 200.0;
  private static final double CCOMPLETION_LIST_PREF_WIDTH = 360.0;

  // Coalesces the writer.accept()/validate()/commitChange() triggered by every keystroke into one, fired
  // this long after the user stops typing - a CodeArea has no built-in equivalent of a TextField's focus-lost
  // commit point, so without this every single character saved the panel's owner to disk (see AbstractPropertyEditor
  // #commitChange) and re-ran validator, which can be expensive (e.g. FilterItemDialogController's QL grammar
  // check) and was visibly janky while typing quickly.
  private static final Duration SAVE_DEBOUNCE = Duration.millis(100);

  @FXML
  private StackPane editorContainer;

  private final CodeArea codeArea = new CodeArea();

  private final Popup completionPopup = new Popup();

  private final ListView<Suggestion> completionList = new ListView<>();

  private final PauseTransition saveDebounce = new PauseTransition(SAVE_DEBOUNCE);

  private Consumer<String> writer;

  // Optional; maps the current text to an error message (or null if valid), e.g. a query-language grammar
  // check. Left unset for plain expression fields with no dedicated grammar to validate against.
  private Function<String, String> validator;

  // Optional; supplies field/path autocomplete proposals for the current caret position (see Suggestion,
  // SuggestionProvider). Left unset for fields with no known field-tree context to suggest from.
  private SuggestionProvider suggestionProvider;

  // Optional; matches whole-word occurrences of a fixed vocabulary (e.g. RuleLanguageConstructs.NAMES) to
  // highlight as keywords, in addition to computeHighlighting's built-in string-literal highlighting. Left
  // null for languages with no such fixed vocabulary (QL, the Overview/Form Expression language).
  private Pattern functionHighlightPattern;

  // The replace range for whichever CompletionResult completionList is currently showing, so commit() knows
  // what to replace regardless of how the list is currently filtered/scrolled.
  private CompletionResult currentCompletion;

  // Set while setCustom() is repopulating codeArea from the model, so the listener below doesn't mistake that
  // programmatic change for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    codeArea.getStyleClass().add("expression-code-area");
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

    initCompletionPopup();
  }

  /** Writes the current text, validates it and persists the change - the debounced tail end of a burst of
   * keystrokes (see {@link #saveDebounce}), or an immediate flush of one still pending (see {@link
   * #flushPendingEdit}). Always reads {@link CodeArea#getText()} fresh rather than closing over whatever text
   * was current when the debounce was (re)started, so only the latest value is ever written. */
  private void commitEdit() {
    String text = codeArea.getText();
    writer.accept(blankToNull(text));
    validate(text);
    commitChange();
  }

  /** Runs a still-pending debounced {@link #commitEdit()} immediately instead of waiting out {@link
   * #SAVE_DEBOUNCE} - so the last keystrokes before this panel is torn down or rebound to a different value
   * are never silently dropped. No-op if nothing is pending. */
  private void flushPendingEdit() {
    if (saveDebounce.getStatus() == Animation.Status.RUNNING) {
      saveDebounce.stop();
      commitEdit();
    }
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

  /** Flushes any still-debounced edit (so the last keystrokes aren't lost) and hides the completion popup so
   * it doesn't linger detached from its owner, once this panel is torn down. */
  @Override
  public void destroy() {
    super.destroy();
    flushPendingEdit();
    completionPopup.hide();
  }

  /**
   * Validates the text on every change (and once immediately in {@link #setCustom}), showing {@code validator}'s
   * message in this panel's error container when non-null. Must be called before {@link #setCustom} so the
   * initial value is validated too.
   */
  public void setValidator(@NonNull Function<String, String> validator) {
    this.validator = validator;
  }

  /**
   * Enables field/path autocomplete: on every text or caret change, {@code provider} is asked whether to show a
   * completion popup at the caret (see {@link Suggestion}/{@link SuggestionProvider} for why this is a
   * pluggable strategy rather than one fixed trigger - different expression languages trigger and replace
   * differently). Optional; left unset, this editor behaves exactly as before.
   */
  public void setSuggestionProvider(@NonNull SuggestionProvider provider) {
    this.suggestionProvider = provider;
  }

  /**
   * Enables keyword highlighting for a fixed vocabulary of identifiers (e.g. {@link
   * RuleLanguageConstructs#NAMES} for the Rule/Computation condition language's built-in functions like {@code
   * GroupFilled}/{@code RangeAsString}), on top of this editor's always-on string-literal highlighting.
   * Optional; left unset, no identifier is highlighted as a keyword.
   */
  public void setHighlightedFunctionNames(@NonNull Collection<String> names) {
    functionHighlightPattern = names.isEmpty() ? null : Pattern.compile(
        "\\b(" + names.stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")\\b");
    codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
  }

  private void initCompletionPopup() {
    completionList.getStyleClass().add("completion-list");
    completionList.setCellFactory(list -> new SuggestionCell());
    completionList.setPrefHeight(COMPLETION_LIST_PREF_HEIGHT);
    completionList.setPrefWidth(CCOMPLETION_LIST_PREF_WIDTH);
    // A Popup opens its own separate Scene/Window, which does NOT inherit the node-level <stylesheets> this
    // panel's own FXML declares (those only cascade within that FXML's own scene graph) - attach the app
    // stylesheet directly so .completion-list/.completion-label are actually styled.
    completionList.getStylesheets().add(getClass().getResource("/de/a12/studio/ui/stylesheet.css").toExternalForm());
    // Keep keyboard focus on codeArea at all times: a Popup auto-focuses the first focus-traversable child
    // of its content the moment it's shown, and a ListView is focus-traversable by default. Once that
    // happened, every key event (including Enter) went to the ListView's own default handling instead of
    // reaching codeArea's InputMap below - UP/DOWN happened to still move the selection (ListView's own
    // built-in behavior), which made it look like only Enter was broken, but neither was actually going
    // through this controller's own logic.
    completionList.setFocusTraversable(false);
    completionList.setOnMouseClicked(event -> {
      Suggestion selected = completionList.getSelectionModel().getSelectedItem();
      if (selected != null) {
        commitCompletion(selected);
      }
    });
    completionPopup.getContent().add(completionList);
    completionPopup.setAutoHide(true);
    completionPopup.setHideOnEscape(true);

    codeArea.textProperty().addListener((observable, oldValue, newValue) -> updateCompletions());
    codeArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> updateCompletions());
    codeArea.focusedProperty().addListener((observable, oldValue, focused) -> {
      if (!focused) {
        hideCompletions();
      }
    });

    // CodeArea's own Enter/Tab/arrow-key handling is installed (in its constructor, i.e. before this
    // controller ever runs) as a *fallback* Nodes input map - the lowest-priority layer of the same
    // wellbehaved-event dispatch chain, meant precisely to be overridden this way. A plain
    // codeArea.addEventFilter(KEY_PRESSED, ...) is a separate, independent filter that's registered on the
    // node strictly later, so it always lost that race silently (CodeArea's own Enter-inserts-newline binding
    // ran and consumed the event first): the popup's selected suggestion was never actually committed.
    // Nodes.addInputMap composes the new map ahead of whatever's already installed, so these bindings are
    // checked - and can consume - before CodeArea's fallback ones do.
    Nodes.addInputMap(codeArea, InputMap.sequence(
        InputMap.consumeWhen(keyPressed(KeyCode.DOWN), completionPopup::isShowing, event -> selectRelative(1)),
        InputMap.consumeWhen(keyPressed(KeyCode.UP), completionPopup::isShowing, event -> selectRelative(-1)),
        InputMap.consumeWhen(keyPressed(KeyCode.ENTER), completionPopup::isShowing, event -> commitSelected()),
        InputMap.consumeWhen(keyPressed(KeyCode.TAB), completionPopup::isShowing, event -> commitSelected()),
        InputMap.consumeWhen(keyPressed(KeyCode.ESCAPE), completionPopup::isShowing, event -> hideCompletions())
    ));
  }

  private void commitSelected() {
    Suggestion selected = completionList.getSelectionModel().getSelectedItem();
    if (selected != null) {
      commitCompletion(selected);
    }
  }

  private void selectRelative(int delta) {
    int size = completionList.getItems().size();
    if (size == 0) {
      return;
    }
    int current = completionList.getSelectionModel().getSelectedIndex();
    int next = current < 0 ? 0 : Math.floorMod(current + delta, size);
    completionList.getSelectionModel().select(next);
    completionList.scrollTo(next);
  }

  private void updateCompletions() {
    if (updatingFromModel || suggestionProvider == null) {
      hideCompletions();
      return;
    }
    Optional<CompletionResult> result = suggestionProvider.suggest(codeArea.getText(), codeArea.getCaretPosition());
    if (result.isEmpty()) {
      hideCompletions();
      return;
    }
    showCompletions(result.get());
  }

  private void showCompletions(CompletionResult result) {
    currentCompletion = result;
    completionList.getItems().setAll(result.suggestions());
    completionList.getSelectionModel().selectFirst();

    Optional<Bounds> caretBounds = codeArea.caretBoundsProperty().getValue();
    if (caretBounds.isEmpty()) {
      hideCompletions();
      return;
    }
    Bounds bounds = caretBounds.get();
    if (completionPopup.isShowing()) {
      completionPopup.setX(bounds.getMinX());
      completionPopup.setY(bounds.getMaxY());
    }
    else {
      completionPopup.show(codeArea, bounds.getMinX(), bounds.getMaxY());
    }
  }

  private void hideCompletions() {
    currentCompletion = null;
    completionPopup.hide();
  }

  private void commitCompletion(@NonNull Suggestion suggestion) {
    if (currentCompletion == null) {
      return;
    }
    codeArea.replaceText(currentCompletion.replaceStart(), currentCompletion.replaceEnd(), suggestion.insertText());
    hideCompletions();
  }

  /** Single-line row showing just the suggestion's label (no documentation text). */
  private static class SuggestionCell extends ListCell<Suggestion> {

    private final Label labelText = new Label();

    SuggestionCell() {
      labelText.getStyleClass().add("completion-label");
    }

    @Override
    protected void updateItem(Suggestion item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setGraphic(null);
        return;
      }
      labelText.setText(item.label());
      setGraphic(labelText);
    }
  }

  private StyleSpans<Collection<String>> computeHighlighting(String text) {
    List<HighlightSpan> spans = new ArrayList<>();
    Matcher stringMatcher = STRING_PATTERN.matcher(text);
    while (stringMatcher.find()) {
      spans.add(new HighlightSpan(stringMatcher.start(), stringMatcher.end(), "string"));
    }
    if (functionHighlightPattern != null) {
      Matcher functionMatcher = functionHighlightPattern.matcher(text);
      while (functionMatcher.find()) {
        // A function name can't legally overlap a string literal, but guard anyway so two spans covering the
        // same characters never reach StyleSpansBuilder#add, which requires strictly increasing offsets.
        if (spans.stream().noneMatch(span -> span.overlaps(functionMatcher.start(), functionMatcher.end()))) {
          spans.add(new HighlightSpan(functionMatcher.start(), functionMatcher.end(), "keyword"));
        }
      }
    }
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

  private record HighlightSpan(int start, int end, String styleClass) {
    boolean overlaps(int otherStart, int otherEnd) {
      return start < otherEnd && otherStart < end;
    }
  }

  /**
   * Binds this panel directly to a caller-supplied expression string, read/written via {@code reader}/{@code
   * writer} (e.g. {@code column::getExpression}/{@code column::setExpression}). {@code reader} repopulates the
   * editor, including right away as this method runs, so it must be safe to call before the user has typed
   * anything.
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
