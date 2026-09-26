package de.a12.studio.models.contentmodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ContentModelContent {

  private ContentConfiguration configuration;
  private ContentElement root;

  // Keys of the content this class does not know (SME's schema allows none, but a newer engine version might add
  // some): kept, so a load/save cycle does not silently drop them.
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
