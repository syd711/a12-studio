package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A free text setting (Label, Title, URL, Message, ...), always edited in a text area and shown without a label
 * column (the label text becomes the prompt unless one is set). Writing an empty text removes the key. Edits are
 * reported on every change; the owner debounces the save. Two URL flavors mirror SME: {@code secureUrl} refuses URLs with script-like schemes (the value stays as it
 * was and the field is marked invalid), and {@code cssUrl} stores the text as {@code url('...')}, the form of a CSS
 * background image.
 */
public class TextRow extends SettingRow {

  private static final Pattern CSS_URL = Pattern.compile("^url\\('([^']*)'\\)$");
  // The scheme allow-list DOMPurify applies to href/src attributes: known safe schemes, or no scheme at all.
  private static final Pattern SAFE_URL = Pattern.compile(
      "^(?:(?:(?:f|ht)tps?|mailto|tel|callto|sms|cid|xmpp|matrix):|[^a-z]|[a-z+.\\-]+(?:[^a-z+.\\-:]|$))",
      Pattern.CASE_INSENSITIVE);
  private static final String INVALID_STYLE = "content-setting-invalid";

  private final TextArea input = new TextArea();
  private boolean secureUrl;
  private boolean cssUrl;
  private String prompt;
  private String labelText;

  public TextRow() {
    input.setPrefRowCount(3);
    input.setWrapText(true);
    HBox.setHgrow(input, Priority.ALWAYS);
    controls().getChildren().add(input);
    input.textProperty().addListener((observable, oldValue, text) -> onTyped(text == null ? "" : text));
  }

  /** A text row shows no label column; the label only serves as the prompt when none is set. */
  @Override
  public void setLabel(String label) {
    labelText = label;
    super.setLabel(null);
    updatePrompt();
  }

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
    updatePrompt();
  }

  private void updatePrompt() {
    // SettingRow's constructor calls setLabel before this class's fields are initialized.
    if (input == null) {
      return;
    }
    input.setPromptText(prompt != null && !prompt.isBlank() ? prompt : labelText);
  }

  public boolean isSecureUrl() {
    return secureUrl;
  }

  public void setSecureUrl(boolean secureUrl) {
    this.secureUrl = secureUrl;
  }

  public boolean isCssUrl() {
    return cssUrl;
  }

  public void setCssUrl(boolean cssUrl) {
    this.cssUrl = cssUrl;
  }

  private void onTyped(String text) {
    input.getStyleClass().remove(INVALID_STYLE);
    if (secureUrl && !text.isEmpty() && !isSafeUrl(text)) {
      input.getStyleClass().add(INVALID_STYLE);
      return;
    }
    edited(props -> props.set(getPath(), text.isEmpty() ? null : (cssUrl ? "url('" + text + "')" : text)));
  }

  /** Whether {@code url} passes SME's URL check: only known safe schemes, or a relative URL. */
  static boolean isSafeUrl(String url) {
    return SAFE_URL.matcher(url.strip()).find();
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    String value = props.getString(getPath());
    if (value != null && cssUrl) {
      Matcher matcher = CSS_URL.matcher(value);
      value = matcher.matches() ? matcher.group(1) : value;
    }
    input.getStyleClass().remove(INVALID_STYLE);
    input.setText(value != null ? value : "");
  }
}
