package de.a12.studio.dataservices.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DirectiveType {

  REGION_CLEAR("REGION_CLEAR"),
  VIEW_ADD("VIEW_ADD"),
  // Falls back to this instead of throwing so unrecognized/future directive types (deserialized as
  // GenericDirective, see Directive's defaultImpl) still load instead of failing the whole document.
  OTHER("OTHER");

  private final String value;

  DirectiveType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static DirectiveType fromValue(String value) {
    for (DirectiveType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return OTHER;
  }
}
