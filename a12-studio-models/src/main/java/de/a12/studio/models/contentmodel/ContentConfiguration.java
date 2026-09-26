package de.a12.studio.models.contentmodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ContentConfiguration {

  // The id of a group of the bound Document Model that the whole model is relative to; only meaningful (and, like
  // in SME's export, only kept) while the model is bound to a Document Model - see ContentModel#setDocumentModelId.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String baseGroupId;

  private Map<String, String> namespaceVersions = new LinkedHashMap<>();

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
