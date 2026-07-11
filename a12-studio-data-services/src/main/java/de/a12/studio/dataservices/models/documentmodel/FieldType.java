package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, defaultImpl = GenericFieldType.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = StringFieldType.class, name = "StringType"),
    @JsonSubTypes.Type(value = NumberFieldType.class, name = "NumberType"),
    @JsonSubTypes.Type(value = EnumerationFieldType.class, name = "EnumerationType")
})
@Getter
@Setter
public abstract class FieldType {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String type;
}
