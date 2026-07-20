package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MultiColumnSection extends ScreenElement {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ColumnLayout layout;
  private List<ScreenElement> screenElements = new ArrayList<>();

  public MultiColumnSection() {
    setType(ScreenElementType.MULTI_COLUMN_SECTION);
  }
}
