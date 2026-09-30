package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.models.contentmodel.LexicalText;
import de.a12.studio.ui.editors.contentmodel.ContentReferences;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.HTMLEditor;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The words of a Paragraph or Heading, whose {@code props} hold them as a Lexical tree (SME edits that inline on its
 * canvas). Shows the plain text and writes edits back through {@link LexicalText}, which keeps the formatting of
 * the runs around the change. Both get a text area (a new line starts a new paragraph block), a heading a smaller
 * one. A field or group reference inside the text shows as its label ({@code [path]}, {@code IndexOf(path)}) and is
 * one unit, see {@link LexicalText}; two pickers under the text insert one at the caret from what the element may
 * reference at its position, and each field reference gets its display (value only, or the label before the value) and
 * the text to show when the field has no value. Text with links cannot be flattened to plain text without losing them, so it is shown
 * read-only with a hint. Not bound to a {@code path}.
 */
public class LexicalTextRow extends SettingRow {

  /** Extra height, in pixels, of the paragraph text area over a plain 4-row {@link TextArea}. */
  private static final double EXTRA_HEIGHT = 100;

  /** A text area that is {@link #EXTRA_HEIGHT} taller than the 4 rows its {@code prefRowCount} asks for. */
  private static class TallTextArea extends TextArea {
    @Override
    protected double computePrefHeight(double width) {
      return super.computePrefHeight(width) + EXTRA_HEIGHT;
    }
  }

  private final Label notEditableHint = new Label(StudioBundle.get("content_settings.text_not_editable"));

  private final ComboBox<ContentReferences.Choice> insertField = referencePicker("content_settings.text_insert_field",
      "content_settings.text_insert_field_hint", false);
  private final ComboBox<ContentReferences.Choice> insertGroup = referencePicker("content_settings.text_insert_group",
      "content_settings.text_insert_group_hint", true);

  private final VBox referenceOptions = new VBox(6);

  private TextArea input;
  // Shown instead of the text area for a heading without references: the heading as it will look, edited in place.
  private HTMLEditor htmlEditor;
  private boolean loading;
  private String prompt;
  private boolean tall;
  // Whether the caret of the text area is a position the user chose (it was focused since the text was shown).
  private boolean caretChosen;

  public LexicalTextRow() {
    notEditableHint.getStyleClass().add("content-setting-hint");
    notEditableHint.setWrapText(true);
    notEditableHint.setManaged(false);
    notEditableHint.setVisible(false);
    addBelow(notEditableHint);
    HBox insertLine = new HBox(6, insertField, insertGroup);
    insertLine.setAlignment(Pos.CENTER_LEFT);
    addBelow(insertLine);
    addBelow(referenceOptions);
    install(true);
  }

  /** A picker that inserts what is chosen at the caret and is ready for the next choice right away. */
  private ComboBox<ContentReferences.Choice> referencePicker(String promptKey, String hintKey, boolean group) {
    ComboBox<ContentReferences.Choice> picker = new ComboBox<>();
    picker.setPromptText(StudioBundle.get(promptKey));
    picker.setTooltip(WidgetFactory.createTooltip(StudioBundle.get(hintKey)));
    picker.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(picker, Priority.ALWAYS);
    picker.setConverter(new StringConverter<>() {
      @Override
      public String toString(ContentReferences.Choice choice) {
        return choice == null ? "" : choice.label();
      }

      @Override
      public ContentReferences.Choice fromString(String string) {
        return null;
      }
    });
    picker.valueProperty().addListener((observable, oldValue, choice) -> {
      if (choice != null) {
        insert(choice, group);
        picker.setValue(null);
      }
    });
    return picker;
  }

