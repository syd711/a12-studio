package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RichText extends PrintNode {

  // HTML fragment; inline <span entity-id="..." entity-type="Field|Calculation"> spans embed
  // Field/Calculation elements referenced through the entities list below.
  private String text;
  private List<EntityRef> entities = new ArrayList<>();
}
