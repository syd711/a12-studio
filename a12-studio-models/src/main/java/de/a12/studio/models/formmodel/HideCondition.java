package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// Hides the owning form node when the referenced master field's value matches any of the listed cases.
// Unlike a single (field, value) pair, this supports multiple trigger values - needed for an Enumeration
// master field, which can have more than one value that should hide the node.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class HideCondition {

  private String masterField;
  private List<HideConditionCase> cases = new ArrayList<>();
}
