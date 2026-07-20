package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, defaultImpl = GenericScreenElement.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Section.class, name = "Section"),
    @JsonSubTypes.Type(value = MultiColumnSection.class, name = "MultiColumnSection"),
    @JsonSubTypes.Type(value = ControlGrid.class, name = "ControlGrid"),
    @JsonSubTypes.Type(value = CustomScreenElement.class, name = "CustomScreenElement"),
    @JsonSubTypes.Type(value = InlineRepeat.class, name = "InlineRepeat"),
    @JsonSubTypes.Type(value = EmbeddedRepeat.class, name = "EmbeddedRepeat"),
    @JsonSubTypes.Type(value = DetachedRepeat.class, name = "DetachedRepeat")
})
@Getter
@Setter
public abstract class ScreenElement {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private ScreenElementType type;
  private String id;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText title;
}
