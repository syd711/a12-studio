package de.a12.studio.models.formmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpressionText extends LocalizedText {

  private String expressionText;

  public ExpressionText() {
    setType(LocalizedTextType.EXPRESSION);
  }
}