  private void insert(ContentReferences.Choice choice, boolean group) {
    edited(props -> {
      int offset = htmlEditor != null || !caretChosen ? LexicalText.getText(props.getElement()).length() : input.getCaretPosition();
      if (LexicalText.insertReference(props.getElement(), offset, choice.id(), choice.label(), group)) {
        show(props);
        if (htmlEditor == null) {
          caretChosen = true;
          input.positionCaret(offset + LexicalText.referenceLabel(choice.label(), group).length());
        }
      }
    });
  }

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
    input.setPromptText(prompt);
  }

  private void install(boolean tall) {
    this.tall = tall;
    htmlEditor = null;
    controls().getChildren().clear();
    input = tall ? new TallTextArea() : new TextArea();
    input.setPrefRowCount(tall ? 4 : 2);
    input.setWrapText(true);
    input.setPromptText(prompt);
    HBox.setHgrow(input, Priority.ALWAYS);
    controls().getChildren().add(input);
    input.textProperty().addListener((observable, oldValue, text) ->
        edited(props -> {
          LexicalText.setText(props.getElement(), text == null ? "" : text);
          showReferenceOptions(props.getElement());
        }));
    input.focusedProperty().addListener((observable, wasFocused, focused) -> caretChosen |= focused);
  }

  /**
   * Swaps between the text area and, for a heading without references, an {@link HTMLEditor} that shows the heading
   * as it will look ({@code html}). {@link LexicalText} rewrites {@code html} from the {@code tree}, so only the words
   * are edited there: its formatting toolbars are hidden, as a change of formatting would not reach the {@code tree}.
   */
  private void useEditor(boolean rich) {
    if (rich == (htmlEditor != null)) {
      return;
    }
    if (!rich) {
      install(!"Heading".equals(currentType));
      return;
    }
    controls().getChildren().clear();
    HTMLEditor editor = new HTMLEditor();
    editor.setPrefHeight(160);
    HBox.setHgrow(editor, Priority.ALWAYS);
    editor.skinProperty().addListener((observable, oldSkin, skin) -> Platform.runLater(() ->
        editor.lookupAll(".tool-bar").forEach(bar -> {
          bar.setVisible(false);
          bar.setManaged(false);
        })));
    // HTMLEditor has no change notification: the words are read back when a key is released or focus leaves.
    editor.addEventFilter(KeyEvent.KEY_RELEASED, event -> commitEditor());
    editor.focusWithinProperty().addListener((observable, wasFocused, focused) -> {
      if (!focused) {
        commitEditor();
      }
    });
    htmlEditor = editor;
    controls().getChildren().add(editor);
  }

  private String currentType = "";

  private void commitEditor() {
    if (loading || htmlEditor == null) {
      return;
    }
    String text = plainText(htmlEditor.getHtmlText());
    edited(props -> {
      LexicalText.setText(props.getElement(), text);
      showReferenceOptions(props.getElement());
    });
  }

  /** The words of an {@link HTMLEditor} document; every block or line break starts a new line. */
  static String plainText(String html) {
    String body = html;
    int start = body.indexOf("<body");
    if (start >= 0) {
      body = body.substring(body.indexOf('>', start) + 1);
    }
    int end = body.indexOf("</body>");
    if (end >= 0) {
      body = body.substring(0, end);
    }
    return body.replaceAll("(?i)<br\s*/?>|</(p|div|h[1-6])>", "\n").replaceAll("<[^>]*>", "")
        .replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&amp;", "&")
        .replaceAll("\n+$", "");
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    ContentElement element = props.getElement();
    currentType = element.getType();
    boolean heading = "Heading".equals(currentType);
    boolean wantsTall = !heading;
    if (wantsTall != tall) {
      install(wantsTall);
    }
    boolean editable = LexicalText.isEditable(element);
    boolean rich = heading && editable && !LexicalText.hasReferences(element);
    useEditor(rich);
    notEditableHint.setVisible(!editable);
    notEditableHint.setManaged(!editable);
    if (htmlEditor != null) {
      loading = true;
      htmlEditor.setHtmlText(element.getProps().get("html") instanceof String html ? html : "");
      loading = false;
    }
    else {
      input.setEditable(editable);
      input.setText(LexicalText.getText(element));
    }
    caretChosen = false;
    loadReferences(element, editable);
    showReferenceOptions(element);
  }

  /** One line per field reference of the text: how it is shown and what shows when the field has no value. */
  private void showReferenceOptions(ContentElement element) {
    referenceOptions.getChildren().clear();
    List<LexicalText.ReferenceOptions> options = LexicalText.fieldReferenceOptions(element);
    for (int i = 0; i < options.size(); i++) {
      int index = i;
      LexicalText.ReferenceOptions option = options.get(i);
      Label name = new Label(option.label());
      name.getStyleClass().add("content-setting-hint");
      ComboBox<String> display = new ComboBox<>();
      display.getItems().setAll(LexicalText.VALUE_ONLY, LexicalText.LABEL_VALUE);
      display.setConverter(new StringConverter<>() {
        @Override
        public String toString(String value) {
          return StudioBundle.get(LexicalText.LABEL_VALUE.equals(value) ? "content_settings.text_ref_label_value"
              : "content_settings.text_ref_value_only");
        }

        @Override
        public String fromString(String string) {
          return string;
        }
      });
      display.setValue(LexicalText.LABEL_VALUE.equals(option.displayOption()) ? LexicalText.LABEL_VALUE : LexicalText.VALUE_ONLY);
      TextField missing = new TextField(option.missingValueText());
      missing.setPromptText(StudioBundle.get("content_settings.text_ref_default_text"));
      HBox.setHgrow(missing, Priority.ALWAYS);
      Runnable write = () -> edited(props -> LexicalText.setReferenceOptions(props.getElement(), index, display.getValue(),
          missing.getText() == null ? "" : missing.getText()));
      display.valueProperty().addListener((observable, oldValue, value) -> write.run());
      missing.textProperty().addListener((observable, oldValue, value) -> write.run());
      referenceOptions.getChildren().add(new VBox(2, name, new HBox(6, display, missing)));
    }
    referenceOptions.setManaged(!options.isEmpty());
    referenceOptions.setVisible(!options.isEmpty());
  }

  private void loadReferences(ContentElement element, boolean editable) {
    ContentReferences references = context() != null ? context().references() : null;
    boolean bound = references != null && references.isBound();
    List<ContentReferences.Choice> fields = bound ? references.fields(element) : List.of();
    List<ContentReferences.Choice> groups = bound ? references.indexGroups(element) : List.of();
    insertField.getItems().setAll(fields);
    insertGroup.getItems().setAll(groups);
    insertField.setDisable(!editable || fields.isEmpty());
    insertGroup.setDisable(!editable || groups.isEmpty());
  }
}
