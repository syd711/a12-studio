package de.a12.studio.models.relationshipmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Multiplicity {

  private Boolean unbounded;
  // Written even when null: the files keep "upperLimit": null for unbounded roles.
  private Integer upperLimit;
}
