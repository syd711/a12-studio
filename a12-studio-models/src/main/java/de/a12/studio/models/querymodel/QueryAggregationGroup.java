package de.a12.studio.models.querymodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One entry of {@link QueryAggregation#getGroup()}: a field of the target Document Model the result is grouped by. */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class QueryAggregationGroup {

  private String field;

  public QueryAggregationGroup(String field) {
    this.field = field;
  }
}
