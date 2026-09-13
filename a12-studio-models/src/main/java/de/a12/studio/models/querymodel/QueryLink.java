package de.a12.studio.models.querymodel;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.a12.studio.models.querymodel.operator.Operator;
import lombok.Getter;
import lombok.Setter;

/**
 * One relationship-traversal hop in the query's document graph: reach the document playing {@code targetRole} in
 * {@code relationshipModel}, project {@code fields} from it (same "/"-separated path shape as {@link
 * QueryModelContent#getFields()}, just relative to this hop's own target Document Model instead of the query's
 * root), and optionally traverse further via nested {@link #links}. Mirrors SME's {@code QueryLink}/{@code
 * QueryElementRelationship} - see docs/sme-reference-comparison.md "Query Model" section.
 *
 * <p>{@code constraint} and {@code linkDocumentFields} are mapped here purely so an existing file with either
 * set round-trips losslessly; neither has editor UI yet (per-node filtering is a separate, not-yet-built piece -
 * see the "Query Model" doc section's Phase 3 - and {@code linkDocumentFields} would need resolving the
 * relationship's own link-document schema, {@link de.a12.studio.models.relationshipmodel.RelationshipModelContent
 * #getLinkDocumentModel()}, which nothing in this editor does yet either).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class QueryLink {

  private String relationshipModel;
  private String targetRole;

  // Some files omit "fields"/"linkDocumentFields" entirely while others write them explicitly as "[]";
  // the *Explicit flags preserve that distinction across a load/save cycle (see the getXForJson/
  // setXForJson bridge methods below), the same trick as QueryModelContent#getFieldsForJson().
  @JsonIgnore
  private List<String> linkDocumentFields = new ArrayList<>();
  @JsonIgnore
  private boolean linkDocumentFieldsExplicit;

  @JsonIgnore
  private List<String> fields = new ArrayList<>();
  @JsonIgnore
  private boolean fieldsExplicit;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer maxDepth;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Operator constraint;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<QueryLink> links = new ArrayList<>();

  @JsonProperty("linkDocumentFields")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> getLinkDocumentFieldsForJson() {
    return linkDocumentFieldsExplicit || !linkDocumentFields.isEmpty() ? linkDocumentFields : null;
  }

  @JsonProperty("linkDocumentFields")
  private void setLinkDocumentFieldsForJson(List<String> value) {
    this.linkDocumentFieldsExplicit = value != null;
    this.linkDocumentFields = value != null ? value : new ArrayList<>();
  }

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
}
