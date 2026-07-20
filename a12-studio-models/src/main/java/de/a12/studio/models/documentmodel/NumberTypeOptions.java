package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class NumberTypeOptions {

  private Integer minFractionalDigits;
  private Integer maxFractionalDigits;
  private Double minValue;
  private Double maxValue;
  private String trait;
  private Boolean zeroNotAllowed;
}
