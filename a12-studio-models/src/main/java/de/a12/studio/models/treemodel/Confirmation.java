package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Confirmation {

  private List<Label> title = new ArrayList<>();
  private List<Label> message = new ArrayList<>();
}
