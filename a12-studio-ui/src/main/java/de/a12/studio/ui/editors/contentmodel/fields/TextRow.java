package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A free text setting (Label, Title, URL, Message, ...). Writing an empty text removes the key. {@code multiline}
 * gives a text area, e.g. for the Message Box message. Edits are reported on every change; the owner debounces the
 * save. Two URL flavors mirror SME: {@code secureUrl} refuses URLs with script-like schemes (the value stays as it
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

  private TextInputControl input = new TextField();
  private boolean multiline;
  private boolean secureUrl;
  private boolean cssUrl;
  private String prompt;

  public TextRow() {
    install();
  }

  public boolean isMultiline() {
    return multiline;
  }

  public void setMultiline(boolean multiline) {
    if (this.multiline != multiline) {
      this.multiline = multiline;
      install();
    }
  }

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
    input.setPromptText(prompt);
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

  private void install() {
    controls().getChildren().clear();
    input = multiline ? new TextArea() : new TextField();
    if (input instanceof TextArea area) {
      area.setPrefRowCount(3);
      area.setWrapText(true);
    }
    input.setPromptText(prompt);
    HBox.setHgrow(input, Priority.ALWAYS);
    controls().getChildren().add(input);
    input.textProperty().addListener((observable, oldValue, text) -> onTyped(text == null ? "" : text));
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
