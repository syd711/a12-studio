package de.a12.studio.models.relationshipmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.NullNode;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Multiplicity {

  private Boolean unbounded;

  // Some files write "upperLimit": null for unbounded roles while others omit the key. Backed by a JsonNode
  // (same trick as RelationshipModelContent.linkDocumentModel): absent stays Java null and is skipped,
  // an explicit null is a NullNode and is written back as null.
  @JsonProperty("upperLimit")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode upperLimitNode;

  @JsonIgnore
  public Integer getUpperLimit() {
    return upperLimitNode == null || upperLimitNode.isNull() ? null : upperLimitNode.asInt();
  }

  // Clearing from the editor writes an explicit null, as saves did before the absent case was preserved.
  @JsonIgnore
  public void setUpperLimit(Integer upperLimit) {
    upperLimitNode = upperLimit == null ? NullNode.getInstance() : IntNode.valueOf(upperLimit);
  }
}
