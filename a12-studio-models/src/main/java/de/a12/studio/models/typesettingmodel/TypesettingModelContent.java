package de.a12.studio.models.typesettingmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The content of a {@link TypesettingModel}, matching the kernel's {@code DomainTypesettingMetaModel} (shipped
 * in the {@code print-typesetting} library) and the {@code TypesettingModelContentDto} the print engine reads.
 *
 * <p>Only {@link #preventLineBreakRules}, {@link #orphan} and {@link #widow} are edited. {@code
 * customHyphenationExclusions} and {@code internal} (the hyphenation dictionary the print engine generates) are
 * never touched by SME's editor either, so they are kept as raw {@link JsonNode}s: that preserves whatever a
 * file carries there across a load/save cycle, and an absent key stays absent (same trick as {@code
 * RelationshipModelContent.linkDocumentModel}). The property order is pinned since Jackson would otherwise push
 * the {@link JsonNode} properties to the end.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"customHyphenationExclusions", "preventLineBreakRules", "internal", "orphan", "widow"})
@Getter
@Setter
public class TypesettingModelContent {

  /** Hyphenation exceptions: a list of {@code {word, index[]}}. Not editable, see the class doc. */
  @JsonProperty("customHyphenationExclusions")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode customHyphenationExclusions;

  private List<PreventLineBreakRule> preventLineBreakRules = new ArrayList<>();

  /** The generated hyphenation dictionary ({@code {}} in every model SME creates). Not editable. */
  @JsonProperty("internal")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode internal;

  /** How many isolated lines may stay at the bottom of a page, 0-10 (default {@link TypesettingModelDefaults#LINE_LIMIT}). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer orphan;

  /** How many isolated lines may stay at the top of a page, 0-10 (default {@link TypesettingModelDefaults#LINE_LIMIT}). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer widow;
}
