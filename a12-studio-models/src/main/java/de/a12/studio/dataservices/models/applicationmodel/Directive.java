package de.a12.studio.dataservices.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, defaultImpl = GenericDirective.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RegionClearDirective.class, name = "REGION_CLEAR"),
    @JsonSubTypes.Type(value = ViewAddDirective.class, name = "VIEW_ADD")
})
@Getter
@Setter
public abstract class Directive {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private DirectiveType type;
  // The region this directive applies to; when omitted, the default region defined on the application's
  // top-level region applies instead.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> region = new ArrayList<>();
}
