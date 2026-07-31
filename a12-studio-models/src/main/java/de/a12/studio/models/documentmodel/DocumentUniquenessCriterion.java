package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * One "Document Uniqueness Criteria" entry on a {@link DocumentModel}'s {@link ModelConfig}: a named set of
 * {@link #fields} (by {@link Element#getId()}) whose combined values must be unique across every existing
 * document, evaluated on save. {@link #name} must be unique within the owning model. {@link #errorMessage} is
 * shown, per locale, when the check fails.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DocumentUniquenessCriterion {

  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> fields = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> errorMessage = new ArrayList<>();
}
