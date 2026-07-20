package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Section extends ScreenElement {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean collapsible;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean initiallyCollapsed;
  private List<ScreenElement> screenElements = new ArrayList<>();

  public Section() {
    setType(ScreenElementType.SECTION);
  }
}
