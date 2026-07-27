package de.a12.studio.models.relationshipmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.StringNode;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RelationshipModelContent {

  private Boolean duplicatesAllowed;

  // Some files write an explicit "linkDocumentModel": null while others omit the key entirely; a
  // JsonNode keeps that distinction across a load/save cycle (absent = Java null, explicit = NullNode).
  @JsonProperty("linkDocumentModel")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode linkDocumentModel;

  private List<Label> labels = new ArrayList<>();

  private List<EntityCharacteristic> entityCharacteristics = new ArrayList<>();

  @JsonIgnore
  public String getLinkDocumentModelValue() {
    if (linkDocumentModel == null || linkDocumentModel.isNull()) {
      return null;
    }
    return linkDocumentModel.asString(null);
  }

  @JsonIgnore
  public void setLinkDocumentModelValue(String value) {
    linkDocumentModel = value == null ? NullNode.getInstance() : StringNode.valueOf(value);
  }
}
