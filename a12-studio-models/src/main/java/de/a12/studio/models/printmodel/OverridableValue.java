package de.a12.studio.models.printmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// The print engine's DEFAULT/INPUT indirection: source DEFAULT means "value" is absent and the
// engine resolves it via "path"; source INPUT carries an explicit "value".
@Getter
@Setter
public class OverridableValue extends PrintNode {

  private String source;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Object value;

  private String path;
}
