package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * One entry of {@link DocumentModelContent#getDocumentUniquenessCriteria()} - a content-level uniqueness
 * check distinct from {@link ModelConfig#getUniquenessCriteria()} ({@link DocumentUniquenessCriterion}):
 * this one addresses fields by full path ({@link Field#getFullName()}, e.g. {@code "/Person/PersonID"})
 * rather than by {@code Element} id. No editor UI yet - mapped purely for lossless round-tripping.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ContentUniquenessCriterion {

  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> errorMessage = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Field> fields = new ArrayList<>();

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  public static class Field {
    private String fullName;
  }
}
