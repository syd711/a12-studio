package de.a12.studio.dataservices.services.documentmodel.features.expansion;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class EnrichedDocumentModelResultDto {
  JsonNode documentModel;
}
