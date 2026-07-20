package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoxElementType {

  SEARCH("search"),
  FILTER("filter"),
  BUTTON("button"),
  MULTI_SELECTION("multi_selection"),
  // Falls back to this instead of throwing so unrecognized/future box element types (deserialized as
  // GenericBoxElement, see BoxElement's defaultImpl) still load instead of failing the whole model.
  OTHER("other");

  private final String value;

  BoxElementType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static BoxElementType fromValue(String value) {
    for (BoxElementType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return OTHER;
  }
}
