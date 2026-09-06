package de.a12.studio.models.querymodel;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> linkDocumentFields = new ArrayList<>();

  private List<String> fields = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer maxDepth;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Operator constraint;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<QueryLink> links = new ArrayList<>();
}
