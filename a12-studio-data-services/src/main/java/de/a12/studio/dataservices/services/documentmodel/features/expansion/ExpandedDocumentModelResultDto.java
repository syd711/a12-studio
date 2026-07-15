package de.a12.studio.dataservices.services.documentmodel.features.expansion;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition.IAdditionOrigins;
import lombok.Getter;

import java.util.Map;

@Getter
public class ExpandedDocumentModelResultDto {
  private final JsonNode documentModel;
  private final TypeDefInfo typeDefInfo;
  private final ElementInfo elementInfo;
  private final Map<String, IAdditionOrigins.IAnnotationsOrigins> annotationInfo;
  private final String error;

  public ExpandedDocumentModelResultDto(
      JsonNode documentModel,
      TypeDefInfo typeDefInfo,
      ElementInfo elementInfo,
      Map<String, IAdditionOrigins.IAnnotationsOrigins> annotationInfo,
      String error) {
    this.documentModel = documentModel;
    this.typeDefInfo = typeDefInfo;
    this.elementInfo = elementInfo;
    this.annotationInfo = annotationInfo;
    this.error = error;
  }

  public ExpandedDocumentModelResultDto(
      JsonNode documentModel,
      TypeDefInfo typeDefInfo,
      ElementInfo elementInfo,
      Map<String, IAdditionOrigins.IAnnotationsOrigins> annotationInfo) {
    this(documentModel, typeDefInfo, elementInfo, annotationInfo, null);
  }
}
