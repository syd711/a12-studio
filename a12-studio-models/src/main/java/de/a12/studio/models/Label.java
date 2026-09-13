package de.a12.studio.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.StringNode;

@Getter
@Setter
public class Label {

  private String locale;

  // An Overview Model filter group's fallback label text when no per-user "technical" override is set - no
  // editor UI yet, mapped purely for lossless round-tripping (see
  // testing/workspaces/advanced_new/models/10_People/Person_Ov.json's newFilterConfiguration.filterGroups).
  @JsonProperty("technical_defaultValue")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String technicalDefaultValue;

  // Some files omit the "text" key entirely (no translation) while others write it explicitly as
  // "text": null; a JsonNode keeps that distinction across a load/save cycle (absent = Java null,
  // explicit = NullNode), mirroring RelationshipModelContent#linkDocumentModel.
  @JsonProperty("text")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode textNode;

  @JsonIgnore
  public String getText() {
    return textNode == null || textNode.isNull() ? null : textNode.asString(null);
  }

  @JsonIgnore
  public void setText(String value) {
    textNode = value == null ? null : StringNode.valueOf(value);
  }
}
