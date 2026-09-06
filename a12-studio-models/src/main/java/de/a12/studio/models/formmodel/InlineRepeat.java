package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// Renders each row's Controls directly inline within the overview table.
@Getter
@Setter
public class InlineRepeat extends AbstractRepeat {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private MultiFileUploadOptions multiFileUploadOptions;

  public InlineRepeat() {
    setType(ScreenElementType.INLINE_REPEAT);
  }
}
