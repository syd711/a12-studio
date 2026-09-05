package de.a12.studio.models.querymodel.operator;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

/**
 * Matches a field against exactly one value ({@code value}) or any of several ({@code values}, OR semantics).
 * Exactly one of the two must be set (per the platform's Data Services documentation) - not enforced here, since
 * this class only models the wire shape; enforcement belongs to a validator.
 *
 * <p>{@code value} is a plain {@link JsonNode} rather than a {@code String} because its JSON type depends on the
 * target field: a string/enumeration field's value is a JSON string, but a number field's value is a raw JSON
 * number (confirmed against the platform's Data Services API doc and SME's own emitter, which passes numeric
 * literals through unconverted while stringifying only booleans).
 */
@Getter
@Setter
public class ExactMatchOperator extends Operator {

  private String field;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode value;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> values;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean caseSensitive;

  public ExactMatchOperator() {
    setOperator("exact_match");
  }
}
