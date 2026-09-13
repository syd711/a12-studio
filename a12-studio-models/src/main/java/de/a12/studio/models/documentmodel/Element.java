package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
// annotations' method-backed (rather than field-backed) property otherwise gets pushed to the end of the
// property order by Jackson's default introspection regardless of declaration order, so pin it explicitly.
@JsonPropertyOrder({"type", "id", "name", "annotations", "externalDescription", "internalDescription"})
public abstract class Element {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private ElementType type;
  private String id;
  private String name;

  // Some files omit "annotations" entirely while others write it explicitly as "[]"; annotationsExplicit
  // preserves that distinction across a load/save cycle instead of always omitting (or always writing) an
  // empty array.
  @JsonIgnore
  private List<Annotation> annotations = new ArrayList<>();
  @JsonIgnore
  private boolean annotationsExplicit;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> externalDescription = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> internalDescription = new ArrayList<>();

  @JsonProperty("annotations")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<Annotation> getAnnotationsForJson() {
    return annotationsExplicit || !annotations.isEmpty() ? annotations : null;
  }

  @JsonProperty("annotations")
  private void setAnnotationsForJson(List<Annotation> value) {
    this.annotationsExplicit = value != null;
    this.annotations = value != null ? value : new ArrayList<>();
  }
}
