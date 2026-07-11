package de.a12.studio.ui.editors.propertyeditors;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LocalizedText {

  private final StringProperty locale;

  private final StringProperty text;

  public LocalizedText(String locale, String text) {
    this.locale = new SimpleStringProperty(locale);
    this.text = new SimpleStringProperty(text);
  }

  public StringProperty localeProperty() {
    return locale;
  }

  public StringProperty textProperty() {
    return text;
  }

  public String getText() {
    return text.get();
  }

  public void setText(String value) {
    text.set(value);
  }
}
