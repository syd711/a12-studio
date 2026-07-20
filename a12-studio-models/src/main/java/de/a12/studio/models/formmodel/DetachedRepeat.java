package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// Opens a row's detail edit form on a separate detail Screen.
@Getter
@Setter
public class DetachedRepeat extends AbstractRepeat {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Screen detailScreen;

  public DetachedRepeat() {
    setType(ScreenElementType.DETACHED_REPEAT);
  }
}
