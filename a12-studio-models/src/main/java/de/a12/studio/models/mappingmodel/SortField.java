package de.a12.studio.models.mappingmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class SortField {

  private String sortFieldFullName;

  // "Ascending" or "Descending".
  private String sortType;
}
