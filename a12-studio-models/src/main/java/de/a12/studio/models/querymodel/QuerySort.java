package de.a12.studio.models.querymodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class QuerySort {

  // Both null when sorting a field on the target Document Model directly, i.e. no relationship is traversed.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String relationshipModel;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String targetRole;

  // On the wire, field/direction/nullHandling/ignoreCase sit directly on the sort entry (no "sortBy"
  // wrapper key) - @JsonUnwrapped flattens them there while keeping the nested QuerySortBy object (and
  // its getSortBy() accessor, used throughout the query model editor UI) in the Java model.
  // Defaults to an empty instance (rather than null) so a sort entry that omits those keys in JSON, or one
  // just added via the editor's "Add Sort Entry" button, never leaves callers needing a null check.
  @JsonUnwrapped
  private QuerySortBy sortBy = new QuerySortBy();
}
