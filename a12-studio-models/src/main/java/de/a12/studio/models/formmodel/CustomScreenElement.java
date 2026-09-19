package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// A placeholder for a custom UI component registered by the application embedding this form; the name
// identifies which custom component to render. Also used to embed a Relationship UI Model component
// (reference names the Ru model, e.g. "PersonSkills_Skills_Ru" - see e.g. PersonEmployee_Fm.json).
@Getter
@Setter
public class CustomScreenElement extends ScreenElement {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String reference;
  // Fixed pixel height for the embedded custom component (e.g. a Relationship UI Model's own scrollable
  // area). Zero is not allowed (SME: NumberType zeroNotAllowed), see FormCustomScreenElementHeightValidator.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer height;

  public CustomScreenElement() {
    setType(ScreenElementType.CUSTOM_SCREEN_ELEMENT);
  }
}
