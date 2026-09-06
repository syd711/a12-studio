package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// Constrains which values a field's Enumeration may offer, based on a master field's own value - distinct
// from DependentConfig/DependentCase, which only affects visibility/readonly, not the set of allowed values.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DependentEnumeration {

  private String masterField;
  // Named "constraints" in Java for clarity; @JsonProperty maps it back to singular "constraint" on disk,
  // matching the kernel wire format (confirmed against a real SME fixture).
  @JsonProperty("constraint")
  private List<DependentEnumerationConstraint> constraints = new ArrayList<>();
}
