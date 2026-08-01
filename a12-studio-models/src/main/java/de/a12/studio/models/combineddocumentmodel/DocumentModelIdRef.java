package de.a12.studio.models.combineddocumentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

// Used for both AdditiveModel and DecorationModel steps, which reference a document model by "dmId".
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DocumentModelIdRef {

  private String dmId;
}
