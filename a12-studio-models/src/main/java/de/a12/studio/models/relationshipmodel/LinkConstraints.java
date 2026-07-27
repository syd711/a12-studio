package de.a12.studio.models.relationshipmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class LinkConstraints {

  private Multiplicity multiplicity;
}
