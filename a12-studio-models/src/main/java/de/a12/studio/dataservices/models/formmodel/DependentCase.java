package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DependentCase {

  // The value of the master field this case applies to; null is a meaningful, distinct case (the master
  // field itself being unset/empty), so this must not be omitted from serialization when null.
  private String masterValue;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean notRelevant;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean readonly;
}
