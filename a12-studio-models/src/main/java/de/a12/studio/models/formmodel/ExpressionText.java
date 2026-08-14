package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpressionText extends LocalizedText {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String expressionText;

  public ExpressionText() {
    setType(LocalizedTextType.EXPRESSION);
  }
}
