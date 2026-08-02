package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ColumnStyles {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> header = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> content = new ArrayList<>();
}
