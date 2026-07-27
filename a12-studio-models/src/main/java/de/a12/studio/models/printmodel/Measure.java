package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Measure extends PrintNode {

  // BigDecimal keeps the on-disk notation: integral values ("74") stay integral instead of
  // becoming "74.0" as they would with a double.
  private BigDecimal value;
  private String unit;
}
