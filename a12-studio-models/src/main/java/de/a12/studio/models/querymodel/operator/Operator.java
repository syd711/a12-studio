package de.a12.studio.models.querymodel.operator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * A Data Services query constraint node - the kernel's "Query.Operator" JSON shape used by
 * {@code QueryModelContent.constraint}, {@code QueryLink.constraint}/{@code linkDocumentConstraint}, and
 * {@code HasOperator}'s nested constraints. Mirrors the operator reference in the platform's Data Services
 * documentation (see docs/sme-reference-comparison.md "Query Model" section) - not just the subset SME's Query
 * Language grammar/emitter currently reaches, since hand-authored or kernel-produced JSON can use the full shape
 * (e.g. exact_match's caseSensitive/values, date_range's value/reverse mode).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "operator", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = AndOperator.class, name = "and"),
    @JsonSubTypes.Type(value = OrOperator.class, name = "or"),
    @JsonSubTypes.Type(value = NotOperator.class, name = "not"),
    @JsonSubTypes.Type(value = ExactMatchOperator.class, name = "exact_match"),
    @JsonSubTypes.Type(value = UndefinedMatchOperator.class, name = "undefined_match"),
    @JsonSubTypes.Type(value = DoubleRangeOperator.class, name = "double_range"),
    @JsonSubTypes.Type(value = DateRangeOperator.class, name = "date_range"),
    @JsonSubTypes.Type(value = DateFragmentRangeOperator.class, name = "datefragment_range"),
    @JsonSubTypes.Type(value = SimpleSearchOperator.class, name = "simple_search"),
    @JsonSubTypes.Type(value = HasOperator.class, name = "has")
})
@Getter
@Setter
public abstract class Operator {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String operator;
}
