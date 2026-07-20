package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// Shared shape for a field's or group's dependency on a "master" field's value; reused for both
// FieldConfigEntry.dependentField and GroupConfigEntry.dependentGroup.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DependentConfig {

  private String masterField;
  // Named "cases" in Java since "case" is a reserved word; @JsonProperty maps it back to "case" on disk.
  @JsonProperty("case")
  private List<DependentCase> cases = new ArrayList<>();
}
