package de.a12.studio.models.transformermodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code content.Cmd}: the transformer's command line parameters (the doc's "Configuration Parameters"). SME's
 * Transformation tab edits {@link #mainXsd} and {@link #rootElement} only; the rest is carried through.
 * {@link #genDocModelName} is a legacy parameter the 2026.06 transformer no longer supports (the generated Document
 * Model takes the Transformer Model's id) - kept so an older file is not silently altered, never written for a new one.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class TransformerCmd {

  private String xsdDir;
  private String outputDir;
  private String mainXsd;
  private Boolean allowRemoteXsd;
  private String clXmlsDir;
  private String rootElement;
  private Boolean minimal;
  private Boolean externalTypeDefs;
  private String roles;
  private Boolean skipConsistencyCheck;
  private String genDocModelName;

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
