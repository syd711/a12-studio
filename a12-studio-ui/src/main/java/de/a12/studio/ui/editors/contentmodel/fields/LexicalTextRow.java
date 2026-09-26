package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.models.contentmodel.LexicalText;
import de.a12.studio.ui.editors.contentmodel.ContentReferences;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
      int offset = caretChosen ? input.getCaretPosition() : input.getLength();
      if (LexicalText.insertReference(props.getElement(), offset, choice.id(), choice.label(), group)) {
        show(props);
        caretChosen = true;
        input.positionCaret(offset + LexicalText.referenceLabel(choice.label(), group).length());
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

  @Override
  protected void load(@NonNull ContentProps props) {
    ContentElement element = props.getElement();
    boolean wantsTall = !"Heading".equals(element.getType());
    if (wantsTall != tall) {
      install(wantsTall);
    }
    boolean editable = LexicalText.isEditable(element);
    input.setEditable(editable);
    notEditableHint.setVisible(!editable);
    notEditableHint.setManaged(!editable);
    input.setText(LexicalText.getText(element));
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
