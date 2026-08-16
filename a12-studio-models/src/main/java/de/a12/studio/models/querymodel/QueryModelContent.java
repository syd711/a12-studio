package de.a12.studio.models.querymodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class QueryModelContent {

  private String projectionName;
  private String targetDocumentModel;
  private List<String> fields = new ArrayList<>();

  // The projection's DM-level filter expression, edited via RichtextEditorController (no query-engine grammar
  // is available in a12-studio to parse/validate a structured constraint, unlike SME's QueryElementDocument.
  // DocumentModel.constraint).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String filterDefinition;

  private List<QuerySort> sort = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private QueryPaging paging;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean aggregateResults;
}
