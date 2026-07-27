package de.a12.studio.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Label {

  private String locale;
  // Locale entries without a translation omit the key entirely on disk ({"locale": "de"}).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String text;
}
