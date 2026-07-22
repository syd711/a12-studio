package de.a12.studio.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
// EXISTING_PROPERTY (rather than the default PROPERTY) serializes "type" as a normal bean property in its
// declared/ordered position instead of always forcing it to be the first key of the object.
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY,
    visible = true, defaultImpl = GenericDirective.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RegionClearDirective.class, name = "REGION_CLEAR"),
    @JsonSubTypes.Type(value = ViewAddDirective.class, name = "VIEW_ADD")
})
@Getter
@Setter
public abstract class Directive {

  private DirectiveType type;
  // The region this directive applies to; when omitted, the default region defined on the application's
  // top-level region applies instead.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> region = new ArrayList<>();
}
