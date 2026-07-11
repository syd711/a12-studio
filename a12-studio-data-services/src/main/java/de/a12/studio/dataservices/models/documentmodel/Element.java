package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.a12.studio.dataservices.models.Annotation;
import de.a12.studio.dataservices.models.Label;
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

  private String type;
  private String id;
  private String name;
  private List<Annotation> annotations = new ArrayList<>();
  private List<Label> externalDescription = new ArrayList<>();
  private List<Label> internalDescription = new ArrayList<>();
}
