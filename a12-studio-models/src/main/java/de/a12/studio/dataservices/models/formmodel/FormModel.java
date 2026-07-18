package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.a12.studio.dataservices.models.A12Model;
import lombok.Getter;
import lombok.Setter;

// "header" is a synthetic getter/setter property on the abstract A12Model superclass while "content" is
// a real field on this subclass, so without an explicit order Jackson does not reliably put header first.
@JsonPropertyOrder({"header", "content"})
@Getter
@Setter
public class FormModel extends A12Model {

  @JsonProperty("content")
  private FormModelContent content;
}
