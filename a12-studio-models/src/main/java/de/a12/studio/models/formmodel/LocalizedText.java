package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, defaultImpl = GenericLocalizedText.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = MultilingualText.class, name = "Multilingual"),
    @JsonSubTypes.Type(value = ExpressionText.class, name = "Expression")
})
@Getter
@Setter
public abstract class LocalizedText {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private LocalizedTextType type;
}
