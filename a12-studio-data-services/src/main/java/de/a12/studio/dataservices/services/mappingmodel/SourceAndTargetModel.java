package de.a12.studio.dataservices.services.mappingmodel;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class SourceAndTargetModel {
  JsonNode sourceModel;
  JsonNode targetModel;
}
