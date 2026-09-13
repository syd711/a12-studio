package de.a12.studio.models.selectionmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * One of {@link SelectionModelContent}'s three identically-shaped sections ({@code Data}, {@code
 * Validation}, {@code Computation}): every element of the reference Document Model is either selected or
 * not, decided by {@link #defaultValue} unless overridden by a matching {@link PathSpecification} in
 * {@link #selected}/{@link #unselected}. Mirrors SME's {@code SelectionContent}.
 *
 * <p>{@link #selected}/{@link #unselected} default to {@code null} (not an empty list) and are annotated
 * {@code NON_NULL} rather than {@code NON_EMPTY}, so a source file's explicit {@code "Selected": []} and an
 * altogether absent {@code "Selected"} key round-trip distinctly - the same absent-vs-explicit-empty
 * fidelity issue documented in this repo's CLAUDE.md for {@code A12Model.Header.labels} and
 * {@code overviewmodel.Column.width}. Real fixtures rely on both shapes (e.g.
 * {@code PersonSkills_NumberConversion_Se.json}'s {@code Data.Selected: []} vs. its {@code
 * Computation}/{@code Validation}, which omit {@code Selected}/{@code Unselected} entirely).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "Default", "Selected", "Unselected" })
@Getter
@Setter
public class SelectionCategory {

  @JsonProperty("Default")
  private SelectionDefault defaultValue;

  @JsonProperty("Selected")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<PathSpecification> selected;

  @JsonProperty("Unselected")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<PathSpecification> unselected;
}
