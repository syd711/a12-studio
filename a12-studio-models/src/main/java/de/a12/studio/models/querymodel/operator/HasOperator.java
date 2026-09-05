package de.a12.studio.models.querymodel.operator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * Matches documents linked via {@code relationshipModel}/{@code targetRole} to a target document (and optionally
 * a link document) satisfying nested constraints. {@code constraint} applies to the target document's fields;
 * {@code linkDocumentConstraint} applies to the relationship's own link document fields.
 */
@Getter
@Setter
public class HasOperator extends Operator {

  private String relationshipModel;
  private String targetRole;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Operator constraint;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Operator linkDocumentConstraint;

  public HasOperator() {
    setOperator("has");
  }
}
