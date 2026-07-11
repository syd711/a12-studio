package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.a12.studio.dataservices.models.A12Model;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentModel extends A12Model {

  @JsonProperty("content")
  private DocumentModelContent content;
}
