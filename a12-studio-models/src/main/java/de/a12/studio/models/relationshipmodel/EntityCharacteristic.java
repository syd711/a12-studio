package de.a12.studio.models.relationshipmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class EntityCharacteristic {

  private Boolean ordered;
  private LinkConstraints linkConstraints;
  private List<Label> labels = new ArrayList<>();
  private String role;
  private String documentModel;
}
