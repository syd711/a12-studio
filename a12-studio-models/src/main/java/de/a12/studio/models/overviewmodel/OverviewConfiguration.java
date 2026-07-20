package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OverviewConfiguration {

  private Boolean enableFilter;
  private Boolean showFullTextSearch;
  private Integer pagingSize;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean showRowCount;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private FilterConfiguration filterConfiguration;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private MultiSelectionConfig multiSelection;
}
