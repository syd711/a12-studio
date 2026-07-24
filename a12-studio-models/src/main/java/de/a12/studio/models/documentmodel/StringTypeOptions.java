package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class StringTypeOptions {

  private Boolean lineBreaksPermitted;
  private Boolean noValueValidation;
  private Integer minLength;
  private Integer maxLength;
  private String pattern;
  private Boolean alphabeticalSorting;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> errorMessage = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<HintList> hintList = new ArrayList<>();
}
