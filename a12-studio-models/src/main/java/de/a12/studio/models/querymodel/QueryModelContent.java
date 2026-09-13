package de.a12.studio.models.querymodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.a12.studio.models.querymodel.operator.Operator;
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

  // Some files omit "fields" entirely while others write it explicitly as "[]"; fieldsExplicit
  // preserves that distinction across a load/save cycle (see getFieldsForJson/setFieldsForJson below).
  @JsonIgnore
  private List<String> fields = new ArrayList<>();
  @JsonIgnore
  private boolean fieldsExplicit;

  // The projection's DM-level filter, either as a structured operator tree (constraint, mirroring the
  // kernel's "Query.Operator" shape - see Operator) or, when no query-engine grammar is available to parse
  // one, as free text edited via RichtextEditorController (filterDefinition). Only one of the two is
  // normally present on a given file.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Operator constraint;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String filterDefinition;

  // Relationship-traversal hops from the target Document Model (see QueryLink) - the tree's editable "graph"
  // part, as opposed to fields/filterDefinition/sort/paging which all describe the target DM itself.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<QueryLink> links = new ArrayList<>();

  // Same absent-vs-explicit-empty distinction as "fields" above.
  @JsonIgnore
  private List<QuerySort> sort = new ArrayList<>();
  @JsonIgnore
  private boolean sortExplicit;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private QueryPaging paging;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean aggregateResults;

  // Inverts "constraint"/"links" into an exclusion filter (used by e.g. an "AvailableItems" query that
  // projects everything NOT already linked). No editor UI yet - preserved purely for lossless round-tripping.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean exclude;

  @JsonProperty("fields")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> getFieldsForJson() {
    return fieldsExplicit || !fields.isEmpty() ? fields : null;
  }

  @JsonProperty("fields")
  private void setFieldsForJson(List<String> value) {
    this.fieldsExplicit = value != null;
    this.fields = value != null ? value : new ArrayList<>();
  }

  @JsonProperty("sort")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<QuerySort> getSortForJson() {
    return sortExplicit || !sort.isEmpty() ? sort : null;
  }

  @JsonProperty("sort")
  private void setSortForJson(List<QuerySort> value) {
    this.sortExplicit = value != null;
    this.sort = value != null ? value : new ArrayList<>();
  }
}
