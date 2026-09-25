package de.a12.studio.models.typesettingmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * One entry of {@link TypesettingModelContent#getPreventLineBreakRules()}: a regular expression the print
 * engine keeps together on one line. The three rule kinds the editor offers (character sequence, number unit,
 * special pattern) are all encoded in {@link #pattern} - see {@link PreventLineBreakRules} for how they are told
 * apart and converted. {@code preventLineBreakBefore}/{@code preventLineBreakAfter} exist on the print
 * engine's {@code PreventLineBreakRuleDto} but SME's editor never writes them; they are kept only so a file that
 * has them survives a load/save cycle.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"preventLineBreakBefore", "preventLineBreakAfter", "pattern"})
@Getter
@Setter
public class PreventLineBreakRule {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean preventLineBreakBefore;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean preventLineBreakAfter;

  private String pattern;

  public PreventLineBreakRule() {
  }

  public PreventLineBreakRule(String pattern) {
    this.pattern = pattern;
  }
}
