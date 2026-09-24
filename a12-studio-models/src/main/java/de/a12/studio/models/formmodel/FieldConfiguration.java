package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FieldConfiguration {

  // Some files omit "field" while others write it as "[]"; the explicit flag preserves that distinction
  // across a load/save cycle (same approach as A12Model labels/locales).
  @JsonIgnore
  private List<FieldConfigEntry> field = new ArrayList<>();

  @JsonIgnore
  private boolean fieldExplicit;

  @JsonProperty("field")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<FieldConfigEntry> getFieldForJson() {
    return fieldExplicit || !field.isEmpty() ? field : null;
  }

  @JsonProperty("field")
  private void setFieldForJson(List<FieldConfigEntry> value) {
    fieldExplicit = value != null;
    field = value != null ? value : new ArrayList<>();
  }
}
