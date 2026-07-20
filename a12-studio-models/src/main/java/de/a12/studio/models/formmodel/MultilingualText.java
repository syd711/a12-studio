package de.a12.studio.models.formmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultilingualText extends LocalizedText {

  private TextContainer multilingualText;

  public MultilingualText() {
    setType(LocalizedTextType.MULTILINGUAL);
  }
}
