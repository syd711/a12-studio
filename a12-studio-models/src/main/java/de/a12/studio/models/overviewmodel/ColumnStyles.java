package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ColumnStyles {

  // Some files omit "header"/"content" entirely while others write either explicitly as "[]"; the
  // *Explicit flags preserve that distinction across a load/save cycle instead of always omitting (or
  // always writing) an empty array.
  @JsonIgnore
  private List<String> header = new ArrayList<>();
  @JsonIgnore
  private boolean headerExplicit;

  @JsonIgnore
  private List<String> content = new ArrayList<>();
  @JsonIgnore
  private boolean contentExplicit;

  @JsonProperty("header")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> getHeaderForJson() {
    return headerExplicit || !header.isEmpty() ? header : null;
  }

  @JsonProperty("header")
  private void setHeaderForJson(List<String> value) {
    this.headerExplicit = value != null;
    this.header = value != null ? value : new ArrayList<>();
  }

  @JsonProperty("content")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> getContentForJson() {
    return contentExplicit || !content.isEmpty() ? content : null;
  }

  @JsonProperty("content")
  private void setContentForJson(List<String> value) {
    this.contentExplicit = value != null;
    this.content = value != null ? value : new ArrayList<>();
  }
}
