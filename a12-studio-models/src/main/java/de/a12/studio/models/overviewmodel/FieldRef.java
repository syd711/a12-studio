package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FieldRef {

  private String fieldId;

  // Only meaningful for FilterConfiguration.fields (the "Custom Selection Of Fields" list, FILTER_MODE_CUSTOM_LIST):
  // for a heterogeneous relationship, the field can be picked from a recursive sub-type of the referenced
  // Document Model instead of the model itself (SME's Subtype column, DocumentModelHeterogeneity.recursiveSubTypes)
  // - fieldId then resolves against that sub-type, not the base model. Absent for every other FieldRef use
  // (enumeratedStringFilter.fields, the filter's sectionData.fields), which have no Subtype concept.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String subModel;
}
