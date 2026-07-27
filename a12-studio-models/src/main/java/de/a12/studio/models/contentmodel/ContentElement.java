package de.a12.studio.models.contentmodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// "type" stays a plain String on purpose: content engine element libraries can contribute arbitrary
// component types, and unknown ones must survive a load/save cycle unchanged.
@Getter
@Setter
public class ContentElement {

  private String id;
  private String type;
  private String namespace;

  // Opaque by design: props carries content-engine payloads such as the Lexical "tree"/"html" pair,
  // whose structure the studio must never reinterpret or normalize.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, Object> props;

  // null = key absent in the file, empty list = explicit "children": []; both forms exist on disk.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<ContentElement> children;

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
