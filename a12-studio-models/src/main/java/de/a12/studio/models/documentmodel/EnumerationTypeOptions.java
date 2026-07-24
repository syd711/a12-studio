package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class EnumerationTypeOptions {

  private List<EnumerationValue> values = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Category> categories = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean alphabeticalSorting;
}
