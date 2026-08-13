package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code newFilterConfiguration.joinOperator}: the "Match" setting (All/Any) shown in the Custom Filter
 * Configuration editor's Filter Result group, paired with its own "User Access" toggle. See {@code
 * testing/basic/models/Company_OM.json}'s {@code "joinOperator": {"enabled": false}} - {@code value} is absent
 * there because the model never overrides the engine's default ("all").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class JoinOperatorConfig {

  public static final String MATCH_ALL = "all";
  public static final String MATCH_ANY = "any";

  private Boolean enabled;
  private String value;
}
