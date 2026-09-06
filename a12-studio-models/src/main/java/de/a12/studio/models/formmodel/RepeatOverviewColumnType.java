package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RepeatOverviewColumnType {

  FIELD_BASED("FieldBasedRepeatOverviewColumn"),
  EXPRESSION_BASED("ExpressionRepeatOverviewColumn"),
  // Falls back to this instead of throwing so unrecognized/future column types (deserialized as
  // GenericRepeatOverviewColumn, see RepeatOverviewColumn's defaultImpl) still load instead of failing
  // the whole document.
  OTHER("Other");

  private final String value;

  RepeatOverviewColumnType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static RepeatOverviewColumnType fromValue(String value) {
    for (RepeatOverviewColumnType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return OTHER;
  }
}
