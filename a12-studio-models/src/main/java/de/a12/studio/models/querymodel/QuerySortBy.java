package de.a12.studio.models.querymodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class QuerySortBy {

  public static final String DIRECTION_ASC = "ASC";
  public static final String DIRECTION_DESC = "DESC";

  public static final String NULLS_FIRST = "NULLS_FIRST";
  public static final String NULLS_LAST = "NULLS_LAST";

  private String field;
  private String direction;
  private String nullHandling;
  private Boolean ignoreCase;
}
