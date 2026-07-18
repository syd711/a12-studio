package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FieldConfiguration {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<FieldConfigEntry> field = new ArrayList<>();
}
