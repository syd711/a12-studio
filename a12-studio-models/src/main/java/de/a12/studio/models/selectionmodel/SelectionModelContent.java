package de.a12.studio.models.selectionmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * A {@link SelectionModel}'s three identically-shaped sections, each independently specifying which
 * elements of the reference Document Model are selected for a different purpose: {@code Data} (which
 * fields/groups are included), {@code Validation} (which validation rules apply) and {@code Computation}
 * (which computation rules apply). Mirrors SME's {@code SelectionModelContent}
 * ({@code selectionModel.ts}) and the kernel's {@code MM_SelectionModel_2} meta-model - field order/casing
 * ("Data", "Computation", "Validation") matches every real fixture found in this repo and in SME
 * (e.g. {@code PersonSkills_NumberConversion_Se.json}).
 *
 * <p>Unlike a Document/Form/Overview Model, a Selection Model has no reference to the Document Model its
 * paths are written against - SME never persists one either (confirmed against every real {@code
 * "modelType": "selection"} fixture in the SME repo, none of which populate {@code header.modelReferences}).
 * The reference model is only ever known contextually, e.g. via whichever Combination Model's base model a
 * Selection step is combined with; SME's own standalone Selection Model editor receives it transiently at
 * open/create time and never writes it back to the file. a12-studio's editor therefore edits {@code Selected}/
 * {@code Unselected} as plain path text (validated against the same pattern rules SME's meta-model defines),
 * with no live Document Model tree to pick paths from - see {@code docs/sme-reference-comparison.md}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "Data", "Computation", "Validation" })
@Getter
@Setter
public class SelectionModelContent {

  @JsonProperty("Data")
  private SelectionCategory data = new SelectionCategory();

  @JsonProperty("Computation")
  private SelectionCategory computation = new SelectionCategory();

  @JsonProperty("Validation")
  private SelectionCategory validation = new SelectionCategory();
}
