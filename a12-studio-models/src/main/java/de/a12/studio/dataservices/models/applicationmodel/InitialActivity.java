package de.a12.studio.dataservices.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class InitialActivity {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, String> descriptor = new LinkedHashMap<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean withoutData;
}
