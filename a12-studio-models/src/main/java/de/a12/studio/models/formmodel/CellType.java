package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CellType {

  CONTROL("Control"),
  TEXT_CELL("TextCell"),
  EXPRESSION_CELL("ExpressionCell"),
  CUSTOM_CELL("CustomCell"),
  // Falls back to this instead of throwing so unrecognized/future cell types (deserialized as
  // GenericCell, see Cell's defaultImpl) still load instead of failing the whole document.
  OTHER("Other");

  private final String value;

  CellType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static CellType fromValue(String value) {
    for (CellType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return OTHER;
  }
}
