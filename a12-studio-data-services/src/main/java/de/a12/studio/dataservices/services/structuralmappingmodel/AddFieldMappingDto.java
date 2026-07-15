package de.a12.studio.dataservices.services.structuralmappingmodel;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class AddFieldMappingDto {
  JsonNode sourceModel;
  JsonNode targetModel;
  String sourceFieldPath;
  String targetFieldPath;
  JsonNode structuralMappingModel;
}
