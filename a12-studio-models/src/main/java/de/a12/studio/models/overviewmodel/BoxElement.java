package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, defaultImpl = GenericBoxElement.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SearchElement.class, name = "search"),
    @JsonSubTypes.Type(value = FilterElement.class, name = "filter"),
    @JsonSubTypes.Type(value = ButtonElement.class, name = "button"),
    @JsonSubTypes.Type(value = MultiSelectionElement.class, name = "multi_selection")
})
@Getter
@Setter
public abstract class BoxElement {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private BoxElementType type;
}
