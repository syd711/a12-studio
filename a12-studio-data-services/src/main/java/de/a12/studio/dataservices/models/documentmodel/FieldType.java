package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

  private String type;
}
