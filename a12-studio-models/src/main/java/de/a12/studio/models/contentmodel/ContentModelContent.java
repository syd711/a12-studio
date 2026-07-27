package de.a12.studio.models.contentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ContentModelContent {

  private ContentConfiguration configuration;
  private ContentElement root;
}
