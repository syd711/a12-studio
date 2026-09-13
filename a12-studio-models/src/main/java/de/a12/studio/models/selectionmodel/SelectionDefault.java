package de.a12.studio.models.selectionmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Whether a {@link SelectionCategory} selects or excludes, by default, every element of the reference
 * Document Model not otherwise matched by a {@link PathSpecification} in its {@code Selected}/{@code
 * Unselected} list. Mirrors SME's {@code SelectionContent.Default} ({@code "Selected" | "Unselected"}).
 */
public enum SelectionDefault {

  SELECTED("Selected"),
  UNSELECTED("Unselected");

  private final String value;

  SelectionDefault(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static SelectionDefault fromValue(String value) {
    for (SelectionDefault selectionDefault : values()) {
      if (selectionDefault.value.equals(value)) {
        return selectionDefault;
      }
    }
    throw new IllegalArgumentException("Unknown selection default: " + value);
  }
}
