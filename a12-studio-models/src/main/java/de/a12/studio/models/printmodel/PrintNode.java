package de.a12.studio.models.printmodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

// Shared base for all print model DTOs: every node in a print model carries a generated "id", and
// unknown keys must survive a load/save cycle because the print schema is owned by the print engine.
@Getter
@Setter
public abstract class PrintNode {

  private String id;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }
}
