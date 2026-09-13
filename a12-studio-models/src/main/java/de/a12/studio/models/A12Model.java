package de.a12.studio.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// "header" is a synthetic getter/setter property on this class while "content" is a real field on the
// generic parameter, so without an explicit order Jackson does not reliably put header first.
@JsonPropertyOrder({"header", "content"})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
abstract public class A12Model<C> {

  @JsonIgnore
  private String id;

  @JsonIgnore
  private ModelType modelType;

  @JsonIgnore
  private String modelVersion;

  @JsonIgnore
  private String description;

  @JsonIgnore
  private List<Locale> locales = new ArrayList<>();

  // Some files omit "locales"/"labels"/"modelReferences" entirely while others write them explicitly as
  // "[]"; these flags preserve that distinction across a load/save cycle instead of always writing an
  // explicit array.
  @JsonIgnore
  private boolean localesExplicit;

  @JsonIgnore
  private List<Label> labels = new ArrayList<>();

  @JsonIgnore
  private boolean labelsExplicit;

  @JsonIgnore
  private List<Annotation> annotations = new ArrayList<>();

  @JsonIgnore
  private List<ModelReference> modelReferences = new ArrayList<>();

  @JsonIgnore
  private boolean modelReferencesExplicit;

  @JsonProperty("content")
  private C content;

  // The json wraps these fields in a "header" object, but callers use them directly on the model, so
  // they are bridged through a private DTO instead of being kept as a nested field.
  @JsonProperty("header")
  private Header getHeader() {
    Header header = new Header();
    header.setId(id);
    header.setModelType(modelType);
    header.setModelVersion(modelVersion);
    header.setDescription(description);
    header.setLocales(localesExplicit || !locales.isEmpty() ? locales : null);
    header.setLabels(labelsExplicit || !labels.isEmpty() ? labels : null);
    header.setAnnotations(annotations);
    header.setModelReferences(modelReferencesExplicit || !modelReferences.isEmpty() ? modelReferences : null);
    return header;
  }

  @JsonProperty("header")
  private void setHeader(Header header) {
    this.id = header.getId();
    this.modelType = header.getModelType();
    this.modelVersion = header.getModelVersion();
    this.description = header.getDescription();
    this.localesExplicit = header.getLocales() != null;
    this.locales = header.getLocales() != null ? header.getLocales() : new ArrayList<>();
    this.labelsExplicit = header.getLabels() != null;
    this.labels = header.getLabels() != null ? header.getLabels() : new ArrayList<>();
    this.annotations = header.getAnnotations();
    this.modelReferencesExplicit = header.getModelReferences() != null;
    this.modelReferences = header.getModelReferences() != null ? header.getModelReferences() : new ArrayList<>();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  private static class Header {

    private String id;
    private ModelType modelType;
    private String modelVersion;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String description;

    // null (not an initialized empty list) for locales/labels/modelReferences so the outer A12Model can
    // tell an absent key apart from an explicit "[]" on load, and reproduce the same shape on save.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Locale> locales;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Label> labels;

    private List<Annotation> annotations = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ModelReference> modelReferences;
  }
}
