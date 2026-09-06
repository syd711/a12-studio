package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ScreenElementType {

  SECTION("Section"),
  MULTI_COLUMN_SECTION("MultiColumnSection"),
  CONTROL_GRID("ControlGrid"),
  CUSTOM_SCREEN_ELEMENT("CustomScreenElement"),
  BUTTON_PANEL("ButtonPanel"),
  INLINE_REPEAT("InlineRepeat"),
  EMBEDDED_REPEAT("EmbeddedRepeat"),
  DETACHED_REPEAT("DetachedRepeat"),
  // Falls back to this instead of throwing so unrecognized/future screen element types (deserialized as
  // GenericScreenElement, see ScreenElement's defaultImpl) still load instead of failing the whole document.
  OTHER("Other");

  private final String value;

  ScreenElementType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static ScreenElementType fromValue(String value) {
    for (ScreenElementType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return OTHER;
  }
}
