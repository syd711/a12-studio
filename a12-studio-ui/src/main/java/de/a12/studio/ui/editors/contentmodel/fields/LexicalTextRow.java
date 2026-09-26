package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.models.contentmodel.LexicalText;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NonNull;

/**
 * The words of a Paragraph or Heading, whose {@code props} hold them as a Lexical tree (SME edits that inline on its
 * canvas). Shows the plain text and writes edits back through {@link LexicalText}, which keeps the formatting of
 * the runs around the change. Both get a text area (a new line starts a new paragraph block), a heading a smaller
 * one. Text with links or field references cannot be flattened to plain text without losing them, so it is shown
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

  private TextArea input;
  private String prompt;
  private boolean tall;

  public LexicalTextRow() {
    notEditableHint.getStyleClass().add("content-setting-hint");
    notEditableHint.setWrapText(true);
    notEditableHint.setManaged(false);
    notEditableHint.setVisible(false);
    addBelow(notEditableHint);
    install(true);
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
        edited(props -> LexicalText.setText(props.getElement(), text == null ? "" : text)));
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
  }
}
