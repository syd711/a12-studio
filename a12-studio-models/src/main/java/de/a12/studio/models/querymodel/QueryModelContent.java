package de.a12.studio.models.querymodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

// The underlying query engine's field/projection format is a separate, more complex schema (dataservices
// query graph) not vendored into this project; individual fields are kept as raw JsonNode so a load/save
// round-trip never loses data, until that shape is modeled explicitly.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class QueryModelContent {

  private String projectionName;
  private String targetDocumentModel;
  private List<JsonNode> fields = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private QueryPaging paging;
}
