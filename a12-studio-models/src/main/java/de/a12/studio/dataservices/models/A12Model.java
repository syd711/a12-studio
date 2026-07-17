package de.a12.studio.dataservices.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
abstract public class A12Model {

  @JsonIgnore
  private String id;

  @JsonIgnore
  private ModelType modelType;

  @JsonIgnore
  private String modelVersion;

  @JsonIgnore
  private List<Locale> locales = new ArrayList<>();

  @JsonIgnore
  private List<Label> labels = new ArrayList<>();

  @JsonIgnore
  private List<Annotation> annotations = new ArrayList<>();

  @JsonIgnore
  private List<ModelReference> modelReferences = new ArrayList<>();

  // The json wraps these fields in a "header" object, but callers use them directly on the model, so
  // they are bridged through a private DTO instead of being kept as a nested field.
  @JsonProperty("header")
  private Header getHeader() {
    Header header = new Header();
    header.setId(id);
    header.setModelType(modelType);
    header.setModelVersion(modelVersion);
    header.setLocales(locales);
    header.setLabels(labels);
    header.setAnnotations(annotations);
    header.setModelReferences(modelReferences);
    return header;
  }

  @JsonProperty("header")
  private void setHeader(Header header) {
    this.id = header.getId();
    this.modelType = header.getModelType();
    this.modelVersion = header.getModelVersion();
    this.locales = header.getLocales();
    this.labels = header.getLabels();
    this.annotations = header.getAnnotations();
    this.modelReferences = header.getModelReferences();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  private static class Header {

    private String id;
    private ModelType modelType;
    private String modelVersion;
    private List<Locale> locales = new ArrayList<>();
    private List<Label> labels = new ArrayList<>();
    private List<Annotation> annotations = new ArrayList<>();
    private List<ModelReference> modelReferences = new ArrayList<>();
  }
}
