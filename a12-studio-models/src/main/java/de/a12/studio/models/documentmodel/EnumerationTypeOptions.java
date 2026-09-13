package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class EnumerationTypeOptions {

  private List<EnumerationValue> values = new ArrayList<>();

  // Some files omit "categories" entirely while others write it explicitly as "[]"; categoriesExplicit
  // preserves that distinction across a load/save cycle instead of always omitting (or always writing) an
  // empty array.
  @JsonIgnore
  private List<Category> categories = new ArrayList<>();
  @JsonIgnore
  private boolean categoriesExplicit;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean alphabeticalSorting;
  // Mirrors StringTypeOptions.errorMessage / SME's ui_useDefaultErrorMessages + ErrorMessages: a custom
  // per-locale message shown instead of the kernel's default "invalid enumeration value" text, opted into by
  // setting useDefaultErrorMessages to false.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean useDefaultErrorMessages;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> errorMessage = new ArrayList<>();

  @JsonProperty("categories")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<Category> getCategoriesForJson() {
    return categoriesExplicit || !categories.isEmpty() ? categories : null;
  }

  @JsonProperty("categories")
  private void setCategoriesForJson(List<Category> value) {
    this.categoriesExplicit = value != null;
    this.categories = value != null ? value : new ArrayList<>();
  }
}
