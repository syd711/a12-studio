package de.a12.studio.models.transformermodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code TypeMapping.NumberType}: how the generated Number field is configured. Ranges are SME's: decimal places
 * {@code MIN_MAX_FRACTIONAL_DIGITS_1} (integers 0..14), lengths {@code MIN_MAX_LENGTH_1} (1..99999), min/max value
 * {@code MIN_MAX_VALUE_1} (at most 14 decimal places), integer places a positive non-zero integer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class NumberTypeConfig {

  public static final int MIN_FRACTIONAL_DIGITS = 0;
  public static final int MAX_FRACTIONAL_DIGITS = 14;
  public static final int MIN_LENGTH = 1;
  public static final int MAX_LENGTH = 99999;

  /** SME's {@code trait} enumeration. */
  public static final String TRAIT_AMOUNT = "Amount";
  public static final String TRAIT_PERCENT = "Percent";
  public static final String TRAIT_PERMILLE = "Permille";

  private Integer minFractionalDigits;
  private Integer maxFractionalDigits;
  private BigDecimal minValue;
  private BigDecimal maxValue;
  private Boolean leadingZerosAllowed;
  private Boolean positivesOnly;
  private Integer minLength;
  private Integer maxLength;
  private String trait;
  private Integer maxIntegerDigits;
  private Boolean zeroNotAllowed;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }

  /** SME's {@code GroupFilled}: whether any value is set. */
  @JsonIgnore
  public boolean isEmpty() {
    return minFractionalDigits == null && maxFractionalDigits == null && minValue == null && maxValue == null
        && leadingZerosAllowed == null && positivesOnly == null && minLength == null && maxLength == null
        && trait == null && maxIntegerDigits == null && zeroNotAllowed == null && extras.isEmpty();
  }
}
