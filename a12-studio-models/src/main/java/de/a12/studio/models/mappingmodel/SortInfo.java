package de.a12.studio.models.mappingmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class SortInfo {

  @JsonProperty("SortFields")
  private List<SortField> sortFields = new ArrayList<>();
}
