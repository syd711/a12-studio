package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, defaultImpl = GenericElement.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = GroupElement.class, name = "Group"),
    @JsonSubTypes.Type(value = FieldElement.class, name = "Field"),
    @JsonSubTypes.Type(value = RuleElement.class, name = "Rule"),
    @JsonSubTypes.Type(value = ComputationElement.class, name = "Computation")
})
@Getter
@Setter
public abstract class Element {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private ElementType type;
  private String id;
  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Annotation> annotations = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> externalDescription = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> internalDescription = new ArrayList<>();
}
