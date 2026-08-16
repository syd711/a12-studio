package de.a12.studio.models.querymodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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

  // Defaults to an empty instance (rather than null) so a sort entry that omits "sortBy" in JSON, or one just
  // added via the editor's "Add Sort Entry" button, never leaves callers needing a null check.
  private QuerySortBy sortBy = new QuerySortBy();
}
