package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, defaultImpl = GenericButton.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = NavigationButton.class, name = "NAVIGATION"),
    @JsonSubTypes.Type(value = EventButton.class, name = "EVENT")
})
@Getter
@Setter
public abstract class Button {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private ButtonType type;
  private String id;
  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ButtonStyling buttonStyling;
  // Validation mode triggered on a button press: e.g. "partial" (current screen) or "full" (entire document).
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String validation;
  // Visibility/enablement depending on the form's readonly state, e.g. "HIDDEN_IN_READONLY_MODE".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String scope;
}
