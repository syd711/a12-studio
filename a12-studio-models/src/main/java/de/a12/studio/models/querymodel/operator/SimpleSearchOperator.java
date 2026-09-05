package de.a12.studio.models.querymodel.operator;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * Substring, case-insensitive full-text search. {@code fields} defaults to all indexed fields of the target
 * document model when omitted; exactly one of {@code value}/{@code values} is expected to be set.
 */
@Getter
@Setter
public class SimpleSearchOperator extends Operator {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> fields;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String value;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> values;

  public SimpleSearchOperator() {
    setOperator("simple_search");
  }
}
