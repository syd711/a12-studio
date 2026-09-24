package de.a12.studio.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Annotation {

  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String value;
}
