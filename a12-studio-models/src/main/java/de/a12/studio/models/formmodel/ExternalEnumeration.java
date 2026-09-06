package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// Sources a field's enumeration options from an external URL instead of the Document Model's own enum
// definition.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ExternalEnumeration {

  private String src;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean customValuesAllowed;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean caseSensitive;
}
